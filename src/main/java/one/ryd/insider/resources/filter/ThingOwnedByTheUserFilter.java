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
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import one.ryd.insider.core.auth.InsiderAuthPrincipal;
import one.ryd.insider.models.CustomEntityRelation;
import one.ryd.insider.models.thing.ThingRole;
import one.ryd.insider.models.thing.ThingType;
import one.ryd.insider.resources.annotation.ThingOwnedByTheUser;
import org.bson.types.ObjectId;

@Provider
@ThingOwnedByTheUser
@Priority(Priorities.AUTHORIZATION)
public class ThingOwnedByTheUserFilter implements ContainerRequestFilter {
  @Override
  public void filter(final ContainerRequestContext ctx) throws IOException {
    final InsiderAuthPrincipal entity = (InsiderAuthPrincipal) ctx.getSecurityContext()
      .getUserPrincipal();
    if (Objects.isNull(entity)) {
      throw new IOException("Authenticated entity is not found");
    }

    if (!ctx.getUriInfo().getPathParameters().containsKey("thingId")) {
      throw new IOException("'thingId' path parameter is not found");
    }

    final String thingIdStr = ctx.getUriInfo().getPathParameters().get("thingId").get(0);
    if (!ObjectId.isValid(thingIdStr)) {
      ctx.abortWith(Response.serverError().status(Response.Status.BAD_REQUEST).build());
      return;
    }

    final List<ObjectId> thingIds = this.thingIds(entity);
    if (!thingIds.contains(new ObjectId(thingIdStr))) {
      ctx.abortWith(Response.status(Response.Status.FORBIDDEN).build());
      return;
    }
  }

  // @todo #7 make role and type configurable via the annotation
  private List<ObjectId> thingIds(final InsiderAuthPrincipal user) {
    return user.entity().getThings().stream()
      .filter(entry ->
        entry.getRole().equals(ThingRole.THING_OWNER.toString())
        && entry.getType().equals(ThingType.CAR.toString())
      )
      .map(CustomEntityRelation::getId)
      .collect(Collectors.toList());
  }
}
