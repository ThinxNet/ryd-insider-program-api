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

package de.tanktaler.insider.core.auth;

import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.Authenticator;
import java.io.IOException;
import java.security.Principal;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;

public final class InsiderTokenAuthFilter<P extends Principal> extends AuthFilter<String, P> {
  // final Authorizer<P> authorizer
  public InsiderTokenAuthFilter(final Authenticator<String, P> authenticator) {
    super.authenticator = authenticator;
    //this.authorizer = authorizer;
  }

  @Override
  public void filter(final ContainerRequestContext requestContext) throws IOException {
    final String token = requestContext.getHeaderString("x-txn-auth-token");
    if (!this.authenticate(requestContext, token, "token")) {
      throw new WebApplicationException(unauthorizedHandler.buildResponse("CHANGE", "ME"));
    }
  }
}
