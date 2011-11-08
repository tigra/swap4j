package com.github.illarion.swap4j.store.scan;

import com.github.illarion.swap4j.Baz;
import com.github.illarion.swap4j.store.StorageException;
import com.github.illarion.swap4j.swap.ProxyList;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

import static com.github.illarion.swap4j.CustomAssertions.assertStorageContains;
import static com.github.illarion.swap4j.CustomAssertions.isEmpty;
import static com.github.illarion.swap4j.CustomAssertions.obj;
import static junit.framework.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
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
    public void testClean() throws StorageException {
        // setup
        fieldStorage.serialize(new FieldRecordBuilder(1, ".").setValue("object1").setClazz(String.class).setRecordType(RECORD_TYPE.PROXIED_VALUE).create());
        fieldStorage.serialize(new FieldRecordBuilder(1, "./field").setValue("string1").setClazz(String.class).setRecordType(RECORD_TYPE.PROXIED_FIELD).create());

        fieldStorage.serialize(new FieldRecordBuilder(2, ".").setValue("object2").setClazz(String.class).setRecordType(RECORD_TYPE.PROXIED_VALUE).create());
        fieldStorage.serialize(new FieldRecordBuilder(2, "./field").setValue("string2").setClazz(String.class).setRecordType(RECORD_TYPE.PROXIED_FIELD).create());

        // excersize
        assertTrue(fieldStorage.clean(new UUID(0, 1)));

        // verify
        assertStorageContains(fieldStorage,
                obj(2, ".", "object2", String.class, RECORD_TYPE.PROXIED_VALUE),
                obj(2, "./field", "string2", String.class, RECORD_TYPE.PROXIED_FIELD));
    }

    @Test
    public void testCleanNothing() throws StorageException {
        // setup
        fieldStorage.serialize(new FieldRecordBuilder(2, ".").setValue("singleObject").setClazz(String.class).setRecordType(RECORD_TYPE.PROXIED_VALUE).create());
        fieldStorage.serialize(new FieldRecordBuilder(2, "./field").setValue("stringInSingleObject").setClazz(String.class).setRecordType(RECORD_TYPE.PROXIED_FIELD).create());

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
        fieldStorage.serialize(new FieldRecordBuilder(1, "./field/subfield").setValue("string11").setClazz(String.class).setRecordType(RECORD_TYPE.PROXIED_FIELD).create());
        fieldStorage.serialize(new FieldRecordBuilder(1, "./field").setValue("string1").setClazz(String.class).setRecordType(RECORD_TYPE.PROXIED_FIELD).create());
        fieldStorage.serialize(new FieldRecordBuilder(1, ".").setValue("object1").setClazz(String.class).setRecordType(RECORD_TYPE.PROXIED_VALUE).create());

        fieldStorage.serialize(new FieldRecordBuilder(2, ".").setValue("object2").setClazz(String.class).setRecordType(RECORD_TYPE.PROXIED_VALUE).create());
        fieldStorage.serialize(new FieldRecordBuilder(2, "./field").setValue("string2").setClazz(String.class).setRecordType(RECORD_TYPE.PROXIED_FIELD).create());

        // excersize
        List<FieldRecord> fieldsRead = fieldStorage.readAll(new UUID(0, 1));

        // verify
        assertEquals(3, fieldsRead.size());
        assertEquals(new FieldRecordBuilder(1, ".").setValue("object1").setClazz(String.class).setRecordType(RECORD_TYPE.PROXIED_VALUE).create(), fieldsRead.get(0));
        assertEquals(new FieldRecordBuilder(1, "./field").setValue("string1").setClazz(String.class).setRecordType(RECORD_TYPE.PROXIED_FIELD).create(), fieldsRead.get(1));
        assertEquals(new FieldRecordBuilder(1, "./field/subfield").setValue("string11").setClazz(String.class).setRecordType(RECORD_TYPE.PROXIED_FIELD).create(), fieldsRead.get(2));
    }

    @Test
    public void testReadElementRecordsEmpty() {
        fieldStorage.serialize(new FieldRecordBuilder(0, ".[").setValue("ignored").setClazz(ProxyList.class)
                .setElementClass(Baz.class).setRecordType(RECORD_TYPE.PROXY_LIST).create()); // only "head" of list

        List<FieldRecord> elementRecords = fieldStorage.readElementRecords(new UUID(0,0), Baz.class);
        MatcherAssert.assertThat(elementRecords, isEmpty());
    }

    @Test
    public void testReadAllDoesnotReadNulls() {
        UUID absentUuid = new UUID(0, 666);
        List<FieldRecord> read = fieldStorage.readAll(absentUuid);
        assertThat(read, isEmpty());
    }

    @Test(expected = StorageException.class)
    public void testReadDoesnotReadNulls() throws StorageException {
        UUID absentUuid = new UUID(0, 666);
        FieldRecord read = fieldStorage.read(new Locator(absentUuid, "."));
    }

}
