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

package one.ryd.insider;

import com.mongodb.MongoClientURI;
import io.dropwizard.Configuration;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public final class ApiConfiguration extends Configuration {
  @NotNull
  @Valid
  private Database dbInsider;

  @NotNull
  @Valid
  private Database dbSession;

  @NotNull
  @Valid
  private Api api;

  public Database getDbInsider() {
    return this.dbInsider;
  }

  public Database getDbSession() {
    return this.dbSession;
  }

  public Api getApi() {
    return this.api;
  }

  public static class Database {
    @NotNull
    private MongoClientURI uri;

    public MongoClientURI getUri() {
      return this.uri;
    }
  }

  public static class Api {
    @NotNull
    @Valid
    private Cors cors;

    public Cors getCors() {
      return this.cors;
    }
  }

  public static class Cors {
    @NotNull
    @Size(min=1)
    private String allowedOrigins;

    @NotNull
    @Size(min=1)
    private String allowedHeaders;

    public String getAllowedOrigins() {
      return this.allowedOrigins;
    }

    public String getAllowedHeaders() {
      return this.allowedHeaders;
    }
  }
}
