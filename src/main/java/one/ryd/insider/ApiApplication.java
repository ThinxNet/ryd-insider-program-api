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

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import io.dropwizard.Application;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import one.ryd.insider.bundles.cors.CorsBundle;
import one.ryd.insider.core.MongoHealthCheck;
import one.ryd.insider.core.MongoManaged;
import one.ryd.insider.core.auth.InsiderAuthPrincipal;
import one.ryd.insider.core.auth.InsiderTokenAuthFilter;
import one.ryd.insider.core.auth.InsiderTokenAuthenticator;
import one.ryd.insider.core.module.InsiderModule;
import one.ryd.insider.resources.AccountResource;
import one.ryd.insider.resources.DeleteMeResource;
import one.ryd.insider.resources.SessionResource;
import one.ryd.insider.resources.StatisticsResource;
import one.ryd.insider.resources.ThingResource;
import one.ryd.insider.resources.filter.AccountBelongsToTheUserFilter;
import one.ryd.insider.resources.filter.SessionBelongsToTheUserFilter;
import one.ryd.insider.resources.filter.ThingBelongsToTheUserFilter;
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
    bootstrap.addBundle(new CorsBundle());
    bootstrap.getObjectMapper().registerModule(new InsiderModule());
  }

  @Override
  public void run(final ApiConfiguration configuration, final Environment environment) {
    // @todo #7:15min move the morphia instance to a separate bundle
    final Morphia morphia = new Morphia();
    morphia.getMapper().getOptions().setStoreEmpties(true);
    morphia.getMapper().getOptions().setStoreNulls(true);
    morphia.mapPackage("one.ryd.insider.models");

    final MongoClientURI dsInsiderUri = configuration.getDbInsider().getUri();
    final MongoClientURI dsSessionUri = configuration.getDbSession().getUri();

    final MongoClient mongoInsider = new MongoClient(dsInsiderUri);
    final MongoClient mongoSession = new MongoClient(dsSessionUri);

    final Datastore dsInsider = morphia.createDatastore(mongoInsider, dsInsiderUri.getDatabase());
    final Datastore dsSession = morphia.createDatastore(mongoSession, dsSessionUri.getDatabase());

    environment.jersey().register(new AbstractBinder() {
      @Override
      protected void configure() {
        bind(dsInsider).to(Datastore.class).proxy(true).proxyForSameScope(false)
          .named("datastoreInsider");
        bind(dsSession).to(Datastore.class).proxy(true).proxyForSameScope(false)
          .named("datastoreSession");
        bind(morphia).to(Morphia.class);
      }
    });

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

    // annotations
    environment.jersey().getResourceConfig().register(
      new AuthValueFactoryProvider.Binder<>(InsiderAuthPrincipal.class), 21
    );
    environment.jersey().register(AccountBelongsToTheUserFilter.class);
    environment.jersey().register(RolesAllowedDynamicFeature.class);
    environment.jersey().register(SessionBelongsToTheUserFilter.class);
    environment.jersey().register(ThingBelongsToTheUserFilter.class);

    // resources
    environment.jersey().register(AccountResource.class);
    environment.jersey().register(DeleteMeResource.class); // @todo #7 remove the delete_me endpoint
    environment.jersey().register(SessionResource.class);
    environment.jersey().register(StatisticsResource.class);
    environment.jersey().register(ThingResource.class);
  }

  @Override
  public String getName() {
    return "ryd-insider-program-api";
  }
}
