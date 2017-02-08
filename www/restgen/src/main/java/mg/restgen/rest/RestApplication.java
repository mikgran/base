package mg.restgen.rest;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath("api3")
public class RestApplication extends ResourceConfig {

    public RestApplication() {

        packages("mg.restgen.rest");

        register(CORSResponseFilter.class);
    }
}
