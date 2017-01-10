package mg.angular.rest;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath("api2")
public class RestApplication extends ResourceConfig {

    public RestApplication() {

        packages("mg.angular.rest");

        register(CORSResponseFilter.class);
    }
}
