package mg.util.db.persist.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ListProxy<T> implements InvocationHandler {

    private Logger logger = LoggerFactory.getLogger(ListProxy.class);
    private List<T> list;

    public ListProxy(List<T> list) {
        Objects.requireNonNull(list);
        this.list = list;
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> newInstance(List<T> listToBeProxied) {
        Objects.requireNonNull(listToBeProxied);
        return (List<T>) Proxy.newProxyInstance(listToBeProxied.getClass().getClassLoader(),
                                                listToBeProxied.getClass().getInterfaces(),
                                                new ListProxy<T>(listToBeProxied));
    }

    // TOIMPROVE: replace with a better exception handling and logging
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        Object result;
        try {

            logger.debug("before method " + method.getName());

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
