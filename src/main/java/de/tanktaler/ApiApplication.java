package de.tanktaler;

import io.crnk.rs.CrnkFeature;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;

public class ApiApplication extends Application<ApiConfiguration> {
    public static void main(String[] args) throws Exception {
        (new ApiApplication()).run(args);
    }

    @Override
    public void run(ApiConfiguration configuration, Environment environment) {
        environment.jersey().register(new CrnkFeature());
    }

    @Override
    public String getName() {
        return "tanktaler-insider-program-api";
    }
}
