package com.github.illarion.swap4j.store.scan;

import com.github.illarion.swap4j.store.StoreException;
import com.github.illarion.swap4j.swap.*;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.util.*;

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

    private TestObjectScannerObjectStorage store;
    private Swap swap;
    private FieldStorage objectSerializer;
    private ObjectScanner scanner;
    private UUIDGenerator uuidGenerator = new RandomUuidGenerator();

    @Before
    public void setUp() throws StoreException {
        store = new TestObjectScannerObjectStorage(null, new MapWriter(), uuidGenerator);
        swap = new Swap(store);
        store.setSwap(swap);

        objectSerializer = context.mock(DummyFieldStorage.class);
        scanner = new ObjectScanner(objectSerializer);
    }

    @Test
    public void testString() throws IllegalAccessException, StoreException {
        context.checking(new Expectations() {{
            one(objectSerializer).serialize(new FieldRecord(null, ".", "hello", String.class, RECORD_TYPE.PRIMITIVE_VALUE));
        }});

        scanner.scanObject("hello");
    }

    @Test
    public void testSimpleObject() throws IllegalAccessException, StoreException {
        context.checking(new Expectations() {{
            one(objectSerializer).serialize(with(any(FieldRecord.class)));
        }});

        Dummy dummy = new Dummy("zzz");
        scanner.scanObject(dummy);
    }

    @Test
    public void testNestedObject() throws IllegalAccessException, StoreException {
        final Sequence serializing = context.sequence("serializing");
        context.checking(new UUIDSequenceExpectations(context) {{
//            expectSequentalUUIDs(4);
            one(objectSerializer).serialize(with(equal(new FieldRecord<String>(null, "./value", "a", String.class, RECORD_TYPE.PRIMITIVE_FIELD))));
            inSequence(serializing);
            one(objectSerializer).serialize(with(equal(new FieldRecord<Nested>(null, "./nested", new Nested("b"), Nested.class, RECORD_TYPE.COMPOUND_FIELD))));
            inSequence(serializing);
            one(objectSerializer).serialize(with(equal(new FieldRecord<String>(null, "./nested/value", "b", String.class, RECORD_TYPE.PRIMITIVE_FIELD))));
            inSequence(serializing);
            one(objectSerializer).serialize(with(equal(new FieldRecord<Nested>(null, "./nested/nested", null, Nested.class, RECORD_TYPE.COMPOUND_FIELD))));
            inSequence(serializing);
        }});

        Nested nested = new Nested("a", new Nested("b"));

        scanner.scanObject(nested);
    }

    @Test
    public void testNestedProxies() throws StoreException, IllegalAccessException {
//        final Sequence serializing = context.sequence("serializing");
        final UUIDGenerator uuidGenerator = context.mock(RandomUuidGenerator.class);

        context.checking(new UUIDSequenceExpectations(context, uuidGenerator, objectSerializer) {{
            expectSequentalUUIDs(0);
            Proxy<ProxyNested> nestedProxy = new Proxy<ProxyNested>(new UUID(0, 0), store, ProxyNested.class);

            expectWrite(null, "./field", "a", String.class, RECORD_TYPE.PRIMITIVE_FIELD);
            expectWrite(0, "./proxy", nestedProxy, ProxyNested.class, RECORD_TYPE.PROXIED_FIELD);
            expectWrite(0, "./proxy/field", "b", String.class, RECORD_TYPE.PRIMITIVE_FIELD);
            expectWrite(0, "./proxy/proxy", null, ProxyNested.class, RECORD_TYPE.PROXIED_FIELD);
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
        final UUIDGenerator uuidGenerator = context.mock(RandomUuidGenerator.class);
        final ProxyList<Dummy> list = new ProxyList<Dummy>(swap, Dummy.class, new UUID(0, 0)); // TODO Can't have ProxyList<Dummy>, only ProxyList<Proxy<Dummy>>
        store.setUuidGenerator(uuidGenerator);

        context.checking(new UUIDSequenceExpectations(context, uuidGenerator, objectSerializer) {{
//            one(objectSerializer).serialize(with(any(SerializedList.class)));
            expectSequentalUUIDs(1, 3);
            expectWrite(0, ".[", list, ProxyList.class, RECORD_TYPE.PROXY_LIST);

            expectWrite(1, ".[0", new Dummy("one"), Dummy.class, RECORD_TYPE.LIST_VALUE);
//            expectWrite(1, ".[0/field", "one", String.class, RECORD_TYPE.PRIMITIVE_FIELD);

            expectWrite(2, ".[1", new Dummy("two"), Dummy.class, RECORD_TYPE.LIST_VALUE);
//            expectWrite(2, ".[1/field", "two", String.class, RECORD_TYPE.PRIMITIVE_FIELD);

            expectWrite(3, ".[2", new Dummy("three"), Dummy.class, RECORD_TYPE.LIST_VALUE);
//            expectWrite(3, ".[2/field", "three", String.class, RECORD_TYPE.PRIMITIVE_FIELD);
        }});

        list.add(new Dummy("one"));
        list.add(new Dummy("two"));
        list.add(new Dummy("three"));

        scanner.scanObject(list, Dummy.class);
    }


    @Test
    public void testGetAllFields() {
        List<Field> fields = Utils.getAllFields(Dummy.class);
        assertEquals(1, fields.size());
        assertEquals("field", fields.get(0).getName());
        assertEquals(String.class, fields.get(0).getType());
    }

    @Test
    public void testTransientFieldsAreIgnored() throws StoreException, IllegalAccessException {
        ObjectWithTransientField objectWithTransientField = new ObjectWithTransientField();

        final UUIDGenerator uuidGenerator = context.mock(RandomUuidGenerator.class);
        store.setUuidGenerator(uuidGenerator);

        context.checking(new UUIDSequenceExpectations(context, uuidGenerator, objectSerializer) {{
//            expectSequentalUUIDs(1);

//            expectWrite(null, ".", new ObjectWithTransientField(), ObjectWithTransientField.class, RECORD_TYPE.COMPOUND_VALUE);
            expectWrite(null, "./nontransientField", "nontransient", String.class, RECORD_TYPE.PRIMITIVE_FIELD);

        }});

        scanner.scanObject(objectWithTransientField);
    }

    public static class ObjectWithTransientField {
        transient String transientField = "transient";
        String nontransientField = "nontransient";

        @SuppressWarnings({"RedundantIfStatement"})
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
