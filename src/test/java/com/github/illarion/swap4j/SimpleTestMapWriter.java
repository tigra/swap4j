package com.github.illarion.swap4j;

import com.github.illarion.swap4j.store.ObjectStorage;
import com.github.illarion.swap4j.store.scan.MapWriter;
import com.github.illarion.swap4j.store.scan.TestObjectScannerObjectStorage;
import com.github.illarion.swap4j.swap.RandomUuidGenerator;

import java.sql.SQLException;

/**
 * TODO Describe class
 *
 * @author Alexey Tigarev tigra@agile-algorithms.com
 */
public class SimpleTestMapWriter extends AbstractSimpleTest {

    public SimpleTestMapWriter(String testMethodName) {
        super(testMethodName);
    }

    /**
     * Define this method in subclasses to run this set of tests on different <code>ObjectStorage</code>'s
     *
     * @return storage to run the tests on
     * @throws ClassNotFoundException
     * @throws java.sql.SQLException
     */
    @Override
    protected ObjectStorage createObjectStore() throws ClassNotFoundException, SQLException {
        fieldStorage = new MapWriter();
        return new TestObjectScannerObjectStorage(swap, fieldStorage, new SequentalUUIDGenerator());
    }
}
