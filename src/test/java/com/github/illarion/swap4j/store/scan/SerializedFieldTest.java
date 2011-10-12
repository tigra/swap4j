package com.github.illarion.swap4j.store.scan;

import com.github.illarion.swap4j.store.StoreException;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Test for <code>FieldRecord</code>
 *
 * @author Alexey Tigarev
 */
public class SerializedFieldTest {

    @Test
    public void testWriteTo() throws NoSuchFieldException, IllegalAccessException, StoreException {
        // setup
        Dummy dummy = new Dummy("a");
        FieldRecord fieldRecord = new FieldRecordBuilder(0, "./field").setValue("b").setClazz(String.class).setRecordType(RECORD_TYPE.PRIMITIVE_FIELD).create();

        // excersize
        fieldRecord.writeTo(dummy);

        // verify
        assertEquals(new Dummy("b"), dummy);
    }

    @Test
    public void testWriteToNested() throws NoSuchFieldException, IllegalAccessException, StoreException {
        // setup
        Nested a = new Nested("a", new Nested("b"));
        FieldRecord fieldRecord = new FieldRecordBuilder(0, "./nested/value").setValue("z").setClazz(String.class).setRecordType(RECORD_TYPE.PRIMITIVE_FIELD).create();

        // excersize
        fieldRecord.writeTo(a);

        // verify
        assertEquals(new Nested("a", new Nested("z")), a);
    }

}
