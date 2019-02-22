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

import com.mongodb.BasicDBList;
import io.dropwizard.auth.Auth;
import io.dropwizard.jersey.caching.CacheControl;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import one.ryd.insider.core.auth.InsiderAuthPrincipal;
import one.ryd.insider.core.response.InsiderEnvelop;
import one.ryd.insider.models.CustomEntityRelation;
import one.ryd.insider.models.device.Device;
import one.ryd.insider.models.session.SessionConfidence;
import one.ryd.insider.models.session.aggregation.DeviceConfidenceDto;
import one.ryd.insider.models.thing.Thing;
import one.ryd.insider.models.thing.ThingRole;
import one.ryd.insider.resources.annotation.ThingBelongsToTheUser;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.aggregation.Group;
import org.mongodb.morphia.query.Sort;

@Path("/things")
@Produces(MediaType.APPLICATION_JSON)
public final class ThingResource {
  @Inject
  @Named("datastoreInsider")
  private Datastore dsInsider;

  @Inject
  @Named("datastoreSession")
  private Datastore dsSession;

  @Inject
  private Morphia morphia;

  @GET
  public Response fetchAll(@Auth final InsiderAuthPrincipal user) {
    return Response
      .ok(new InsiderEnvelop(
        this.dsInsider.createQuery(Thing.class)
          .field("users").elemMatch(
            this.dsInsider.createQuery(CustomEntityRelation.class)
              .field("id").equal(user.entity().getId())
              .field("role").equal(ThingRole.THING_OWNER.toString())
          )
          .asList().stream().map(this.morphia::toDBObject).toArray()
        )
      )
      .build();
  }

  @GET
  @Path("/{thingId}")
  @ThingBelongsToTheUser
  public Response fetchOne(
    @Auth final InsiderAuthPrincipal user,
    @PathParam("thingId") final ObjectId id
  ) {
    return Response
      .ok(new InsiderEnvelop(this.morphia.toDBObject(this.dsInsider.get(Thing.class, id))))
      .build();
  }

  @GET
  @Path("/{thingId}/device")
  @CacheControl(maxAge = 30, maxAgeUnit = TimeUnit.MINUTES)
  @ThingBelongsToTheUser
  public Response device(
    @Auth final InsiderAuthPrincipal user,
    @PathParam("thingId") final ObjectId id
  ) {
    final Device device = this.dsInsider
      .get(Device.class, this.dsInsider.get(Thing.class, id).getDevice());
    if (Objects.isNull(device)) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    return Response.ok(new InsiderEnvelop(this.morphia.toDBObject(device))).build();
  }

  @GET
  @Path("{thingId}/device/confidence")
  @CacheControl(maxAge = 1, maxAgeUnit = TimeUnit.HOURS)
  @ThingBelongsToTheUser
  public Response deviceConfidence(
    @Auth final InsiderAuthPrincipal user,
    @PathParam("thingId") final ObjectId id
  ) {
    final BasicDBList result = new BasicDBList();

    this.dsSession.createAggregation(SessionConfidence.class)
      .match(
        this.dsSession.createQuery(SessionConfidence.class)
          .field("device").equal(this.dsInsider.get(Thing.class, id).getDevice())
          .field("confidence").greaterThan(60) // do not frustrate a user
      )
      .sort(Sort.descending("timestamp"))
      .group(
        Group.grouping("_id", "dataSet"),
        Group.grouping("confidence", Group.first("confidence")),
        Group.grouping("device", Group.first("device")),
        Group.grouping("sampleSize", Group.first("sampleSize")),
        Group.grouping("score", Group.first("score")),
        Group.grouping("timestamp", Group.first("timestamp"))
      )
      .aggregate(DeviceConfidenceDto.class)
      .forEachRemaining(result::add);

    return Response.ok(new InsiderEnvelop(result)).build();
  }
}
