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

package de.tanktaler.insider.models.session.embedded;

import java.util.List;
import org.mongodb.morphia.annotations.Embedded;

@Embedded
public final class EnhancementMapMatch {
  private Double confidence;
  private Float distance;
  private Float duration;
  private Float weight;
  //private Instant lastLocationTimestamp;
  private List<Double[]> coordinates;

  @Embedded
  private List<EnhancementMapMatchTrace> traces;

  public Double getConfidence() {
    return this.confidence;
  }

  public Float getDistance() {
    return this.distance;
  }

  public Float getDuration() {
    return this.duration;
  }

  public Float getWeight() {
    return this.weight;
  }

  /*public Instant getLastLocationTimestamp() {
    return this.lastLocationTimestamp;
  }*/

  public List<Double[]> getCoordinates() {
    return this.coordinates;
  }

  public List<EnhancementMapMatchTrace> getTraces() {
    return this.traces;
  }

  /*
    "legs":[  ],
    "coordinates":[  ],
  */
}
