package mg.restgen.service;

import static mg.util.validation.Validator.validateNotNull;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import mg.restgen.rest.CustomAnnotationIntrospector;
import mg.util.Tuple2;
import mg.util.db.DBConfig;
import mg.util.db.persist.DBValidityException;
import mg.util.db.persist.Persistable;
import mg.util.functional.consumer.ThrowingConsumer;

public class CrudService extends RestService {

    private Map<String, ThrowingConsumer<Tuple2<String, Object>, Exception>> commands = new HashMap<>();
    private DBConfig dbConfig;
    private SimpleFilterProvider defaultFilterProvider;
    private ObjectMapper mapper;
    private ObjectWriter writer;

    public CrudService(DBConfig dbConfig) throws IllegalArgumentException, ClassNotFoundException, SQLException {
        validateNotNull("dbConfig", dbConfig);
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

        Optional<Object> command = Optional.ofNullable(parameters.get("command"));

        command.map(String.class::cast)
               .map(cmd -> Tuple2.of(cmd, target))
               .ifPresent(t2 -> commands.get(t2._1)
                                        .accept(t2)); // fire the handler
    }

    // signal every object as applicable
    @Override
    public List<Class<? extends Object>> getAcceptableTypes() {
        return Arrays.asList(Object.class);
    }

    public void handlePut(Tuple2<String, Object> tuple2) throws IllegalArgumentException, ClassNotFoundException {

        Persistable persistable = Persistable.class.cast(tuple2._2);
        try {
            persistable.setConnectionAndDB(dbConfig.getConnection());
            persistable.save();

        } catch (SQLException | DBValidityException e) {
            System.out.println(e.getMessage());
            // TOIPROVE: logging!
        }
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
