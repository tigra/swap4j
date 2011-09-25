/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.illarion.swap4j.swap;

import com.github.illarion.swap4j.store.Store;
import com.github.illarion.swap4j.store.StoreException;

import java.util.UUID;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;

/**
 * @author shaman
 */
public class Proxy<T> implements Locatable<T> {

    UUID id; // TODO Make sure they don't repeat
    transient final Store store;
    volatile T realObject;
    transient final Class<T> clazz;

    transient final Callback callback = new SwapCallback(this);
    private transient int depth = 0;

    public Proxy(UUID id, Store store, Class<T> clazz) throws StoreException {
        checkIfClassIsAllowed(clazz);
        this.id = id;
        this.store = store;
        this.clazz = clazz;
        this.realObject = null;
    }

    public Proxy(Store store, T realObject, Class<T> clazz) throws StoreException {
        checkIfClassIsAllowed(clazz);
        checkObjectIsAllowed(realObject);
        this.id = store.createUUID();
        this.store = store;
        this.realObject = realObject;
        this.clazz = clazz;
        unload();
    }

    private void checkIfClassIsAllowed(Class<T> clazz) throws StoreException {
        if (Proxy.class.isAssignableFrom(clazz)) {
            throw new StoreException("Proxy inside Proxy is not allowed");
        }
    }

    private void checkObjectIsAllowed(T object) throws StoreException {
        if (null != object && Proxy.class.isAssignableFrom(object.getClass())) {
            throw new StoreException("Proxy inside Proxy is not allowed");
        }
    }


    @Override
    public void unload() throws StoreException {
        synchronized (id) {
//            store.store(id, realObject);
            store.store(id, this);
            realObject = null;
        }
    }

    @Override
    public void load() throws StoreException {
        synchronized (id) {
            if (null == realObject) {
                realObject = store.reStore(id, clazz);
            }
        }
    }

    public T get() throws StoreException {
        synchronized (id) {
            if (null == realObject) {
                load();
            }

            Enhancer enhancer = new Enhancer();
            enhancer.setCallback(callback);
            enhancer.setSuperclass(clazz);
            enhancer.setInterfaces(new Class[]{SwapPowered.class});

            // TODO Try to use just one callback for all objects, store Proxy reference in wrapped object itself?
            try {
                return (T) enhancer.create();
            } catch (ClassCastException cce) {
                throw new StoreException("Proxy.get(), id=" + id + ",clazz=" + clazz, cce);
            }
        }

    }

    @Override
    protected void finalize() throws Throwable {
        unload();
        super.finalize();
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isLoaded() {
        return null == realObject;
    }

    public T getRealObject() {
        return realObject;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Proxy proxy = (Proxy) o;

        if (clazz != null ? !clazz.equals(proxy.clazz) : proxy.clazz != null) return false;
        if (id != null ? !id.equals(proxy.id) : proxy.id != null) return false;
        if (store != null ? !store.equals(proxy.store) : proxy.store != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (store != null ? store.hashCode() : 0);
        result = 31 * result + (clazz != null ? clazz.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Proxy");
        sb.append("{callback=").append(callback);
        sb.append(", id=").append(id);
        sb.append(", store=").append(store);
        sb.append(", realObject=").append(realObject);
        sb.append(", clazz=").append(clazz);
        sb.append('}');
        return sb.toString();
    }

    public Class getClazz() {
        return clazz;
    }

    public void enterContext() {
        depth++;
    }

    public void exitContext() {
        depth--;
    }


    public boolean canUnload() {
        return depth <= 1;
    }
}
