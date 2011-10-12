/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.illarion.swap4j;

import com.github.illarion.swap4j.store.StoreException;
import com.github.illarion.swap4j.store.scan.MapWriter;
import com.github.illarion.swap4j.store.scan.TestObjectScannerObjectStorage;
import com.github.illarion.swap4j.swap.RandomUuidGenerator;
import org.junit.rules.TemporaryFolder;
import com.github.illarion.swap4j.store.ObjectStorage;
import com.github.illarion.swap4j.swap.Swap;

import java.util.UUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author shaman
 */
public class StoreSingleObjectTest {

    public static class Foo {
        private String bar;
        private Foo nestedFoo;

        public Foo() {
        }

        public Foo(String bar, Foo nestedFoo) {
            this.bar = bar;
            this.nestedFoo = nestedFoo;
        }

        public String getBar() {
            return bar;
        }

        public void setBar(String bar) {
            this.bar = bar;
        }

        public Foo getNestedFoo() {
            return nestedFoo;
        }

        @Override
        public String toString() {
            return "Foo{" + "bar=" + bar + ", nestedFoo=" + nestedFoo + '}';
        }

        @SuppressWarnings({"RedundantIfStatement"})
        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Foo other = (Foo) obj;
            if ((this.bar == null) ? (other.bar != null) : !this.bar.equals(other.bar)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 61 * hash + (this.bar != null ? this.bar.hashCode() : 0);
            return hash;
        }
    }

    public static class A {
        String field = "A";
    }

    public static class B {
        String field = "B";
        A a = null;

        public B(A a, String f) {
            this.a = a;
            this.field = f;
        }

        public B() {
        }
    }

    private ObjectStorage objectStore;
    private Swap swap;
    
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
//        store = new SimpleStore(testFolder.newFolder("temp"));
        objectStore = new TestObjectScannerObjectStorage(null, new MapWriter(), new RandomUuidGenerator());
        swap = Swap.newInstance(objectStore);
        Swap.setInstance(swap);
        objectStore.setSwap(swap);
    }

    @Test
    public void testStoreRestoreSingleObject() throws Exception {
        Foo foo = swap.wrap(new Foo("1", null), Foo.class);
        assertEquals("1", foo.getBar());
    }


    @Test
    public void testNestedWithNullInside() throws StoreException {
//        A a = swap.wrap(new A(), A.class);
        B b = swap.wrap(new B(null, "B"), B.class);
    }

    @Test
    public void testStoreRestoreNestedNullProxy() throws StoreException {
        Foo foo = swap.wrap(new Foo("FOO", null), Foo.class);
        assertEquals(null, foo.getNestedFoo());
        assertEquals("FOO", foo.getBar());
    }

    @Test
    public void testStoreRestoreNestedObject() throws Exception {
        // setup
        Foo nested = swap.wrap(new Foo("2", null), Foo.class);
        Foo foo = swap.wrap(new Foo("1", nested), Foo.class);
        // excersize
        Foo nestedActual = foo.getNestedFoo();
        // verify
        assertNotNull(nestedActual);
        assertEquals("2", nestedActual.getBar());
        assertEquals(null, nestedActual.getNestedFoo());
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
    public void storeTest() throws Exception {
        Foo foo = swap.wrap(new Foo("1", null), Foo.class);
        objectStore.store(UUID.fromString("00000000-0000-0000-0000-000000000000"), foo);
    }
}
