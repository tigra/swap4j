/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.illarion.swap4j.swap;

import com.github.illarion.swap4j.store.ObjectStorage;
import com.github.illarion.swap4j.store.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.*;

/**
 * @author shaman
 */
public class Swap {
    private static Swap instance = null;

    private final ObjectStorage objectStore;
    public static final boolean DONT_UNLOAD = false;

    private final static Logger log = LoggerFactory.getLogger("Swap");

    private final static Set<WeakReference<Swappable>> registry
            = new HashSet<WeakReference<Swappable>>();

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
     * @throws com.github.illarion.swap4j.store.StorageException
     */
    public <T> T wrap(T instance, Class<T> clazz) throws StorageException {
        if (null == instance) {
            log.error("Swap.wrap - can't wrap null");
            return null;
        } else if (isWrapped(instance)) {
            log.debug("Swap.wrap - already wrapped: " + ((SwapPowered) instance).getRealObject());
            return instance;
        } else {
            log.debug("Swap.wrap - wrapping: " + instance);
            Proxy<T> proxy = new Proxy<T>(objectStore, instance, clazz, true);
            T wrapped = proxy.get();
            return wrapped;
        }
    }

    private <T> boolean isWrapped(T instance) {
        return null == instance || instance instanceof SwapPowered;
    }

    public <T> List<T> newWrapList(Class<T> clazz) throws StorageException {
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

    public static <T> T doWrap(T object, Class<T> clazz) throws StorageException {
        return getInstance().wrap(object, clazz);
    }

    @Deprecated
    public static Swap newInstance(ObjectStorage objectStore) {
        instance = new Swap(objectStore);
        return instance;
    }

    public static <T> T newEmptyWrapper(UUID uuid, Class<T> clazz) throws StorageException {
        return new Proxy<T>(uuid, getInstance().getStore(), clazz).get();
    }

    public static void register(Swappable swappable) {
        registry.add(new WeakReference<Swappable>(swappable));
    }

    public static void finishHim() {
        instance = null;
        System.gc();
        sleep(100);
        int aliveObjectCount;
        log.debug("Waiting for object finalization...");
        do {
            aliveObjectCount = 0;
            Set<WeakReference<Swappable>> toRemove = new HashSet<WeakReference<Swappable>>();
            Iterator<WeakReference<Swappable>> iterator = registry.iterator();
            while (iterator.hasNext()) {
                WeakReference<Swappable> ref = iterator.next();
                Swappable swappable = ref.get();
                if (swappable == null) {
                    iterator.remove();
                } else {
                    log.debug("Still alive: " + swappable);
                    swappable.nullify();
                    aliveObjectCount++;
                }
            }
            log.debug("Objects alive: " + aliveObjectCount);
            System.gc();
            sleep(100);
        } while (aliveObjectCount > 0);
    }

    private static void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            log.error("", e);
        }
    }

    public static <T> List<T> proxyList(Class<T> clazz) throws StorageException {
        return getInstance().newWrapList(clazz);
    }

    public static ObjectStorage getStorage() {
        return getInstance().getStore();
    }
}
