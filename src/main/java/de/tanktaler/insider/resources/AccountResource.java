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

import de.tanktaler.insider.core.auth.InsiderAuthPrincipal;
import de.tanktaler.insider.models.account.Account;
import de.tanktaler.insider.models.account.AccountRole;
import io.dropwizard.auth.Auth;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/accounts")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public final class AccountResource {
  private final Datastore datastore;

  public AccountResource(final Datastore datastore) {
    this.datastore = datastore;
  }

  @GET
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Account fetchOne(
    @Auth final InsiderAuthPrincipal user,
    @PathParam("id") final ObjectId id
  ) {
    return this.datastore.createQuery(Account.class)
      .field("_id").equal(id)
      .field("users.role").equal(AccountRole.ACCOUNT_OWNER).get();
  }

  @GET
  public List<Account> fetchAll(@Auth final InsiderAuthPrincipal user) {
    return this.datastore.createQuery(Account.class)
      .field("users.id").equal(user.entity().getId())
      .field("users.role").equal(AccountRole.ACCOUNT_OWNER).asList();
  }
}
