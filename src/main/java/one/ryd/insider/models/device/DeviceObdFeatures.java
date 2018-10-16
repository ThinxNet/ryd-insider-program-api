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

package one.ryd.insider.models.device;

import java.util.Map;
import org.mongodb.morphia.annotations.Embedded;

@Embedded
public final class DeviceObdFeatures {
  private Protocol protocol;
  private Map<String, Boolean> params;

  public Protocol getProtocol() {
    return this.protocol;
  }

  public Map<String, Boolean> getParams() {
    return this.params;
  }

  private final static class Protocol {
    private Integer code;
    private String description;

    public Integer getCode() {
      return this.code;
    }

    public String getDescription() {
      return this.description;
    }
  }
}
