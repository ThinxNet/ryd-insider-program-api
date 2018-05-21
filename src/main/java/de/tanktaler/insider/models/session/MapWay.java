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

import de.tanktaler.insider.models.session.embedded.MapWayAddress;
import de.tanktaler.insider.models.session.embedded.MapWayTag;
import java.time.Instant;
import java.util.List;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Entity(value = "ip_map_ways", noClassnameStored = true)
public final class MapWay {
  @Id
  private Long id;

  private Instant createdAt;

  private List<Double[]> geometry;

  private List<Long> nodes;

  @Embedded
  private List<MapWayTag> tags;

  @Embedded
  private MapWayAddress address;

  private Long changeset;

  public Long getId() {
    return this.id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public List<MapWayTag> getTags() {
    return this.tags;
  }

  public Instant getCreatedAt() {
    return this.createdAt;
  }

  public List<Double[]> getGeometry() {
    return this.geometry;
  }

  public List<Long> getNodes() {
    return this.nodes;
  }

  public Long getChangeset() {
    return this.changeset;
  }

  public MapWayAddress getAddress() {
    return this.address;
  }
}
