/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.illarion.swap4j.store;

import com.github.illarion.swap4j.store.scan.FieldRecord;
import com.github.illarion.swap4j.store.scan.Locator;

import java.util.Iterator;
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
     * @throws StoreException
     */
    public<T> void store(UUID id, T object) throws StoreException;

    /**
     * Load specified object of type T with given id.
     * @param id
     * @param clazz
     * @param <T>
     * @return loaded object
     * @throws StoreException
     */
    public<T> T reStore(UUID id, Class<T> clazz) throws StoreException;

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
    FieldRecord getSerializedField(Locator locator);
}
