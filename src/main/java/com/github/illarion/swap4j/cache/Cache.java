/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.illarion.swap4j.cache;

/**
 *
 * @author shaman
 */
interface Cache<R, T> {
    
    public T getObject(final R id) throws Exception;
    
}
