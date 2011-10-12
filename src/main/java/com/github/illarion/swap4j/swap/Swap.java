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
 * @author shaman
 */
public class Swap {
    private static Swap instance = null;

    private final ObjectStorage objectStore;
    public static final boolean DONT_UNLOAD = false;

    private Swap(ObjectStorage objectStore) {
        this.objectStore = objectStore;
    }

    /**
     * In case class is inner, it have to be static.
     *
     * @param instance
     * @param clazz
     * @param <T>
     * @return
     * @throws StoreException
     */
    public <T> T wrap(T instance, Class<T> clazz) throws StoreException {
        if (isWrapped(instance)) {
            return instance;
        } else {
            return new Proxy<T>(objectStore, instance, clazz).get();
        }
    }

    private <T> boolean isWrapped(T instance) {
        return null == instance || instance instanceof SwapPowered;
    }

    public <T> List<T> newWrapList(Class<T> clazz) throws StoreException {
        return new ProxyList<T>(this, clazz);
    }

    public <T> Set<T> newWrapSet(Class<T> clazz) {
        return new ProxySet<T>(this, clazz);
    }

    public ObjectStorage getStore() {
        return objectStore;
    }

    @Deprecated
    public static Swap getInstance() {
        return instance;
    }


    @Deprecated
    public static void setInstance(Swap swap) {
//        instance = swap;
    }

    public static <T> T doWrap(T object, Class<T> clazz) throws StoreException {
        return getInstance().wrap(object, clazz);
    }

    @Deprecated
    public static Swap newInstance(ObjectStorage objectStore) {
        instance = new Swap(objectStore);
        return instance;
    }

    public static void shutdown() {
        instance = null;
        System.gc();
    }
}
