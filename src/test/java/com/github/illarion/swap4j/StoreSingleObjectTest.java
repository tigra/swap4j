/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.illarion.swap4j;

import com.github.illarion.swap4j.swap.Proxy;
import org.junit.rules.TemporaryFolder;
import com.github.illarion.swap4j.store.simplegsonstore.SimpleStore;
import com.github.illarion.swap4j.store.Store;
import com.github.illarion.swap4j.swap.Swap;
import java.io.File;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author shaman
 */
public class StoreSingleObjectTest {

    public static class Foo {

        private String bar;
        private Proxy<Foo> nestedFoo;

        public Foo() {
        }

        public void setBar(String bar) {
            this.bar = bar;
        }


        public Foo(String bar, Proxy<Foo> nestedFoo) {
            this.bar = bar;
            this.nestedFoo = nestedFoo;
        }

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
    private Store store;
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        store = new SimpleStore(testFolder.newFolder("temp"));
    }

    @Test
    public void testStoreRestore() throws Exception {
        Foo foo = new Foo("1", null);
        UUID id = UUID.randomUUID();

        store.store(id, foo);

        Foo baz = store.reStore(id, Foo.class);

        assertEquals(foo, baz);
    }

    @Test
    public void testStoreRestoreObjectWithCglibProxy() throws Exception {
        Swap swap = new Swap(store);

        Proxy<Foo> nested = swap.wrap(new Foo("2", null), Foo.class);

        Foo foo = new Foo("1", nested);

        UUID id = UUID.randomUUID();

        store.store(id, foo);

        Foo baz = store.reStore(id, Foo.class);

        assertEquals(foo, baz);
        
        Foo get = baz.nestedFoo.get();
        

    }
}
