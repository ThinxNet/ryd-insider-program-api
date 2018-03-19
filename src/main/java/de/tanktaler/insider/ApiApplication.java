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
import com.mongodb.MongoClientURI;
import de.tanktaler.insider.core.MongoHealthCheck;
import de.tanktaler.insider.core.MongoManaged;
import de.tanktaler.insider.core.auth.InsiderAuthPrincipal;
import de.tanktaler.insider.core.auth.InsiderTokenAuthFilter;
import de.tanktaler.insider.core.auth.InsiderTokenAuthenticator;
import de.tanktaler.insider.core.module.InsiderModule;
import de.tanktaler.insider.resources.AccountResource;
import de.tanktaler.insider.resources.DeviceResource;
import de.tanktaler.insider.resources.SessionResource;
import de.tanktaler.insider.resources.StatisticsResource;
import de.tanktaler.insider.resources.ThingResource;
import de.tanktaler.insider.resources.UserResource;
import io.dropwizard.Application;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import java.util.EnumSet;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

public final class ApiApplication extends Application<ApiConfiguration> {
  public static void main(final String[] args) throws Exception {
    (new ApiApplication()).run(args);
  }

  @Override
  public void initialize(final Bootstrap<ApiConfiguration> bootstrap) {
    bootstrap.getObjectMapper().registerModule(new InsiderModule());
  }

  @Override
  public void run(final ApiConfiguration configuration, final Environment environment) {
    // @todo! remove it
    FilterRegistration.Dynamic filter = environment.servlets()
      .addFilter("CrossOriginFilter", CrossOriginFilter.class);
    filter.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "http://localhost:*");
    filter.setInitParameter(
      CrossOriginFilter.ALLOWED_HEADERS_PARAM,
      "Authorization,X-Requested-With,X-Txn-Auth-Token,Content-Type,Accept,Origin"
    );
    filter.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "*");

    final Morphia morphia = new Morphia();
    morphia.getMapper().getOptions().setStoreEmpties(true);
    morphia.getMapper().getOptions().setStoreNulls(true);
    morphia.mapPackage("de.tanktaler.insider.models");

    environment.jersey().register(new AbstractBinder() {
      @Override
      protected void configure() {
        bind(morphia).to(Morphia.class);
      }
    });

    // @todo! move these to a bundle
    final MongoClientURI dsInsiderUri = configuration.getDbInsider().getUri();
    final MongoClientURI dsSessionUri = configuration.getDbSession().getUri();

    final MongoClient mongoInsider = new MongoClient(dsInsiderUri);
    final MongoClient mongoSession = new MongoClient(dsSessionUri);

    final Datastore dsInsider = morphia.createDatastore(mongoInsider, dsInsiderUri.getDatabase());
    final Datastore dsSession = morphia.createDatastore(mongoSession, dsSessionUri.getDatabase());

    environment.lifecycle().manage(new MongoManaged(mongoInsider, dsInsider));
    environment.lifecycle().manage(new MongoManaged(mongoSession, dsSession));

    environment.healthChecks().register("mongoInsider", new MongoHealthCheck(dsInsider));
    environment.healthChecks().register("mongoSession", new MongoHealthCheck(dsSession));

    environment.jersey().register(
      new AuthDynamicFeature(
        new InsiderTokenAuthFilter<>(
          new InsiderTokenAuthenticator(dsInsider)
        )
      )
    );

    environment.jersey().register(RolesAllowedDynamicFeature.class);
    environment.jersey().register(
      new AuthValueFactoryProvider.Binder<>(InsiderAuthPrincipal.class)
    );
    environment.jersey().register(new AccountResource(dsInsider));
    environment.jersey().register(new DeviceResource(dsInsider));
    environment.jersey().register(new SessionResource(dsInsider, dsSession));
    environment.jersey().register(new StatisticsResource(dsInsider, dsSession));
    environment.jersey().register(new ThingResource(dsInsider));
    environment.jersey().register(new UserResource(dsInsider));
  }

  @Override
  public String getName() {
    return "tanktaler-insider-program-api";
  }
}
