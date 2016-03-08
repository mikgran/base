package mg.util.db.persist.proxy;

import static mg.util.validation.Validator.validateNotNull;
import static mg.util.validation.Validator.validateNotNullOrEmpty;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.List;

import mg.util.db.persist.DBMappingException;
import mg.util.db.persist.DBValidityException;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.matcher.ElementMatchers;

public class DBProxy<T> {

    @SuppressWarnings("unchecked")
    public static <T> List<T> newInstance(DBProxyParameters<List<T>> parameters) throws InstantiationException, IllegalAccessException {

        validateNotNull("listProxyParameters", parameters);
        validateNotNull("listProxyParameters.list", parameters.type);
        validateNotNull("listProxyParameters.db", parameters.db);
        validateNotNullOrEmpty("listProxyParameters.listPopulationSql", parameters.listPopulationSql);

        DBProxy<T> listProxy = new DBProxy<T>(parameters);

        // TOIMPROVE: filter down by relevant method names; not all names necessary.
        List<T> list = new ByteBuddy().subclass(List.class)
                                      //.method(named("size").or(named("get")).or(named("add")).or(named("empty")))
                                      .method(ElementMatchers.any())
                                      .intercept(MethodDelegation.to(listProxy))
                                      .make()
                                      .load(DBProxy.class.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                                      .getLoaded()
                                      .newInstance();
        return list;
    }

    // private Logger logger = LoggerFactory.getLogger(ListProxy.class);
    private DBProxyParameters<List<T>> params;

    private DBProxy(DBProxyParameters<List<T>> listProxyParameters) {
        this.params = listProxyParameters;

    }

    @SuppressWarnings("unchecked")
    @RuntimeType
    public Object intercept(@Origin Method method, @AllArguments Object[] allArguments)
        throws IllegalAccessException,
        IllegalArgumentException,
        InvocationTargetException,
        DBValidityException,
        DBMappingException,
        SQLException {

        System.out.println("Method: " + method.toString());

        if (!params.fetched) {

            List<T> persistables = (List<T>) params.db.findAllBy(params.refPersistable, params.listPopulationSql);

            params.type.clear();
            params.type.addAll(persistables);

            params = new DBProxyParameters<List<T>>(params, true);
        }

        return method.invoke(params.type, allArguments);
    }
}
