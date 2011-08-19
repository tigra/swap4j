/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.illarion.swap4j.swap;

import com.github.illarion.swap4j.store.StoreService;
import java.lang.reflect.Method;
import java.util.UUID;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 *
 * @author shaman
 */
public class Proxy<T> {

    private final UUID id = UUID.randomUUID();
    private final StoreService store;
    private volatile T t;
    private final Class<T> clazz;

    public Proxy(StoreService store, T t, Class<T> clazz) {
        this.store = store;
        this.t = t;
        this.clazz = clazz;
    }
    private Callback callback = new MethodInterceptor() {

        @Override
        public Object intercept(Object o, Method method, Object[] os, MethodProxy mp) throws Throwable {

            if (method.getName().equals("finalize")) {
                unload();
                return mp.invokeSuper(o, os);
            }

            synchronized (id) {
                load();
                if (null == t) {
                    throw new NullPointerException("t not loaded!");
                }
                Object result =  mp.invoke(t, os);
                unload();
                return result;
            }


        }
    };

    private void unload() {
        synchronized (id) {
            store.store(t, id);
            t = null;
        }
    }

    private void load() {
        synchronized (id) {
            if (null == t) {
                t = store.reStore(id);
            }
        }
    }

    public T get() {
        synchronized (id) {
            if (null == t) {
                load();
            }

            Enhancer enhancer = new Enhancer();
            enhancer.setCallback(callback);
            enhancer.setSuperclass(clazz);

            return (T) enhancer.create();
        }

    }
}
