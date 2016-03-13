package mg.util.db.persist.proxy;

import static mg.util.validation.Validator.validateNotNull;

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

public class DBListProxy<T> {

    @SuppressWarnings("unchecked")
    public static <T> List<T> newList(DBProxyParameters<List<T>> parameters) throws InstantiationException, IllegalAccessException {

        validateParameters(parameters);

        DBListProxy<T> listProxy = new DBListProxy<>(parameters);

        // TOIMPROVE: filter down by relevant method names; not all names necessary.
        List<T> list = new ByteBuddy().subclass(List.class)
                                      .method(ElementMatchers.any())
                                      .intercept(MethodDelegation.to(listProxy))
                                      .make()
                                      .load(DBListProxy.class.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                                      .getLoaded()
                                      .newInstance();

        return list;
    }

    private static <T> void validateParameters(DBProxyParameters<?> parameters) {
        validateNotNull("parameters", parameters);
        validateNotNull("parameters.type", parameters.type);
        validateNotNull("parameters.db", parameters.db);
        validateNotNull("parameters.refPersistable", parameters.refPersistable);
        validateNotNull("parameters.listPopulationSql", parameters.populationSql);
    }

    private DBProxyParameters<List<T>> listParameters;

    private DBListProxy(DBProxyParameters<List<T>> listProxyParameters) {
        this.listParameters = listProxyParameters;
    }

    // TOIMPROVE: replace this shit
    @SuppressWarnings("unchecked")
    @RuntimeType
    public Object intercept(@Origin Method method, @AllArguments Object[] allArguments)
        throws IllegalAccessException,
        IllegalArgumentException,
        InvocationTargetException,
        DBValidityException,
        DBMappingException,
        SQLException {

        if (listParameters != null && !listParameters.fetched) {

            System.out.println("(fetch) List: Method: " + method.toString());

            List<T> persistables = (List<T>) listParameters.db.findAllBy(listParameters.refPersistable, listParameters.populationSql);

            listParameters.type.clear();
            listParameters.type.addAll(persistables);

            listParameters = new DBProxyParameters<List<T>>(listParameters, true);

        }

        return method.invoke(listParameters.type, allArguments);
    }
}
