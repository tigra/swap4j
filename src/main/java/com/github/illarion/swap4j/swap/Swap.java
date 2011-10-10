/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.illarion.swap4j.swap;

import com.github.illarion.swap4j.store.ObjectStorage;
import com.github.illarion.swap4j.store.StoreException;
import java.util.List;
import java.util.Set;

/**
 *
 * @author shaman
 */
public class Swap {

    private final ObjectStorage objectStore;
    public static final boolean DONT_UNLOAD = false;

    public Swap(ObjectStorage objectStore) {
        this.objectStore = objectStore;
    }

    public <T> T wrap(T instance, Class<T> clazz) throws StoreException {
        return new Proxy<T>(objectStore, instance, clazz).get();
        // TODO Should get() be in Proxy??
    }
    
    public <T> List<T> newWrapList(Class<T> clazz) throws StoreException {
        return new ProxyList<T>(this, clazz);
    }
    
    public <T> Set<T> newWrapSet(Class<T> clazz){
        return new ProxySet<T>(this, clazz);
    }

    public ObjectStorage getStore() {
        return objectStore;
    }
    
}
