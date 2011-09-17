package com.github.illarion.swap4j.store.scan;

import com.github.illarion.swap4j.store.StoreException;
import com.github.illarion.swap4j.store.simplegsonstore.SimpleStore;
import com.github.illarion.swap4j.swap.*;
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
import java.util.List;
import java.util.UUID;

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

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();
    
    private SimpleStore store;
    private Swap swap;
    private ObjectSerializer objectSerializer;
    private ObjectScanner scanner;

    @Before
    public void setUp() throws StoreException {
        store = new SimpleStore(testFolder.newFolder("temp"));
        swap = new Swap(store);

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

    static class Dummy {
        String field = null;

        Dummy() {
        }

        Dummy(String field) {
            this.field = field;
        }
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
            one(objectSerializer).serialize(with(equal(new SerializedField<String>(null, "./value", "a", String.class, TYPE.PRIMITIVE_FIELD)))); inSequence(serializing);
            one(objectSerializer).serialize(with(equal(new SerializedField<Nested>(null, "./nested", new Nested("b"), Nested.class, TYPE.COMPOUND_FIELD)))); inSequence(serializing);
            one(objectSerializer).serialize(with(equal(new SerializedField<String>(null, "./nested/value", "b", String.class, TYPE.PRIMITIVE_FIELD)))); inSequence(serializing);
            one(objectSerializer).serialize(with(equal(new SerializedField<Nested>(null, "./nested/nested", null, Nested.class, TYPE.COMPOUND_FIELD)))); inSequence(serializing);
        }});

        Nested nested = new Nested("a", new Nested("b"));

        scanner.scanObject(nested);
    }

    @Test
    public void testNestedProxies() throws StoreException, IllegalAccessException {
        final Sequence serializing = context.sequence("serializing");
        final UUIDGenerator uuidGenerator = context.mock(UUIDGenerator.class);

        context.checking(new UUIDSequenceExpectations(context, uuidGenerator) {{
            expectSequentalUUIDs(1);
            expectWrite(null, "./field", "a", String.class, TYPE.PRIMITIVE_FIELD);
            Proxy<ProxyNested> nestedProxy = new Proxy<ProxyNested>(new UUID(0,0), store, ProxyNested.class);
            one(objectSerializer).serialize(with(equal(new SerializedField<Proxy<ProxyNested>>(0, "./proxy",
                    nestedProxy, ProxyNested.class/*nestedProxy.getClass()*/, TYPE.PROXIED_FIELD)))); inSequence(serializing);
//            expectWrite<Proxy<ProxyNested>>(0, "./proxy", nestedProxy, nestedProxy.getClass(), TYPE.PROXIED_FIELD);
            one(objectSerializer).serialize(with(equal(new SerializedField<String>(0, "./proxy/field", "b", String.class, TYPE.PRIMITIVE_FIELD)))); inSequence(serializing);
            one(objectSerializer).serialize(with(equal(new SerializedField<String>(0, "./proxy/proxy", null, ProxyNested.class, TYPE.PROXIED_FIELD)))); inSequence(serializing);
        }

            private <T> void expectWrite(int id, String path, T value, Class<Proxy> clazz, TYPE recordType) {
                expectWrite(new UUID(0, id), path, value, clazz, recordType);
            }

            private <T> void expectWrite(UUID id, String path, T value, Class clazz, TYPE recordType) {
                one(objectSerializer).serialize(with(equal(new SerializedField<T>(id, path, value, clazz, recordType))));
                inSequence(serializing);
            }
        });

        store.setUuidGenerator(uuidGenerator);
        ProxyNested proxyNested = new ProxyNested("a", new Proxy<ProxyNested>(store, new ProxyNested("b", null), ProxyNested.class));

        scanner.scanObject(proxyNested);
    }

    @Test
    public void testProxyList() {

    }

    @Test
    public void testList() throws IllegalAccessException, StoreException {
        context.checking(new Expectations() {{
            one(objectSerializer).serialize(with(any(SerializedList.class)));
        }});

        ProxyList<Dummy> list = new ProxyList<Dummy>(swap, Dummy.class);
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
