package mg.util.db.persist.proxy;

import static mg.util.validation.Validator.validateNotNull;
import static mg.util.validation.Validator.validateNotNullOrEmpty;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.List;

import mg.util.db.persist.DBMappingException;
import mg.util.db.persist.DBValidityException;
import mg.util.db.persist.Persistable;
import mg.util.db.persist.SqlBuilder;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.matcher.ElementMatchers;

public class DBProxy<T> {

    @SuppressWarnings("unchecked")
    public static <T> T newInstance(DBProxyParameters<T> parameters) throws InstantiationException, IllegalAccessException {

        validateParameters(parameters);

        DBProxy<T> instanceProxy = new DBProxy<>(parameters, true);

        // TOIMPROVE: filter down by relevant method names; not all names necessary.
        T newInstance = (T) new ByteBuddy().subclass(parameters.type.getClass())
                                                 //.method(named("size").or(named("get")).or(named("add")).or(named("empty")))
                                                 .method(ElementMatchers.any())
                                                 .intercept(MethodDelegation.to(instanceProxy))
                                                 .make()
                                                 .load(DBProxy.class.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                                                 .getLoaded()
                                                 .newInstance();
        return newInstance;
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> newList(DBProxyParameters<List<T>> parameters) throws InstantiationException, IllegalAccessException {

        validateParameters(parameters);

        DBProxy<T> listProxy = new DBProxy<>(parameters);

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

    private static <T> void validateParameters(DBProxyParameters<?> parameters) {
        validateNotNull("parameters", parameters);
        validateNotNull("parameters.type", parameters.type);
        validateNotNull("parameters.db", parameters.db);
        validateNotNull("parameters.refPersistable", parameters.refPersistable);
        validateNotNullOrEmpty("parameters.listPopulationSql", parameters.populationSql);
    }

    private DBProxyParameters<T> instanceParameters;
    private DBProxyParameters<List<T>> listParameters;

    private DBProxy(DBProxyParameters<List<T>> listProxyParameters) {
        this.listParameters = listProxyParameters;
    }

    private DBProxy(DBProxyParameters<T> instanceParameters, boolean b) {
        this.instanceParameters = instanceParameters;
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

        if (listParameters != null && !listParameters.fetched) {

            System.out.println("List: Method: " + method.toString());

            List<T> persistables = (List<T>) listParameters.db.findAllBy(listParameters.refPersistable, listParameters.populationSql);

            listParameters.type.clear();
            listParameters.type.addAll(persistables);

            listParameters = new DBProxyParameters<List<T>>(listParameters, true);

            return method.invoke(listParameters.type, allArguments);

        } else if (instanceParameters != null && !instanceParameters.fetched) {

            System.out.println("Instance: Method: " + method.toString());

            // TODO FIX: target persistable, ref persistable assign ref to target
            Persistable persistable = instanceParameters.db.findBy(instanceParameters.refPersistable, instanceParameters.populationSql);

            SqlBuilder sqlBuilder = new SqlBuilder(persistable);
            // use sqlbuilder to do an assignment: target.field <- refPersistable
        }

        return method.invoke(instanceParameters.type, allArguments);
    }
}
