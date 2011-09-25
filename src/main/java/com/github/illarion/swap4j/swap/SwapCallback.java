/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.illarion.swap4j.swap;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
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
        final String methodName = method.getName();
        if ("finalize".equals(methodName)) {
            proxy.unload();
            return mp.invokeSuper(target, params);
        }
        if ("getRealObject".equals(methodName)) {
            proxy.load();
            return proxy.realObject;
        }
        if ("toString".equals(methodName)) {
            return "E{" + proxy.toString() + "}";
        }
        synchronized (proxy.id) {
            try {
                proxy.enterContext();
                proxy.load();
                if (null == proxy.realObject) {
                    throw new NullPointerException("realObject not loaded!");
                    // TODO Better throw our runtime exception (?)
//                return null;
                }
                Object result = mp.invoke(proxy.realObject, params);
                if (proxy.canUnload()) {
                    proxy.unload(); // should be reenterable. Nested calls don't have to do .unload() (?)
                }
                return result;
            } finally {
                proxy.exitContext();
            }
        }
    }

}
