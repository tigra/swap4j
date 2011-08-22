/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.illarion.swap4j.store;

/**
 *
 * @author shaman
 */
public class StoreException extends Exception {

    public StoreException(String string) {
        super(string);
    }

    public StoreException(Throwable thrwbl) {
        super(thrwbl);
    }

    public StoreException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }
}
