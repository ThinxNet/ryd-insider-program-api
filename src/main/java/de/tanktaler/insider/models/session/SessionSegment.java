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

package de.tanktaler.insider.models.session;

import de.tanktaler.insider.models.Model;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import java.time.Instant;

@Entity(value = "ip_session_segment", noClassnameStored = true)
public final class SessionSegment implements Model {
  @Id
  private ObjectId id;

  private ObjectId session;

  private Instant timestamp;

  private Document attributes;

  private String device;

  public ObjectId getId() {
    return this.id;
  }

  public Instant getTimestamp() {
    return this.timestamp;
  }

  public Document getAttributes() {
    return this.attributes;
  }

  public String getDevice() {
    return this.device;
  }

  public ObjectId getSession() {
    return this.session;
  }
}
