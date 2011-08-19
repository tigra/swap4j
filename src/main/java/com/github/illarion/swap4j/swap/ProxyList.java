/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.illarion.swap4j.swap;

import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author shaman
 */
class ProxyList<T> extends ArrayList<T> {

    private final Swap swap;
    private final Class<T> clazz;

    public ProxyList(Swap swap, Class<T> clazz) {
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
