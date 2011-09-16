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

    UUID id = UUID.randomUUID(); // TODO Make sure they don't repeat
    transient final Store store;
    volatile T realObject;
    transient final Class<T> clazz;
    
    transient final Callback callback = new SwapCallback(this);

    public Proxy(UUID id, Store store, Class<T> clazz) {
        this.id = id;
        this.store = store;
        this.clazz = clazz;

        this.realObject = null;
    }

    public Proxy(Store store, T realObject, Class<T> clazz) throws StoreException {
        this.id = UUID.randomUUID();
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

    void load() throws StoreException {
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
}
