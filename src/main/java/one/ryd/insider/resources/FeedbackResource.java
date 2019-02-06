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
import java.time.Instant;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import one.ryd.insider.core.auth.InsiderAuthPrincipal;
import one.ryd.insider.core.response.InsiderEnvelop;
import one.ryd.insider.models.feedback.WidgetFeedback;
import one.ryd.insider.resources.request.FeedbackWidgetParam;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Sort;

@Path("/feedback")
@Consumes(MediaType.APPLICATION_JSON)
public final class FeedbackResource {
  @Inject
  @Named("datastoreInsider")
  private Datastore dsInsider;

  @POST
  @Path("/widget/{id}")
  public Response widgetNewEntry(
    @Auth final InsiderAuthPrincipal user,
    @PathParam("id") final String reference,
    @Valid @NotNull final FeedbackWidgetParam param
  ) {
    try {
      this.dsInsider.save(
        new WidgetFeedback(
          user.entity().getId(),
          user.entity().getAccount(),
          reference,
          param.getMessage().trim(),
          param.getPayload(),
          param.getCategory(),
          Instant.now()
        )
      );
    } catch (final Exception exception) {
      return Response.serverError().status(422).build();
    }
    return Response.status(Response.Status.NO_CONTENT).build();
  }

  @GET
  public Response fetchAll(
    @Auth final InsiderAuthPrincipal user,
    @Context final HttpServletRequest httpRequest
  ) {
    return Response.ok(
      new InsiderEnvelop(
        this.dsInsider.createQuery(WidgetFeedback.class)
          .field("user").equal(user.entity().getId())
          .order(Sort.descending("timestamp"), Sort.ascending("reference"))
          .asList()
      )
    ).build();
  }
}
