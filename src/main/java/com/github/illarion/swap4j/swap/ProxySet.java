/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.illarion.swap4j.swap;

import com.github.illarion.swap4j.store.StoreException;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author shaman
 */
class ProxySet<T> extends HashSet<T> {

    private final Swap swap;
    private final Class<T> clazz;

    public ProxySet(Swap swap, Class<T> clazz) {
        this.swap = swap;
        this.clazz = clazz;
    }

    @Override
    public boolean add(T e) {
        T wrapped;
        try {
            wrapped = swap.wrap(e, clazz).get();
            return super.add(wrapped);
        } catch (StoreException ex) {
            Logger.getLogger(ProxySet.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends T> clctn) {
        for (T t : clctn) {
            add(t);
        }
        return true;
    }
}
