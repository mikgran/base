package mg.util.db.persist.proxy;

import static mg.util.validation.Validator.validateNotNull;
import static mg.util.validation.Validator.validateNotNullOrEmpty;
import static net.bytebuddy.matcher.ElementMatchers.named;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;

public class ListProxy<T> {

    @SuppressWarnings("unchecked")
    public static <T> List<T> newInstance(ListProxyParameters<List<T>> listProxyParameters) throws InstantiationException, IllegalAccessException {

        validateNotNull("listProxyParameters", listProxyParameters);
        validateNotNull("listProxyParameters.list", listProxyParameters.list);
        validateNotNull("listProxyParameters.db", listProxyParameters.db);
        validateNotNullOrEmpty("listProxyParameters.listPopulationSql", listProxyParameters.listPopulationSql);

        //        return (List<T>) Proxy.newProxyInstance(listProxyParameters.list.getClass().getClassLoader(),
        //                                                listProxyParameters.list.getClass().getInterfaces(),
        //                                                new ListProxy<T>(listProxyParameters));

        List<T> newInstance = new ByteBuddy().subclass(List.class)
                                             //.method(ElementMatchers.named("apply"));
                                             .method(named("add").or(named("size"))).intercept(MethodDelegation.to())
                                             //.method(ElementMatchers.)
                                             //.intercept(MethodDelegation.to(new GreetingInterceptor()))
                                             .make()
                                             .load(listProxyParameters.list.getClass().getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                                             .getLoaded()
                                             .newInstance();

        return newInstance;
    }

    private Logger logger = LoggerFactory.getLogger(ListProxy.class);
    private ListProxyParameters<List<T>> params;

    private ListProxy(ListProxyParameters<List<T>> listProxyParameters) {
        this.params = listProxyParameters;

    }

    // TOIMPROVE: replace with a better exception handling and logging
    @SuppressWarnings("unchecked")
    //@Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

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
