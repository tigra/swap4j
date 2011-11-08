/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.illarion.swap4j;

import com.github.illarion.swap4j.store.StorageException;
import com.github.illarion.swap4j.store.scan.MapWriter;
import com.github.illarion.swap4j.store.scan.TestObjectScannerObjectStorage;
import com.github.illarion.swap4j.swap.ProxyUtils;
import com.github.illarion.swap4j.swap.RandomUuidGenerator;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.internal.matchers.TypeSafeMatcher;
import org.junit.rules.TemporaryFolder;
import com.github.illarion.swap4j.store.ObjectStorage;
import com.github.illarion.swap4j.swap.Swap;

import java.util.UUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static com.github.illarion.swap4j.CustomAssertions.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * @author shaman
 */
public class StoreSingleObjectTest {
    private MapWriter fieldStorage;

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
        fieldStorage = new MapWriter();
        objectStore = new TestObjectScannerObjectStorage(null, fieldStorage, new RandomUuidGenerator());
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
    public void testNestedWithNullInside() throws StorageException {
//        A a = swap.wrap(new A(), A.class);
        B b = swap.wrap(new B(null, "B"), B.class);
    }

    @Test
    public void testStoreRestoreNestedNullProxy() throws StorageException {
        Foo foo = swap.wrap(new Foo("FOO", null), Foo.class);
        assertThat(foo.getBar(), equalTo("FOO"));
        assertThat(foo.getNestedFoo(), nullValue());
    }

    @Test
    public void testStoreRestoreNestedObject() throws Exception {
        // setup
        Foo nested = swap.wrap(new Foo("2", null), Foo.class);
        Foo foo = swap.wrap(new Foo("1", nested), Foo.class);
        // excersize
        Foo nestedActual = foo.getNestedFoo();
        // verify
        assertThat(nestedActual, allOf(notNullValue(), isWrapped()));
        assertThat(nestedActual, getterValue("nestedFoo", nullValue()));
        assertThat(nestedActual, getterValue("bar", equalTo("2")));
    }


    private <T> Matcher<T> isWrapped() {
        return new TypeSafeMatcher<T>() {
            public StorageException exceptionThrown = null;

            @Override
            public boolean matchesSafely(T item) {
                try {
                    ProxyUtils.getProxy(item);
                    return true;
                } catch (StorageException e) {
                    exceptionThrown = e;
                    return false;
                }
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("object is enhanced proxy");
            }

            @Override
            public void describeMismatch(Object item, Description description) {
                description.appendText("object of class ").appendValue(item.getClass().getSimpleName())
                        .appendText(" is not enhanced Proxy, exception was thrown: ").appendValue(exceptionThrown);
            }
        };
    }

    @Test
    public void testNestedList() throws StorageException {
        Baz root = swap.wrap(new Baz("/"), Baz.class);
        Baz c1 = new Baz("c1");
        Baz c2 = new Baz("c2");
        Baz c3 = new Baz("c3");
        Baz c11 = new Baz("c11");
        Baz c12 = new Baz("c12");
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
