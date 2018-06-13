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

import java.time.Instant;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Entity(value = "ip_session_confidence", noClassnameStored = true)
public class SessionConfidence {
  @Id
  private ObjectId id;

  private ObjectId session;

  private ObjectId device;

  private Instant timestamp;

  @Embedded
  private Attributes attributes;

  public ObjectId getId() {
    return id;
  }

  public ObjectId getSession() {
    return session;
  }

  public ObjectId getDevice() {
    return device;
  }

  public Instant getTimestamp() {
    return timestamp;
  }

  public Attributes getAttributes() {
    return attributes;
  }

  @Embedded
  private static final class Attributes {
    private Double confidence;
    private Double score;
    private Integer sampleSize;
    private String source;
    private String target;

    public Double getConfidence() {
      return confidence;
    }

    public Double getScore() {
      return score;
    }

    public Integer getSampleSize() {
      return sampleSize;
    }

    public String getSource() {
      return source;
    }

    public String getTarget() {
      return target;
    }
  }
}
