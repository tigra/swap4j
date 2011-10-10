package com.github.illarion.swap4j.swap;

import com.github.illarion.swap4j.store.StoreException;
import com.github.illarion.swap4j.store.scan.MapWriter;
import com.github.illarion.swap4j.store.scan.TestObjectScannerObjectStorage;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * TODO Describe class
 * <p/>
 * <p/>
 * Created at: Sep 25, 2011 10:26:49 PM
 *
 * @author Alexey Tigarev
 */
public class ProxyTest {
    private TestObjectScannerObjectStorage storage;
    private Swap swap;

    @Before
    public void setUp() throws Exception {
        storage = new TestObjectScannerObjectStorage(null, new MapWriter(), new RandomUuidGenerator());
        swap = new Swap(storage);
        storage.setSwap(swap);
    }

    static class A {
        private String field;

        public A() {
        }

        A(String field) {
            this.field = field;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof A)) return false;

            A a = (A) o;

            if (field != null ? !field.equals(a.getField()) : a.getField() != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return field != null ? field.hashCode() : 0;
        }

        public String getField() {
            return field;
        }
    }
    @Test
    public void testEquals() throws StoreException {
//        sleep();

        // setup
        A a = new A("a");
//        final TestObjectScannerObjectStorage storage = new TestObjectScannerObjectStorage(null, new MapWriter(), new UUIDGenerator());
//        Swap swap = new Swap(storage);
//        storage.setSwap(swap);

        // excersize
        A aWrapped = swap.wrap(a, A.class);

        // verify
        assertEquals(aWrapped, a);
        assertEquals(a, aWrapped);
    }

    private void sleep() {
        try {
            Thread.sleep(10*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testUnloadLoad() throws StoreException {
        // setup
        A realObject = new A("a");

        // excersize
        Proxy<A> proxyA = new Proxy<A>(storage, realObject, A.class); // it unloads itself immediately
        proxyA.load();

        // verify
        assertTrue(proxyA.isLoaded());
        assertEquals(new A("a"), proxyA.getRealObject());
    }

    @Test
//    @Ignore("let testEquals run alone")
    public void testUnloadGet() throws StoreException {
        // setup
        A realObject = new A("a");

        // excersize
        Proxy<A> proxyA = new Proxy<A>(storage, realObject, A.class); // it unloads itself immediately
        A aWrapped = proxyA.get();

        // verify
        assertTrue(proxyA.isLoaded());
        assertEquals("a", aWrapped.getField());
        assertEquals(new A("a"), aWrapped);
    }

    @Test
    public void testConstructorUnloads() throws StoreException {
        // setup
//        final TestObjectScannerObjectStorage storage = new TestObjectScannerObjectStorage(null, new MapWriter(), new UUIDGenerator());
//        Swap swap = new Swap(storage);
//        storage.setSwap(swap);

        // excersize
        Proxy<A> proxyA = new Proxy<A>(storage, new A("a"), A.class); // it unloads itself immediately

        // verify
        assertFalse("Proxy should unload itself immediately", proxyA.isLoaded());
    }
}
