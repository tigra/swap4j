package com.github.illarion.swap4j.store.scan;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Test for <code>FieldRecord</code>
 *
 * @author Alexey Tigarev
 */
public class SerializedFieldTest {

    @Test
    public void testWriteTo() throws NoSuchFieldException, IllegalAccessException {
        // setup
        Dummy dummy = new Dummy("a");
        FieldRecord fieldRecord = new FieldRecord(0, "./field", "b", String.class, RECORD_TYPE.PRIMITIVE_FIELD);

        // excersize
        fieldRecord.writeTo(dummy);

        // verify
        assertEquals(new Dummy("b"), dummy);
    }

    @Test
    public void testWriteToNested() throws NoSuchFieldException, IllegalAccessException {
        // setup
        Nested a = new Nested("a", new Nested("b"));
        FieldRecord fieldRecord = new FieldRecord(0, "./nested/value", "z", String.class, RECORD_TYPE.PRIMITIVE_FIELD);

        // excersize
        fieldRecord.writeTo(a);

        // verify
        assertEquals(new Nested("a", new Nested("z")), a);
    }

}
