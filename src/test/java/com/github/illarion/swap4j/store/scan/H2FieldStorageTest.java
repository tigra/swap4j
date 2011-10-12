package com.github.illarion.swap4j.store.scan;

import com.github.illarion.swap4j.CustomAssertions;
import com.github.illarion.swap4j.store.StoreException;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.UUID;

import static com.github.illarion.swap4j.CustomAssertions.*;
import static junit.framework.Assert.assertEquals;

/**
 * TODO Describe class
 *
 * @author Alexey Tigarev tigra@agile-algorithms.com
 */
public class H2FieldStorageTest {   
    @Before
    public void setUp() throws ClassNotFoundException, SQLException {
        new H2FieldStorage().cleanAll();
    }

    @Test
    public void testConstructor() throws ClassNotFoundException, SQLException {
        new H2FieldStorage();
    }

    @Test
    public void testSerialize() throws ClassNotFoundException, SQLException {
        FieldStorage storage = new H2FieldStorage();
        storage.serialize(new FieldRecordBuilder(3, "./field").setValue("hello").setClazz(String.class).setRecordType(RECORD_TYPE.PRIMITIVE_FIELD).create());

        FieldRecord retrieved = storage.read(new Locator(3, "./field"));
        assertEquals(new FieldRecordBuilder(3, "./field").setValue("hello").setClazz(String.class).setRecordType(RECORD_TYPE.PRIMITIVE_FIELD).create(), retrieved);
    }

    @Test
    public void testSerializeOverwrite() throws ClassNotFoundException, SQLException {
        FieldStorage storage = new H2FieldStorage();
        storage.serialize(new FieldRecordBuilder(3, "./field").setValue("hello").setClazz(String.class).setRecordType(RECORD_TYPE.PRIMITIVE_FIELD).create());
        storage.serialize(new FieldRecordBuilder(3, "./field").setValue("there").setClazz(String.class).setRecordType(RECORD_TYPE.PRIMITIVE_FIELD).create());

        FieldRecord retrieved = storage.read(new Locator(3, "./field"));
        assertEquals(new FieldRecordBuilder(3, "./field").setValue("there").setClazz(String.class).setRecordType(RECORD_TYPE.PRIMITIVE_FIELD).create(), retrieved);
    }

    @Test
    public void testClean() throws ClassNotFoundException, SQLException, StoreException {
        // setup
        FieldStorage storage = new H2FieldStorage();
        storage.serialize(new FieldRecordBuilder(3, "./field1").setValue("hello").setClazz(String.class).setRecordType(RECORD_TYPE.PRIMITIVE_FIELD).create());
        storage.serialize(new FieldRecordBuilder(3, "./field2").setValue("there").setClazz(String.class).setRecordType(RECORD_TYPE.PRIMITIVE_FIELD).create());
        storage.serialize(new FieldRecordBuilder(1, "./a").setValue("b").setClazz(String.class).setRecordType(RECORD_TYPE.PRIMITIVE_FIELD).create());

        // excersize
        storage.clean(new UUID(0,3));

        // verify
//        CustomAssertions.assertStorageContains(storage, obj(1, "./a", "b", String.class, RECORD_TYPE.PRIMITIVE_FIELD));
        CustomAssertions.assertStorageContains(storage,
                at(1, "./a", valueIs("b").and(clazzIs(String.class)).and(recordTypeIsPrimitiveField())));
    }

}
