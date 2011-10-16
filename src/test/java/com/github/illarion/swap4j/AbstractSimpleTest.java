package com.github.illarion.swap4j;

import com.github.illarion.swap4j.store.ObjectStorage;
import com.github.illarion.swap4j.store.StoreException;
import com.github.illarion.swap4j.store.scan.FieldRecordBuilder;
import com.github.illarion.swap4j.store.scan.FieldStorage;
import com.github.illarion.swap4j.store.scan.RECORD_TYPE;
import com.github.illarion.swap4j.swap.Proxy;
import com.github.illarion.swap4j.swap.ProxyList;
import com.github.illarion.swap4j.swap.Swap;
import junit.framework.TestCase;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.imageio.plugins.bmp.BMPImageWriteParam;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.github.illarion.swap4j.CustomAssertions.*;
import static com.github.illarion.swap4j.CustomAssertions.elementClassIs;
import static com.github.illarion.swap4j.store.scan.RECORD_TYPE.*;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * TODO Describe class
 *
 * @author Alexey Tigarev tigra@agile-algorithms.com
 */
public abstract class AbstractSimpleTest extends TestCase {
    protected Swap swap;
    protected ObjectStorage objectStore;
    protected FieldStorage fieldStorage;

    public AbstractSimpleTest(String testMethodName) {
        super(testMethodName);
        try {            
            setUp();
        } catch (ClassNotFoundException e) {
            fail();
//            throw new IllegalStateException(e);
        } catch (SQLException e) {
//            throw new IllegalStateException(e);
            fail();
        }
    }

