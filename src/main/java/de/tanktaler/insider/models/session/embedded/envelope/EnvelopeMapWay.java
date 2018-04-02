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

package de.tanktaler.insider.models.session.embedded.envelope;

import java.time.Instant;
import org.mongodb.morphia.annotations.Embedded;

@Embedded
public final class EnvelopeMapWay implements Envelope<EnvelopeMapWay.Payload> {
  private Long[] nodes;

  public Long[] getNodes() {
    return this.nodes;
  }

  @Override
  public Payload payload() {
    return null;
  }

  @Override
  public Instant timestamp() {
    return null;
  }

  @Override
  public String type() {
    return null;
  }

  @Embedded
  public final static class Payload {
    private Long id;
    private Double speed;
    private Long changeset;

    public Long getId() {
      return this.id;
    }

    public Double getSpeed() {
      return this.speed;
    }

    public Long getChangeset() {
      return this.changeset;
    }
  }
}
