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

import de.tanktaler.insider.model.thing.Thing;
import de.tanktaler.insider.model.user.User;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceList;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;

import java.util.function.Supplier;

public final class ThingRepository extends ResourceRepositoryBase<Thing, ObjectId> {
  private final Datastore datastore;
  private final Supplier<User> currentUser;

  public ThingRepository(final Datastore datastore, final Supplier<User> currentUser) {
    super(Thing.class);
    this.datastore = datastore;
    this.currentUser = currentUser;
  }

  @Override
  public <S extends Thing> S save(final S thing) {
    this.datastore.save(thing);
    return thing;
  }

  @Override
  public ResourceList<Thing> findAll(final QuerySpec querySpec) {
    final DefaultResourceList<Thing> list = new DefaultResourceList<>();
    list.addAll(
      this.datastore.createQuery(Thing.class)
        .field("users.id").equal(this.currentUser.get().getId()).asList()
    );
    return list;
  }

  @Override
	public Thing findOne(ObjectId id, QuerySpec querySpec) {
	  return this.datastore.createQuery(Thing.class)
      .field("users.id").equal(this.currentUser.get().getId())
      .field("_id").equal(id).get();
  }

  @Override
  public <S extends Thing> S create(final S thing) {
    throw new UnsupportedOperationException();
  }
}
