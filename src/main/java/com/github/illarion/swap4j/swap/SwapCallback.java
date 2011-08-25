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

    public SwapCallback(Proxy<T> callback) {
        this.proxy = callback;
    }

    public Proxy<T> getProxy() {
        return proxy;
    }
    
    

    @Override
    public Object intercept(Object o, Method method, Object[] os, MethodProxy mp) throws Throwable {
        if (method.getName().equals("finalize")) {
            proxy.unload();
            return mp.invokeSuper(o, os);
        }
        if (method.getName().equals("getRealObject")) {
            proxy.load();
            return proxy.t;
        }
        synchronized (proxy.id) {
            proxy.load();
            if (null == proxy.t) {
                throw new NullPointerException("t not loaded!");
            }
            Object result = mp.invoke(proxy.t, os);
            proxy.unload();
            return result;
        }
    }
    
}
