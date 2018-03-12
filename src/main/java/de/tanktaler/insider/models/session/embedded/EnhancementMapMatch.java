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

import java.time.Instant;
import java.util.List;
import org.mongodb.morphia.annotations.Embedded;

@Embedded
public final class EnhancementMapMatch {
  private Double confidence;

  private Float distance;
  private Float duration;
  private Float weight;
  private Instant lastLocationTimestamp;
  private List<Double[]> coordinates;

  @Embedded
  private List<Leg> legs;

  @Embedded
  private List<Trace> traces;

  public Double getConfidence() {
    return this.confidence;
  }

  public Float getDistance() {
    return this.distance;
  }

  public Float getDuration() {
    return this.duration;
  }

  public Float getWeight() {
    return this.weight;
  }

  public Instant getLastLocationTimestamp() {
    return this.lastLocationTimestamp;
  }

  public List<Double[]> getCoordinates() {
    return this.coordinates;
  }

  public List<Trace> getTraces() {
    return this.traces;
  }

  public void setConfidence(final Double confidence) {
    this.confidence = confidence;
  }

  public void setDistance(final Float distance) {
    this.distance = distance;
  }

  public void setDuration(final Float duration) {
    this.duration = duration;
  }

  public void setWeight(final Float weight) {
    this.weight = weight;
  }

  public void setLastLocationTimestamp(final Instant lastLocationTimestamp) {
    this.lastLocationTimestamp = lastLocationTimestamp;
  }

  public void setCoordinates(final List<Double[]> coordinates) {
    this.coordinates = coordinates;
  }

  public List<Leg> getLegs() {
    return this.legs;
  }

  public void setTraces(final List<Trace> traces) {
    this.traces = traces;
  }

  public void setLegs(final List<Leg> legs) {
    this.legs = legs;
  }

  @Embedded
  public static final class Trace {
    private Double[] location;
    private String street;

    public Double[] getLocation() {
      return this.location;
    }

    public String getStreet() {
      return this.street;
    }

    public void setLocation(final Double[] location) {
      this.location = location;
    }

    public void setStreet(final String street) {
      this.street = street;
    }
  }

  @Embedded
  public static final class Leg {
    private Annotation annotation;
    private Float distance;
    private Float duration;

    public Annotation getAnnotation() {
      return this.annotation;
    }

    public Float getDistance() {
      return this.distance;
    }

    public Float getDuration() {
      return this.duration;
    }

    public void setAnnotation(final Annotation annotation) {
      this.annotation = annotation;
    }

    public void setDistance(final Float distance) {
      this.distance = distance;
    }

    public void setDuration(final Float duration) {
      this.duration = duration;
    }

    @Embedded
    public static final class Annotation {
      private List<Float> speed;
      private List<Long> nodes;

      public List<Float> getSpeed() {
        return this.speed;
      }

      public List<Long> getNodes() {
        return this.nodes;
      }

      public void setSpeed(final List<Float> speed) {
        this.speed = speed;
      }

      public void setNodes(final List<Long> nodes) {
        this.nodes = nodes;
      }
    }
  }
}
