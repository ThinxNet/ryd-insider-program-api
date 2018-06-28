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

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import de.tanktaler.insider.models.session.embedded.SegmentTypedEnvelope;
import java.time.Instant;
import org.mongodb.morphia.annotations.Embedded;

@Embedded
public final class EnvelopeMapWay implements Envelope<EnvelopeMapWay.Payload> {
  private final String type;
  private final Instant timestamp;

  @Embedded
  private final Payload payload;

  public EnvelopeMapWay(final String type, final Instant timestamp, final Payload payload) {
    this.type = type;
    this.timestamp = timestamp;
    this.payload = payload;
  }

  public EnvelopeMapWay(final SegmentTypedEnvelope envelope) {
    this(envelope.type(), envelope.timestamp(), new Payload(envelope.payload()));
  }

  @Override
  public String type() {
    return this.type;
  }

  @Override
  public Instant timestamp() {
    return this.timestamp;
  }

  @Override
  public Payload payload() {
    return this.payload;
  }

  @Embedded
  public final static class Payload {
    private Long id;
    private Double speed;
    private Double distance;
    private Double duration;
    private Long changeset;
    private Long[] nodes;

    public Payload(
      final Long id,
      final Double speed,
      final Double distance,
      final Double duration,
      final Long changeset,
      final Long[] nodes
    ) {
      this.id = id;
      this.speed = speed;
      this.distance = distance;
      this.duration = duration;
      this.changeset = changeset;
      this.nodes = nodes;
    }

    public Payload(final BasicDBObject doc) {
      this(
        doc.getLong("id"),
        doc.getDouble("speed"),
        doc.getDouble("distance"),
        doc.getDouble("duration"),
        doc.getLong("changeset"),
        ((BasicDBList) doc.getOrDefault("nodes", new BasicDBList())).toArray(new Long[0])
      );
    }

    public Long id() {
      return this.id;
    }

    public Double speed() {
      return this.speed;
    }

    public Double distance() {
      return this.distance;
    }

    public Double duration() {
      return this.duration;
    }

    public Long changeset() {
      return this.changeset;
    }

    public Long[] nodes() {
      return this.nodes;
    }
  }
}
