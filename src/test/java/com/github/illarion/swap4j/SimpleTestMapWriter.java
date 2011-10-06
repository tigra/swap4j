package com.github.illarion.swap4j;

import com.github.illarion.swap4j.store.ObjectStorage;
import com.github.illarion.swap4j.store.scan.MapWriter;
import com.github.illarion.swap4j.store.scan.TestObjectScannerObjectStorage;
import com.github.illarion.swap4j.swap.UUIDGenerator;

import java.sql.SQLException;

/**
 * TODO Describe class
 *
 * @author Alexey Tigarev tigra@agile-algorithms.com
 */
public class SimpleTestMapWriter extends AbstractSimpleTest {

    /**
     * Define this method in subclasses to run this set of tests on different <code>ObjectStorage</code>'s
     *
     * @return storage to run the tests on
     * @throws ClassNotFoundException
     * @throws java.sql.SQLException
     */
    @Override
    protected ObjectStorage createObjectStore() throws ClassNotFoundException, SQLException {
        return new TestObjectScannerObjectStorage(swap, new MapWriter(), new UUIDGenerator());
    }
}
