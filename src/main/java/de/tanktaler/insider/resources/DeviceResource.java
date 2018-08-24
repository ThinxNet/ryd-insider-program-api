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

import com.mongodb.BasicDBList;
import de.tanktaler.insider.core.auth.InsiderAuthPrincipal;
import de.tanktaler.insider.core.response.InsiderEnvelop;
import de.tanktaler.insider.models.device.Device;
import de.tanktaler.insider.models.session.SessionConfidence;
import de.tanktaler.insider.models.session.aggregation.DeviceConfidenceDto;
import io.dropwizard.auth.Auth;
import io.dropwizard.jersey.caching.CacheControl;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.aggregation.Group;

@Path("/devices")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public final class DeviceResource {
  @Inject
  @Named("dsInsider")
  private Datastore dsInsider;

  @Inject
  @Named("dsSession")
  private Datastore dsSession;

  @Inject
  private Morphia morphia;

  @GET
  @Path("/{id}")
  public Object fetchOne(
    @Auth final InsiderAuthPrincipal user,
    @PathParam("id") final ObjectId id
  ) {
    final Device device = this.dsInsider.get(Device.class, id);
    if (Objects.isNull(device)) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    return new ItemContentSingletonResource(device);
  }

  @GET
  @Path("/{id}/confidence")
  @CacheControl(maxAge = 1, maxAgeUnit = TimeUnit.HOURS)
  public Response confidence(
    @Auth final InsiderAuthPrincipal user,
    @PathParam("id") final ObjectId id
  ) {
    final BasicDBList result = new BasicDBList();

    this.dsSession.createAggregation(SessionConfidence.class)
      .match(this.dsSession.createQuery(SessionConfidence.class).field("device").equal(id))
      .group(
        Group.grouping("_id", "target"),
        Group.grouping("confidence", Group.average("confidence")),
        Group.grouping("score", Group.average("score")),
        Group.grouping("sampleSize", Group.max("sampleSize"))
      )
      .aggregate(DeviceConfidenceDto.class)
      .forEachRemaining(result::add);

    return Response.ok(new InsiderEnvelop(result)).build();
  }

  @GET
  public Response fetchAll(@Auth final InsiderAuthPrincipal user) {
    return Response.ok(
      new InsiderEnvelop(
        this.dsInsider.createQuery(Device.class).field("thing").in(
          user.entity().getThings().stream().map(e -> e.getId()).collect(Collectors.toSet())
        ).asList().stream().map(this.morphia::toDBObject).toArray()
      )
    ).build();
  }

  @Singleton
public class ItemContentSingletonResource {
  private final Device device;

  @Inject
  private Morphia morphia;

  public ItemContentSingletonResource(Device device) {
    this.device = device;
  }

    @GET
  @Path("/")
  public Object fetchOne(
    @Auth final InsiderAuthPrincipal user,
    @PathParam("id") final ObjectId id
  ) {

    return Response.ok(new InsiderEnvelop(this.morphia.toDBObject(this.device))).build();
  }
}
}
