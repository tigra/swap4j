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
public interface Store {
    
    public<T> void store(UUID id, T t) throws StoreException;
    
    public<T> T reStore(UUID id, Class<T> clazz) throws StoreException;
    
}
