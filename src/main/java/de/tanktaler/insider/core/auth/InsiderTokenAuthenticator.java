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

import de.tanktaler.insider.models.user.User;
import io.dropwizard.auth.Authenticator;
import org.mongodb.morphia.Datastore;

import java.util.Objects;
import java.util.Optional;

public final class InsiderTokenAuthenticator
  implements Authenticator<String, InsiderAuthPrincipal> {
  private final Datastore datastore;

  public InsiderTokenAuthenticator(final Datastore datastore) {
    this.datastore = datastore;
  }

  @Override
  public Optional<InsiderAuthPrincipal> authenticate(String token) {
    final User user = this.datastore.createQuery(User.class)
      .filter("auth_tokens.token", token).get();
    return Objects.isNull(user) ? Optional.empty() : Optional.of(new InsiderAuthPrincipal(user));
  }
}

