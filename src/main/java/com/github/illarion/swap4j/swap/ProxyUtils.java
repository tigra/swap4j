package com.github.illarion.swap4j.swap;

import com.github.illarion.swap4j.store.StoreException;
import net.sf.cglib.proxy.Callback;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * TODO Describe class
 * <p/>
 * <p/>
 * Created at: Sep 16, 2011 1:45:07 AM
 *
 * @author Alexey Tigarev
 */
public class ProxyUtils {
    private static final Logger logger = Logger.getLogger(ProxyUtils.class.getName());

    /**
     * Try to get proxy from enhanced proxy.
     *
     * @param enhanced
     * @param <T>
     * @return
     * @throws com.github.illarion.swap4j.store.StoreException
     */
    public static <T> Proxy<T> getProxy(Object enhanced) throws StoreException {
        try {
            Callback callback = ((Callback[]) call(enhanced, "getCallbacks"))[0];
            return (Proxy) call(callback, "getProxy");

            // TODO Different reactions to different exceptions. In some cases we actually have to fail.
        } catch (NoSuchMethodException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (ArrayIndexOutOfBoundsException aioobe) {
            logger.log(Level.SEVERE, null, aioobe);
            throw aioobe; // this is unexpected situation, so we rethrow the exception...
        }

        return null; // callback or proxy not found
    }

    private static Object call(Object object, String methodName)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method method = object.getClass().getMethod(methodName, new Class[]{});
        return method.invoke(object );
    }
}
