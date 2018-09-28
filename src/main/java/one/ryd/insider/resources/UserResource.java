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
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import one.ryd.insider.core.auth.InsiderAuthPrincipal;
import one.ryd.insider.models.user.User;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;

@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public final class UserResource {
  private final Datastore datastore;

  public UserResource(final Datastore datastore) {
    this.datastore = datastore;
  }

  @GET
  @Path("/delete_me")
  public Integer status() {
    return 1;
  }

  @GET
  @Path("/{id}")
  public User fetchOne(
    @Auth final InsiderAuthPrincipal user,
    @PathParam("id") final ObjectId id
  ) {
    // @todo! incorrect
    return this.datastore.get(User.class, user.entity().getId());
  }

  @GET
  public List<User> fetchAll(@Auth final InsiderAuthPrincipal user) {
    // @todo! incorrect
    return this.datastore.createQuery(User.class)
      .filter("_id", user.entity().getId()).asList();
  }
}
