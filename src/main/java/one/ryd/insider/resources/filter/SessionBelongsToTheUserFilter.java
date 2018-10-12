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
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import one.ryd.insider.core.auth.InsiderAuthPrincipal;
import one.ryd.insider.models.device.Device;
import one.ryd.insider.models.session.SessionSummary;
import one.ryd.insider.resources.annotation.SessionBelongsToTheUser;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;

@Provider
@SessionBelongsToTheUser
@Priority(Priorities.AUTHORIZATION)
public class SessionBelongsToTheUserFilter implements ContainerRequestFilter {
  @Inject
  @Named("datastoreInsider")
  private Datastore dsInsider;

  @Inject
  @Named("datastoreSession")
  private Datastore dsSession;

  @Override
  public void filter(final ContainerRequestContext ctx) throws IOException {
    final InsiderAuthPrincipal entity = (InsiderAuthPrincipal) ctx.getSecurityContext()
      .getUserPrincipal();
    if (Objects.isNull(entity)) {
      throw new IOException("Authenticated entity is not found");
    }

    if (!ctx.getUriInfo().getPathParameters().containsKey("sessionId")) {
      throw new IOException("'sessionId' path parameter is not found");
    }

    final String sessionIdStr = ctx.getUriInfo().getPathParameters().get("sessionId").get(0);
    if (!ObjectId.isValid(sessionIdStr)) {
      ctx.abortWith(Response.serverError().status(Response.Status.BAD_REQUEST).build());
      return;
    }

    final SessionSummary session = this.dsSession
      .createQuery(SessionSummary.class)
      .project("device", true)
      .field("_id").equal(new ObjectId(sessionIdStr))
      .get();
    if (Objects.isNull(session)) {
      ctx.abortWith(Response.status(Response.Status.NOT_FOUND).build());
      return;
    }

    final List<ObjectId> deviceIds = this.deviceIds(entity);
    if (!deviceIds.contains(session.getDevice())) {
      ctx.abortWith(Response.status(Response.Status.NOT_FOUND).build());
      return;
    }
  }

  private List<ObjectId> deviceIds(final InsiderAuthPrincipal user) {
    return this.dsInsider.createQuery(Device.class)
      .project("_id", true)
      .field("account").equal(user.entity().getAccount())
      .asList().stream()
      .map(Device::getId)
      .collect(Collectors.toList());
  }
}
