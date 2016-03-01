package mg.util.db.persist.proxy;

import static mg.util.validation.Validator.validateNotNull;
import static mg.util.validation.Validator.validateNotNullOrEmpty;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mg.util.db.persist.Persistable;

public class ListProxy<T> implements InvocationHandler {

    @SuppressWarnings("unchecked")
    public static <T> List<T> newInstance(ListProxyParameters<List<T>> listProxyParameters) {

        validateNotNull("listProxyParameters", listProxyParameters);
        validateNotNull("listProxyParameters.list", listProxyParameters.list);
        validateNotNull("listProxyParameters.db", listProxyParameters.db);
        validateNotNullOrEmpty("listProxyParameters.listPopulationSql", listProxyParameters.listPopulationSql);

        return (List<T>) Proxy.newProxyInstance(listProxyParameters.list.getClass().getClassLoader(),
                                                listProxyParameters.list.getClass().getInterfaces(),
                                                new ListProxy<T>(listProxyParameters));
    }

    private Logger logger = LoggerFactory.getLogger(ListProxy.class);
    private ListProxyParameters<List<T>> params;

    private ListProxy(ListProxyParameters<List<T>> listProxyParameters) {
        this.params = listProxyParameters;

    }

    // TOIMPROVE: replace with a better exception handling and logging
    @SuppressWarnings("unchecked")
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        Object result;
        try {

            logger.debug("ListProxy.invoke: before method " + method.getName());

            if (!params.fetched) {

                List<T> persistables = (List<T>) params.db.findAllBy(params.refPersistable, params.listPopulationSql);

                params = new ListProxyParameters<List<T>>(params.db,
                                                          persistables,
                                                          params.listPopulationSql,
                                                          params.refPersistable);

                setFetchedToParameters();
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

    private void setFetchedToParameters() {
        this.params = new ListProxyParameters<>(params, true);
    }
}
