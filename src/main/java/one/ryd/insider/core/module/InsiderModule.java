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

package one.ryd.insider.core.module;

import com.fasterxml.jackson.databind.module.SimpleModule;
import java.time.Instant;
import one.ryd.insider.core.serialize.InstantDeserialize;
import one.ryd.insider.core.serialize.InstantSerialize;
import one.ryd.insider.core.serialize.ObjectIdDeserialize;
import one.ryd.insider.core.serialize.ObjectIdSerialize;
import org.bson.types.ObjectId;

public final class InsiderModule extends SimpleModule {
  @Override
  public void setupModule(final SetupContext context) {
    this.addDeserializer(Instant.class, new InstantDeserialize());
    this.addDeserializer(ObjectId.class, new ObjectIdDeserialize());
    this.addSerializer(Instant.class, new InstantSerialize());
    this.addSerializer(ObjectId.class, new ObjectIdSerialize());
    super.setupModule(context);
  }
}
