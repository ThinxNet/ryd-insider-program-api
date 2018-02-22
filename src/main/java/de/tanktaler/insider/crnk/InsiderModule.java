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
import de.tanktaler.insider.resource.AccountRepository;
import de.tanktaler.insider.resource.DeviceRepository;
import de.tanktaler.insider.resource.SessionSegmentRepository;
import de.tanktaler.insider.resource.SessionSummaryRepository;
import de.tanktaler.insider.resource.ThingRepository;
import de.tanktaler.insider.resource.UserRepository;
import io.crnk.core.engine.http.HttpRequestContextAware;
import io.crnk.core.engine.http.HttpRequestContextProvider;
import io.crnk.core.module.SimpleModule;
import java.util.Objects;
import java.util.function.Supplier;

import org.mongodb.morphia.Datastore;

public final class InsiderModule extends SimpleModule implements HttpRequestContextAware {
  private final Datastore dsInsider;
  private final Datastore dsSession;
  private HttpRequestContextProvider reqContextProvider;

  public InsiderModule(final Datastore dsInsider, final Datastore dsSession) {
    super(InsiderModule.class.getName());
    this.dsInsider = dsInsider;
    this.dsSession = dsSession;
  }

  @Override
  public void setupModule(final ModuleContext ctx) {
    // @todo! move it out
    this.addHttpRequestProcessor(context -> {
      if (context.getMethod().equals("OPTIONS")) {
        context.setResponse(200, "OK");
        return;
      }
      final String token = context.getRequestHeader("x-txn-auth-token");
      if (!Objects.isNull(token) && !token.isEmpty()) {
        context.setRequestAttribute(
          "user",
          this.dsInsider.createQuery(User.class).filter("auth_tokens.token", token).get()
        );
      }
    });

    // @todo! move it out
    this.addHttpRequestProcessor(context -> {
      if (Objects.isNull(context.getRequestAttribute("user"))) {
        context.setResponse(403, "No token");
      }
    });

    final Supplier<User> userSupplier = () -> ((User) this.reqContextProvider
      .getRequestContext().getRequestAttribute("user"));

    this.addRepository(new UserRepository(this.dsInsider, userSupplier));
    this.addRepository(new ThingRepository(this.dsInsider, userSupplier));
    this.addRepository(new DeviceRepository(this.dsInsider, userSupplier));
    this.addRepository(new AccountRepository(this.dsInsider, userSupplier));
    this.addRepository(new SessionSummaryRepository(this.dsInsider, this.dsSession, userSupplier));
    this.addRepository(new SessionSegmentRepository(this.dsInsider, this.dsSession, userSupplier));

    super.setupModule(ctx);
  }

  @Override
  public void setHttpRequestContextProvider(final HttpRequestContextProvider reqContextProvider) {
    this.reqContextProvider = reqContextProvider;
  }
}
