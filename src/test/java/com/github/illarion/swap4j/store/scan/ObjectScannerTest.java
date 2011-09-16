package com.github.illarion.swap4j.store.scan;

import com.github.illarion.swap4j.store.StoreException;
import com.github.illarion.swap4j.store.simplegsonstore.SimpleStore;
import com.github.illarion.swap4j.swap.ProxyList;
import com.github.illarion.swap4j.swap.Swap;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

/**
 * TODO Describe class
 * <p/>
 * <p/>
 * Created at: Sep 16, 2011 1:53:08 AM
 *
 * @author Alexey Tigarev
 */
@RunWith(JMock.class)
public class ObjectScannerTest {
    private Mockery context = new JUnit4Mockery() {{
            setImposteriser(ClassImposteriser.INSTANCE);
    }};

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();
    
    private SimpleStore store;
    private Swap swap;

    @Before
    public void setUp() throws StoreException {
        store = new SimpleStore(testFolder.newFolder("temp"));
        swap = new Swap(store);
    }

    @Test
    public void testString() {
        final ObjectSerializer writer = context.mock(ObjectSerializer.class);
        ObjectScanner scanner = new ObjectScanner(writer);

        context.checking(new Expectations() {{
            one(writer).serialize(new Atom("hello", String.class));
        }});

        scanner.scan("hello");
    }

    static class Dummy {
        String field = null;

        Dummy() {
        }

        Dummy(String field) {
            this.field = field;
        }
    }

    @Test
    public void testList() {
        final ObjectSerializer writer = context.mock(ObjectSerializer.class);
        ObjectScanner scanner = new ObjectScanner(writer);

        context.checking(new Expectations() {{
            one(writer).serialize(with(any(SerializedList.class)));
        }});

        ProxyList<Dummy> list = new ProxyList<Dummy>(swap, Dummy.class);
        list.add(new Dummy("one"));
        list.add(new Dummy("two"));
        list.add(new Dummy("three"));

        scanner.scan(list);
    }

}
