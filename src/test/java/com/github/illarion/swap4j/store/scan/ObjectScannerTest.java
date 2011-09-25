package com.github.illarion.swap4j.store.scan;

import com.github.illarion.swap4j.store.Store;
import com.github.illarion.swap4j.store.StoreException;
import com.github.illarion.swap4j.swap.*;
import com.sun.rowset.internal.XmlReaderContentHandler;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Logger;

import static junit.framework.Assert.assertEquals;

/**
 * TODO Describe class
 * <p/>
 * <p/>
 * Created at: Sep 16, 2011 1:53:08 AM
 *
 * @author Alexey Tigarev
 */
@RunWith(JMock.class)
public class ObjectScannerTest {
    private Mockery context = new JUnit4Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};

    private TestObjectScannerStore store;
    private Swap swap;
    private ObjectSerializer objectSerializer;
    private ObjectScanner scanner;
    private UUIDGenerator uuidGenerator = new UUIDGenerator();

    @Before
    public void setUp() throws StoreException {
        store = new TestObjectScannerStore(null, new MapWriter(), uuidGenerator);
        swap = new Swap(store);
        ((TestObjectScannerStore)store).setSwap(swap);

        objectSerializer = context.mock(ObjectSerializer.class);
        scanner = new ObjectScanner(objectSerializer);
    }

    @Test
    public void testString() throws IllegalAccessException, StoreException {
        context.checking(new Expectations() {{
            one(objectSerializer).serialize(new SerializedField(null, ".", "hello", String.class, TYPE.PRIMITIVE_VALUE));
        }});

        scanner.scanObject("hello");
    }

    @Test
    public void testSimpleObject() throws IllegalAccessException, StoreException {
        context.checking(new Expectations() {{
            one(objectSerializer).serialize(with(any(SerializedField.class)));
        }});

        Dummy dummy = new Dummy("zzz");
        scanner.scanObject(dummy);
    }

    @Test
    public void testNestedObject() throws IllegalAccessException, StoreException {
        final Sequence serializing = context.sequence("serializing");
        context.checking(new UUIDSequenceExpectations(context) {{
//            expectSequentalUUIDs(4);
            one(objectSerializer).serialize(with(equal(new SerializedField<String>(null, "./value", "a", String.class, TYPE.PRIMITIVE_FIELD))));
            inSequence(serializing);
            one(objectSerializer).serialize(with(equal(new SerializedField<Nested>(null, "./nested", new Nested("b"), Nested.class, TYPE.COMPOUND_FIELD))));
            inSequence(serializing);
            one(objectSerializer).serialize(with(equal(new SerializedField<String>(null, "./nested/value", "b", String.class, TYPE.PRIMITIVE_FIELD))));
            inSequence(serializing);
            one(objectSerializer).serialize(with(equal(new SerializedField<Nested>(null, "./nested/nested", null, Nested.class, TYPE.COMPOUND_FIELD))));
            inSequence(serializing);
        }});

        Nested nested = new Nested("a", new Nested("b"));

        scanner.scanObject(nested);
    }

    @Test
    public void testNestedProxies() throws StoreException, IllegalAccessException {
//        final Sequence serializing = context.sequence("serializing");
        final UUIDGenerator uuidGenerator = context.mock(UUIDGenerator.class);

        context.checking(new UUIDSequenceExpectations(context, uuidGenerator, objectSerializer) {{
            expectSequentalUUIDs(0);
            Proxy<ProxyNested> nestedProxy = new Proxy<ProxyNested>(new UUID(0, 0), store, ProxyNested.class);

            expectWrite(null, "./field", "a", String.class, TYPE.PRIMITIVE_FIELD);
            expectWrite(0, "./proxy", nestedProxy, ProxyNested.class, TYPE.PROXIED_FIELD);
            expectWrite(0, "./proxy/field", "b", String.class, TYPE.PRIMITIVE_FIELD);
            expectWrite(0, "./proxy/proxy", null, ProxyNested.class, TYPE.PROXIED_FIELD);
        }});

        store.setUuidGenerator(uuidGenerator);
        ProxyNested proxyNested = new ProxyNested("a", new Proxy<ProxyNested>(store, new ProxyNested("b", null), ProxyNested.class));

        scanner.scanObject(proxyNested);
    }

    @Test
    public void testProxyList() {
        // TODO write
    }

    @Test
    public void testList() throws IllegalAccessException, StoreException {
        final UUIDGenerator uuidGenerator = context.mock(UUIDGenerator.class);
        final ProxyList<Dummy> list = new ProxyList<Dummy>(swap, Dummy.class, new UUID(0, 0)); // TODO Can't have ProxyList<Dummy>, only ProxyList<Proxy<Dummy>>
        store.setUuidGenerator(uuidGenerator);

        context.checking(new UUIDSequenceExpectations(context, uuidGenerator, objectSerializer) {{
//            one(objectSerializer).serialize(with(any(SerializedList.class)));
            expectSequentalUUIDs(1, 3);
            expectWrite(0, ".[", list, Dummy.class, TYPE.PROXY_LIST);

            expectWrite(1, ".[0", new Dummy("one"), Dummy.class, TYPE.LIST_VALUE);
//            expectWrite(1, ".[0/field", "one", String.class, TYPE.PRIMITIVE_FIELD);

            expectWrite(2, ".[1", new Dummy("two"), Dummy.class, TYPE.LIST_VALUE);
//            expectWrite(2, ".[1/field", "two", String.class, TYPE.PRIMITIVE_FIELD);

            expectWrite(3, ".[2", new Dummy("three"), Dummy.class, TYPE.LIST_VALUE);
//            expectWrite(3, ".[2/field", "three", String.class, TYPE.PRIMITIVE_FIELD);
        }});

        list.add(new Dummy("one"));
        list.add(new Dummy("two"));
        list.add(new Dummy("three"));

        scanner.scanObject(list);
    }


    @Test
    public void testGetAllFields() {
        List<Field> fields = ObjectScanner.getAllFields(Dummy.class);
        assertEquals(1, fields.size());
        assertEquals("field", fields.get(0).getName());
        assertEquals(String.class, fields.get(0).getType());
    }

    @Test
    public void testTransientFieldsAreIgnored() throws StoreException, IllegalAccessException {
        ObjectWithTransientField objectWithTransientField = new ObjectWithTransientField();

        final UUIDGenerator uuidGenerator = context.mock(UUIDGenerator.class);
        store.setUuidGenerator(uuidGenerator);

        context.checking(new UUIDSequenceExpectations(context, uuidGenerator, objectSerializer) {{
//            expectSequentalUUIDs(1);

//            expectWrite(null, ".", new ObjectWithTransientField(), ObjectWithTransientField.class, TYPE.COMPOUND_VALUE);
            expectWrite(null, "./nontransientField", "nontransient", String.class, TYPE.PRIMITIVE_FIELD);

        }});

        scanner.scanObject(objectWithTransientField);
    }

    public static class ObjectWithTransientField {
        transient String transientField = "transient";
        String nontransientField = "nontransient";

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ObjectWithTransientField that = (ObjectWithTransientField) o;

            if (nontransientField != null ? !nontransientField.equals(that.nontransientField) : that.nontransientField != null)
                return false;
            if (transientField != null ? !transientField.equals(that.transientField) : that.transientField != null)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = transientField != null ? transientField.hashCode() : 0;
            result = 31 * result + (nontransientField != null ? nontransientField.hashCode() : 0);
            return result;
        }
    }

    private class Nested {
        String value;
        Nested nested = null;

        public Nested(String value) {
            this.value = value;
        }

        public Nested(String value, Nested nested) {
            this.value = value;
            this.nested = nested;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Nested nested1 = (Nested) o;

            if (nested != null ? !nested.equals(nested1.nested) : nested1.nested != null) return false;
            if (value != null ? !value.equals(nested1.value) : nested1.value != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = value != null ? value.hashCode() : 0;
            result = 31 * result + (nested != null ? nested.hashCode() : 0);
            return result;
        }
    }

    private class ProxyNested {
        String field;
        Proxy<ProxyNested> proxy;

        ProxyNested() {
        }

        ProxyNested(String field, Proxy<ProxyNested> proxy) {
            this.field = field;
            this.proxy = proxy;
        }

    }
}
