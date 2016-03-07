package mg.util.db.persist.proxy;

import static mg.util.validation.Validator.validateNotNull;
import static mg.util.validation.Validator.validateNotNullOrEmpty;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.matcher.ElementMatchers;

public class ListProxy<T> {

    @SuppressWarnings("unchecked")
    public static <T> List<T> newInstance(ListProxyParameters<List<T>> listProxyParameters) throws InstantiationException, IllegalAccessException {

        validateNotNull("listProxyParameters", listProxyParameters);
        validateNotNull("listProxyParameters.list", listProxyParameters.list);
        validateNotNull("listProxyParameters.db", listProxyParameters.db);
        validateNotNullOrEmpty("listProxyParameters.listPopulationSql", listProxyParameters.listPopulationSql);

        ListProxy<T> listProxy = new ListProxy<T>(listProxyParameters);

        List<T> list = new ByteBuddy().subclass(List.class)
                                      //.method(named("size").or(named("get")).or(named("add")).or(named("empty")))
                                      .method(ElementMatchers.any())
                                      .intercept(MethodDelegation.to(listProxy))
                                      .make()
                                      .load(ListProxy.class.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                                      .getLoaded()
                                      .newInstance();
        return list;
    }

    private Logger logger = LoggerFactory.getLogger(ListProxy.class);
    private ListProxyParameters<List<T>> params;

    private ListProxy(ListProxyParameters<List<T>> listProxyParameters) {
        this.params = listProxyParameters;

    }

    @RuntimeType
    public Object intercept(@AllArguments Object[] allArguments, @Origin Method method)
        throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

        // intercept any method of any signature

        System.out.println("Hello World!");

        return method.invoke(params.list, allArguments);
    }

    // TOIMPROVE: replace with a better exception handling and logging
    @SuppressWarnings({"unchecked", "unused"})
    //@Override
    private Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        Object result;
        try {

            logger.debug("ListProxy.invoke: before method " + method.getName());

            if (!params.fetched) {

                List<T> persistables = (List<T>) params.db.findAllBy(params.refPersistable, params.listPopulationSql);

                params.list.clear();
                params.list.addAll(persistables);

                params = new ListProxyParameters<List<T>>(params.db,
                                                          params.list,
                                                          params.listPopulationSql,
                                                          params.refPersistable);

                setFetchedParameter();
            }

            result = method.invoke(params.list, args);

        } catch (InvocationTargetException e) {

            throw e.getTargetException();

        } catch (Exception e) {

            // TOIMPROVE: change this
            throw new RuntimeException(e.getMessage());

        } finally {

            logger.debug("ListProxy.invoke: after method " + method.getName());
        }
        return result;
    }

    private void setFetchedParameter() {
        this.params = new ListProxyParameters<>(params, true);
    }
}
