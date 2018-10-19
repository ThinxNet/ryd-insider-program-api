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

package one.ryd.insider.models.feedback;

import java.time.Instant;
import javax.validation.constraints.NotNull;
import one.ryd.insider.models.DatabaseModel;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Entity(value = "ip_feedback_widget", noClassnameStored = true)
public class WidgetFeedback implements DatabaseModel {
  @Id
  private ObjectId id;

  @NotNull
  private ObjectId user;

  @NotNull
  private ObjectId account;

  @NotNull
  private String reference;

  @NotNull
  private String message;

  @NotNull
  private String payload;

  @NotNull
  private Instant timestamp;

  @NotNull
  private WidgetFeedbackCategory category;

  @NotNull
  private WidgetFeedbackState state = WidgetFeedbackState.UNKNOWN;

  public WidgetFeedback(
    final ObjectId user,
    final ObjectId account,
    final String reference,
    final String message,
    final String payload,
    final WidgetFeedbackCategory category,
    final Instant timestamp
  ) {
    this.user = user;
    this.account = account;
    this.reference = reference;
    this.message = message;
    this.payload = payload;
    this.category = category;
    this.timestamp = timestamp;
  }
}
