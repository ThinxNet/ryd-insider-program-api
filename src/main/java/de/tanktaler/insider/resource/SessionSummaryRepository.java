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

import de.tanktaler.insider.model.session.SessionSummary;
import de.tanktaler.insider.model.user.User;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;

import java.util.function.Supplier;

public final class SessionSummaryRepository
  extends ResourceRepositoryBase<SessionSummary, ObjectId> {
  private final Datastore datastore;
  private final Supplier<User> currentUser;

  public SessionSummaryRepository(final Datastore datastore, final Supplier<User> currentUser) {
    super(SessionSummary.class);
    this.datastore = datastore;
    this.currentUser = currentUser;
  }

  @Override
  public <S extends SessionSummary> S save(final S summary) {
    this.datastore.save(summary);
    return summary;
  }

  @Override
  public ResourceList<SessionSummary> findAll(final QuerySpec querySpec) {
    return querySpec.apply(
      this.datastore.createQuery(SessionSummary.class).field("esn").equal("4532313244").fetch()
    );
  }

  @Override
  public <S extends SessionSummary> S create(final S summary) {
    throw new UnsupportedOperationException();
  }
}
