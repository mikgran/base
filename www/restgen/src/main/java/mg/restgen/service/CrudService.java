package mg.restgen.service;

import static mg.util.Common.asInstanceOfT;
import static mg.util.validation.Validator.validateNotNull;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mg.util.db.DBConfig;
import mg.util.db.persist.DBValidityException;
import mg.util.db.persist.Persistable;
import mg.util.functional.function.ThrowingFunction;
import mg.util.functional.option.Opt;

public class CrudService extends RestService {

    private static final String GET = "get";
    private static final String PUT = "put";
    private Map<String, ThrowingFunction<Persistable, ServiceResult, Exception>> commands = new HashMap<>();
    private DBConfig dbConfig;

    public CrudService(DBConfig dbConfig) throws IllegalArgumentException, ClassNotFoundException, SQLException {

        validateNotNull("dbConfig", dbConfig);
        this.dbConfig = dbConfig;

        commands.put(PUT, this::handlePut);
        commands.put(GET, this::handleGet);
    }

    @Override
    public ServiceResult apply(Object target, Map<String, Object> parameters) throws RuntimeException, Exception {

        validateNotNull("target", target);
        validateNotNull("parameters", parameters);

        // fire the handler
        Opt<ServiceResult> serviceResult;
        serviceResult = Opt.of(parameters.get("command"))
                           .map(asInstanceOfT(String.class))
                           .filter(cmd -> Persistable.class.isInstance(target)) // (out)side effect filter O_o?
                           .map((ThrowingFunction<String, ServiceResult, RuntimeException>) cmd -> {
                               return Opt.of(commands.get(cmd))
                                         .map(function -> function.apply((Persistable) target))
                                         .getOrElseGet(() -> ServiceResult.badQuery("No service defined for: " + cmd + " and target: " + target));
                           });

        return serviceResult.getOrElseGet(() -> ServiceResult.badQuery());
    }

    // signal every Persistable as applicable
    @Override
    public List<Class<? extends Object>> getAcceptableTypes() {
        return Arrays.asList(Persistable.class);
    }

    public ServiceResult handleGet(Persistable persistable) {

        ServiceResult result;
        try {
            result = Opt.of(persistable)
                        .map(asInstanceOfT(Persistable.class))
                        .map((ThrowingFunction<Persistable, Persistable, Exception>) p -> {
                            p.setConnectionAndDB(dbConfig.getConnection());
                            return p;
                        })
                        .map((ThrowingFunction<Persistable, Persistable, Exception>) Persistable::find)
                        .map(p -> ServiceResult.ok(p))
                        .getOrElseGet(() -> ServiceResult.noContent());

        } catch (Exception e) {
            // TOIMPROVE: logging!
            // TOIMPROVE: remove exception exposure
            result = ServiceResult.internalError(e.getMessage());
        }

        return result;
    }

    public ServiceResult handlePut(Persistable persistable) throws IllegalArgumentException, ClassNotFoundException {

        ServiceResult result;
        try {
            persistable.setConnectionAndDB(dbConfig.getConnection());
            persistable.save();
            result = ServiceResult.ok();

        } catch (SQLException | DBValidityException e) {
            // TOIMPROVE: logging!
            // TOIMPROVE: remove exception exposure
            result = ServiceResult.internalError(e.getMessage());
        }

        return result;
    }

}
