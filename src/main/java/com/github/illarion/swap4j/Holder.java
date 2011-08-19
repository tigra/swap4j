/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.illarion.swap4j;

import com.github.illarion.swap4j.store.StoreService;
import java.util.UUID;

/**
 *
 * @author shaman
 */
public abstract class Holder<T> {

    private final StoreService store;
    private final UUID id;
    private T t;

    public Holder(StoreService store, UUID id) {
        this.store = store;
        this.id = id;
    }

    /**
     * May return null, if holded object is not loaded
     * @return 
     */
    public T get() {
        return t;
    }

    public void load() {
        t = store.reStore(id);
    }

    public void unload() {
        store.store(t, id);
        t = null;
    }
}
