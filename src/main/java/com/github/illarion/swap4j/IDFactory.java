/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.illarion.swap4j;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author shaman
 */
public final class IDFactory {

    private static final Set<UUID> ids = new HashSet<UUID>();

    private IDFactory() {
    }
    
    
    
    public static UUID newUniqueId() {
        UUID newId;

        synchronized (ids) {
            do {
                newId = UUID.randomUUID();
            } while (!ids.add(newId));
        }
        
        return newId;
    }
    
    
}
