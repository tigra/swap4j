/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.illarion.swap4j;

import com.github.illarion.swap4j.store.StoreService;
import java.util.HashMap;
import java.util.UUID;

/**
 *
 * @author shaman
 */
public class MemoryStoreService<T> implements StoreService<T> {
    
    private final HashMap<UUID, T> map = new HashMap<UUID, T>();

    @Override
    public void store(T t, UUID id) {
      map.put(id, t);
    }

    @Override
    public T reStore(UUID id) {
        return map.get(id);
    }
    
}
