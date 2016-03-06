package mg.util.db.persist.proxy;

import static mg.util.validation.Validator.validateNotNull;
import static mg.util.validation.Validator.validateNotNullOrEmpty;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.returns;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.Pipe;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.matcher.MethodParametersMatcher;

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

        ListProxy<T> listProxy = new ListProxy<T>(listProxyParameters);

        List<T> list = new ByteBuddy().subclass(List.class)
                                      .method(named("size").and(returns(Integer.class)))
                                      .intercept(MethodDelegation.to(listProxy)
                                                                 .filter(named("listPipeTypeGetters"))
                                                                 .appendParameterBinder(Pipe.Binder.install(Forwarder.class)))
                                      .method(named("get").or(named("empty")))
                                      .intercept(MethodDelegation.to(listProxy)
                                                                 .filter(named("listPipe"))
                                                                 .appendParameterBinder(Pipe.Binder.install(Forwarder.class)))
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

    public T listPipeTypeGetters(@Pipe Forwarder<T, List<T>> pipe) {
        System.out.println("Calling list pipe2");
        try {
            return pipe.to(params.list);

        } finally {
            System.out.println("Returned from list");
        }
    }

    public Integer listPipe(@Pipe Forwarder<Integer, List<T>> pipe) {
        System.out.println("Calling list pipe1");
        try {
            return pipe.to(params.list);

        } finally {
            System.out.println("Returned from list");
        }
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
