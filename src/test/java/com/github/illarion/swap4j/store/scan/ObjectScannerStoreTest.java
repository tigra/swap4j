package com.github.illarion.swap4j.store.scan;

import com.github.illarion.swap4j.CustomAssertions;
import com.github.illarion.swap4j.store.StoreException;
import com.github.illarion.swap4j.swap.ProxyList;
import com.github.illarion.swap4j.swap.Swap;
import com.github.illarion.swap4j.swap.UUIDGenerator;
import com.github.illarion.swap4j.swap.UUIDSequenceExpectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

import static junit.framework.Assert.*;

/**
 * TODO Describe class
 * <p/>
 * <p/>
 * Created at: Sep 23, 2011 3:22:38 PM
 *
 * @author Alexey Tigarev
 */
@RunWith(JMock.class)
public class ObjectScannerStoreTest {
    private UUIDGenerator uuidGenerator;
    private Mockery context = new JUnit4Mockery() {{
            setImposteriser(ClassImposteriser.INSTANCE);
    }};
    private TestObjectScannerObjectStorage store;
    private Swap swap;


    @Before
    public void setUp() {
        uuidGenerator = context.mock(UUIDGenerator.class);
        store = new TestObjectScannerObjectStorage(null, new MapWriter(), uuidGenerator);
        swap = new Swap(store);
        store.setSwap(swap);
    }


    @Test
    public void testSimpleProxyStore() throws StoreException {
        // setup
        context.checking(new UUIDSequenceExpectations(uuidGenerator, context) {{
            expectSequentalUUIDs(0);
        }});

        Dummy dummy = swap.wrap(new Dummy("dummY"), Dummy.class);

        assertStoreContains(
                com.github.illarion.swap4j.CustomAssertions.obj(0, "./field", "dummY", String.class, TYPE.PRIMITIVE_FIELD),
                com.github.illarion.swap4j.CustomAssertions.obj(0, ".", new Dummy("dummY"), Dummy.class, TYPE.PROXIED_VALUE));
    }

    @Test
    public void testSimpleProxyRestore() throws StoreException {
        initializeStore(com.github.illarion.swap4j.CustomAssertions.obj(0, "./field", "Dummy", String.class, TYPE.PRIMITIVE_FIELD),
                com.github.illarion.swap4j.CustomAssertions.obj(0, ".", new Dummy("Dummy"), Dummy.class, TYPE.PROXIED_VALUE));

        Dummy restored = store.reStore(new UUID(0,0), Dummy.class);

        assertEquals(new Dummy("Dummy"), restored);
    }

    private void initializeStore(SerializedField... serializedFields) {
        for (SerializedField serializedField : serializedFields) {
            store.getWriter().serialize(serializedField);
        }
    }

    @Test
    public void testProxyListStore() throws StoreException {
        // setup
        context.checking(new UUIDSequenceExpectations(uuidGenerator, context) {{
            expectSequentalUUIDs(1, 3);
        }});

        // excersize

        final ProxyList<Dummy> list = new ProxyList<Dummy>(swap, Dummy.class, new UUID(0,0));
        list.add(new Dummy("one"));
        list.add(new Dummy("two"));
        list.add(new Dummy("three"));

        // verify
        assertStoreContains(CustomAssertions.obj(0, ".[", list, Dummy.class, TYPE.PROXY_LIST),
                com.github.illarion.swap4j.CustomAssertions.obj(1, ".", new Dummy("one"), Dummy.class, TYPE.PROXIED_VALUE),
                com.github.illarion.swap4j.CustomAssertions.obj(1, "./field", "one", String.class, TYPE.PRIMITIVE_FIELD),
                com.github.illarion.swap4j.CustomAssertions.obj(2, ".", new Dummy("two"), Dummy.class, TYPE.PROXIED_VALUE),
                com.github.illarion.swap4j.CustomAssertions.obj(2, "./field", "two", String.class, TYPE.PRIMITIVE_FIELD),
                com.github.illarion.swap4j.CustomAssertions.obj(3, ".", new Dummy("three"), Dummy.class, TYPE.PROXIED_VALUE),
                com.github.illarion.swap4j.CustomAssertions.obj(3, "./field", "three", String.class, TYPE.PRIMITIVE_FIELD));

//        assertStoreContains(obj(0, ".[", list, Dummy.class, TYPE.PROXY_LIST),
//                obj(1, ".[0", new Dummy("one"), Dummy.class, TYPE.LIST_VALUE),
//                obj(1, ".[0/field", "one", String.class, TYPE.PRIMITIVE_FIELD),
//                obj(2, ".[1", new Dummy("two"), Dummy.class, TYPE.LIST_VALUE),
//                obj(2, ".[1/field", "two", String.class, TYPE.PRIMITIVE_FIELD),
//                obj(3, ".[2", new Dummy("three"), Dummy.class, TYPE.LIST_VALUE),
//                obj(3, ".[2/field", "three", String.class, TYPE.PRIMITIVE_FIELD));
    }

    @Test
    public void testProxyListRestore() throws StoreException {
        // setup
        context.checking(new UUIDSequenceExpectations(uuidGenerator, context) {{
            expectSequentalUUIDs(0);
        }});

        initializeStore(com.github.illarion.swap4j.CustomAssertions.obj(0, ".[", new ProxyListRecord(
                    new UUID(0,1), new UUID(0,2), new UUID(0,3)), Dummy.class, TYPE.PROXY_LIST),
                com.github.illarion.swap4j.CustomAssertions.obj(1, ".", new Dummy("one"), Dummy.class, TYPE.PROXIED_VALUE),
                com.github.illarion.swap4j.CustomAssertions.obj(1, "./field", "one", String.class, TYPE.PRIMITIVE_FIELD),
                com.github.illarion.swap4j.CustomAssertions.obj(2, ".", new Dummy("two"), Dummy.class, TYPE.PROXIED_VALUE),
                com.github.illarion.swap4j.CustomAssertions.obj(2, "./field", "two", String.class, TYPE.PRIMITIVE_FIELD),
                com.github.illarion.swap4j.CustomAssertions.obj(3, ".", new Dummy("three"), Dummy.class, TYPE.PROXIED_VALUE),
                com.github.illarion.swap4j.CustomAssertions.obj(3, "./field", "three", String.class, TYPE.PRIMITIVE_FIELD));

        ProxyList list = store.reStore(new UUID(0, 0), ProxyList.class);

        assertNotNull(list);
        assertEquals(3, list.size());
        assertEquals(new Dummy("one"), list.get(0));
        assertEquals(new Dummy("two"), list.get(1));
        assertEquals(new Dummy("three"), list.get(2));
    }

    /**
     * Verifies that store contains ONLY of specified SerializedField instances
     * @param elements
     * @throws com.github.illarion.swap4j.store.StoreException
     */
    private void assertStoreContains(SerializedField... elements) throws StoreException {
        com.github.illarion.swap4j.CustomAssertions.assertStorageContains(store.getWriter(), elements);
    }


}
