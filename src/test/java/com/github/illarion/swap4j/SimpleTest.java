/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.illarion.swap4j;

import com.github.illarion.swap4j.store.ObjectStorage;
import com.github.illarion.swap4j.store.StoreException;

import java.util.*;

import com.github.illarion.swap4j.store.scan.MapWriter;
import com.github.illarion.swap4j.store.scan.TestObjectScannerObjectStorage;
import com.github.illarion.swap4j.swap.*;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author shaman
 */
public class SimpleTest {
    private Swap swap;

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

    private ObjectStorage objectStore;
//    private Store store = new Store() {
//
//        private Map<UUID, Object> map = new HashMap<UUID, Object>();
//        public UUIDGenerator uuidGenerator = new UUIDGenerator();
//
//        @Override
//        public SerializedField getSerializedField(Locator locator) {
//            throw new UnsupportedOperationException(""); // TODO Implement this method
//        }
//
//        @Override
//        public <T> void store(UUID id, T t) {
////            System.out.println("Storing " + t.toString());
//            map.put(id, t);
//        }
//
//        @Override
//        public <T> T reStore(UUID id, Class<T> clazz) throws StoreException {
////            System.out.println("Restoring something by id = " + id);
//            try {
//                return (T) map.get(id);
//            } catch (ClassCastException cce) {
//                throw new StoreException("SimpleTest.Store.reStore(" + id + "," + clazz, cce);
//            }
//        }
//
//        public UUID createUUID() {
//            return uuidGenerator.createUUID();
//        }
//
//        @Override
//        public SerializedField deserialize(UUID id) {
//            throw new UnsupportedOperationException("ST.deserialize"); // TODO Implement this method
//        }
//
//        @Override
//        public Iterator<Locator> iterator() {
//            throw new UnsupportedOperationException(""); // TODO Implement this method
//        }
//    };

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

    @Before
    public void setUp() {
        objectStore = new TestObjectScannerObjectStorage(swap, new MapWriter(), new UUIDGenerator());
        swap = new Swap(objectStore);
    }

    @Test
    public void testNestedList() throws StoreException {
        Baz root = swap.wrap(new Baz(swap, "/"), Baz.class);
        Baz c1 = new Baz(swap, "c1");
        Baz c2 = new Baz(swap, "c2");
        Baz c3 = new Baz(swap, "c3");
        Baz c11 = new Baz(swap, "c11");
        Baz c12 = new Baz(swap, "c12");
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
}
