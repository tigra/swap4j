/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.illarion.swap4j.swap;

import java.lang.reflect.Method;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 *
 * @author shaman
 */
public class SwapCallback<T> implements MethodInterceptor {
    Proxy<T> proxy;

    public SwapCallback(Proxy<T> proxy) {
        this.proxy = proxy;
    }

    public Proxy<T> getProxy() {
        return proxy;
    }
    
    

    @Override
    public Object intercept(Object target, Method method, Object[] params, MethodProxy mp) throws Throwable {
        if (method.getName().equals("finalize")) {
            proxy.unload();
            return mp.invokeSuper(target, params);
        }
        if (method.getName().equals("getRealObject")) {
            proxy.load();
            return proxy.realObject;
        }
        synchronized (proxy.id) {
            proxy.load();
            if (null == proxy.realObject) {
                throw new NullPointerException("realObject not loaded!");
                // TODO Better throw our runtime exception (?)
            }
            Object result = mp.invoke(proxy.realObject, params);
            proxy.unload();
            return result;
        }
    }
    
}
