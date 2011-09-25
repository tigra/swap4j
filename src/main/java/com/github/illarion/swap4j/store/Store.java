/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.illarion.swap4j.store;

import com.github.illarion.swap4j.store.scan.Locator;
import com.github.illarion.swap4j.store.scan.SerializedField;

import java.util.Iterator;
import java.util.UUID;

/**
 * External storage for objects. Objects are swapped there.
 *
 * @author shaman
 */
public interface Store extends Iterable<Locator> {

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

    UUID createUUID();

    SerializedField deserialize(UUID id);

    @Override
    Iterator<Locator> iterator();

    SerializedField getSerializedField(Locator locator);
}
