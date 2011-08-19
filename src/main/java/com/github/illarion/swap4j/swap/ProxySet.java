/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.illarion.swap4j.swap;

import java.util.Collection;
import java.util.HashSet;

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
        T wrapped = swap.wrap(e, clazz);
        return super.add(wrapped);
    }

    @Override
    public boolean addAll(Collection<? extends T> clctn) {
        for (T t : clctn) {
            add(t);
        }
        return true;
    }
}
