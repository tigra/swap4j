/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.illarion.swap4j.store;

import java.util.UUID;

/**
 *
 * @author shaman
 */
public interface StoreService {
    
    public<T> void store(T t, UUID id);
    
    public<T> T reStore(UUID id);
    
}
