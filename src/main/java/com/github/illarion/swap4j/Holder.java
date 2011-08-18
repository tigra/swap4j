/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.illarion.swap4j;

import java.util.UUID;

/**
 *
 * @author shaman
 */
public abstract class Holder<T> {
    
    private final UUID id;
    private T t;

    public Holder(UUID id) {
        this.id = id;
    }
    
    public T get(){
        return t;
    }
    
    public void load(){
        
    }
    
    public void unload(){
        
    }
    
    
    
    

}
