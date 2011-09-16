/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.illarion.swap4j.swap;

import com.github.illarion.swap4j.store.StoreException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author shaman
 */
public class ProxyList<T> extends ArrayList<T> {

    private final Swap swap;
    private final Class<T> clazz;

    public ProxyList(Swap swap, Class<T> clazz) {
        this.swap = swap;
        this.clazz = clazz;
    }

    @Override
    public boolean add(T e) {
        T wrapped = null;
        try {
            wrapped = swap.wrap(e, clazz);
            return super.add(wrapped);
        } catch (StoreException ex) {
            Logger.getLogger(ProxyList.class.getName()).log(Level.SEVERE, null, ex);
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
