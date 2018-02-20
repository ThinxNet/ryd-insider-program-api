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

import de.tanktaler.insider.model.device.Device;
import de.tanktaler.insider.model.user.User;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;

import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class DeviceRepository extends ResourceRepositoryBase<Device, ObjectId> {
  private final Datastore datastore;
  private final Supplier<User> currentUser;

  public DeviceRepository(final Datastore datastore, final Supplier<User> currentUser) {
    super(Device.class);
    this.datastore = datastore;
    this.currentUser = currentUser;
  }

  @Override
  public <S extends Device> S save(final S device) {
    this.datastore.save(device);
    return device;
  }

  @Override
  public ResourceList<Device> findAll(final QuerySpec querySpec) {
    return querySpec.apply(
      this.datastore.createQuery(Device.class)
        .field("thing").in(
          this.currentUser.get().getThings().stream()
            .map(e -> new ObjectId(e.getId())).collect(Collectors.toSet())
        )
        .asList()
    );
  }

  @Override
	public <S extends Device> S create(final S device) {
		throw new UnsupportedOperationException();
	}
}
