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

package one.ryd.insider.resources.request;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import one.ryd.insider.models.feedback.WidgetFeedbackCategory;

public final class FeedbackWidgetParam {
  @NotNull(message = "can't be empty")
  @Size(min = 10, message = "minimum required length is {min} chars")
  private String message;

  @NotNull
  @Size(min = 1)
  private String payload;

  @NotNull
  private WidgetFeedbackCategory category;

  public String getMessage() {
    return this.message;
  }

  public String getPayload() {
    return this.payload;
  }

  public WidgetFeedbackCategory getCategory() {
    return this.category;
  }
}
