package com.github.illarion.swap4j.store.scan;

import com.github.illarion.swap4j.CustomAssertions;
import com.github.illarion.swap4j.store.StorageException;
import com.github.illarion.swap4j.swap.*;
import org.hamcrest.CoreMatchers;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.github.illarion.swap4j.CustomAssertions.*;
import static junit.framework.Assert.*;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

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
        uuidGenerator = context.mock(RandomUuidGenerator.class);
        store = new TestObjectScannerObjectStorage(null, new MapWriter(), uuidGenerator);
        swap = Swap.newInstance(store);
        Swap.setInstance(swap);
        store.setSwap(swap);
    }


    @Test
    public void testSimpleProxyStore() throws StorageException {
        // setup
        context.checking(new UUIDSequenceExpectations(uuidGenerator, context) {{
            expectSequentalUUIDs(0);
        }});

        Dummy dummy = swap.wrap(new Dummy("dummY"), Dummy.class);

        assertStorageContains(
                obj(0, "./field", "dummY", String.class, RECORD_TYPE.PRIMITIVE_FIELD),
                obj(0, ".", "Dummy{field='dummY'}", Dummy.class, RECORD_TYPE.PROXIED_VALUE));
    }

    @Test
    public void testSimpleProxyRestore() throws StorageException {
        // setup
        initializeStore(obj(0, "./field", "Dummy", String.class, RECORD_TYPE.PRIMITIVE_FIELD),
                obj(0, ".", new Dummy("Dummy"), Dummy.class, RECORD_TYPE.PROXIED_VALUE));

        // excersize
        Dummy restored = store.reStore(new UUID(0,0), Dummy.class);

        // verify
        assertEquals(new Dummy("Dummy"), restored);
    }

    private void initializeStore(FieldRecord... fieldRecords) {
        for (FieldRecord fieldRecord : fieldRecords) {
            store.getWriter().serialize(fieldRecord);
        }
    }

    @Test
    public void testProxyListStore() throws StorageException {
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
        CustomAssertions.assertStorageContains(store.getWriter(),
                at(0, ".[",
                    allOf(elementClassIs(Dummy.class), clazzIs(ProxyList.class), recordTypeIsProxyList())),
                at(1, ".",
                    clazzIs(Dummy.class).and(recordTypeIsProxiedValue())),
                at(1, "./field",
                    valueIs("one").and(clazzIs(String.class)).and(recordTypeIsPrimitiveField())),
                at(2, ".",
                    clazzIs(Dummy.class).and(recordTypeIsProxiedValue())),
                at(2, "./field",
                    valueIs("two").and(clazzIs(String.class)).and(recordTypeIsPrimitiveField())),
                at(3, ".",
                    clazzIs(Dummy.class).and(recordTypeIsProxiedValue())),
                at(3, "./field",
                    valueIs("three").and(clazzIs(String.class)).and(recordTypeIsPrimitiveField()))
        );


//        assertStorageContains(obj(0, ".[", list, ProxyList.class, RECORD_TYPE.PROXY_LIST),
//                obj(1, ".", new Dummy("one"), Dummy.class, RECORD_TYPE.PROXIED_VALUE),
//                obj(1, "./field", "one", String.class, RECORD_TYPE.PRIMITIVE_FIELD),
//                obj(2, ".", new Dummy("two"), Dummy.class, RECORD_TYPE.PROXIED_VALUE),
//                obj(2, "./field", "two", String.class, RECORD_TYPE.PRIMITIVE_FIELD),
//                obj(3, ".", new Dummy("three"), Dummy.class, RECORD_TYPE.PROXIED_VALUE),
//                obj(3, "./field", "three", String.class, RECORD_TYPE.PRIMITIVE_FIELD));
    }

    @Test
    public void testProxyListRestore() throws StorageException {
        // setup
        initializeStore(obj(0, ".[", new ProxyListRecord(
                    new UUID(0,1), new UUID(0,2), new UUID(0,3)), Dummy.class, RECORD_TYPE.PROXY_LIST),
                obj(1, ".", new Dummy("one"), Dummy.class, RECORD_TYPE.PROXIED_VALUE),
                obj(1, "./field", "one", String.class, RECORD_TYPE.PRIMITIVE_FIELD),
                obj(2, ".", new Dummy("two"), Dummy.class, RECORD_TYPE.PROXIED_VALUE),
                obj(2, "./field", "two", String.class, RECORD_TYPE.PRIMITIVE_FIELD),
                obj(3, ".", new Dummy("three"), Dummy.class, RECORD_TYPE.PROXIED_VALUE),
                obj(3, "./field", "three", String.class, RECORD_TYPE.PRIMITIVE_FIELD));

        // excersize
//        ProxyList list = store.reStore(new UUID(0, 0), ProxyList.class);
        List list = store.reStoreList(new UUID(0, 0), Dummy.class, new ArrayList<Dummy>());

        // verify
        assertNotNull(list);
        assertThat(list.size(), equalTo(3));

        assertEquals(new Dummy("one"), list.get(0));
        assertEquals(new Dummy("two"), list.get(1));
        assertEquals(new Dummy("three"), list.get(2));
    }

    /**
     * Verifies that store contains ONLY of specified FieldRecord instances
     * @param elements
     * @throws com.github.illarion.swap4j.store.StorageException
     */
    @Deprecated
    private void assertStorageContains(FieldRecord... elements) throws StorageException {
        CustomAssertions.assertStorageContains(store.getWriter(), elements);
    }


}
