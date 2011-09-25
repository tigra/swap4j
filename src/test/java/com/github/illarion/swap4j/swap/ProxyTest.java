package com.github.illarion.swap4j.swap;

import com.github.illarion.swap4j.store.StoreException;
import com.github.illarion.swap4j.store.scan.MapWriter;
import com.github.illarion.swap4j.store.scan.TestObjectScannerStore;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * TODO Describe class
 * <p/>
 * <p/>
 * Created at: Sep 25, 2011 10:26:49 PM
 *
 * @author Alexey Tigarev
 */
public class ProxyTest {
    static class A {
        String field;

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

            if (field != null ? !field.equals(a.field) : a.field != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return field != null ? field.hashCode() : 0;
        }
    }
    @Test
    public void testEquals() throws StoreException {
        // setup
        A a = new A("a");
        final TestObjectScannerStore store = new TestObjectScannerStore(null, new MapWriter(), new UUIDGenerator());
        Swap swap = new Swap(store);
        store.setSwap(swap);

        // excersize
        A aWrapped = swap.wrap(a, A.class);

        // verify
        assertEquals(aWrapped, a);
        assertEquals(a, aWrapped);
    }
}
