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

package de.tanktaler.insider.resources;

import de.tanktaler.insider.models.User;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;

import java.util.function.Supplier;

public final class UserRepositoryResource extends ResourceRepositoryBase<UserResource, ObjectId> {
  private final Datastore datastore;
  private final Supplier<User> currentUser;

  public UserRepositoryResource(final Datastore datastore, final Supplier<User> currentUser) {
    super(UserResource.class);
    this.datastore = datastore;
    this.currentUser = currentUser;
  }

  @Override
  public synchronized void delete(ObjectId id) {
    //examples.remove(id);
  }

  @Override
  public synchronized <S extends UserResource> S save(S user) {
    this.datastore.save(user);
    return user;
  }

  @Override
  public synchronized ResourceList<UserResource> findAll(QuerySpec querySpec) {
    return querySpec.apply(
      this.datastore.createQuery(UserResource.class)
        .filter("_id", this.currentUser.get().getId()).asList()
    );
  }
}
