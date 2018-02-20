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

package de.tanktaler.insider.resource;

import de.tanktaler.insider.model.user.User;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;
import java.util.function.Supplier;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;

public final class UserRepository extends ResourceRepositoryBase<User, ObjectId> {
  private final Datastore datastore;
  private final Supplier<User> currentUser;

  public UserRepository(final Datastore datastore, final Supplier<User> currentUser) {
    super(User.class);
    this.datastore = datastore;
    this.currentUser = currentUser;
  }

  @Override
  public <S extends User> S save(final S user) {
    this.datastore.save(user);
    return user;
  }

  @Override
  public ResourceList<User> findAll(final QuerySpec querySpec) {
    return querySpec.apply(
      this.datastore.createQuery(User.class)
        .filter("_id", this.currentUser.get().getId()).asList()
    );
  }

  @Override
  public <S extends User> S create(final S user) {
    throw new UnsupportedOperationException();
  }
}
