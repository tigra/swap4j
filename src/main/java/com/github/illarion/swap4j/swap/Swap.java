/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.illarion.swap4j.swap;

import com.github.illarion.swap4j.store.StoreService;
import java.util.List;
import java.util.Set;

/**
 *
 * @author shaman
 */
public class Swap {

    private final StoreService store;

    public Swap(StoreService store) {
        this.store = store;
    }

    public <T> T wrap(T instance, Class<T> clazz) {
        return new Proxy<T>(store, instance, clazz).get();
    }
    
    public <T> List<T> newWrapList(Class<T> clazz){
        return new ProxyList<T>(this, clazz);
    }
    
    public <T> Set<T> newWrapSet(Class<T> clazz){
        return new ProxySet<T>(this, clazz);
    }
    
}
