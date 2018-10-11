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

package one.ryd.insider.models.user;

import java.util.List;
import one.ryd.insider.models.CustomEntityRelation;
import one.ryd.insider.models.DatabaseModel;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

@Entity(value = "users", noClassnameStored = true)
public class User implements DatabaseModel {
  @Id
  private ObjectId id;

  private String email;

  private ObjectId account;

  @Embedded
  private List<CustomEntityRelation> accounts;

  @Embedded
  private List<CustomEntityRelation> things;

  @Property("auth_tokens")
  private List<UserAuthToken> authTokens;

  public ObjectId getId() {
    return this.id;
  }

  public String getEmail() {
    return this.email;
  }

  public List<CustomEntityRelation> getAccounts() {
    return this.accounts;
  }

  public List<CustomEntityRelation> getThings() {
    return this.things;
  }

  public ObjectId getAccount() {
    return this.account;
  }
}
