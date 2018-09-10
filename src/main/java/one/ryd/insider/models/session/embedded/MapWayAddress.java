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

package one.ryd.insider.models.session.embedded;

import org.mongodb.morphia.annotations.Embedded;

@Embedded
public final class MapWayAddress {
  private Integer postcode;
  private String city;
  private String cityDistrict;
  private String country;
  private String countryCode;
  private String county;
  private String neighbourhood;
  private String road;
  private String state;
  private String stateDistrict;
  private String suburb;
  private String town;
  private String village;

  public Integer getPostcode() {
    return this.postcode;
  }

  public String getCity() {
    return this.city;
  }

  public String getCityDistrict() {
    return this.cityDistrict;
  }

  public String getCountry() {
    return this.country;
  }

  public String getCountryCode() {
    return this.countryCode;
  }

  public String getCounty() {
    return this.county;
  }

  public String getNeighbourhood() {
    return this.neighbourhood;
  }

  public String getRoad() {
    return this.road;
  }

  public String getState() {
    return this.state;
  }

  public String getStateDistrict() {
    return this.stateDistrict;
  }

  public String getSuburb() {
    return this.suburb;
  }

  public String getTown() {
    return this.town;
  }

  public String getVillage() {
    return this.village;
  }
}
