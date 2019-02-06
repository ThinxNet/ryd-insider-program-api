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

package one.ryd.insider.models.device;

import one.ryd.insider.models.DatabaseModel;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

@Entity(value = "devices", noClassnameStored = true)
public class Device implements DatabaseModel {
  @Id
  private ObjectId id;

  @Property("id")
  private String code;

  private Boolean pluggedIn;

  private ObjectId account;

  private ObjectId thing;

  private String configVersion;

  private String fwVersion;

  @Property("VBUSFW")
  private String vBusFw;

  @Property("VBUSDB")
  private String vBusDb;

  private Integer obdBusConflictCounter;

  private Boolean obdDisabled;

  private DeviceObdFeatures obdFeatures;

  private String scriptVersion;

  @Property("sn")
  private String serialNumber;

  private String state;

  @Embedded("dongleStatus")
  private DeviceStatus status;

  public ObjectId getId() {
    return this.id;
  }

  public String getCode() {
    return this.code;
  }

  public Boolean getPluggedIn() {
    return this.pluggedIn;
  }

  public ObjectId getAccount() {
    return this.account;
  }

  public ObjectId getThing() {
    return this.thing;
  }

  public String getConfigVersion() {
    return this.configVersion;
  }

  public String getFwVersion() {
    return this.fwVersion;
  }

  public String getVBusFw() {
    return this.vBusFw;
  }

  public String getVBusDb() {
    return this.vBusDb;
  }

  public Integer obdBusConflictCounter() {
    return this.obdBusConflictCounter;
  }

  public Boolean obdDisabled() {
    return this.obdDisabled;
  }

  public DeviceObdFeatures getObdFeatures() {
    return this.obdFeatures;
  }

  public String getScriptVersion() {
    return this.scriptVersion;
  }

  public String getSerialNumber() {
    return this.serialNumber;
  }

  public String getState() {
    return this.state;
  }

  public DeviceStatus getStatus() {
    return this.status;
  }
}
