/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.illarion.swap4j;

import com.github.illarion.swap4j.store.ObjectStorage;

import java.sql.SQLException;

import com.github.illarion.swap4j.store.scan.H2FieldStorage;
import com.github.illarion.swap4j.store.scan.H2ObjectStorage;
import org.junit.Before;

/**
 *
 * @author shaman
 */
public class SimpleTestH2FieldStorage extends AbstractSimpleTest {
    public SimpleTestH2FieldStorage(String testMethodName) {
        super(testMethodName);
    }

    @Override
    public void setUp() throws ClassNotFoundException, SQLException {
        super.setUp();
        cleanAll();
    }

    @Before
    public void cleanAll() throws SQLException {
        ((H2FieldStorage)fieldStorage).cleanAll();
        assertEquals("Can't start tests, database is full of mud",
                0, fieldStorage.getRecordCount());
    }

    @Override
    protected ObjectStorage createObjectStore() throws ClassNotFoundException, SQLException {
//        return new TestObjectScannerObjectStorage(swap, new H2FieldStorage(swap), new RandomUuidGenerator());
        fieldStorage = new H2FieldStorage(swap);
        return new H2ObjectStorage(swap, fieldStorage);
    }
    

}
