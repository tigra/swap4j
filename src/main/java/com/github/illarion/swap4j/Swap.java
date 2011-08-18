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
public final class Swap {

    private static final Set<UUID> ids = new HashSet<UUID>();

    public static UUID newUniqueId() {
        UUID randomUUID;

        synchronized (ids) {
            do {
                randomUUID = UUID.randomUUID();
            } while (!ids.add(randomUUID));
        }
        
        return randomUUID;
    }
    
    
}
