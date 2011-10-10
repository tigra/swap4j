package com.github.illarion.swap4j;

import com.github.illarion.swap4j.store.ObjectStorage;
import com.github.illarion.swap4j.store.StoreException;
import com.github.illarion.swap4j.store.scan.FieldStorage;
import com.github.illarion.swap4j.swap.ProxyList;
import com.github.illarion.swap4j.swap.Swap;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import static com.github.illarion.swap4j.CustomAssertions.*;
import static com.github.illarion.swap4j.CustomAssertions.elementClassIs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * TODO Describe class
 *
 * @author Alexey Tigarev tigra@agile-algorithms.com
 */
public abstract class AbstractSimpleTest {
    protected Swap swap;
    protected ObjectStorage objectStore;
    protected FieldStorage fieldStorage;

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
        swap = new Swap(objectStore);
        objectStore.setSwap(swap);
    }

    @Test
    public void testSwapSingleValue() throws StoreException {
        Swap swap = new Swap(objectStore);

        Bar bar = swap.wrap(new Bar("new"), Bar.class);

        bar.change("old");
        bar.change("too old");

        assertEquals("too old", bar.getValue());
    }

    @Test
    public void testSwapList() throws StoreException {
        Swap swap = new Swap(objectStore);

        List<Bar> list = swap.newWrapList(Bar.class);

        list.add(new Bar("1"));
        list.add(new Bar("2"));
        list.add(new Bar("3"));

        list.get(1).change("5");

    }

    @Test
    public void testSimpleNestedList() throws StoreException {
        Baz root = swap.wrap(new Baz(swap, "root"), Baz.class);
        Baz inside = swap.wrap(new Baz(swap, "inside"), Baz.class);
        Baz deepInside = swap.wrap(new Baz(swap, "deepInside"), Baz.class);
        root.add(inside);
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
    public void testEmptyProxyList() throws StoreException {
        List<Baz> list = swap.newWrapList(Baz.class);

        assertStorageContains(fieldStorage,
            at(0, ".[", clazzIs(ProxyList.class).and(elementClassIs(Baz.class)).and(recordTypeIsProxyList()))); 
    }

    @Test
    public void testSimplestNestedList() throws StoreException {
        Baz root = swap.wrap(new Baz(swap, "root"), Baz.class);
        Baz inside = swap.wrap(new Baz(swap, "inside"), Baz.class);
        root.add(inside);

        assertEquals(1, root.getChildren().size());
        assertEquals("inside", root.getChildren().get(0).getValue());
        assertEquals(0, inside.getChildren().size());
    }

    @Test
    public void testNestedList() throws StoreException {
        Baz root = swap.wrap(new Baz(swap, "/"), Baz.class);
        Baz c1 = swap.wrap(new Baz(swap, "c1"), Baz.class);
        Baz c2 = swap.wrap(new Baz(swap, "c2"), Baz.class);
        Baz c3 = swap.wrap(new Baz(swap, "c3"), Baz.class);
        Baz c11 = swap.wrap(new Baz(swap, "c11"), Baz.class);
        Baz c12 = swap.wrap(new Baz(swap, "c12"), Baz.class);
        root.add(c1);
        root.add(c2);
        root.add(c3);
        c1.add(c11);
        c1.add(c12);

        assertEquals(3, root.getChildren().size());
        assertEquals(2, root.getChildren().get(0).getChildren().size());
    }

    @Test
    public void testBigSwapList() throws StoreException {
        Swap swap = new Swap(objectStore);
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
        Swap swap = new Swap(objectStore);

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
