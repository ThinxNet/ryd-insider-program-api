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

package one.ryd.insider.resources.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import one.ryd.insider.core.auth.InsiderAuthPrincipal;
import one.ryd.insider.models.CustomEntityRelation;
import one.ryd.insider.models.account.Account;
import one.ryd.insider.models.account.AccountRole;
import one.ryd.insider.resources.annotation.AccountBelongsToTheUser;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;

@Provider
@AccountBelongsToTheUser
@Priority(Priorities.AUTHORIZATION)
public class AccountBelongsToTheUserFilter implements ContainerRequestFilter {
  @Inject
  @Named("datastoreInsider")
  private Datastore dsInsider;

  @Override
  public void filter(final ContainerRequestContext ctx) throws IOException {
    final InsiderAuthPrincipal entity = (InsiderAuthPrincipal) ctx.getSecurityContext()
      .getUserPrincipal();
    if (Objects.isNull(entity)) {
      throw new IOException("Authenticated entity is not found");
    }

    if (!ctx.getUriInfo().getPathParameters().containsKey("accountId")) {
      throw new IOException("'accountId' path parameter is not found");
    }

    final String accountIdStr = ctx.getUriInfo().getPathParameters().get("accountId").get(0);
    if (!ObjectId.isValid(accountIdStr)) {
      ctx.abortWith(Response.serverError().status(Response.Status.BAD_REQUEST).build());
      return;
    }

    final List<ObjectId> accountIds = this.accountIds(
      entity, Arrays.asList(AccountRole.ACCOUNT_OWNER, AccountRole.ACCOUNT_VIEWER)
    );
    if (!accountIds.contains(new ObjectId(accountIdStr))) {
      ctx.abortWith(Response.status(Response.Status.NOT_FOUND).build());
      return;
    }
  }

  // @todo #7 make roles configurable via the annotation
  private List<ObjectId> accountIds(
    final InsiderAuthPrincipal user, final List<AccountRole> roles
  ) {
    return this.dsInsider.createQuery(Account.class)
      .project("_id", true)
      .field("users").elemMatch(
        this.dsInsider.createQuery(CustomEntityRelation.class)
          .field("id").equal(user.entity().getId())
          .field("role").in(roles.stream().map(Enum::toString).collect(Collectors.toList()))
      )
      .asList().stream()
      .map(Account::getId)
      .collect(Collectors.toList());
  }
}
