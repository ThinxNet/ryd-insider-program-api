/**
 * Copyright 2019 ThinxNet GmbH
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
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import io.dropwizard.auth.Auth;
import io.dropwizard.jersey.caching.CacheControl;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import one.ryd.insider.core.auth.InsiderAuthPrincipal;
import one.ryd.insider.core.response.InsiderEnvelop;
import one.ryd.insider.models.device.Device;
import one.ryd.insider.models.session.MapWay;
import one.ryd.insider.models.session.SessionSegment;
import one.ryd.insider.models.session.SessionSummary;
import one.ryd.insider.models.session.aggregation.SessionAlikeDto;
import one.ryd.insider.models.session.embedded.MapWayTag;
import one.ryd.insider.models.session.embedded.envelope.EnvelopeDeviceEvent;
import one.ryd.insider.models.session.embedded.envelope.EnvelopeMapMatch;
import one.ryd.insider.models.session.embedded.envelope.EnvelopeMapWay;
import one.ryd.insider.models.session.embedded.envelope.EnvelopeWeather;
import one.ryd.insider.models.thing.Thing;
import one.ryd.insider.resources.annotation.SessionBelongsToTheUser;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.commons.math3.util.Precision;
import org.bson.types.ObjectId;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.aggregation.Group;
import org.mongodb.morphia.aggregation.Projection;
import org.mongodb.morphia.query.Criteria;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.Sort;

// @todo #7:2h separate methods by domain

@Path("/sessions")
@Produces(MediaType.APPLICATION_JSON)
public final class SessionResource {
  @Inject
  @Named("datastoreInsider")
  private Datastore dsInsider;

  @Inject
  @Named("datastoreSession")
  private Datastore dsSession;

  @Inject
  private Morphia morphia;

  @GET
  @Path("/{sessionId}")
  @SessionBelongsToTheUser
  public Response fetchOne(
    @Auth final InsiderAuthPrincipal user,
    @PathParam("sessionId") final ObjectId id,
    @Context final HttpServletRequest httpRequest
  ) {
    final SessionSummary session = this.dsSession.get(SessionSummary.class, id);

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
  @Path("/{sessionId}/environment")
  @SessionBelongsToTheUser
  @CacheControl(maxAge = 1, maxAgeUnit = TimeUnit.HOURS)
  public Response segments(
    @Auth final InsiderAuthPrincipal user,
    @PathParam("sessionId") final ObjectId id,
    @Context final HttpServletRequest httpRequest
  ) {
    final List<SessionSegment> segments = this.dsSession.createQuery(SessionSegment.class)
      .field("session").equal(id)
      .field("enhancements.type").equal("MAP_WAY")
      .project("enhancements", true)
      .order(Sort.ascending("timestamp"))
      .asList();

    final List<ObjectNode> ways = new ArrayList<>();
    for (final SessionSegment segment: segments) {
      ways.addAll(
        segment.getEnhancements().stream()
          .filter(entry -> entry.type().equals("MAP_WAY"))
          .map(EnvelopeMapWay::new)
          .filter(way -> way.payload().confidence() >= 0.9 || way.payload().alternatives() == 0)
          .map(way -> {
            final MapWay entity = this.dsSession.createQuery(MapWay.class)
              .field("osmId").equal(way.payload().id())
              .field("timestamp").equal(way.payload().timestamp())
              .project("address", true)
              .project("geometry", true)
              .project("tags", true)
              .get();

            return Objects.isNull(entity)
              ? null
              : JsonNodeFactory.instance.objectNode()
                  .putPOJO("segment", segment.getId())
                  .put("distanceM", way.payload().distanceM())
                  .put("durationS", way.payload().durationS())
                  .put("speedMs", way.payload().speedMs())
                  .put("timestamp", way.timestamp().toEpochMilli()) // timestamp of the way
                  .putPOJO("address", entity.getAddress())
                  .putPOJO("geometry", entity.getGeometry())
                  .putPOJO("tags", entity.getTags());
          })
          .filter(Objects::nonNull)
          .collect(Collectors.toList())
      );
    }

    return Response.ok(new InsiderEnvelop(ways)).build();
  }

  @GET
  @Path("/{sessionId}/environment/overspeed")
  @SessionBelongsToTheUser
  @CacheControl(maxAge = 1, maxAgeUnit = TimeUnit.DAYS)
  public Response segments(
    @Auth final InsiderAuthPrincipal user,
    @PathParam("sessionId") final ObjectId id,
    @DefaultValue("geo") @QueryParam("source") final String source
  ) {
    final List<SessionSegment> segments = this.dsSession.createQuery(SessionSegment.class)
      .field("session").equal(id)
      .field("enhancements.type").equal("MAP_WAY")
      .project("attributes.geoSpeedKmH", true)
      .project("attributes.gpsSpeedKmH", true)
      .project("attributes.obdSpeedKmH", true)
      .project("attributes.speedKmH", true)
      .project("enhancements", true)
      .order(Sort.ascending("timestamp"))
      .asList();

    final List<ObjectNode> ways = new ArrayList<>();
    for (final SessionSegment segment: segments) {
      ways.addAll(
        segment.getEnhancements().stream()
          .filter(entry -> entry.type().equals("MAP_WAY"))
          .map(EnvelopeMapWay::new)
          .filter(way -> way.payload().confidence() >= 0.9 || way.payload().alternatives() == 0)
          .map(envelope -> {
            final Query<MapWay> query = this.dsSession.createQuery(MapWay.class)
              .field("osmId").equal(envelope.payload().id())
              .field("timestamp").equal(envelope.payload().timestamp())
              .field("tags.key").equal("maxspeed")
              .project("address", true)
              .project("geometry", true)
              .project("nodes", true)
              .project("tags", true);

            final MapWay way = query.get();
            if (Objects.isNull(way)) {
              return null;
            }

            final String maxSpeed = way.getTags().stream()
              .filter(tag -> tag.getKey().equals("maxspeed"))
              .findAny().get().getValue();
            if (!StringUtils.isNumeric(maxSpeed)) {
              return null;
            }

            Integer fieldValue = segment.getAttributes().getGeoSpeedKmH();
            switch (source.toLowerCase()) {
              case "mixed":
                fieldValue = Objects.nonNull(segment.getAttributes().getSpeedKmH())
                  ? segment.getAttributes().getSpeedKmH().intValue() : null;
                break;
              case "gps":
                fieldValue = segment.getAttributes().getGpsSpeedKmH();
                break;
              case "obd":
                fieldValue = segment.getAttributes().getObdSpeedKmH();
                break;
            }

            final Integer maxSpeedKmH = Integer.valueOf(maxSpeed);
            if (Objects.isNull(fieldValue) || fieldValue < 5 || maxSpeedKmH > fieldValue) {
              return null;
            }

            final List<Long> matches = Arrays.asList(envelope.payload().matches());
            final List<Double[]> geometry = way.getNodes().stream()
              .filter(matches::contains)
              .map(node -> way.getGeometry().get(way.getNodes().indexOf(node)))
              .collect(Collectors.toList());

            return JsonNodeFactory.instance.objectNode()
              .put("currentSpeedKmH", fieldValue)
              .put("distanceM", envelope.payload().distanceM())
              .put("maxSpeedKmH", maxSpeedKmH)
              .putPOJO("address", way.getAddress())
              .putPOJO("geometry", geometry)
              .putPOJO("segment", segment.getId())
              .putPOJO("timestamp", envelope.timestamp()); // timestamp of the way
          })
          .filter(Objects::nonNull)
          .collect(Collectors.toList())
      );
    }

    return Response.ok(new InsiderEnvelop(ways)).build();
  }

  @GET
  @Path("/{sessionId}/events")
  @SessionBelongsToTheUser
  @CacheControl(maxAge = 1, maxAgeUnit = TimeUnit.DAYS)
  public Response fetchAllEvents(
    @Auth final InsiderAuthPrincipal user,
    @PathParam("sessionId") final ObjectId id
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
      .forEachOrdered(entry -> events.addObject()
        .put("type", entry.type())
        .put("timestamp", entry.timestamp().toEpochMilli())
        .set("payload", json.pojoNode(entry.payload())));

    return Response.ok(new InsiderEnvelop(events)).build();
  }

  @GET
  @Path("/{sessionId}/locations")
  @SessionBelongsToTheUser
  @CacheControl(maxAge = 7, maxAgeUnit = TimeUnit.DAYS)
  public Response fetchAllLocations(
    @Auth final InsiderAuthPrincipal user,
    @PathParam("sessionId") final ObjectId id,
    @DefaultValue("gps") @QueryParam("source") final String source
  ) {
    final JsonNodeFactory json = JsonNodeFactory.instance;
    final ArrayNode locations = json.arrayNode();
    final List<Triple<ObjectId, List<Double[]>, String>> coordinates = new ArrayList<>();
    final Query<SessionSegment> query = this.dsSession
      .createQuery(SessionSegment.class).field("session").equal(id);

    switch (source) {
      case "mixed": {
        final List<Triple<SessionSegment, List<Double[]>, String>> bufferResults =
          new ArrayList<>();
        final List<Triple<SessionSegment, List<Double[]>, String>> bufferGeo =
          new ArrayList<>();
        final GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();

        final List<SessionSegment> segments = query
          .field("attributes.latitude").exists()
          .project("attributes.latitude", true)
          .project("attributes.longitude", true)
          .project("enhancements", true)
          .project("timestamp", true)
          .order(Sort.ascending("timestamp"))
          .asList();

        segments.forEach(segment -> {
          final Double[] singleCoordinate = new Double[]{
            segment.getAttributes().getLongitude(),
            segment.getAttributes().getLatitude()
          };
          final List<EnvelopeMapMatch> matches = segment.getEnhancements().stream()
            .filter(enhancement -> enhancement.type().equals("MAP_MATCH"))
            .map(EnvelopeMapMatch::new)
            .filter(e -> e.payload().alternatives() == 0)
            .collect(Collectors.toList());
          if (matches.isEmpty()
            || segments.indexOf(segment) == 0
            || segments.indexOf(segment) == segments.size() - 1) {
            bufferGeo.add(Triple.of(segment, Collections.singletonList(singleCoordinate), null));
            if (matches.isEmpty()) {
              return;
            }
          }

          matches.forEach(entry ->
            bufferResults
              .add(Triple.of(segment, entry.payload().coordinates(), entry.payload().name()))
          );

          final Coordinate[] matchedPoints = matches.stream().map(e -> e.payload().coordinates())
            .flatMap(Collection::stream)
            .map(pair -> new Coordinate(pair[0], pair[1]))
            .toArray(Coordinate[]::new);
          final Geometry tmp = matchedPoints.length > 1
            ? geometryFactory.createLineString(matchedPoints)
            : geometryFactory.createPoint(matchedPoints[0]);
          final Point point = geometryFactory
            .createPoint(new Coordinate(singleCoordinate[0], singleCoordinate[1]));

          if (!tmp.norm().isWithinDistance(point, 0.005)) {
            bufferGeo.add(Triple.of(segment, Collections.singletonList(singleCoordinate), null));
          }
        });

        GeometryCollection collection = geometryFactory.createGeometryCollection(
          bufferResults.stream()
            .map(triple ->
              (triple.getMiddle().size() > 1)
                ? JTSFactoryFinder.getGeometryFactory().createLineString(
                    triple.getMiddle().stream()
                      .map(coordinate -> new Coordinate(coordinate[0], coordinate[1]))
                      .toArray(Coordinate[]::new)
                  )
                : JTSFactoryFinder.getGeometryFactory().createPoint(
                    new Coordinate(triple.getMiddle().get(0)[0], triple.getMiddle().get(0)[1])
                  )
            )
            .toArray(Geometry[]::new)
        );

        bufferResults.add(0, bufferGeo.remove(0));

        for (int idx = 0; idx < bufferGeo.size() - 1; idx++) {
          final Double[] current = bufferGeo.get(idx).getMiddle().get(0);
          final Point point = geometryFactory.createPoint(new Coordinate(current[0], current[1]));
          if (!collection.isWithinDistance(point, 0.0008)) {
            bufferResults.add(bufferGeo.get(idx));
          }
        }

        bufferResults.add(bufferGeo.get(bufferGeo.size() - 1));
        bufferResults.sort(Comparator.comparing(entry -> entry.getLeft().getTimestamp()));
        bufferResults.forEach(entry ->
          coordinates.add(Triple.of(entry.getLeft().getId(), entry.getMiddle(), entry.getRight()))
        );
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
  @Path("/{sessionId}/alike")
  @SessionBelongsToTheUser
  @CacheControl(maxAge = 15, maxAgeUnit = TimeUnit.MINUTES)
  public Response alike(
    @Auth final InsiderAuthPrincipal user,
    @PathParam("sessionId") final ObjectId id,
    @DefaultValue("90") @QueryParam("confidence") final Integer confidence
  ) {
    final SessionSummary session = this.dsSession.get(SessionSummary.class, id);

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
  @Path("/{sessionId}/weather")
  @SessionBelongsToTheUser
  @CacheControl(maxAge = 1, maxAgeUnit = TimeUnit.DAYS)
  public Response weather(
    @Auth final InsiderAuthPrincipal user, @PathParam("sessionId") final ObjectId id
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
  @Path("/{sessionId}/consumption")
  @SessionBelongsToTheUser
  @CacheControl(maxAge = 1, maxAgeUnit = TimeUnit.DAYS)
  public Response consumption(
    @Auth final InsiderAuthPrincipal user, @PathParam("sessionId") final ObjectId id
  ) {
    final SessionSummary session = this.dsSession.get(SessionSummary.class, id);
    final Thing thing = this.dsInsider.createQuery(Thing.class)
      .field("device").equal(session.getDevice())
      .get();
    if (Objects.isNull(thing)) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    final JsonNodeFactory json = JsonNodeFactory.instance;
    final BasicDBObject statistics = (BasicDBObject) session.getStatistics();
    final int distanceM = statistics.getInt("distanceM");

    final ObjectNode result = json.objectNode()
      .put("distanceM", distanceM)
      .putNull("amountMl")
      .putNull("amountPerM");

    Double amountMl = null;
    if (statistics.getDouble("obdFuelLevelLNormality", 10.) < 10.) {
      amountMl = statistics.getDouble("obdFuelLevelLDiff") * 1000;
    } else if (
      Objects.nonNull(thing.getEnvironment().getFuelTankSizeL())
      && statistics.getDouble("obdFuelLevelPercentNormality", 10.) < 10.
    ) {
      amountMl = thing.getEnvironment().getFuelTankSizeL() * 0.01
        * statistics.getDouble("obdFuelLevelPercentDiff") * 1000;
    }

    if (Objects.nonNull(amountMl) && amountMl > .0) {
      result.put("amountMl", amountMl);
      result.put("amountPerM", Precision.round(amountMl / distanceM, 3));
    }

    return Response.ok(new InsiderEnvelop(result)).build();
  }

  @GET
  @Path("/{sessionId}/highlights")
  @SessionBelongsToTheUser
  @CacheControl(maxAge = 1, maxAgeUnit = TimeUnit.DAYS)
  public Response highlights(
    @Auth final InsiderAuthPrincipal user,
    @PathParam("sessionId") final ObjectId sessionId
  ) {
    final JsonNodeFactory json = JsonNodeFactory.instance;
    final SessionSummary session = this.dsSession.createQuery(SessionSummary.class)
      .field("_id").equal(sessionId)
      .project("statistics", true)
      .get();

    // overspeed
    final ArrayNode entriesOverspeed = json.arrayNode();
    final ArrayNode entriesRoadCategory = json.arrayNode();

    final List<SessionSegment> segments = this.dsSession.createQuery(SessionSegment.class)
      .field("session").equal(sessionId)
      .order(Sort.descending("timestamp"))
      .asList();
    for (final SessionSegment segment: segments) {
      if (Objects.isNull(segment.getAttributes().getSpeedKmH())) {
        continue;
      }

      final Map<Long, EnvelopeMapWay> ways = segment.getEnhancements().stream()
        .filter(entry -> entry.type().equals("MAP_WAY"))
        .map(EnvelopeMapWay::new)
        .filter(way -> way.payload().confidence() >= 0.9 || way.payload().alternatives() == 0)
        .collect(
          HashMap::new,
          (hashMap, entry) -> hashMap.put(entry.payload().id(), entry),
          Map::putAll
        );
      if (ways.isEmpty()) {
        continue;
      }

      final Query<MapWay> query = this.dsSession.createQuery(MapWay.class)
        .project("osmId", true)
        .project("address", true)
        .project("tags", true);
      query.or(
        ways.entrySet().stream()
          .map(val ->
            query
              .criteria("osmId").equal(val.getKey())
              .criteria("timestamp").equal(val.getValue().payload().timestamp())
          )
          .toArray(Criteria[]::new)
      );

      final List<MapWay> entries = query.asList();

      final List<String> categoryFederal = Arrays.asList("primary_link", "primary");
      final List<String> categoryCountry = Arrays
        .asList("tertiary", "tertiary_link", "unclassified");
      final Map<String, List<ImmutableTriple<Long, String, String>>> roadCategoryEntries = entries
        .stream()
        .flatMap(entry ->
          entry.getTags().stream()
            .map(sub -> ImmutableTriple.of(entry.getOsmId(), sub.getKey(), sub.getValue()))
        )
        .filter(triple -> triple.getMiddle().equals("highway"))
        .collect(
          Collectors.groupingBy(
            triple -> categoryFederal.contains(triple.getRight())
              ? "federal" : categoryCountry.contains(triple.getRight())
                ? "country" : "other"
          )
        );
      if (!roadCategoryEntries.isEmpty()) {
        roadCategoryEntries.keySet().forEach(key ->
          entriesRoadCategory.add(
            json.objectNode()
              .put(
                "distanceM",
                roadCategoryEntries.get(key).stream()
                  .mapToDouble(entry -> ways.get(entry.getLeft()).payload().distanceM())
                  .sum()
              )
              .put("class", key)
              .putPOJO("segment", segment.getId())
              .putPOJO("timestamp", segment.getTimestamp())
          )
        );
      }

      entries.stream()
        .map(way -> {
          Integer maxSpeed = -1;
          try {
            maxSpeed = Integer.parseUnsignedInt(
              way.getTags().stream()
                .filter(tag -> tag.getKey().equals("maxspeed"))
                .map(MapWayTag::getValue).findAny().orElse("-1")
            );
          } catch (final NumberFormatException exception){
            // do nothing
          }
          return ImmutableTriple.of(
            way.getOsmId(),
            maxSpeed,
            Objects.nonNull(way.getAddress().getCity())
              || Objects.nonNull(way.getAddress().getTown())
          );
        })
        .filter(pair ->
          pair.getMiddle() > 0 && (pair.getMiddle() + 11) <= segment.getAttributes().getSpeedKmH()
        )
        .forEachOrdered(triple ->
          entriesOverspeed.add(
            json.objectNode()
              .put("distanceM", ways.get(triple.getLeft()).payload().distanceM())
              .put("cityArea", triple.getRight())
              .put("maxSpeedKmH", triple.getMiddle())
              .put("speedKmH", segment.getAttributes().getSpeedKmH())
              .putPOJO("segment", segment.getId())
              .putPOJO("timestamp", segment.getTimestamp())
          )
        );
    }

    final ArrayNode results = json.arrayNode();

    final ArrayNode dayTimePayload = segments.stream()
      .collect(
        Collectors.groupingBy(
          segment -> LocalDateTime.ofInstant(
            segment.getTimestamp().minusSeconds(segment.getAttributes().getSegmentDurationS()),
            ZoneOffset.UTC
          ).getDayOfWeek().getValue(),
          Collectors.groupingBy(
            segment -> LocalDateTime.ofInstant(
              segment.getTimestamp().minusSeconds(segment.getAttributes().getSegmentDurationS()),
              ZoneOffset.UTC
            ).getHour()
          )
        )
      )
      .entrySet().stream()
      .flatMap(groupDow ->
        groupDow.getValue().entrySet().stream().map(
          groupHour -> Triple.of(
            groupDow.getKey(),
            groupHour.getKey(),
            groupHour.getValue().stream()
              .mapToInt(segment -> segment.getAttributes().getSegmentDurationS()).sum()
          )
        )
      )
      .collect(
        json::arrayNode,
        (haystack, triple) -> haystack.add(
          json.objectNode()
            .put("dayOfWeek", triple.getLeft())
            .put("durationS", triple.getRight())
            .put("hourNumber", triple.getMiddle())
        ),
        ArrayNode::addAll
      );
    if (dayTimePayload.size() > 0) {
      results.add(
        json.objectNode()
          .put("type", "TIME_OF_DAY")
          .set("attributes", json.objectNode().set("intervals", dayTimePayload))
      );
    }

    if (entriesRoadCategory.size() > 0) {
      results.add(
        json.objectNode()
          .put("type", "ROAD_CLASSIFICATION")
          .set(
            "attributes",
            json.objectNode()
              .put("sessionDistanceM", (int) session.getStatistics().get("distanceM"))
              .set("segments", entriesRoadCategory)
          )
      );
    }
    if (entriesOverspeed.size() > 0) {
      results.add(
        json.objectNode()
          .put("type", "OVERSPEED")
          .set("attributes", json.objectNode().set("segments", entriesOverspeed))
      );
    }

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
            //.field("incomplete").equal(false)
            .order(Sort.descending("end"))
            .asList().stream().map(this.morphia::toDBObject).toArray()
        )
      )
      .build();
  }
}
