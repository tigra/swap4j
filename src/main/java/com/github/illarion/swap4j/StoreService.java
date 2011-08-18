/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.illarion.swap4j;

/**
 *
 * @author shaman
 */
public interface StoreService<T, I> {
    
    public void store(T t, I id);
    
    public T reStore(I id);
    
}
