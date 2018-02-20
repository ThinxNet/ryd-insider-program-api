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

package de.tanktaler.insider.model.session;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Field;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.Indexes;
import org.mongodb.morphia.annotations.Reference;

import java.time.Instant;
import java.util.List;

@Entity(value = "ip_session_summary", noClassnameStored = true)
@Indexes(
  @Index(value = "idx_esn_start_end", fields = {@Field("esn"), @Field("start"), @Field("end")})
)
@JsonApiResource(type = "sessions")
public final class SessionSummary {
  @Id
  @JsonApiId
  private ObjectId id;

  private Boolean incomplete;

  private String esn;

  private Instant start;

  private Instant end;

  private Instant timestamp;

  private Document statistics;

  @JsonApiRelation
  @Reference
  private List<SessionSegment> segments;

  public ObjectId getId() {
    return id;
  }

  public String getEsn() {
    return esn;
  }

  public Document getStatistics() {
    return statistics;
  }

  public Instant getStart() {
    return start;
  }

  public Instant getEnd() {
    return end;
  }

  public Instant getTimestamp() {
    return timestamp;
  }

  public Boolean getIncomplete() {
    return incomplete;
  }

  public List<SessionSegment> getSegments() {
    return segments;
  }
}
