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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import de.tanktaler.insider.core.auth.InsiderAuthPrincipal;
import de.tanktaler.insider.core.response.InsiderEnvelop;
import de.tanktaler.insider.models.device.Device;
import de.tanktaler.insider.models.session.SessionSegment;
import de.tanktaler.insider.models.session.SessionSummary;
import io.dropwizard.auth.Auth;
import java.util.ArrayList;
import java.util.stream.StreamSupport;
import org.bson.types.Decimal128;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.Sort;

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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    @Context HttpServletRequest httpRequest
  ) {
    final SessionSummary session = this.dsSession.get(SessionSummary.class, id);
    if (Objects.isNull(session)) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    final DBObject result = this.morphia.toDBObject(session);
    final List<ObjectId> segments = ((List<ObjectId>) result.get("segments"));
    final Query<SessionSegment> query = this.dsSession.createQuery(SessionSegment.class);

    final String fields = httpRequest.getParameter("fields[segments]");
    if (!Objects.isNull(fields)) {
      Arrays.stream(fields.split(",")).map(v -> v.trim()).forEach(v -> query.project(v, true));
    }

    final BasicDBList projectedSegments = new BasicDBList();
    for (final Object segmentId: segments) {
      projectedSegments.add(this.morphia.toDBObject(query.cloneQuery().field("_id").equal(segmentId).get()));
    }

    result.put("segments", projectedSegments);

    return Response.ok(new InsiderEnvelop(result)).build();
  }

  @GET
  @Path("/{id}/locations")
  public Response fetchOne(
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
                  .add(segment.getAttributes().getDouble("longitude"))
                  .add(segment.getAttributes().getDouble("latitude"))
              )
          )
        );
        return Response.ok(new InsiderEnvelop(locations)).build();
      }

      case "map-match": {
        final List<SessionSegment> segments = query
          .field("enhancements.mapMatches").exists()
          .field("enhancements.mapMatches").not().sizeEq(0)
          .project("enhancements.mapMatches", true)
          .asList();

        final ArrayNode locations = json.arrayNode(segments.size() + 1);

        segments.forEach(segment ->
          segment.getEnhancements().getMapMatches().forEach(match ->
            match.getCoordinates().forEach(
              coordinate -> locations.add(
                json.objectNode()
                  .put(
                    "street",
                    match.getTraces().stream()
                      .filter(trace ->
                        !trace.getStreet().isEmpty()
                        && Arrays.equals(trace.getLocation(), coordinate)
                      )
                      .map(e -> e.getStreet())
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
  public Response fetchAll(@Auth final InsiderAuthPrincipal user) {
    final List<Device> devices = this.dsInsider.createQuery(Device.class)
      .filter("account", user.entity().getAccount()).project("_id", true).asList();
    return Response
      .ok(
        new InsiderEnvelop(
          this.dsSession.createQuery(SessionSummary.class).field("device")
            .in(devices.stream().map(device -> device.getId()).collect(Collectors.toSet()))
            .order(Sort.descending("end"))
            .asList().stream().map(this.morphia::toDBObject).toArray()
        )
      )
      .build();
  }

  /*final List<Device> devices = this.dsInsider.createQuery(Device.class)
      .filter("account", this.currentUser.get().getAccount()).project("_id", true).asList();
    final Query<SessionSegment> query = this.dsSession
      .createQuery(SessionSegment.class).field("device")
      .in(devices.stream().map(device -> device.getId()).collect(Collectors.toSet()));
    return querySpec.apply(query.fetch());*/
}
