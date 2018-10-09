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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Consumes;
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
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;

@Path("/accounts")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public final class AccountResource {
  @Inject
  @Named("datastoreInsider")
  private Datastore dsInsider;

  @GET
  @Path("/{id}")
  public Response fetchOne(
    @Auth final InsiderAuthPrincipal user,
    @PathParam("id") final ObjectId id
  ) {
    final List<ObjectId> list = this.accountIds(user);
    if (!list.contains(id)) {
      return Response.status(Response.Status.FORBIDDEN).build();
    }

    final Account account = this.dsInsider.get(Account.class, id);
    if (Objects.isNull(account)) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    return Response.ok(new InsiderEnvelop(account)).build();
  }

  @GET
  @Path("/{id}/users")
  public Response fetchUsersAll(
    @Auth final InsiderAuthPrincipal user,
    @PathParam("id") final ObjectId id
  ) {
    final List<ObjectId> list = this.accountIds(user);
    if (!list.contains(id)) {
      return Response.status(Response.Status.FORBIDDEN).build();
    }

    final Account account = this.dsInsider.get(Account.class, id);
    if (Objects.isNull(account)) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    final List<User> users = this.dsInsider
      .get(
        User.class,
        account.getUsers().stream()
          .map(CustomEntityRelation::getId).collect(Collectors.toList())
      )
      .asList();

    return Response.ok(new InsiderEnvelop(users)).build();
  }

  @GET
  public Response fetchAll(@Auth final InsiderAuthPrincipal user) {
    final List<Account> accounts = this.dsInsider
      .get(Account.class, this.accountIds(user))
      .asList();
    return Response.ok(new InsiderEnvelop(accounts)).build();
  }

  private List<ObjectId> accountIds(final InsiderAuthPrincipal user) {
    return user.entity().getAccounts().stream()
      .filter(entry ->
        Arrays
          .asList(AccountRole.ACCOUNT_OWNER.toString(), AccountRole.ACCOUNT_VIEWER.toString())
          .contains(entry.getRole())
      )
      .map(CustomEntityRelation::getId)
      .collect(Collectors.toList());
  }
}
