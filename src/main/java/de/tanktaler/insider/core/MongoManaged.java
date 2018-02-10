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

package de.tanktaler.insider.core;

import com.mongodb.Mongo;
import io.dropwizard.lifecycle.Managed;
import org.mongodb.morphia.Datastore;

public final class MongoManaged implements Managed {
  private final Datastore datastore;
  private final Mongo instance;

  public MongoManaged(final Mongo mongo, final Datastore datastore) {
      this.instance = mongo;
      this.datastore = datastore;
  }

  public Datastore datastore() {
    return this.datastore;
  }

  @Override
  public void start() throws Exception {
    this.datastore.ensureIndexes(true);
  }

  @Override
  public void stop() throws Exception {
    this.instance.close();
  }
}
