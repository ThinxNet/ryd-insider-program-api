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

package de.tanktaler.insider.models.device;

import de.tanktaler.insider.models.account.Account;
import de.tanktaler.insider.models.thing.Thing;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Reference;

@Entity(value = "devices", noClassnameStored = true)
public class Device {
  @Id
  private ObjectId id;

  @Reference(idOnly = true)
  private Account account;

  @Reference(idOnly = true)
  private Thing thing;

  @Property("sn")
  private String serialNumber;

  private Boolean pluggedIn;

  public ObjectId getId() {
    return this.id;
  }

  public Thing getThing() {
    return this.thing;
  }

  public Boolean getPluggedIn() {
    return this.pluggedIn;
  }

  public Account getAccount() {
    return this.account;
  }

  public String getSerialNumber() {
    return this.serialNumber;
  }
}
