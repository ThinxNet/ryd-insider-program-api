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

package one.ryd.insider.models.session.embedded.envelope;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import one.ryd.insider.models.session.embedded.SegmentTypedEnvelope;
import org.mongodb.morphia.annotations.Embedded;

@Embedded
public final class EnvelopeMapMatch implements Envelope<EnvelopeMapMatch.Payload> {
  private final String type;

  private final Instant timestamp;

  private final Integer version;

  @Embedded
  private final Payload payload;

  public EnvelopeMapMatch(
    final String type, final Instant timestamp, final Integer version, final Payload payload
  ) {
    this.type = type;
    this.timestamp = timestamp;
    this.version = version;
    this.payload = payload;
  }

  public EnvelopeMapMatch(
    final String type, final Instant timestamp, final Payload payload
  ) {
    this(type, timestamp, 1, payload);
  }

  public EnvelopeMapMatch(final SegmentTypedEnvelope envelope) {
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
  public Integer version() {
    return this.version;
  }

  @Override
  public Payload payload() {
    return this.payload;
  }

  @Embedded
  public static final class Payload {
    private final Double confidence;

    private final Double distanceM;

    private final Double durationS;

    private final Double speedMs;

    private final Integer alternatives;

    private final List<Double[]> coordinates;

    @Embedded
    private final Reference reference;

    private final Long[] nodes;

    private final String name;

    private final Integer matchingsIndex;


    public Payload(
      final Double confidence,
      final Double distanceM,
      final Double durationS,
      final Double speedMs,
      final Integer alternatives,
      final List<Double[]> coordinates,
      final Long[] nodes,
      final Reference reference,
      final String name,
      final Integer matchingsIndex
    ) {
      this.confidence = confidence;
      this.distanceM = distanceM;
      this.durationS = durationS;
      this.speedMs = speedMs;
      this.alternatives = alternatives;
      this.coordinates = coordinates;
      this.nodes = nodes;
      this.reference = reference;
      this.name = name;
      this.matchingsIndex = matchingsIndex;
    }

    public Payload(final BasicDBObject obj) {
      this(
        obj.getDouble("confidence"),
        obj.getDouble("distanceM"),
        obj.getDouble("durationS"),
        obj.getDouble("speedMs"),
        obj.getInt("alternatives"),
        ((BasicDBList) obj.get("coordinates")).stream()
          .map(v -> ((BasicDBList) v).toArray(new Double[2]))
          .collect(Collectors.toList()),
        ((BasicDBList) obj.get("nodes")).toArray(new Long[0]),
        new Reference((BasicDBObject) obj.get("reference")),
        obj.getString("name"),
        obj.getInt("matchingsIndex")
      );
    }

    public Double confidence() {
      return this.confidence;
    }

    public Double distanceM() {
      return this.distanceM;
    }

    public Double durationS() {
      return this.durationS;
    }

    public Double speedMs() {
      return this.speedMs;
    }

    public Integer alternatives() {
      return this.alternatives;
    }

    public List<Double[]> coordinates() {
      return this.coordinates;
    }

    public Reference reference() {
      return this.reference;
    }

    public Long[] nodes() {
      return this.nodes;
    }

    public String name() {
      return this.name;
    }

    public Integer matchingsIndex() {
      return this.matchingsIndex;
    }

    @Embedded
    public static final class Reference {
      private final Double[] location;
      private final Instant timestamp;

      public Reference(final Double[] location, final Instant timestamp) {
        this.location = location;
        this.timestamp = timestamp;
      }

      public Reference(final BasicDBObject obj) {
        this(
          ((BasicDBList) obj.getOrDefault("location", new BasicDBList())).toArray(new Double[0]),
          obj.getDate("timestamp").toInstant()
        );
      }

      public Double[] location() {
        return this.location;
      }

      public Instant timestamp() {
        return this.timestamp;
      }
    }
  }
}
