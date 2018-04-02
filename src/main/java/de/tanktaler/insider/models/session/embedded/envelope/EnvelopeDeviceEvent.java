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

import de.tanktaler.insider.models.session.embedded.SegmentTypedEnvelope;
import java.time.Instant;
import org.bson.Document;

public final class EnvelopeDeviceEvent implements Envelope<Document> {
  private final String type;
  private final Instant timestamp;
  private final Document payload;

  public EnvelopeDeviceEvent(
    final String type, final Instant timestamp, final Document payload
  ) {
    this.type = type;
    this.timestamp = timestamp;
    this.payload = payload;
  }

  public EnvelopeDeviceEvent(final SegmentTypedEnvelope envelope) {
    this(envelope.type(), envelope.timestamp(), new Document(envelope.payload()));
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
  public Document payload() {
    return this.payload;
  }
}
