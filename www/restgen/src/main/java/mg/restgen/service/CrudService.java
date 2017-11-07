package mg.restgen.service;

import static mg.util.validation.Validator.validateNotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.tuple.Pair;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import mg.restgen.rest.CustomAnnotationIntrospector;
import mg.util.db.DBConfig;
import mg.util.validation.Validator;

public class CrudService extends RestService {

    private Map<String, BiConsumer<String, Object>> commands = new HashMap<>();
    private DBConfig dbConfig;
    private SimpleFilterProvider defaultFilterProvider;
    private ObjectMapper mapper;
    private ObjectWriter writer;

    public CrudService(DBConfig dbConfig) {
        Validator.validateNotNull("dbConfig", dbConfig);
        this.dbConfig = dbConfig;
        initMapper();
        initDefaultFilterProvider();
        initDefaultWriter();

        commands.put("put", this::handlePut);
    }

    @Override
    public void apply(Object target, Map<String, Object> parameters) {

        validateNotNull("target", target);
        validateNotNull("parameters", parameters);

        // - get command
        // - convert target into accepted object
        // - carry the Class<?> in the parameters -> change the type of the Map<String, String> -> Map<String, Object>

        Optional<Object> command = Optional.ofNullable(parameters.get("command"));

        command.map(String.class::cast)
               .map(cmd -> Pair.of(cmd, target))
               .ifPresent(pair -> commands.get(pair.getLeft())
                                          .accept(pair.getLeft(), pair.getRight()));

        // FIXME: last last last
    }

    // signal every object as applicable
    @Override
    public List<Class<? extends Object>> getAcceptableTypes() {
        return Arrays.asList(Object.class);
    }

    public void handlePut(String command, Object object) {

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
