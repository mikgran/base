package mg.restgen.rest;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath("api3")
public class RestApplication extends ResourceConfig {

    public RestApplication() {

        packages("mg.restgen.rest");

        register(CORSResponseFilter.class);

        String[] packagesAr = {"mg.restgen.rest"};

        // TOIMPROVE: reuse jersey internals for this? or
        // PackageNamesScanner packageNamesScanner = new PackageNamesScanner(packagesAr, true);
        // packageNamesScanner.hasNext();

        // FIXME: add annotations for service classes that should register
        // FIXME: RestGen: call Services.register("<packageName>"); -> scan all classes in that package, and register the
        // ones with @Service annotation.
        // allow multiple packages Services.register(String... packageNames) or Services.register(List<String> packageNames)
    }
}
