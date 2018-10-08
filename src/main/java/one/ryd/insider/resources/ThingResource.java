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

import io.dropwizard.auth.Auth;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import one.ryd.insider.core.auth.InsiderAuthPrincipal;
import one.ryd.insider.core.response.InsiderEnvelop;
import one.ryd.insider.models.thing.Thing;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

@Path("/things")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public final class ThingResource {
  @Inject
  private Datastore dsInsider;

  @Inject
  private Morphia morphia;

  @GET
  @Path("/{id}")
  public Thing fetchOne(
    @Auth final InsiderAuthPrincipal user,
    @PathParam("id") final ObjectId id
  ) {
    return this.dsInsider.createQuery(Thing.class)
      .field("users.id").equal(user.entity().getId())
      .field("_id").equal(id).get();
  }

  @GET
  public Response fetchAll(@Auth final InsiderAuthPrincipal user) {
    return Response
      .ok(new InsiderEnvelop(
        this.dsInsider.createQuery(Thing.class)
          .field("users.id").equal(user.entity().getId())
          .asList().stream().map(this.morphia::toDBObject).toArray()
        )
      )
      .build();
  }
}
