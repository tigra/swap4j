package com.github.illarion.swap4j.store.scan;

import com.github.illarion.swap4j.store.StoreException;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

import static com.github.illarion.swap4j.CustomAssertions.assertStorageContains;
import static com.github.illarion.swap4j.CustomAssertions.obj;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * TODO Describe class
 * <p/>
 * <p/>
 * Created at: Sep 29, 2011 2:24:46 PM
 *
 * @author Alexey Tigarev
 */
public class MapWriterTest {
    FieldStorage fieldStorage;

    @Before
    public void setUp() {
        fieldStorage = new MapWriter();
    }

    @Test
    public void testClean() throws StoreException {
        // setup
        fieldStorage.serialize(new FieldRecord(1, ".", "object1", String.class, RECORD_TYPE.PROXIED_VALUE));
        fieldStorage.serialize(new FieldRecord(1, "./field", "string1", String.class, RECORD_TYPE.PROXIED_FIELD));

        fieldStorage.serialize(new FieldRecord(2, ".", "object2", String.class, RECORD_TYPE.PROXIED_VALUE));
        fieldStorage.serialize(new FieldRecord(2, "./field", "string2", String.class, RECORD_TYPE.PROXIED_FIELD));

        // excersize
        assertTrue(fieldStorage.clean(new UUID(0, 1)));

        // verify
        assertStorageContains(fieldStorage,
                obj(2, ".", "object2", String.class, RECORD_TYPE.PROXIED_VALUE),
                obj(2, "./field", "string2", String.class, RECORD_TYPE.PROXIED_FIELD));
    }

    @Test
    public void testCleanNothing() throws StoreException {
        // setup
        fieldStorage.serialize(new FieldRecord(2, ".", "singleObject", String.class, RECORD_TYPE.PROXIED_VALUE));
        fieldStorage.serialize(new FieldRecord(2, "./field", "stringInSingleObject", String.class, RECORD_TYPE.PROXIED_FIELD));

        // excersize
        assertFalse(fieldStorage.clean(new UUID(0, 1))); // nothing removed

        // verify
        assertStorageContains(fieldStorage,
                obj(2, ".", "singleObject", String.class, RECORD_TYPE.PROXIED_VALUE),
                obj(2, "./field", "stringInSingleObject", String.class, RECORD_TYPE.PROXIED_FIELD));
    }

    @Test
    public void testReadAll() {
        // setup
        // field order is mangled intentionally
        fieldStorage.serialize(new FieldRecord(1, "./field/subfield", "string11", String.class, RECORD_TYPE.PROXIED_FIELD));
        fieldStorage.serialize(new FieldRecord(1, "./field", "string1", String.class, RECORD_TYPE.PROXIED_FIELD));
        fieldStorage.serialize(new FieldRecord(1, ".", "object1", String.class, RECORD_TYPE.PROXIED_VALUE));

        fieldStorage.serialize(new FieldRecord(2, ".", "object2", String.class, RECORD_TYPE.PROXIED_VALUE));
        fieldStorage.serialize(new FieldRecord(2, "./field", "string2", String.class, RECORD_TYPE.PROXIED_FIELD));

        // excersize
        List<FieldRecord> fieldsRead = fieldStorage.readAll(new UUID(0, 1));

        // verify
        assertEquals(3, fieldsRead.size());
        assertEquals(new FieldRecord(1, ".", "object1", String.class, RECORD_TYPE.PROXIED_VALUE), fieldsRead.get(0));
        assertEquals(new FieldRecord(1, "./field", "string1", String.class, RECORD_TYPE.PROXIED_FIELD), fieldsRead.get(1));
        assertEquals(new FieldRecord(1, "./field/subfield", "string11", String.class, RECORD_TYPE.PROXIED_FIELD), fieldsRead.get(2));
    }


}
