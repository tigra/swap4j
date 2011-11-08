/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.illarion.swap4j.store;

import com.github.illarion.swap4j.store.scan.Locator;

/**
 *
 * @author shaman
 */
public class StorageException extends Exception {
    protected Locator locator;
    // TODO Make runtime exception (it is too frequent) (?)

    public StorageException(String string) {
        super(string);
    }

    public StorageException(Throwable thrwbl) {
        super(thrwbl);
    }

    public StorageException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }

    public StorageException(String string, Locator locator) {
        this(String.format("%s at %s", string, locator));
        this.locator = locator;
    }
}
