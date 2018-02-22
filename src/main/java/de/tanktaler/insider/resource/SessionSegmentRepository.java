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
import de.tanktaler.insider.model.session.SessionSegment;
import de.tanktaler.insider.model.user.User;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class SessionSegmentRepository
  extends ResourceRepositoryBase<SessionSegment, ObjectId> {
  private final Datastore dsInsider;
  private final Datastore dsSession;
  private final Supplier<User> currentUser;

  public SessionSegmentRepository(
    final Datastore dsInsider,
    final Datastore dsSession,
    final Supplier<User> currentUser
  ) {
    super(SessionSegment.class);
    this.dsInsider = dsInsider;
    this.dsSession = dsSession;
    this.currentUser = currentUser;
  }

  @Override
  public <S extends SessionSegment> S save(final S segment) {
    this.dsSession.save(segment);
    return segment;
  }

  @Override
  public ResourceList<SessionSegment> findAll(final QuerySpec querySpec) {
    final List<Device> devices = this.dsInsider.createQuery(Device.class)
      .filter("account", this.currentUser.get().getAccount()).project("_id", true).asList();
    final Query<SessionSegment> query = this.dsSession
      .createQuery(SessionSegment.class).field("device")
      .in(devices.stream().map(device -> device.getId()).collect(Collectors.toSet()));
    return querySpec.apply(query.fetch());
  }

  @Override
  public <S extends SessionSegment> S create(final S segment) {
    throw new UnsupportedOperationException();
  }
}
