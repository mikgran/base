package mg.restgen.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import mg.restgen.rest.CustomAnnotationIntrospector;
import mg.util.db.DBConfig;

public class CrudService extends RestService {

    private SimpleFilterProvider defaultFilterProvider;
    private ObjectMapper mapper;
    private ObjectWriter writer;

    public CrudService(DBConfig dbConfig) {
        initMapper();
        initDefaultFilterProvider();
        initDefaultWriter();
    }

    @Override
    public void apply(Object target, Map<String, Object> parameters) {

        // - get command
        // - convert target into accepted object
        // - carry the Class<?> in the parameters -> change the type of the Map<String, String> -> Map<String, Object>

        String command = (String) parameters.get("command");
        String nameRef = (String) parameters.get("classRef");

        // FIXME: last last last


    }

    // signal every object as applicaple
    @Override
    public List<Class<? extends Object>> getAcceptableTypes() {
        return Arrays.asList(Object.class);
    }

    private void initDefaultFilterProvider() {
        defaultFilterProvider = new SimpleFilterProvider();
        defaultFilterProvider.setFailOnUnknownId(false);
    }

    private void initDefaultWriter() {
        writer = mapper.writer(defaultFilterProvider);
    }

    private void initMapper() {
        mapper = new ObjectMapper();
        mapper.setAnnotationIntrospector(new CustomAnnotationIntrospector());
        mapper.disable(MapperFeature.USE_GETTERS_AS_SETTERS);
        mapper.enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
    }

}
