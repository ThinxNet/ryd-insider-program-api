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

import com.mongodb.DBObject;
import de.tanktaler.insider.models.Model;
import java.time.Instant;
import java.util.List;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Entity(value = "ip_session_summary", noClassnameStored = true)
public class SessionSummary implements Model {
  @Id
  private ObjectId id;

  private Boolean incomplete;

  private ObjectId device;

  private Instant start;

  private Instant end;

  private Instant timestamp;

  private DBObject statistics;

  private List<ObjectId> segments;

  public ObjectId getId() {
    return id;
  }

  public DBObject getStatistics() {
    return this.statistics;
  }

  public Instant getStart() {
    return this.start;
  }

  public Instant getEnd() {
    return this.end;
  }

  public Instant getTimestamp() {
    return this.timestamp;
  }

  public Boolean getIncomplete() {
    return this.incomplete;
  }

  public List<ObjectId> getSegments() {
    return this.segments;
  }

  public ObjectId getDevice() {
    return this.device;
  }
}
