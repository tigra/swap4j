/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.illarion.swap4j;

import com.github.illarion.swap4j.store.ObjectStorage;

import java.sql.SQLException;

import com.github.illarion.swap4j.store.scan.H2FieldStorage;
import com.github.illarion.swap4j.store.scan.TestObjectScannerObjectStorage;
import com.github.illarion.swap4j.swap.*;

/**
 *
 * @author shaman
 */
public class SimpleTestH2FieldStorage extends AbstractSimpleTest {

    @Override
    protected ObjectStorage createObjectStore() throws ClassNotFoundException, SQLException {
        return new TestObjectScannerObjectStorage(swap, new H2FieldStorage(swap), new UUIDGenerator());
    }

}
