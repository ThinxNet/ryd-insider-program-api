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

package one.ryd.insider.models.session;

import com.mongodb.DBObject;
import java.time.Instant;
import java.util.List;
import one.ryd.insider.models.DatabaseModel;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

// do not specify indexes here
@Entity(value = "ip_session_summary", noClassnameStored = true)
public class SessionSummary implements DatabaseModel {
  @Id
  private ObjectId id;
  private Boolean incomplete;
  private DBObject statistics;
  private Instant start;
  private Instant end;
  private Instant timestamp;
  private List<ObjectId> segments;
  private ObjectId device;
  private Quality quality;

  public ObjectId getId() {
    return id;
  }

  public Boolean getIncomplete() {
    return this.incomplete;
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

  public List<ObjectId> getSegments() {
    return this.segments;
  }

  public ObjectId getDevice() {
    return this.device;
  }

  public Quality getQuality() {
    return this.quality;
  }

  public enum Quality {
    A, B, C, D, E, F // A is the best one
  }
}
