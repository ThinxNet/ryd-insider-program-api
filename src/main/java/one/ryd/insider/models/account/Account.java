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

package one.ryd.insider.models.account;

import java.util.List;
import one.ryd.insider.models.CustomEntityRelation;
import one.ryd.insider.models.DatabaseModel;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Entity(value = "accounts", noClassnameStored = true)
public class Account implements DatabaseModel {
  @Id
  private ObjectId id;

  @Embedded
  private List<CustomEntityRelation> things;

  @Embedded
  private List<CustomEntityRelation> users;

  public ObjectId getId() {
    return this.id;
  }

  public List<CustomEntityRelation> getThings() {
    return this.things;
  }

  public List<CustomEntityRelation> getUsers() {
    return this.users;
  }
}
