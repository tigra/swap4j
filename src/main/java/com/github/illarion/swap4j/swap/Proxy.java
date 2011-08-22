/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.illarion.swap4j.swap;

import com.github.illarion.swap4j.store.Store;
import com.github.illarion.swap4j.store.StoreException;
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

    private UUID id = UUID.randomUUID();
    private transient final Store store;
    private volatile T t;
    private transient final Class<T> clazz;

    public Proxy(UUID id, Store store, Class<T> clazz) {
        this.id = id;
        this.store = store;
        this.clazz = clazz;

        this.t = null;

    }

    public Proxy(Store store, T t, Class<T> clazz) throws StoreException {
        this.id = UUID.randomUUID();
        this.store = store;
        this.t = t;
        this.clazz = clazz;
        unload();
    }
    private transient Callback callback = new MethodInterceptorImpl();

    private void unload() throws StoreException {
        synchronized (id) {
            store.store(id, t);
            t = null;
        }
    }

    private void load() throws StoreException {
        synchronized (id) {
            if (null == t) {
                t = store.reStore(id, clazz);
            }
        }
    }

    public T get() throws StoreException {
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

    @Override
    protected void finalize() throws Throwable {
        unload();
        super.finalize();
    }

    public UUID getId() {
        return id;
    }

    private class MethodInterceptorImpl implements MethodInterceptor {

        public MethodInterceptorImpl() {
        }

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
                Object result = mp.invoke(t, os);
                unload();
                return result;
            }


        }
    }
}
