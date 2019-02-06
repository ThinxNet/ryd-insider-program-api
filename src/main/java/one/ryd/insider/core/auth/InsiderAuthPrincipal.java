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

package one.ryd.insider.core.auth;

import io.dropwizard.auth.PrincipalImpl;
import one.ryd.insider.models.user.User;

public final class InsiderAuthPrincipal extends PrincipalImpl {
  private final User entity;

  public InsiderAuthPrincipal(final User entity) {
    super(entity.getEmail().toLowerCase());
    this.entity = entity;
  }

  public User entity() {
    return this.entity;
  }
}
