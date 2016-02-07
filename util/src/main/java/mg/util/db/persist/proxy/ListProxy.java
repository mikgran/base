package mg.util.db.persist.proxy;

import static mg.util.validation.Validator.validateNotNull;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mg.util.functional.consumer.ThrowingConsumer;

class ListProxy<T> implements InvocationHandler {

    @SuppressWarnings("unchecked")
    public static <T> List<T> newInstance(ListProxyParameters<List<T>> listProxyParameters,
        ThrowingConsumer<ListProxyParameters<List<T>>, Exception> processor) {

        validateNotNull("listProxyParameters", listProxyParameters);
        List<T> listToBeProxied = listProxyParameters.getListProxy();
        validateNotNull("listToBeProxied", listToBeProxied);

        return (List<T>) Proxy.newProxyInstance(listToBeProxied.getClass().getClassLoader(),
                                                listToBeProxied.getClass().getInterfaces(),
                                                new ListProxy<T>(listProxyParameters, processor));
    }

    private List<T> list;
    private Logger logger = LoggerFactory.getLogger(ListProxy.class);
    private ThrowingConsumer<ListProxyParameters<List<T>>, Exception> processor;
    private ListProxyParameters<List<T>> listProxyParameters;

    public ListProxy(ListProxyParameters<List<T>> listProxyParameters, ThrowingConsumer<ListProxyParameters<List<T>>, Exception> processor) {
        this.listProxyParameters = validateNotNull("listProxyParameters", listProxyParameters);
        this.processor = validateNotNull("processor", processor);
        this.list = validateNotNull("list", listProxyParameters.getListProxy());
    }

    // TOIMPROVE: replace with a better exception handling and logging
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        Object result;
        try {

            logger.debug("before method " + method.getName());

            processor.accept(listProxyParameters);

            result = method.invoke(list, args);

        } catch (InvocationTargetException e) {

            throw e.getTargetException();

        } catch (Exception e) {

            throw new RuntimeException("unexpected invocation exception: " + e.getMessage());

        } finally {

            logger.debug("after method " + method.getName());
        }
        return result;
    }
}
