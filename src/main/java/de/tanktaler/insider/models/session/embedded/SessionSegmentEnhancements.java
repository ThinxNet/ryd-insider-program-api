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

package de.tanktaler.insider.models.session.embedded;

import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.mongodb.morphia.annotations.Embedded;

@Embedded
public final class SessionSegmentEnhancements {
  @Embedded
  private Document weather;

  @Embedded
  private List<EnhancementMapMatch> mapMatches = new ArrayList<>();

  public Document getWeather() {
    return this.weather;
  }

  public void setWeather(final Document doc) {
    this.weather = doc;
  }

  public List<EnhancementMapMatch> getMapMatches() {
    return this.mapMatches;
  }

  public void setMapMatches(final List<EnhancementMapMatch> matches) {
    this.mapMatches = matches;
  }

  public void addToMapMatches(final EnhancementMapMatch obj) {
    this.mapMatches.add(obj);
  }
}
