package com.github.illarion.swap4j;

import com.github.illarion.swap4j.store.StoreException;
import com.github.illarion.swap4j.store.scan.*;

import java.util.HashSet;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * TODO Describe class
 * <p/>
 * <p/>
 * Created at: Sep 29, 2011 2:38:10 PM
 *
 * @author Alexey Tigarev
 */
public class CustomAssertions {
    public static void assertGreaterOrEqual(int a, int b) {
        assertTrue(a >= b);
    }

    public static void assertGreaterOrEqual(String message, int a, int b) {
        assertTrue(message, a >= b);
    }

    /**
     * Verifies that store contains ONLY of specified FieldRecord instances
     * @param fieldStorage
     * @param expected
     * @throws com.github.illarion.swap4j.store.StoreException
     */
    public static void assertStorageContains(FieldStorage fieldStorage, FieldRecord... expected) throws StoreException {
        Set<Locator> locators = new HashSet<Locator>();
        for (FieldRecord fieldRecord : expected) {
            final Locator locator = fieldRecord.getLocator();
            FieldRecord restored = fieldStorage.read(locator);
            if (fieldRecord == null) {
                fail("Don't pass nulls to assertStorageContains!\n" + dumpStoreContents(fieldStorage));
            }
            if (restored == null) {
                fail("Not found: " + fieldRecord + "\n" + dumpStoreContents(fieldStorage));
            }
            assertEquals("Other object found in store than fieldRecord.\n" + dumpStoreContents(fieldStorage), fieldRecord, restored);
            locators.add(locator);
        }
        assertGreaterOrEqual("Make sure you don't have repeating IDs when calling assertStoreContains()",
                expected.length, locators.size());
        assertEquals("Extra expected found in storage", expected.length, locators.size());
    }

    private static String dumpStoreContents(FieldStorage fieldStorage) {
        StringBuilder builder = new StringBuilder("Present objects in storage: [\n");
        for (Locator locator: fieldStorage) {
            builder.append(locator).append(" => ").append(fieldStorage.read(locator)).append("\n");
        }
        return builder.append("]").toString();
    }

    public static FieldRecord obj(int id, String path, Object value, Class clazz, RECORD_TYPE recordType) {
        return new FieldRecord(id, path, value, clazz, recordType);
    }
}
