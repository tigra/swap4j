/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.illarion.swap4j.swap;

import com.github.illarion.swap4j.store.StoreService;

/**
 *
 * @author shaman
 */
public class Swap {

    private final StoreService store;

    public Swap(StoreService store) {
        this.store = store;
    }

    public <T> Proxy<T> wrap(T instance, Class<T> clazz) {
        return new Proxy<T>(store, instance, clazz);
    }
}
