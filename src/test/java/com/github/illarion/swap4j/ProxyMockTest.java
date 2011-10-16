package com.github.illarion.swap4j;

import com.github.illarion.swap4j.store.StoreException;
import com.github.illarion.swap4j.store.scan.MapWriter;
import com.github.illarion.swap4j.store.scan.TestObjectScannerObjectStorage;
import com.github.illarion.swap4j.swap.Swap;
import com.github.illarion.swap4j.swap.UUIDGenerator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * TODO Describe class
 *
 * @author Alexey Tigarev tigra@agile-algorithms.com
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest()
public class ProxyMockTest {
    private UUIDGenerator uuidGenerator;
//    private Mockery context = new JUnit4Mockery() {{
//            setImposteriser(ClassImposteriser.INSTANCE);
//    }};
    private TestObjectScannerObjectStorage store;
    private Swap swap;

    @Before
    public void setUp() {
//        uuidGenerator = context.mock(RandomUuidGenerator.class);
        uuidGenerator = mock(UUIDGenerator.class);
        store = new TestObjectScannerObjectStorage(null, new MapWriter(), uuidGenerator);
        swap = Swap.newInstance(store);
        Swap.setInstance(swap);
        store.setSwap(swap);
    }

    @Test
    public void testToStringDoesntTouchRealObject() throws StoreException {
        expect(uuidGenerator.createUUID()).thenReturn(new UUID(0,0));
        final AbstractSimpleTest.Bar realObject = mock(AbstractSimpleTest.Bar.class);
        final AbstractSimpleTest.Bar wrapped = Swap.doWrap(realObject, AbstractSimpleTest.Bar.class);


        assertThat(wrapped.toString(), equalTo("E{Proxy{id=0-0, c=Bar}}"));
        verify(realObject, never()).toString();
    }
}
