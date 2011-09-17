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
 *
 * @author shaman
 */
public class Proxy<T> {

    UUID id = UUIDGenerator.createUUID(); // TODO Make sure they don't repeat
    transient final Store store;
    volatile T realObject;
    transient final Class<T> clazz;
    
    transient final Callback callback = new SwapCallback(this);
    
    static UUIDGenerator UUIDGenerator = new UUIDGenerator();

    public Proxy(UUID id, Store store, Class<T> clazz) {
        this.id = id;
        this.store = store;
        this.clazz = clazz;

        this.realObject = null;
    }

    public Proxy(Store store, T realObject, Class<T> clazz) throws StoreException {
        this.id = store.createUUID();
        this.store = store;
        this.realObject = realObject;
        this.clazz = clazz;
        unload();
    }


    void unload() throws StoreException {
        synchronized (id) {
            store.store(id, realObject);
            realObject = null;
        }
    }

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

            return (T) enhancer.create();
            // TODO Try to use just one callback for all objects, store Proxy reference in wrapped object itself? 
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
}
