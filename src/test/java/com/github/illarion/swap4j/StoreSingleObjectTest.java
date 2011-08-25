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
        private Foo nestedFoo;

        public Foo() {
        }

        public void setBar(String bar) {
            this.bar = bar;
        }


        public Foo(String bar, Foo nestedFoo) {
            this.bar = bar;
            this.nestedFoo = nestedFoo;
        }

        public String getBar() {
            return bar;
        }

        @Override
        public String toString() {
            return "Foo{" + "bar=" + bar + ", nestedFoo=" + nestedFoo + '}';
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

        public Foo getNestedFoo() {
            return nestedFoo;
        }
        
        
    }
    private Store store;
    private Swap swap;
    
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        store = new SimpleStore(testFolder.newFolder("temp"));
        swap = new Swap(store);
    }

    @Test
    public void testStoreRestoreSingleObject() throws Exception {
        
        Foo foo = swap.wrap(new Foo("1", null), Foo.class);
        
        assertEquals("1", foo.getBar());
    }

    @Test
    public void testStoreRestoreNestedObject() throws Exception {
        Foo nested = swap.wrap(new Foo("2", null), Foo.class);

        Foo foo = swap.wrap(new Foo("1", nested), Foo.class);

        Foo nestedActual = foo.getNestedFoo();
        
        assertNotNull(nestedActual);
        
        assertEquals("2", nestedActual.getBar());
    }
    
    @Test
    public void storeTest() throws Exception {
        Foo foo = swap.wrap(new Foo("1", null), Foo.class);
        store.store(UUID.fromString("00000000-0000-0000-0000-000000000000"), foo);
    }
}
