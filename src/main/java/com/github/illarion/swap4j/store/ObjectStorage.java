/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.illarion.swap4j.store;

import com.github.illarion.swap4j.store.scan.FieldRecord;
import com.github.illarion.swap4j.store.scan.Locator;
import com.github.illarion.swap4j.swap.ProxyList;
import com.github.illarion.swap4j.swap.Swap;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * External storage for objects. Objects are swapped there.
 *
 * @author shaman
 */
public interface ObjectStorage extends Iterable<Locator> {

    /**
     * Store specified object of type T with given id.
     *
     * @param id
     * @param object
     * @param <T>
     * @throws StorageException
     */
    public<T> void store(UUID id, T object) throws StorageException;

    /**
     * Load specified object of type T with given id.
     * @param id
     * @param clazz
     * @param <T>
     * @return loaded object
     * @throws StorageException
     */
    public<T> T reStore(UUID id, Class<T> clazz) throws StorageException;

    /**
     * Creates unique UUID
     * @return
     */
    UUID createUUID();

    /**
     * Get iterator over <code>FieldRecord</code>s stored in this <code>Store</code>
     * @return iterator
     */
    @Override
    Iterator<Locator> iterator();

    /**
     * Access <code>FieldRecord</code> identified by given <code>Locator</code>.
     * Used for testing purposes.
     *
     * @param locator Locator identifying the field to load
     * @return loaded <code>FieldRecord</code>
     */
    FieldRecord getSerializedField(Locator locator) throws StorageException;

    @Deprecated // TODO Decouple Swap from ObjectStorage
    void setSwap(Swap swap);

    public <T> void storeProxyList(UUID uuid, ProxyList proxyList, Class elementClass) throws StorageException;

    public <T> List<T> reStoreList(UUID uuid, Class<T> elementClass, List<T> restored) throws StorageException;
}