    /**
     * Define this method in subclasses to run this set of tests on different <code>ObjectStorage</code>'s 
     * @return storage to run the tests on
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    protected abstract ObjectStorage createObjectStore() throws ClassNotFoundException, SQLException;

    @Before
    public void setUp() throws ClassNotFoundException, SQLException {
        objectStore = createObjectStore();
        swap = Swap.newInstance(objectStore);
        Swap.setInstance(swap);
        objectStore.setSwap(swap);
    }

    @After
    public void tearDown() throws InterruptedException {
        fieldStorage = null;
        objectStore = null;
        swap = null;
        Swap.finishHim();
    }

    @Test(timeout = 2000)
    public void testSwapSingleValue() throws StoreException {
//        Swap swap = new Swap(objectStore);

        Bar bar = swap.wrap(new Bar("new"), Bar.class);

        bar.change("old");
        bar.change("too old");

        assertEquals("too old", bar.getValue());
    }

    @Test(timeout = 2000)
    public void testSwapList() throws StoreException {
//        Swap swap = new Swap(objectStore);

        List<Bar> list = swap.newWrapList(Bar.class);

        list.add(new Bar("1"));
        list.add(new Bar("2"));
        list.add(new Bar("3"));

        list.get(1).change("5");

    }

    @Test
    @Ignore
    public void testSimpleNestedList() throws StoreException {
        Baz root = swap.wrap(new Baz("root"), Baz.class);
        Baz inside = swap.wrap(new Baz("inside"), Baz.class);
        Baz deepInside = swap.wrap(new Baz("deepInside"), Baz.class);
        root.add(inside); // TODO call getter of swapped, add to ProxyList - test
        inside.add(deepInside);

        assertEquals(1, root.getChildren().size());
//        assertEquals("Baz{(inside)[Baz{(deepInside)}]}",
//                root.getChildren().get(0).toString());
        assertEquals("inside", root.getChildren().get(0).getValue());
        assertEquals(1, inside.getChildren().size());

        assertEquals("deepInside",
                root.getChildren().get(0).getChildren().get(0).getValue());
        assertEquals(0, deepInside.getChildren().size());
    }

    @Test
    public void testEmptySwapList() throws StoreException {
        List<Baz> list = swap.newWrapList(Baz.class);

        assertStorageContains(fieldStorage,
            at(0, ".[", clazzIs(ProxyList.class).and(elementClassIs(Baz.class)).and(recordTypeIsProxyList()))); 
    }

    @Test
    public void testEmptySwapListInsideWrapped() throws StoreException {
        Baz baz = swap.wrap(new Baz("baz"), Baz.class);

        assertStorageContains(fieldStorage,
                at(0, ".[", clazzIs(ProxyList.class).and(elementClassIs(Baz.class)).and(recordTypeIsProxyList())),

                at(1, ".", clazzIs(Baz.class).and(recordTypeIsProxiedValue())),
                at(1, "./value", valueIs("baz").and(clazzIs(String.class).and(recordTypeIsPrimitiveField()))),
                at(1, "./children", valueIsUuidStr(0).and(clazzIs(ProxyList.class).and(elementClassIs(Baz.class).and(recordTypeIsListField()))))
        );
    }

    @Test
    public void testSwapListInsideProxyAddRegular() throws StoreException {
        Baz root = new Baz("root");
        Proxy<Baz> bazProxy = new Proxy<Baz>(objectStore, root, Baz.class);

        assertStorageContains(fieldStorage,
                at(0, ".[", clazzIs(ProxyList.class).and(elementClassIs(Baz.class).and(recordTypeIsProxyList()))),

                at(1, ".", clazzIs(Baz.class).and(recordTypeIsProxiedValue())),
                at(1, "./value", valueIs("root").and(clazzIs(String.class).and(recordTypeIsPrimitiveField()))),
                at(1, "./children", valueIsUuidStr(0).and(clazzIs(ProxyList.class).and(elementClassIs(Baz.class).and(recordTypeIsListField()))))
        );

        Baz inside = new Baz("inside");
        root.add(inside);

        assertStorageContains(fieldStorage,
                at(0, ".[", clazzIs(ProxyList.class).and(elementClassIs(Baz.class).and(recordTypeIsProxyList()))),

                at(1, ".", clazzIs(Baz.class).and(recordTypeIsProxiedValue())),
                at(1, "./value", valueIs("root").and(clazzIs(String.class).and(recordTypeIsPrimitiveField()))),
                at(1, "./children", valueIsUuidStr(0).and(clazzIs(ProxyList.class).and(elementClassIs(Baz.class).and(recordTypeIsListField())))),

                at(2, ".[", clazzIs(ProxyList.class).and(elementClassIs(Baz.class).and(recordTypeIsProxyList()))),
                at(3, ".", clazzIs(Baz.class).and(recordTypeIsProxiedValue())),
                at(3, "./value", valueIs("inside").and(clazzIs(String.class).and(recordTypeIsPrimitiveField()))),
                at(3, "./children", valueIs(new UUID(0, 2).toString()).and(clazzIs(ProxyList.class).and(elementClassIs(Baz.class).and(recordTypeIsListField()))))
        ); // TODO Check what is part of what list?
    }


    @Test
    public void testSwapListInsideProxyAddWrapped() throws StoreException {
        Baz root = new Baz("root");
        Proxy<Baz> bazProxy = new Proxy<Baz>(objectStore, root, Baz.class);

        assertStorageContains(fieldStorage,
                at(0, ".[", clazzIs(ProxyList.class).and(elementClassIs(Baz.class).and(recordTypeIsProxyList()))),

                at(1, ".", clazzIs(Baz.class).and(recordTypeIsProxiedValue())),
                at(1, "./value", valueIs("root").and(clazzIs(String.class).and(recordTypeIsPrimitiveField()))),
                at(1, "./children", valueIsUuidStr(0).and(clazzIs(ProxyList.class).and(elementClassIs(Baz.class).and(recordTypeIsListField()))))
        );

        Baz inside = swap.wrap(new Baz("inside"), Baz.class);
        root.add(inside);

        assertStorageContains(fieldStorage,
                at(0, ".[", clazzIs(ProxyList.class).and(elementClassIs(Baz.class).and(recordTypeIsProxyList()))),

                at(1, ".", clazzIs(Baz.class).and(recordTypeIsProxiedValue())),
                at(1, "./value", valueIs("root").and(clazzIs(String.class).and(recordTypeIsPrimitiveField()))),
                at(1, "./children", valueIsUuidStr(0).and(clazzIs(ProxyList.class).and(elementClassIs(Baz.class).and(recordTypeIsListField())))),

                at(2, ".[", clazzIs(ProxyList.class).and(elementClassIs(Baz.class).and(recordTypeIsProxyList()))),

                at(3, ".", clazzIs(Baz.class).and(recordTypeIsProxiedValue())),
                at(3, "./value", valueIs("inside").and(clazzIs(String.class).and(recordTypeIsPrimitiveField()))),
                at(3, "./children", valueIs(new UUID(0,2).toString()).and(clazzIs(ProxyList.class).and(elementClassIs(Baz.class).and(recordTypeIsListField()))))
        ); // TODO Check what is part of what list?
    }

    @Test
    public void testSwapListInsideWrappedAddWrapped() throws StoreException {
        Baz root = swap.wrap(new Baz("root"), Baz.class);

        assertStorageContains(fieldStorage,
                at(0, ".[", clazzIs(ProxyList.class).and(elementClassIs(Baz.class).and(recordTypeIsProxyList()))),

                at(1, ".", clazzIs(Baz.class).and(recordTypeIsProxiedValue())),
                at(1, "./value", valueIs("root").and(clazzIs(String.class).and(recordTypeIsPrimitiveField()))),
                at(1, "./children", valueIsUuidStr(0).and(clazzIs(ProxyList.class)).and(elementClassIs(Baz.class)).and(recordTypeIsListField()))
        );

        Baz inside = new Baz("inside");
        root.add(inside);

        assertStorageContains(fieldStorage,
                at(0, ".[", clazzIs(ProxyList.class).and(elementClassIs(Baz.class).and(recordTypeIsProxyList()))),
                // empty list inside root (?)

                at(1, ".", clazzIs(Baz.class).and(recordTypeIsProxiedValue())),
                at(1, "./value", valueIs("root").and(clazzIs(String.class).and(recordTypeIsPrimitiveField()))),
                at(1, "./children", allOf(valueIsUuidStr(0), clazzIs(ProxyList.class), elementClassIs(Baz.class), recordTypeIsListField())),
                // root

                at(2, ".[", clazzIs(ProxyList.class).and(elementClassIs(Baz.class).and(recordTypeIsProxyList()))),
                // list containing

                at(3, ".", clazzIs(Baz.class).and(recordTypeIsProxiedValue())),
                at(3, "./value", valueIs("inside").and(clazzIs(String.class).and(recordTypeIsPrimitiveField()))),
                at(3, "./children", clazzIs(ProxyList.class).and(elementClassIs(Baz.class).and(recordTypeIsListField()))),
                // => 2

//                at(3, ".[0", valueIs("?").and(clazzIs(Baz.class).and(recordTypeIsListValue())))
                at(0, ".[0", valueIsUuidStr(3).and(clazzIs(Baz.class)).and(recordTypeIsListElement()))
                // 0 .[0 => 3
        ); // TODO Check what is part of what list?
    }


    @Test
    public void testSimplestNestedList() throws StoreException {
        Baz root = swap.wrap(new Baz("root"), Baz.class);
        Baz inside = swap.wrap(new Baz("inside"), Baz.class);
        root.add(inside);
        
        assertThat(root.getChildren(), allOf(notNullValue(), containsOneElement(inside)));
        assertThat(root.getChildren().get(0).getChildren(), isEmpty());

        assertEquals(1, root.getChildren().size());
        assertEquals("inside", root.getChildren().get(0).getValue());
        assertEquals(0, inside.getChildren().size());
    }


    @Test
    public void testRestoreProxyList() throws StoreException {
        fieldStorage.serialize(new FieldRecordBuilder(3, ".").setValue("=Baz").setClazz(Baz.class).setRecordType(PROXIED_VALUE).create());
        fieldStorage.serialize(new FieldRecordBuilder(3, "./value").setValue("inside").setClazz(String.class).setRecordType(PRIMITIVE_FIELD).create());
        fieldStorage.serialize(new FieldRecordBuilder(3, "./children").setUuidValue(2).setClazz(ProxyList.class).setElementClass(Baz.class).setRecordType(LIST_FIELD).create());
        fieldStorage.serialize(new FieldRecordBuilder(2, ".[").setValue("=ProxyList").setClazz(ProxyList.class).setElementClass(Baz.class).setRecordType(PROXY_LIST).create());
        fieldStorage.serialize(new FieldRecordBuilder(1, ".").setValue("=Baz").setClazz(Baz.class).setRecordType(PROXIED_VALUE).create());
        fieldStorage.serialize(new FieldRecordBuilder(1, "./value").setValue("root").setClazz(String.class).setRecordType(PRIMITIVE_FIELD).create());
        fieldStorage.serialize(new FieldRecordBuilder(1, "./children").setUuidValue(0).setClazz(ProxyList.class).setElementClass(Baz.class).setRecordType(LIST_FIELD).create());
        fieldStorage.serialize(new FieldRecordBuilder(0, ".[").setValue("=ProxyList").setClazz(ProxyList.class).setElementClass(Baz.class).setRecordType(PROXY_LIST).create());
        fieldStorage.serialize(new FieldRecordBuilder(0, ".[0").setUuidValue(3).setClazz(Baz.class).setElementClass(Baz.class).setRecordType(LIST_ELEMENT).create());

        Baz restoredRoot = objectStore.reStore(new UUID(0, 1), Baz.class);
        assertThat(restoredRoot.getChildren(), allOf(notNullValue(), containsOneElement()));
        assertThat(restoredRoot.getChildren().get(0).getChildren(), isEmpty());
    }


    @Test
//        @Ignore
    public void testNestedList() throws StoreException {
        Baz root = swap.wrap(new Baz("/"), Baz.class);
        Baz c1 = swap.wrap(new Baz("c1"), Baz.class);
        Baz c2 = swap.wrap(new Baz("c2"), Baz.class);
        Baz c3 = swap.wrap(new Baz("c3"), Baz.class);
        Baz c11 = swap.wrap(new Baz("c11"), Baz.class);
        Baz c12 = swap.wrap(new Baz("c12"), Baz.class);
        root.add(c1);
        root.add(c2);
        root.add(c3);
        c1.add(c11);
        c1.add(c12);

        assertEquals(3, root.getChildren().size());
        assertEquals(2, root.getChildren().get(0).getChildren().size());
    }

    @Test
    public void testPrimitiveNull() throws StoreException {
        // TODO This test fails if testBigSwapList() and testSwapSet() are run before it. Why?
        Bar bar = Swap.doWrap(new Bar(null), Bar.class);

        assertStorageContains(fieldStorage,
                at(0, ".", clazzIs(Bar.class).and(recordTypeIsProxiedValue())),
                at(0, "./value", valueIs(null).and(clazzIs(String.class).and(recordTypeIsPrimitiveField())))
        );
    }

    @Test
    @Ignore
    public void testBigSwapList() throws StoreException {
//        Swap swap = new Swap(objectStore);
        List<Bar> list = swap.newWrapList(Bar.class);

        for (int i = 0; i < 10000; i++) {
            list.add(new Bar(String.valueOf(i)));
        }

        assertEquals("555", list.get(555).getValue());
        assertEquals("1", list.get(1).getValue());
        assertEquals("9999", list.get(9999).getValue());

        list.get(555).change("_555_");

        assertEquals("_555_", list.get(555).getValue());
    }

    @Test
    public void testSwapSet() {
//        Swap swap = new Swap(objectStore);

        Set<Bar> set = swap.newWrapSet(Bar.class);

        set.add(new Bar("1"));
        set.add(new Bar("1"));
        set.add(new Bar("3"));

        set.iterator().next().change("5");
    }

    public static class Bar {

        String value = "new";

        public Bar(String value) {
            this.value = value;
        }

        public Bar() {
        }

        public void change(String change) {
            value = change;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "Bar{" + "value=" + value + '}';
        }
    }
}
