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

package one.ryd.insider.models.thing;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Property;

@Embedded
public final class ThingYmme {
  private String color;
  private String fuelType;
  private String make;
  private String model;
  private String modelType;
  private String vehicleId;
  private String year;

  @Property("VIN")
  private String vin;

  public String getColor() {
    return this.color;
  }

  public String getFuelType() {
    return this.fuelType;
  }

  public String getMake() {
    return this.make;
  }

  public String getModel() {
    return this.model;
  }

  public String getModelType() {
    return this.modelType;
  }

  public String getVehicleId() {
    return this.vehicleId;
  }

  public String getYear() {
    return this.year;
  }

  public String getVin() {
    return this.vin;
  }
}
