package com.github.illarion.swap4j.swap;

import com.github.illarion.swap4j.Bar;
import com.github.illarion.swap4j.Baz;
import com.github.illarion.swap4j.SequentalUUIDGenerator;
import com.github.illarion.swap4j.store.StoreException;
import com.github.illarion.swap4j.store.scan.MapWriter;
import com.github.illarion.swap4j.store.scan.TestObjectScannerObjectStorage;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.StringDescription;
import org.junit.Before;
import org.junit.Test;

import static com.github.illarion.swap4j.CustomAssertions.containsOneElement;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThat;

/**
 * TODO Describe class
 *
 * @author Alexey Tigarev tigra@agile-algorithms.com
 */
public class ProxyListTest {
    private TestObjectScannerObjectStorage objectStorage;
    private Swap swap;

    @Before
    public void setUp() {
        objectStorage = new TestObjectScannerObjectStorage(null, new MapWriter(), new SequentalUUIDGenerator());
        swap = Swap.newInstance(objectStorage);
    }
    @Test
    public void testEqualsEmptyLists() throws StoreException {
        assertThat(Swap.proxyList(Baz.class), equalTo(Swap.proxyList(Baz.class)));
    }

    @Test
    public void testDoubleLoadEmpty() throws StoreException {
        ProxyList<Bar> list = (ProxyList<Bar>)Swap.proxyList(Bar.class);
        list.load();
        assertThat(list.size(), equalTo(0));
        list.load();
        assertThat(list.size(), equalTo(0));
    }

    @Test
    public void testLoad() throws StoreException {
        ProxyList<Bar> list = (ProxyList<Bar>)Swap.proxyList(Bar.class);
        list.load();
        assertThat(list.size(), equalTo(0));
    }

    @Test
    public void testLoad1() throws StoreException {
        ProxyList<Bar> list = (ProxyList<Bar>)Swap.proxyList(Bar.class);
        list.add(new Bar("element"));
        assertThat(list.size(), equalTo(1));
        assertThat(list, hasItem(new Bar("element")));
    }

    @Test
    public void testLoad2() throws StoreException {
        ProxyList<Bar> list = (ProxyList<Bar>)Swap.proxyList(Bar.class);
        list.unload();
        list.add(new Bar("element"));
        assertThat(list.size(), equalTo(1));
        assertThat(list, hasItem(new Bar("element")));
    }

    @Test
    public void testContains() throws StoreException {
        ProxyList<Bar> list = (ProxyList<Bar>)Swap.proxyList(Bar.class);
        list.add(new Bar("element"));
        assertTrue("List should contain element that was inserted there", list.contains(new Bar("element")));
    }

    @Test
    public void testUnloadedContains() throws StoreException {
        ProxyList<Bar> list = (ProxyList<Bar>)Swap.proxyList(Bar.class);
        list.add(new Bar("element"));
        list.unload();
        assertTrue("List should contain element that was inserted there", list.contains(new Bar("element")));
    }

    @Test
    public void testLoad3() throws StoreException {
        ProxyList<Bar> list = (ProxyList<Bar>)Swap.proxyList(Bar.class);
        list.add(new Bar("element"));
        list.unload();
        assertThat(list.size(), equalTo(1));
        MatcherAssert.assertThat(list, containsOneElement());
        MatcherAssert.assertThat(list, containsOneElement(new Bar("element")));
    }

    private <T> void assertThat2(T item, Matcher<T> matcher) {
        if (!matcher.matches(item)) {
            Description description = new StringDescription();
            matcher.describeMismatch(item, description);
            fail(description.toString());
        }
    }

    @Test
    public void testDoubleLoadOneElement() throws StoreException {
        ProxyList<Bar> list = (ProxyList<Bar>)Swap.proxyList(Bar.class);
        list.add(new Bar("onlyElement"));
        assertThat(list.size(), equalTo(1));
        list.load();
        assertThat(list.size(), equalTo(1));
        list.load();
        assertThat(list.size(), equalTo(1));
    }


}
