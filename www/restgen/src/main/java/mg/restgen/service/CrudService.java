package mg.restgen.service;

import static mg.util.Common.asInstanceOf;
import static mg.util.validation.Validator.validateNotNull;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mg.util.db.DBConfig;
import mg.util.db.persist.DBValidityException;
import mg.util.db.persist.Persistable;
import mg.util.functional.function.ThrowingBiFunction;
import mg.util.functional.function.ThrowingFunction;
import mg.util.functional.option.Opt;

public class CrudService extends RestService {

    private static final String GET = "get";
    private static final String PUT = "put";
    private Map<String, ThrowingBiFunction<Persistable, Map<String, Object>, ServiceResult, Exception>> handlers = new HashMap<>();
    private DBConfig dbConfig;

    public CrudService(DBConfig dbConfig) throws IllegalArgumentException, ClassNotFoundException, SQLException {

        validateNotNull("dbConfig", dbConfig);
        this.dbConfig = dbConfig;

        handlers.put(PUT, this::handlePut);
        handlers.put(GET, this::handleGet);
    }

    @Override
    public ServiceResult apply(Object target, Map<String, Object> parameters) throws Exception {

        validateNotNull("target", target);
        validateNotNull("parameters", parameters);

        // fire the handler
        Opt<ServiceResult> serviceResult;
        serviceResult = Opt.of(parameters.get("command"))
                           .map(asInstanceOf(String.class))
                           .filter(command -> Persistable.class.isInstance(target)) // (out)side effect filter O_o?
                           .map(applyCrudHandler(target, parameters));

        return serviceResult.getOrElseGet(() -> ServiceResult.badQuery());
    }

    // signal every Persistable as applicable
    @Override
    public List<Class<? extends Object>> getAcceptableTypes() {
        return Arrays.asList(Persistable.class);
    }

    /*
     * XXX: add support for: find, findById and FindAll cases.
     */
    public ServiceResult handleGet(Persistable persistable, Map<String, Object> parameters) {

        ServiceResult result;
        try {
            Opt.of(persistable)
               .match(persistable, (p) -> true, n -> n)

               ;

            // case findAllBy(T)
            result = Opt.of(persistable)
                        .map(p -> p.setConnectionAndDB(dbConfig.getConnection()))
                        .map(Persistable::find)
                        .map(p -> ServiceResult.ok(p))
                        .getOrElseGet(() -> ServiceResult.noContent());

        } catch (Exception e) {
            // TOIMPROVE: logging!
            // TOIMPROVE: remove exception exposure
            result = ServiceResult.internalError(e.getMessage());
        }

        return result;
    }

    public ServiceResult handlePut(Persistable persistable, Map<String, Object> parameters) throws IllegalArgumentException, ClassNotFoundException {

        ServiceResult result;
        try {
            persistable.setConnectionAndDB(dbConfig.getConnection());
            persistable.save();
            result = ServiceResult.created("" + persistable.getId());

        } catch (SQLException | DBValidityException e) {
            // TOIMPROVE: logging!
            // TOIMPROVE: remove exception exposure
            result = ServiceResult.internalError(e.getMessage());
        }

        return result;
    }

    @Override
    public boolean isGeneralService() {
        return true;
    }

    private ThrowingFunction<String, ServiceResult, RuntimeException> applyCrudHandler(Object target, Map<String, Object> parameters) {
        return cmd -> {
            return Opt.of(handlers.get(cmd))
                      .map(function -> function.apply((Persistable) target, parameters))
                      .getOrElseGet(() -> ServiceResult.badQuery("No handler defined for: " + cmd + " and target: " + target));
        };
    }
}
