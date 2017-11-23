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
import mg.util.db.DBConfig;
import mg.util.db.persist.DBValidityException;
import mg.util.db.persist.Persistable;
import mg.util.functional.function.ThrowingFunction;

public class CrudService extends RestService {

    private Map<String, ThrowingFunction<Persistable, ServiceResult, Exception>> commands = new HashMap<>();
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
    public ServiceResult apply(Object target, Map<String, Object> parameters) {

        validateNotNull("target", target);
        validateNotNull("parameters", parameters);

        Optional<Object> command = Optional.ofNullable(parameters.get("command"));

        // fire the handler
        Optional<ServiceResult> serviceResult;
        serviceResult = command.filter(String.class::isInstance)
                               .map(String.class::cast)
                               .filter(cmd -> Persistable.class.isInstance(target)) // (out)side effect filter O_o?
                               .map(cmd -> commands.get(cmd)
                                                   .apply((Persistable) target));

        return serviceResult.orElseGet(() -> ServiceResult.badQuery());
    }

    // signal every object as applicable
    @Override
    public List<Class<? extends Object>> getAcceptableTypes() {
        return Arrays.asList(Object.class);
    }

    public ServiceResult handlePut(Persistable persistable) throws IllegalArgumentException, ClassNotFoundException {

        try {
            persistable.setConnectionAndDB(dbConfig.getConnection());
            persistable.save();

        } catch (SQLException | DBValidityException e) {
            // TOIMPROVE: logging!
            // TOIMPROVE: remove exception exposure
            return ServiceResult.internalError(e.getMessage());
        }

        return ServiceResult.ok();
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
