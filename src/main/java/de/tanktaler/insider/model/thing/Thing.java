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

import de.tanktaler.insider.model.CustomEntityRelation;
import de.tanktaler.insider.model.account.Account;
import de.tanktaler.insider.model.device.Device;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Reference;

import java.util.List;

@Entity(value = "things", noClassnameStored = true)
@JsonApiResource(type = "things")
public final class Thing {
  @Id
  @JsonApiId
  private ObjectId id;

  @JsonApiRelation
  @Reference(idOnly = true)
  private Account account;

  private String nickName;

  @JsonApiRelation
  @Reference(idOnly = true)
  private Device device;

  private ThingYmme ymme;

  private List<CustomEntityRelation> users;

  public ObjectId getId() {
    return id;
  }

  public Account getAccount() {
    return account;
  }

  public String getNickName() {
    return nickName;
  }

  public Device getDevice() {
    return device;
  }

  public ThingYmme getYmme() {
    return ymme;
  }
}

/*  // @todo DBREF; the database structure historically messed up, hope at some point
  //       this must be removed
  @PreLoad
  void preLoad(final DBObject obj) {
    final String field = "users";
    obj.put(
      field,
      StreamSupport
        .stream(((BasicDBList) obj.get(field)).spliterator(), false)
        .map(entry -> ((BasicDBObject) entry).get("id"))
        .collect(BasicDBList::new, (r, e) -> r.add(e), (l, r) -> l.putAll(r))
    );
  }*/
