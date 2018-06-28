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
import java.util.List;
import java.util.stream.Collectors;
import org.mongodb.morphia.annotations.Embedded;

@Embedded
public final class EnvelopeMapMatch implements Envelope<EnvelopeMapMatch.Payload> {
  private final String type;
  private final Instant timestamp;

  @Embedded
  private final Payload payload;

  public EnvelopeMapMatch(
    final String type, final Instant timestamp, final Payload payload
  ) {
    this.type = type;
    this.timestamp = timestamp;
    this.payload = payload;
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
  public Payload payload() {
    return this.payload;
  }

  @Embedded
  public final static class Payload {
    private final Double confidence;
    private final Double distance;
    private final Double duration;
    private final Double weight;
    private final Instant lastCoordinateTimestamp;
    private final List<Double[]> coordinates;

    @Embedded
    private List<Trace> traces;

    public Payload(
      final Double confidence,
      final Double distance,
      final Double duration,
      final Double weight,
      final Instant lastCoordinateTimestamp,
      final List<Double[]> coordinates,
      final List<Trace> traces
    ) {
      this.confidence = confidence;
      this.distance = distance;
      this.duration = duration;
      this.weight = weight;
      this.lastCoordinateTimestamp = lastCoordinateTimestamp;
      this.coordinates = coordinates;
      this.traces = traces;
    }

    public Payload(final BasicDBObject doc) {
      this(
        doc.getDouble("confidence"),
        doc.getDouble("distance"),
        doc.getDouble("duration"),
        doc.getDouble("weight"),
        doc.getDate("lastCoordinateTimestamp").toInstant(),
        ((BasicDBList) doc.get("coordinates")).stream()
          .map(e -> ((BasicDBList) e).toArray(new Double[0]))
          .collect(Collectors.toList()),
        ((BasicDBList) doc.get("traces")).stream()
          .map(e -> new Trace((BasicDBObject) e))
          .collect(Collectors.toList())
      );
    }

    public Double confidence() {
      return this.confidence;
    }

    public Double distance() {
      return this.distance;
    }

    public Double duration() {
      return this.duration;
    }

    public Double weight() {
      return this.weight;
    }

    public Instant lastCoordinateTimestamp() {
      return this.lastCoordinateTimestamp;
    }

    public List<Double[]> coordinates() {
      return this.coordinates;
    }

    public List<Trace> traces() {
      return this.traces;
    }

    @Embedded
    public static final class Trace {
      private final Double[] location;
      private final String street;

      public Trace(final Double[] location, final String street) {
        this.location = location;
        this.street = street;
      }

      public Trace(final BasicDBObject obj) {
        this(
          ((BasicDBList) obj.get("location")).toArray(new Double[0]),
          obj.getString("street")
        );
      }

      public Double[] location() {
        return this.location;
      }

      public String street() {
        return this.street;
      }
    }
  }
}
