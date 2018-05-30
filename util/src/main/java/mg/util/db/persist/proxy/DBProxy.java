package mg.util.db.persist.proxy;

import static mg.util.validation.Validator.validateNotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;

import mg.util.db.persist.DBMappingException;
import mg.util.db.persist.DBValidityException;
import mg.util.db.persist.Persistable;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.matcher.ElementMatchers;

public class DBProxy<T> {

    private DBProxyParameters<T> instanceParameters;

    @SuppressWarnings("unchecked")
    public static <T> T newInstance(DBProxyParameters<T> parameters) throws InstantiationException,
        IllegalAccessException,
        IllegalArgumentException,
        InvocationTargetException,
        NoSuchMethodException,
        SecurityException {

        validateParameters(parameters);

        DBProxy<T> instanceProxy = new DBProxy<>(parameters);

        // TOIMPROVE: filter by relevant method names
        T newInstance = (T) new ByteBuddy().subclass(parameters.type.getClass())
                                           .method(ElementMatchers.any())
                                           .intercept(MethodDelegation.to(instanceProxy))
                                           .make()
                                           .load(DBProxy.class.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                                           .getLoaded()
                                           .getDeclaredConstructor()
                                           .newInstance();

        return newInstance;
    }

    // TOIMPROVE: move to superclass: generalize for both proxy classes.
    private static <T> void validateParameters(DBProxyParameters<?> parameters) {
        validateNotNull("parameters", parameters);
        validateNotNull("parameters.type", parameters.type);
        validateNotNull("parameters.db", parameters.db);
        validateNotNull("parameters.refPersistable", parameters.refPersistable);
        validateNotNull("parameters.listPopulationSql", parameters.populationSql);
    }

    private DBProxy(DBProxyParameters<T> instanceParameters) {
        this.instanceParameters = instanceParameters;
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

        if (instanceParameters != null && !instanceParameters.fetched) {

            System.out.println("(fetch) Instance: Method: " + method.toString());

            Persistable persistable = instanceParameters.db.findBy(instanceParameters.refPersistable, instanceParameters.populationSql);

            instanceParameters = new DBProxyParameters<>(instanceParameters.db,
                                                         (T) persistable,
                                                         instanceParameters.populationSql,
                                                         instanceParameters.refPersistable,
                                                         true);

        }

        return method.invoke(instanceParameters.type, allArguments);
    }
}
