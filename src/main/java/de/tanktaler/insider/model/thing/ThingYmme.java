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

package de.tanktaler.insider.model.thing;

public final class ThingYmme {
  private String color;
  private String fuelType;
  private String make;
  private String model;
  private String modelType;
  private String vehicleId;
  private String VIN;
  private String year;

  public String getColor() {
    return color;
  }

  public String getFuelType() {
    return fuelType;
  }

  public String getMake() {
    return make;
  }

  public String getModel() {
    return model;
  }

  public String getModelType() {
    return modelType;
  }

  public String getVehicleId() {
    return vehicleId;
  }

  public String getVIN() {
    return VIN;
  }

  public String getYear() {
    return year;
  }
}
