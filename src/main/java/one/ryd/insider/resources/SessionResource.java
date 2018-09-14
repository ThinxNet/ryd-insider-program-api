/**
 * Copyright 2018 ThinxNet GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package one.ryd.insider.resources;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import one.ryd.insider.core.auth.InsiderAuthPrincipal;
import one.ryd.insider.core.response.InsiderEnvelop;
import one.ryd.insider.models.device.Device;
import one.ryd.insider.models.session.MapWay;
import one.ryd.insider.models.session.SessionSegment;
import one.ryd.insider.models.session.SessionSummary;
import one.ryd.insider.models.session.aggregation.SessionAlikeDto;
import one.ryd.insider.models.session.embedded.envelope.EnvelopeDeviceEvent;
import one.ryd.insider.models.session.embedded.envelope.EnvelopeMapMatch;
import one.ryd.insider.models.session.embedded.envelope.EnvelopeMapWay;
import one.ryd.insider.models.session.embedded.envelope.EnvelopeWeather;
import io.dropwizard.auth.Auth;
import io.dropwizard.jersey.caching.CacheControl;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.commons.math3.util.Precision;
import org.bson.types.ObjectId;
import org.geotools.referencing.GeodeticCalculator;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.aggregation.Group;
import org.mongodb.morphia.aggregation.Projection;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.Sort;

@Path("/sessions")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public final class SessionResource {
  // @todo! move it to validators
  private final Datastore dsInsider;
  private final Datastore dsSession;

  @Inject
  private Morphia morphia;

  public SessionResource(final Datastore dsInsider, final Datastore dsSession) {
    this.dsInsider = dsInsider;
    this.dsSession = dsSession;
  }

  @GET
  @Path("/{id}")
  public Response fetchOne(
    @Auth final InsiderAuthPrincipal user,
    @PathParam("id") final ObjectId id,
    @Context final HttpServletRequest httpRequest
  ) {
    final SessionSummary session = this.dsSession.get(SessionSummary.class, id);
    if (Objects.isNull(session)) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    final DBObject result = this.morphia.toDBObject(session);
    final List<ObjectId> segments = (List<ObjectId>) result.get("segments");
    final Query<SessionSegment> query = this.dsSession.createQuery(SessionSegment.class);

    final String fields = httpRequest.getParameter("fields[segments]");
    if (!Objects.isNull(fields)) {
      Arrays.stream(fields.split(",")).map(String::trim).forEach(v -> query.project(v, true));
    }

    final BasicDBList projectedSegments = new BasicDBList();
    for (final Object segmentId: segments) {
      projectedSegments.add(
        this.morphia.toDBObject(query.cloneQuery().field("_id").equal(segmentId).get())
      );
    }

    result.put("segments", projectedSegments);

    return Response.ok(new InsiderEnvelop(result)).build();
  }

  @GET
  @Path("/{id}/environment")
  @CacheControl(maxAge = 1, maxAgeUnit = TimeUnit.HOURS)
  public Response segments(
    @Auth final InsiderAuthPrincipal user,
    @PathParam("id") final ObjectId id,
    @Context final HttpServletRequest httpRequest
  ) {
    final SessionSummary session = this.dsSession.get(SessionSummary.class, id);
    if (Objects.isNull(session)) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    final List<SessionSegment> segments = this.dsSession.createQuery(SessionSegment.class)
      .field("session").equal(id)
      .field("enhancements.type").equal("MAP_WAY")
      .project("enhancements", true)
      .order(Sort.ascending("timestamp"))
      .asList();

    final List<ObjectNode> ways = segments.parallelStream()
      .flatMap(segment -> segment.getEnhancements().stream())
      .filter(segment -> segment.type().equals("MAP_WAY"))
      .map(EnvelopeMapWay::new)
      .map(way -> {
        final MapWay entity = this.dsSession.createQuery(MapWay.class)
          .field("osmId").equal(way.payload().id())
          .field("timestamp").equal(way.payload().timestamp()) // timestamp of the way
          .project("geometry", true)
          .project("tags", true)
          .project("address", true)
          .get();
        return Objects.isNull(entity) ? null : JsonNodeFactory.instance.objectNode()
          .put("distanceM", way.payload().distanceM())
          .put("durationS", way.payload().durationS())
          .put("speedMs", way.payload().speedMs())
          .put("timestamp", way.timestamp().toEpochMilli()) // timestamp of the entry
          .putPOJO("address", entity.getAddress())
          .putPOJO("tags", entity.getTags());
      })
      .filter(Objects::nonNull)
      .collect(Collectors.toList());

    return Response.ok(new InsiderEnvelop(ways)).build();
  }

  @GET
  @Path("/{id}/events")
  @CacheControl(maxAge = 1, maxAgeUnit = TimeUnit.DAYS)
  public Response fetchAllEvents(
    @Auth final InsiderAuthPrincipal user,
    @PathParam("id") final ObjectId id
  ) {
    final JsonNodeFactory json = JsonNodeFactory.instance;
    final ArrayNode events = json.arrayNode();

    this.dsSession
      .createQuery(SessionSegment.class).field("session").equal(id)
      .project("events", true)
      .order(Sort.ascending("timestamp"))
      .asList()
      .stream()
      .flatMap(entry -> entry.getEvents().stream())
      .map(EnvelopeDeviceEvent::new)
      .forEachOrdered(entry ->
        events.addObject()
          .put("type", entry.type())
          .put("timestamp", entry.timestamp().toEpochMilli())
          .set("payload", json.pojoNode(entry.payload()))
      );

    return Response.ok(new InsiderEnvelop(events)).build();
  }

  @GET
  @Path("/{id}/locations")
  @CacheControl(maxAge = 1, maxAgeUnit = TimeUnit.DAYS)
  public Response fetchAllLocations(
    @Auth final InsiderAuthPrincipal user,
    @PathParam("id") final ObjectId id,
    @DefaultValue("gps") @QueryParam("source") final String source
  ) {
    final JsonNodeFactory json = JsonNodeFactory.instance;
    final ArrayNode locations = json.arrayNode();
    final List<Triple<ObjectId, List<Double[]>, String>> coordinates = new ArrayList<>();
    final Query<SessionSegment> query = this.dsSession
      .createQuery(SessionSegment.class).field("session").equal(id);

    switch (source) {
      case "mixed": {
        final List<SessionSegment> segments = query
          .field("attributes.latitude").exists()
          .project("attributes.latitude", true)
          .project("attributes.longitude", true)
          .project("enhancements", true)
          .project("timestamp", true)
          .order(Sort.ascending("timestamp"))
          .asList();

        final List<Triple<ObjectId, List<Double[]>, String>> buffer = new ArrayList<>();
        final int segmentsCount = segments.size();

        Double[] lastSuitableCoordinate = null;
        Instant lastMapLocationTimestamp = null;

        for (int idx = 0; idx < segmentsCount; idx++) {
          final SessionSegment segment = segments.get(idx);
          final Triple<ObjectId, List<Double[]>, String> point = new ImmutableTriple<>(
            segment.getId(),
            Arrays.<Double[]>asList(new Double[]{
              segment.getAttributes().getLongitude(),
              segment.getAttributes().getLatitude()
            }),
            null
          );

          final List<EnvelopeMapMatch> list = segment.getEnhancements().stream()
            .filter(enhancement -> enhancement.type().equals("MAP_MATCH"))
            .map(EnvelopeMapMatch::new)
            .filter(enhancement -> enhancement.payload().alternatives() == 0)
            .collect(Collectors.toList());

          // always inject the first coordinate
          if (idx < 1) {
            coordinates.add(point);
          } else if (list.isEmpty()) {
            if (Objects.isNull(lastMapLocationTimestamp)
              || (
                lastMapLocationTimestamp.isBefore(segment.getTimestamp())
                && Duration
                  .between(lastMapLocationTimestamp, segment.getTimestamp()).getSeconds() > 5
              )
            ) {
              coordinates.add(point);
              buffer.add(point);
              continue;
            }
          }

          if (buffer.size() > 0 && Objects.nonNull(lastSuitableCoordinate)) {
            final GeodeticCalculator calc = new GeodeticCalculator();
            for (int i = 0; i < buffer.size(); i++) {
              final Double[] locationPrevious = (i < 1)
                ? lastSuitableCoordinate : buffer.get(i - 1).getMiddle().get(0);
              final Double[] locationNext = (i == buffer.size() - 1)
                ? list.get(0).payload().coordinates().get(0) : buffer.get(i + 1).getMiddle().get(0);
              final Double[] locationCurrent = buffer.get(i).getMiddle().get(0);

              calc.setStartingGeographicPoint(locationPrevious[0], locationPrevious[1]);
              calc.setDestinationGeographicPoint(locationNext[0], locationNext[1]);
              double distanceDirect = calc.getOrthodromicDistance();

              calc.setStartingGeographicPoint(locationPrevious[0], locationPrevious[1]);
              calc.setDestinationGeographicPoint(locationCurrent[0], locationCurrent[1]);
              double distanceFromCurrent = calc.getOrthodromicDistance();

              if (distanceDirect <= distanceFromCurrent
                || distanceFromCurrent < 10
                || distanceDirect - distanceFromCurrent < 10) {
                coordinates.remove(buffer.get(i));
                buffer.remove(i);
                i--;
              }
            }
          }

          buffer.clear();

          for (final EnvelopeMapMatch entry: list) {
            final List<Double[]> cords = entry.payload().coordinates();
            coordinates.add(new ImmutableTriple<>(segment.getId(), cords, entry.payload().name()));
            lastSuitableCoordinate = cords.get(cords.size() - 1);
            lastMapLocationTimestamp = entry.timestamp()
              .plusMillis(Math.round(entry.payload().durationS() * 1000));
          }
        }
      } break;

      case "gps": {
        final List<SessionSegment> segments = query
          .field("attributes.latitude").exists()
          .project("attributes.latitude", true)
          .project("attributes.longitude", true)
          .order(Sort.ascending("timestamp"))
          .asList();

        segments.forEach(segment ->
          coordinates.add(
            new ImmutableTriple<>(
              segment.getId(),
              Arrays.<Double[]>asList(new Double[]{
                segment.getAttributes().getLongitude(),
                segment.getAttributes().getLatitude()
              }),
              null
            )
          )
        );
      } break;

      case "map": {
        final List<SessionSegment> segments = query
          .field("enhancements.type").equal("MAP_MATCH")
          .project("enhancements", true)
          .order(Sort.ascending("timestamp"))
          .asList();

        segments.forEach(segment ->
          segment.getEnhancements().stream()
            .filter(enhancement -> enhancement.type().equals("MAP_MATCH"))
            .map(EnvelopeMapMatch::new)
            .forEachOrdered(entry ->
              coordinates.add(new ImmutableTriple<>(
                segment.getId(), entry.payload().coordinates(), entry.payload().name()
              ))
            )
        );
      } break;

      default:
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    coordinates.forEach(entry ->
      locations.add(
        json.objectNode()
          .put("_id", entry.getLeft().toString())
          .put("name", entry.getRight())
          .putPOJO("coordinates",
            entry.getMiddle().stream().collect(
              json::arrayNode,
              (k, v) -> k.add(json.arrayNode().add(v[0]).add(v[1])),
              ArrayNode::addAll
            )
          )
      )
    );

    return Response.ok(new InsiderEnvelop(locations)).build();
  }

  @GET
  @Path("/{id}/alike")
  @CacheControl(maxAge = 15, maxAgeUnit = TimeUnit.MINUTES)
  public Response alike(
    @Auth final InsiderAuthPrincipal user,
    @PathParam("id") final ObjectId id,
    @DefaultValue("90") @QueryParam("confidence") final Integer confidence
  ) {
    final SessionSummary session = this.dsSession.get(SessionSummary.class, id);
    if (Objects.isNull(session)) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    final List<SessionSegment> segments = this.dsSession
      .get(SessionSegment.class, session.getSegments())
      .field("enhancements.type").equal("MAP_WAY")
      .project("enhancements", true)
      .asList();

    final List<Long> nodes = segments.stream()
      .flatMap(segment -> segment.getEnhancements().stream())
      .filter(enhancement -> enhancement.type().equals("MAP_WAY"))
      .map(EnvelopeMapWay::new)
      .flatMap(way -> Arrays.stream(way.payload().matches()))
      .distinct()
      .collect(Collectors.toList());

    final BasicDBList result = new BasicDBList();
    if (nodes.isEmpty()) {
      return Response.ok(new InsiderEnvelop(result)).build();
    }

    this.dsSession.createAggregation(SessionSegment.class)
      .match(
        this.dsInsider.createQuery(SessionSegment.class)
          .field("session").notEqual(session.getId())
          .field("device").equal(session.getDevice())
          .field("enhancements.type").equal("MAP_WAY")
      )
      .unwind("enhancements")
      .match(
        this.dsInsider.createQuery(SessionSegment.class)
          .field("enhancements.type").equal("MAP_WAY")
      )
      .unwind("enhancements.payload.matches")
      .project(
        Projection.projection("session"),
        Projection.projection("matches", "enhancements.payload.matches")
      )
      .unwind("matches")
      .group(
        Group.grouping("_id", "session"),
        Group.grouping("matches", Group.addToSet("matches"))
      )
      .project(
        Projection.projection("matches"),
        Projection.projection("hits", Projection.projection("$size", "matches"))
      )
      .match(
        this.dsInsider.createQuery(SessionAlikeDto.class) // 50% window
          .field("hits").greaterThanOrEq(Math.round(nodes.size() * 0.75)) // 75%
          .field("hits").lessThanOrEq(Math.round(nodes.size() * 1.25)) // 125%
      )
      .project(
        Projection.projection("hits"),
        Projection.projection(
          "intersection",
          Projection.projection(
            "$size",
            Projection.expression("$setIntersection", "$matches", nodes)
          )
        )
      )
      .match(
        this.dsInsider.createQuery(SessionAlikeDto.class).field("intersection").greaterThan(
          Math.min(confidence < 40 ? 40 : confidence, 100) * (nodes.size() * 0.01)
        )
      )
      .sort(Sort.descending("intersection"))
      .aggregate(SessionAlikeDto.class)
      .forEachRemaining(entry -> {
        entry.confidence = Precision.round(entry.intersection / (nodes.size() * 0.01), 1);
        result.add(entry);
      });

    return Response.ok(new InsiderEnvelop(result)).build();
  }

  @GET
  @Path("/{id}/weather")
  @CacheControl(maxAge = 1, maxAgeUnit = TimeUnit.DAYS)
  public Response weather(
    @Auth final InsiderAuthPrincipal user, @PathParam("id") final ObjectId id
  ) {
    final List<SessionSegment> segments = this.dsSession.createQuery(SessionSegment.class)
      .field("session").equal(id)
      .asList();

    final JsonNodeFactory json = JsonNodeFactory.instance;
    final ArrayNode results = json.arrayNode(segments.size() + 1);

    segments.stream()
      .flatMap(segment ->
        segment.getEnhancements().stream()
          .filter(enhancement -> enhancement.type().equals("WEATHER"))
          .map(EnvelopeWeather::new)
      )
      .forEach(weather ->
        results.add(
          json.objectNode()
            .put("timestamp", weather.timestamp().toEpochMilli())
            .putPOJO("payload", weather.payload())
        )
      );

    return Response.ok(new InsiderEnvelop(results)).build();
  }

  @GET
  public Response fetchAll(
    @Auth final InsiderAuthPrincipal user,
    @Context final HttpServletRequest httpRequest
  ) {
    final List<Device> devices = this.dsInsider.createQuery(Device.class)
      .filter("account", user.entity().getAccount()).project("_id", true).asList();

    Query<SessionSummary> query = this.dsSession.createQuery(SessionSummary.class);

    final String deviceId = httpRequest.getParameter("filter[device]");
    if (Objects.nonNull(deviceId)
      && ObjectId.isValid(deviceId)
      && devices.stream().anyMatch(device -> device.getId().equals(new ObjectId(deviceId)))) {
      query = query.field("device").equal(new ObjectId(deviceId));
    } else {
      query = query.field("device")
        .in(devices.stream().map(Device::getId).collect(Collectors.toSet()));
    }

    return Response
      .ok(
        new InsiderEnvelop(
          query
            .field("incomplete").equal(false)
            .order(Sort.descending("end"))
            .asList().stream().map(this.morphia::toDBObject).toArray()
        )
      )
      .build();
  }
}