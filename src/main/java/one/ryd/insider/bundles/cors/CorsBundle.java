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

package one.ryd.insider.bundles.cors;

import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import java.util.EnumSet;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import one.ryd.insider.ApiConfiguration;
import org.eclipse.jetty.servlets.CrossOriginFilter;

public final class CorsBundle implements ConfiguredBundle<ApiConfiguration> {
  @Override
  public void run(final ApiConfiguration configuration, final Environment environment) {
    FilterRegistration.Dynamic filter = environment.servlets()
      .addFilter("CrossOriginFilter", CrossOriginFilter.class);
    filter.setInitParameter(
      CrossOriginFilter.ALLOWED_ORIGINS_PARAM,
      configuration.getApi().getCors().getAllowedOrigins()
    );
    filter.setInitParameter(
      CrossOriginFilter.ALLOWED_HEADERS_PARAM,
      configuration.getApi().getCors().getAllowedHeaders()
    );
    filter.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "*");
  }

  @Override
  public void initialize(final Bootstrap bootstrap) {
    // nothing here
  }
}
