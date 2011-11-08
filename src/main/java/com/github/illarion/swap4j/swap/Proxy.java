/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.illarion.swap4j.swap;

import com.github.illarion.swap4j.store.ObjectStorage;
import com.github.illarion.swap4j.store.StorageException;

import java.util.UUID;

import com.github.illarion.swap4j.store.scan.ID;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author shaman
 */
public class Proxy<T> extends Swappable<T> implements Locatable<T> {

    private static final Logger log = LoggerFactory.getLogger("Proxy");

//    UUID id; // TODO Make sure they don't repeat
    transient final ObjectStorage objectStore;
    volatile T realObject;
    transient final Class<T> clazz;

    transient final Callback callback = new SwapCallback(this);
    private transient int depth = 0;

    public Proxy(UUID id, ObjectStorage objectStore, Class<T> clazz) throws StorageException {
        checkIfClassIsAllowed(clazz);
        this.id = id;
        this.objectStore = objectStore;
        this.clazz = clazz;
        this.realObject = null;
        Swap.register(this);
    }

    public Proxy(ObjectStorage objectStore, T realObject, Class<T> clazz) throws StorageException {
        this(objectStore, realObject, clazz, true);
    }

    public Proxy(ObjectStorage objectStore, T realObject, Class<T> clazz, boolean haveToUnload) throws StorageException {
        checkIfClassIsAllowed(clazz);
        checkObjectIsAllowed(realObject);
        this.id = objectStore.createUUID();
        this.objectStore = objectStore;
        this.realObject = realObject;
        this.clazz = clazz;
        Swap.register(this);
        if (haveToUnload) {
            unload();
        }
    }

    private void checkIfClassIsAllowed(Class<T> clazz) throws StorageException {
        if (Proxy.class.isAssignableFrom(clazz)) {
            throw new StorageException("Proxy inside Proxy is not allowed");
        }
    }

    private void checkObjectIsAllowed(T object) throws StorageException {
        if (null != object && Proxy.class.isAssignableFrom(object.getClass())) {
            throw new StorageException("Proxy inside Proxy is not allowed");
        }
    }


    @Override
    public void unload() throws StorageException {
        synchronized (id) {
            enter("unload");
            log.debug("unload(), " + this);
            store();
            realObject = null;
            exit();
        }
    }

    public void store() throws StorageException {
        enter("store");
        try {
            objectStore.store(id, this);
        } finally {
            exit();
        }
    }

    @Override
    public void load() throws StorageException {
        synchronized (id) {
            enter("load");
            if (null == realObject) {
                log.debug("load(), " + this);
                realObject = objectStore.reStore(id, clazz);
            } else {
                log.debug("already loaded - " + ID.shortRepresentation(id));
            }
            exit();
        }
    }

    public T get() throws StorageException {
        synchronized (id) {
            enter("get");
            if (null == realObject) {
                load();
            }

            Enhancer enhancer = new Enhancer();
            enhancer.setCallback(callback);
            enhancer.setSuperclass(clazz);
            enhancer.setInterfaces(new Class[]{SwapPowered.class});

            // TODO Try to use just one callback for all objects, store Proxy reference in wrapped object itself?
            try {
                T enhanced = (T) enhancer.create();
                return enhanced;
            } catch (ClassCastException cce) {
                throw new StorageException("Proxy.get(), id=" + id + ",clazz=" + clazz, cce);
            } finally {
                exit();
            }
        }

    }

    @Override
    protected void finalize() throws Throwable {
        enter("finalize");
        try {
            unload();
            super.finalize();
        } finally {
            exit();
        }
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isLoaded() {
        return null != realObject;
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
        if (objectStore != null ? !objectStore.equals(proxy.objectStore) : proxy.objectStore != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (objectStore != null ? objectStore.hashCode() : 0);
        result = 31 * result + (clazz != null ? clazz.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Proxy{");
//        sb.append("{callback=").append(callback);
        sb.append("id=").append(ID.shortRepresentation(id));
//        sb.append(", store=").append(objectStore);
        sb.append(", c=").append(shortClassName());
//        sb.append(", r=").append(realObject);
        sb.append('}');
        return sb.toString();
    }

    private String shortClassName() {
        return null == clazz ? "null" : clazz.getSimpleName();
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

    @Override
    public void nullify() {
        realObject = null;
        id = null;
    }
}
