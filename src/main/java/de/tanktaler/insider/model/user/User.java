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

package de.tanktaler.insider.model.user;

import de.tanktaler.insider.model.thing.Thing;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Field;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.Indexes;
import org.mongodb.morphia.annotations.Reference;

import java.util.List;

@Entity(value = "users", noClassnameStored = true)
@Indexes(
  @Index(value = "email", fields = @Field("email"))
)
@JsonApiResource(type = "users")
public final class User {
  @Id
  @JsonApiId
  private ObjectId id;
  private String email;
  private List<UserAuthToken> auth_tokens;

  @JsonApiRelation
  @Reference
  private List<Thing> things;

  public ObjectId getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }

  public List<Thing> getThings() {
    return things;
  }
}
