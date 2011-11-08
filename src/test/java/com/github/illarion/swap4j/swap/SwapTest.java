package com.github.illarion.swap4j.swap;

import com.github.illarion.swap4j.SequentalUUIDGenerator;
import com.github.illarion.swap4j.store.ObjectStorage;
import com.github.illarion.swap4j.store.StorageException;
import com.github.illarion.swap4j.store.scan.FieldStorage;
import com.github.illarion.swap4j.store.scan.MapWriter;
import com.github.illarion.swap4j.store.scan.TestObjectScannerObjectStorage;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
 * TODO Describe class
 *
 * @author Alexey Tigarev tigra@agile-algorithms.com
 */
public class SwapTest {
    private Swap swap;
    private FieldStorage fieldStorage;
    private ObjectStorage objectStorage;

    public static class A {
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

    @Before
    public void setUp() {
        fieldStorage = new MapWriter();
        objectStorage = new TestObjectScannerObjectStorage(null, fieldStorage, new SequentalUUIDGenerator());
        swap = Swap.newInstance(objectStorage);
        Swap.setInstance(swap);
        objectStorage.setSwap(swap);
    }

    @Test
    public void testWrap() throws StorageException {
        A a = new A("a");
        A aWrapped = swap.wrap(a, A.class);
        assertThat(aWrapped, instanceOf(A.class));
        assertThat(aWrapped, instanceOf(SwapPowered.class));
        Object realObject = ((SwapPowered)aWrapped).getRealObject();
        assertThat(realObject, equalTo((Object)a));
//        assertThat(realObject, sameInstance((Object)a));
    }

    @Test
    public void testSwapWrapsOnlyOnce() throws StorageException {
        // setup
        A a = new A("A");
        A aWrapped = swap.wrap(a, A.class);

        // excersize
        A doubleWrapped = swap.wrap(aWrapped, A.class);

        // verify
        assertThat(doubleWrapped, instanceOf(A.class));
        assertThat(doubleWrapped, instanceOf(SwapPowered.class));
        Object realObject = ((SwapPowered)doubleWrapped).getRealObject();
        assertThat("Should equal object as if when we single-wrap it",
                realObject, equalTo((Object)a));
//        assertThat("Should get same object as if when we single-wrap it", realObject, sameInstance((Object)a));
    }

    @Test
    public void testWrapTransparentForNulls() throws StorageException {
        assertEquals(null, swap.wrap(null, A.class));
    }
}
