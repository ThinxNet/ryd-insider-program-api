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

package one.ryd.insider.models.device.converter;

import java.util.Objects;
import one.ryd.insider.models.device.DeviceStatusValue;
import org.mongodb.morphia.converters.TypeConverter;
import org.mongodb.morphia.mapping.MappedField;
import org.mongodb.morphia.mapping.MappingException;

public final class StringToDeviceStatusValueConverter extends TypeConverter {
  public StringToDeviceStatusValueConverter() {
    super(DeviceStatusValue.class);
  }

  @Override
  public DeviceStatusValue decode(
    final Class targetClass, final Object fromDbObject, final MappedField optionalExtraInfo
  ) throws MappingException {
    final String value = String.valueOf(fromDbObject);
    if (Objects.isNull(value)) {
      return DeviceStatusValue.UNKNOWN;
    }
    return DeviceStatusValue.valueOf(value.toUpperCase());
  }

  @Override
  public String encode(final Object value, final MappedField optionalExtraInfo) {
    switch ((DeviceStatusValue) value) {
      case OK: return "OK";
      case ERROR: return "Error";
      default: return null;
    }
  }
}
