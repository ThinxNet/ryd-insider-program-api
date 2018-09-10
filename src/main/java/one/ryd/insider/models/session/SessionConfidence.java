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

package one.ryd.insider.models.session;

import java.time.Instant;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Entity(value = "ip_session_confidence", noClassnameStored = true)
public final class SessionConfidence {
  @Id
  private ObjectId id;

  private Double confidence;

  private Double score;

  private Instant timestamp;

  private Integer sampleSize;

  private ObjectId device;

  private String source;

  private String target;

  public ObjectId getId() {
    return this.id;
  }

  public Double getConfidence() {
    return this.confidence;
  }

  public Double getScore() {
    return this.score;
  }

  public Instant getTimestamp() {
    return this.timestamp;
  }

  public Integer getSampleSize() {
    return this.sampleSize;
  }

  public ObjectId getDevice() {
    return this.device;
  }

  public String getSource() {
    return this.source;
  }

  public String getTarget() {
    return this.target;
  }
}
