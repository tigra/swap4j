package com.github.illarion.swap4j.store.scan;

import com.github.illarion.swap4j.Baz;
import com.github.illarion.swap4j.CustomAssertions;
import com.github.illarion.swap4j.store.StoreException;
import com.github.illarion.swap4j.swap.ProxyList;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static com.github.illarion.swap4j.CustomAssertions.*;
import static com.github.illarion.swap4j.store.scan.RECORD_TYPE.LIST_ELEMENT;
import static com.github.illarion.swap4j.store.scan.RECORD_TYPE.PRIMITIVE_FIELD;
import static com.github.illarion.swap4j.store.scan.RECORD_TYPE.PROXIED_VALUE;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertThat;

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
        storage.serialize(new FieldRecordBuilder(3, "./field").setValue("hello").setClazz(String.class).setRecordType(PRIMITIVE_FIELD).create());

        FieldRecord retrieved = storage.read(new Locator(3, "./field"));
        assertEquals(new FieldRecordBuilder(3, "./field").setValue("hello").setClazz(String.class).setRecordType(PRIMITIVE_FIELD).create(), retrieved);
    }

    @Test
    public void testSerializeOverwrite() throws ClassNotFoundException, SQLException {
        FieldStorage storage = new H2FieldStorage();
        storage.serialize(new FieldRecordBuilder(3, "./field").setValue("hello").setClazz(String.class).setRecordType(PRIMITIVE_FIELD).create());
        storage.serialize(new FieldRecordBuilder(3, "./field").setValue("there").setClazz(String.class).setRecordType(PRIMITIVE_FIELD).create());

        FieldRecord retrieved = storage.read(new Locator(3, "./field"));
        assertEquals(new FieldRecordBuilder(3, "./field").setValue("there").setClazz(String.class).setRecordType(PRIMITIVE_FIELD).create(), retrieved);
    }

    @Test
    public void testClean() throws ClassNotFoundException, SQLException, StoreException {
        // setup
        FieldStorage storage = new H2FieldStorage();
        storage.serialize(new FieldRecordBuilder(3, "./field1").setValue("hello").setClazz(String.class).setRecordType(PRIMITIVE_FIELD).create());
        storage.serialize(new FieldRecordBuilder(3, "./field2").setValue("there").setClazz(String.class).setRecordType(PRIMITIVE_FIELD).create());
        storage.serialize(new FieldRecordBuilder(1, "./a").setValue("b").setClazz(String.class).setRecordType(PRIMITIVE_FIELD).create());

        // excersize
        storage.clean(new UUID(0,3));

        // verify
//        CustomAssertions.assertStorageContains(storage, obj(1, "./a", "b", String.class, RECORD_TYPE.PRIMITIVE_FIELD));
        CustomAssertions.assertStorageContains(storage,
                at(1, "./a", valueIs("b").and(clazzIs(String.class)).and(recordTypeIsPrimitiveField())));
    }

    @Test
    public void testReadElementRecords() throws ClassNotFoundException, SQLException {
        // setup
        FieldStorage storage = new H2FieldStorage();
        storage.serialize(new FieldRecordBuilder(0, "[").setValue("PL(...)").setClazz(ProxyList.class).setElementClass(Baz.class).setRecordType(PROXIED_VALUE).create());
        storage.serialize(new FieldRecordBuilder(0, "[0").setValue(new UUID(0,1)).setClazz(Baz.class).setRecordType(LIST_ELEMENT).create());
        storage.serialize(new FieldRecordBuilder(0, "[1").setValue(new UUID(0,2)).setClazz(Baz.class).setRecordType(LIST_ELEMENT).create());

        // excersize
        List<FieldRecord> elementRecords = storage.readElementRecords(new UUID(0,0), Baz.class);

        // verify
        assertThat(elementRecords, CustomAssertions.containsElements(
                new FieldRecordBuilder(0, "[0").setValue(new UUID(0,1).toString()).setClazz(Baz.class).setRecordType(LIST_ELEMENT).create(),
                new FieldRecordBuilder(0, "[1").setValue(new UUID(0,2).toString()).setClazz(Baz.class).setRecordType(LIST_ELEMENT).create()
        ));
    }

}
