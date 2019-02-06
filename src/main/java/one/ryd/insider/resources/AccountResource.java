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

import io.dropwizard.auth.Auth;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
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
import one.ryd.insider.models.account.Account;
import one.ryd.insider.models.account.AccountRole;
import one.ryd.insider.models.user.User;
import one.ryd.insider.resources.annotation.AccountBelongsToTheUser;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;

@Path("/accounts")
@Produces(MediaType.APPLICATION_JSON)
public final class AccountResource {
  @Inject
  @Named("datastoreInsider")
  private Datastore dsInsider;

  @GET
  @AccountBelongsToTheUser
  @Path("/{accountId}")
  public Response fetchOne(
    @Auth final InsiderAuthPrincipal user,
    @PathParam("accountId") final ObjectId id
  ) {
    final Account account = this.dsInsider.get(Account.class, id);
    return Response.ok(new InsiderEnvelop(account)).build();
  }

  @GET
  @Path("/{accountId}/users")
  @AccountBelongsToTheUser
  public Response fetchUsersAll(
    @Auth final InsiderAuthPrincipal user,
    @PathParam("accountId") final ObjectId id
  ) {
    final Account account = this.dsInsider.get(Account.class, id);
    final List<User> users = this.dsInsider
      .get(
        User.class,
        account.getUsers().stream().map(CustomEntityRelation::getId).collect(Collectors.toList())
      )
      .asList();
    return Response.ok(new InsiderEnvelop(users)).build();
  }

  @GET
  @Path("/{accountId}/users/{userId}")
  @AccountBelongsToTheUser
  public Response fetchUsersAll(
    @Auth final InsiderAuthPrincipal user,
    @PathParam("accountId") final ObjectId accountId,
    @PathParam("userId") final ObjectId userId
  ) {
    final User entity = this.dsInsider.createQuery(User.class)
      .field("_id").equal(userId)
      .field("account").equal(accountId)
      .get();
    return Objects.isNull(entity)
      ? Response.status(Response.Status.NOT_FOUND).build()
      : Response.ok(new InsiderEnvelop(entity)).build();
  }

  @GET
  public Response fetchAll(@Auth final InsiderAuthPrincipal user) {
    final List<Account> accounts = this.dsInsider.createQuery(Account.class)
      .field("users").elemMatch(
        this.dsInsider.createQuery(CustomEntityRelation.class)
          .field("id").equal(user.entity().getId())
          .field("role").equal(AccountRole.ACCOUNT_OWNER.toString())
      )
      .asList();
    return Response.ok(new InsiderEnvelop(accounts)).build();
  }
}
