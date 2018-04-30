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

package de.tanktaler.insider.resources;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import de.tanktaler.insider.core.auth.InsiderAuthPrincipal;
import de.tanktaler.insider.core.response.InsiderEnvelop;
import de.tanktaler.insider.models.device.Device;
import de.tanktaler.insider.models.session.MapWay;
import de.tanktaler.insider.models.session.SessionSegment;
import de.tanktaler.insider.models.session.SessionSummary;
import de.tanktaler.insider.models.session.aggregation.SessionAlikeDto;
import de.tanktaler.insider.models.session.embedded.envelope.EnvelopeMapMatch;
import de.tanktaler.insider.models.session.embedded.envelope.EnvelopeMapWay;
import de.tanktaler.insider.models.session.embedded.envelope.EnvelopeWeather;
import io.dropwizard.auth.Auth;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
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
import org.apache.commons.math3.util.Precision;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.aggregation.Accumulator;
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
          .field("_id").equal(way.payload().id())
          .field("changeset").equal(way.payload().changeset())
          .project("geometry", true)
          .project("tags", true)
          .get();
        return Objects.isNull(entity) ? null : JsonNodeFactory.instance.objectNode()
          .put("speed", way.payload().speed())
          .putPOJO("geometry", entity.getGeometry())
          .putPOJO("tags", entity.getTags());
      })
      .filter(Objects::nonNull)
      .collect(Collectors.toList());

    return Response.ok(new InsiderEnvelop(ways)).build();
  }

  @GET
  @Path("/{id}/locations")
  public Response fetchAllLocations(
    @Auth final InsiderAuthPrincipal user,
    @PathParam("id") final ObjectId id,
    @DefaultValue("gps") @QueryParam("source") final String source
  ) {
    final JsonNodeFactory json = JsonNodeFactory.instance;
    final Query<SessionSegment> query = this.dsSession
      .createQuery(SessionSegment.class).field("session").equal(id);

    switch (source) {
      case "gps": {
        final List<SessionSegment> segments = query
          .field("attributes.latitude").exists()
          .project("attributes.latitude", true)
          .project("attributes.longitude", true)
          .asList();

        final ArrayNode locations = json.arrayNode(segments.size() + 1);
        segments.forEach(segment ->
          locations.add(
            json.objectNode()
              .putNull("street")
              .set(
                "coordinate",
                json.arrayNode(2)
                  .add(segment.getAttributes().getLongitude())
                  .add(segment.getAttributes().getLatitude())
              )
          )
        );
        return Response.ok(new InsiderEnvelop(locations)).build();
      }

      case "map": {
        final List<SessionSegment> segments = query
          .field("enhancements.type").equal("MAP_MATCH")
          .project("enhancements", true)
          .asList();

        final ArrayNode locations = json.arrayNode(segments.size() + 1);

        segments.forEach(segment ->
          segment.getEnhancements().stream()
            .filter(enhancement -> enhancement.type().equals("MAP_MATCH"))
            .map(EnvelopeMapMatch::new).forEach(enhancement ->
              enhancement.payload().coordinates().forEach(
                coordinate -> locations.add(
                  json.objectNode()
                    .put(
                      "street",
                      enhancement.payload().traces().stream()
                        .filter(trace ->
                          !trace.street().isEmpty()
                          && Arrays.equals(trace.location(), coordinate)
                        )
                        .map(EnvelopeMapMatch.Payload.Trace::street)
                        .findFirst().orElse(null)
                    )
                    .set("coordinate", json.arrayNode(2).add(coordinate[0]).add(coordinate[1]))
                )
              )
          )
        );
        return Response.ok(new InsiderEnvelop(locations)).build();
      }

      default:
        return Response.status(Response.Status.BAD_REQUEST).build();
    }
  }

  @GET
  @Path("/{id}/alike")
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
      .flatMap(way -> Arrays.stream(way.payload().nodes()))
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
      .unwind("enhancements.payload.nodes")
      .project(
        Projection.projection("session"),
        Projection.projection("nodes", "enhancements.payload.nodes")
      )
      .unwind("nodes")
      .group(
        Group.grouping("_id", "session"),
        Group.grouping("nodes", Accumulator.accumulator("$addToSet", "nodes"))
      )
      .project(
        Projection.projection(
          "intersection",
          Projection.projection(
            "$size",
            Projection.expression("$setIntersection", "$nodes", nodes)
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
