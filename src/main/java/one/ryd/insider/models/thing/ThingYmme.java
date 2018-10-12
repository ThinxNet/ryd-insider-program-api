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
  private Boolean needsReview;
  private Integer year;
  private String cableStatus;
  private String color;
  private String hsn;
  private String licensePlate;
  private String make;
  private String model;
  private String tsn;
  private YmmeFuelType fuelType;

  @Property("VIN")
  private String vin;

  public Boolean getNeedsReview() {
    return this.needsReview;
  }

  public Integer getYear() {
    return this.year;
  }

  public String getCableStatus() {
    return this.cableStatus;
  }

  public String getColor() {
    return this.color;
  }

  public String getHsn() {
    return this.hsn;
  }

  public String getLicensePlate() {
    return this.licensePlate;
  }

  public String getMake() {
    return this.make;
  }

  public String getModel() {
    return this.model;
  }

  public String getTsn() {
    return this.tsn;
  }

  public YmmeFuelType getFuelType() {
    return this.fuelType;
  }

  public String getVin() {
    return this.vin;
  }
}
