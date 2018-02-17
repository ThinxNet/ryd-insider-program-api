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

import de.tanktaler.insider.model.user.User;
import de.tanktaler.insider.resource.ThingRepository;
import de.tanktaler.insider.resource.UserRepository;
import io.crnk.core.engine.http.HttpRequestContextAware;
import io.crnk.core.engine.http.HttpRequestContextProvider;
import io.crnk.core.module.SimpleModule;
import org.mongodb.morphia.Datastore;

import java.util.Objects;

public final class InsiderModule extends SimpleModule implements HttpRequestContextAware {
  private final Datastore datastore;
  private HttpRequestContextProvider reqContextProvider;

  public InsiderModule(final Datastore datastore) {
    super(InsiderModule.class.getName());
    this.datastore = datastore;
  }

  @Override
  public void setupModule(ModuleContext ctx) {
    // @todo! move it out
    this.addHttpRequestProcessor(context -> {
      final String token = context.getRequestHeader("x-txn-auth-token");
      if (!Objects.isNull(token) && !token.isEmpty()) {
        context.setRequestAttribute(
          "user",
          datastore.createQuery(User.class).filter("auth_tokens.token", token).get()
        );
      }
    });
    // @todo! move it out
    this.addHttpRequestProcessor(context -> {
      if (Objects.isNull(context.getRequestAttribute("user"))) {
        context.setResponse(403, "No token");
      }
    });

    this.addRepository(
      new UserRepository(
        datastore,
        () -> ((User) this.reqContextProvider.getRequestContext().getRequestAttribute("user"))
      )
    );

    this.addRepository(
      new ThingRepository(
        datastore,
        () -> ((User) this.reqContextProvider.getRequestContext().getRequestAttribute("user"))
      )
    );

    super.setupModule(ctx);
  }

  @Override
  public void setHttpRequestContextProvider(HttpRequestContextProvider reqContextProvider) {
    this.reqContextProvider = reqContextProvider;
  }
}
