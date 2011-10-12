package com.github.illarion.swap4j.store.scan;

import org.junit.Test;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static com.github.illarion.swap4j.CustomAssertions.*;

/**
 * TODO Describe class
 *
 * @author Alexey Tigarev tigra@agile-algorithms.com
 */
public class FieldRecordBuilderTest {
    @Test
    public void testWithSetLocator() {
        // setup & excersize
        FieldRecordBuilder initialBuilder = new FieldRecordBuilder();
        FieldRecordBuilder updatedBuilder = initialBuilder.setLocator(new Locator(1, "."))
                .setClazz(FieldRecordBuilderTest.class)
                .setElementClass(String.class)
                .setRecordType(RECORD_TYPE.COMPOUND_FIELD);

        FieldRecord fieldRecord = updatedBuilder.create();

        // verify
        assertThat(updatedBuilder, sameInstance(initialBuilder));

        assertThat(fieldRecord, clazzIs(FieldRecordBuilderTest.class).and(elementClassIs(String.class)
                .and(recordTypeIs(RECORD_TYPE.COMPOUND_FIELD).and(idIs(1)).and(pathIs(".")))));
    }

    @Test
    public void testWithSetUUID() {
        // setup & excersize
        FieldRecordBuilder initialBuilder = new FieldRecordBuilder();
        FieldRecordBuilder updatedBuilder = initialBuilder.setId(new UUID(0,1)).setPath(".")
                .setClazz(FieldRecordBuilderTest.class)
                .setElementClass(String.class)
                .setRecordType(RECORD_TYPE.COMPOUND_FIELD);

        FieldRecord fieldRecord = updatedBuilder.create();

        // verify
        assertThat(updatedBuilder, sameInstance(initialBuilder));

        assertThat(fieldRecord, clazzIs(FieldRecordBuilderTest.class).and(elementClassIs(String.class)
                .and(recordTypeIs(RECORD_TYPE.COMPOUND_FIELD).and(idIs(1)).and(pathIs(".")))));
    }

    @Test
    public void testWithSetId() {
        // setup & excersize
        FieldRecordBuilder initialBuilder = new FieldRecordBuilder();
        FieldRecordBuilder updatedBuilder = initialBuilder.setId(1).setPath(".")
                .setClazz(FieldRecordBuilderTest.class)
                .setElementClass(String.class)
                .setRecordType(RECORD_TYPE.COMPOUND_FIELD);

        FieldRecord fieldRecord = updatedBuilder.create();

        // verify
        assertThat(updatedBuilder, sameInstance(initialBuilder));

        assertThat(fieldRecord, clazzIs(FieldRecordBuilderTest.class).and(elementClassIs(String.class)
                .and(recordTypeIs(RECORD_TYPE.COMPOUND_FIELD).and(idIs(1)).and(pathIs(".")))));
    }
    



}
