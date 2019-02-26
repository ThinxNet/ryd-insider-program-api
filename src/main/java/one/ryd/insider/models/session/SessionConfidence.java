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

import java.time.Instant;
import one.ryd.insider.models.DatabaseModel;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

// do not specify indexes here
@Entity(value = "ip_session_confidence", noClassnameStored = true)
public final class SessionConfidence implements DatabaseModel {
  @Id
  private ObjectId id;
  private Double confidence;
  private Double score;
  private Instant timestamp;
  private Integer sampleSize;
  private ObjectId device;
  private String source;
  private Boolean obdSupport;
  private String origin;
  private String dataSet;

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

  public Boolean getObdSupport() {
    return this.obdSupport;
  }

  public String getOrigin() {
    return this.origin;
  }

  public String getDataSet() {
    return this.dataSet;
  }
}
