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

package de.tanktaler.insider;

import com.mongodb.MongoClient;
import de.tanktaler.insider.core.MongoHealthCheck;
import de.tanktaler.insider.core.MongoManaged;
import de.tanktaler.insider.crnk.InsiderModule;
import io.crnk.rs.CrnkFeature;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import java.util.EnumSet;

public final class ApiApplication extends Application<ApiConfiguration> {
  public static void main(String[] args) throws Exception {
    (new ApiApplication()).run(args);
  }

  @Override
	public void initialize(Bootstrap<ApiConfiguration> bootstrap) {

  }

  @Override
  public void run(ApiConfiguration configuration, Environment environment) {
    // @todo! remove it
    FilterRegistration.Dynamic filter = environment.servlets()
      .addFilter("CrossOriginFilter", CrossOriginFilter.class);
    filter.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "http://localhost:*");
    filter.setInitParameter(
      CrossOriginFilter.ALLOWED_HEADERS_PARAM,
      "Authorization,X-Requested-With,Content-Type,Accept,Origin"
    );
    filter.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "*");

    final MongoClient mongo = new MongoClient();
    final Morphia morphia = new Morphia();
    morphia.mapPackage("de.tanktaler.insider.resources");

    final Datastore datastore = morphia.createDatastore(mongo, "insider_program");

    environment.lifecycle().manage(new MongoManaged(mongo, datastore));
    environment.healthChecks().register("mongo", new MongoHealthCheck(datastore));

    final CrnkFeature crnk = new CrnkFeature();
    crnk.getBoot().addModule(new InsiderModule(datastore));

    environment.jersey().register(crnk);
  }

  @Override
  public String getName() {
    return "tanktaler-insider-program-api";
  }
}
