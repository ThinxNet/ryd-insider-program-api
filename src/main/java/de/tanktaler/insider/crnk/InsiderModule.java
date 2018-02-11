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

package de.tanktaler.insider.crnk;

import de.tanktaler.insider.models.User;
import de.tanktaler.insider.resources.UserRepository;
import io.crnk.core.engine.http.HttpRequestContext;
import io.crnk.core.engine.http.HttpRequestContextAware;
import io.crnk.core.engine.http.HttpRequestContextProvider;
import io.crnk.core.engine.http.HttpRequestProcessor;
import io.crnk.core.module.SimpleModule;
import org.mongodb.morphia.Datastore;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

public final class InsiderModule extends SimpleModule implements HttpRequestContextAware {
  private final Datastore datastore;
  private HttpRequestContextProvider requestContextProvider;

  public InsiderModule(final Datastore datastore) {
    super(InsiderModule.class.getName());
    this.datastore = datastore;
  }

  @Override
  public void setupModule(ModuleContext ctx) {
    this.addHttpRequestProcessor(new HttpRequestProcessor() { // @todo! move it out
      @Override
      public void process(HttpRequestContext context) throws IOException {
        final String token = context.getRequestHeader("x-txn-auth-token");
        Optional<User> user = Optional.empty();
        if (!Objects.isNull(token) && !token.isEmpty()) {
          user = Optional
            .ofNullable(datastore.createQuery(User.class).filter("auth_tokens.token", token).get());
        }
        context.setRequestAttribute("user", user);

      }
    });
    this.addHttpRequestProcessor(new HttpRequestProcessor() { // @todo! move it out
      @Override
      public void process(HttpRequestContext context) throws IOException {
        final Optional<User> user = (Optional<User>) context.getRequestAttribute("user");
        if (!user.isPresent()) {
          context.setResponse(403, "No token");
        }
      }
    });

    this.addRepository(new UserRepository(datastore));

    super.setupModule(ctx);
  }

  @Override
  public void setHttpRequestContextProvider(HttpRequestContextProvider requestContextProvider) {
    this.requestContextProvider = requestContextProvider;
  }
}
