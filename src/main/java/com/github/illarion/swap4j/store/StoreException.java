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
public class StoreException extends Exception { // TODO Make runtime exception (it is too frequent)

    public StoreException(String string) {
        super(string);
    }

    public StoreException(Throwable thrwbl) {
        super(thrwbl);
    }

    public StoreException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }

    public StoreException(String string, Locator locator) {
        this(string + locator);
    }
}
