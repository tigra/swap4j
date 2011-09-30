package com.github.illarion.swap4j.store.scan;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Test for <code>SerializedField</code>
 *
 * @author Alexey Tigarev
 */
public class SerializedFieldTest {

    @Test
    public void testWriteTo() throws NoSuchFieldException, IllegalAccessException {
        // setup
        Dummy dummy = new Dummy("a");
        SerializedField field = new SerializedField(0, "./field", "b", String.class, TYPE.PRIMITIVE_FIELD);

        // excersize
        field.writeTo(dummy);

        // verify
        assertEquals(new Dummy("b"), dummy);
    }

    @Test
    public void testWriteToNested() throws NoSuchFieldException, IllegalAccessException {
        // setup
        Nested a = new Nested("a", new Nested("b"));
        SerializedField field = new SerializedField(0, "./nested/value", "z", String.class, TYPE.PRIMITIVE_FIELD);

        // excersize
        field.writeTo(a);

        // verify
        assertEquals(new Nested("a", new Nested("z")), a);
    }

}
