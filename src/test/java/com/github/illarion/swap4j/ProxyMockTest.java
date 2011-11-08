package com.github.illarion.swap4j;

import com.github.illarion.swap4j.store.StorageException;
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
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

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
    public void testToStringDoesntTouchRealObject() throws StorageException {
        when(uuidGenerator.createUUID()).thenReturn(new UUID(0,0));
//        final Object realObject = mock(Object.class);
//        when(realObject.toString()).thenReturn("(RealObject)").thenThrow(new AssertionError("toString() of real object haven't be called"));
        final Object realObject = new Object() {
            public boolean firstInvocation = true;
            @Override
            public String toString() {
                if (firstInvocation) {
                    firstInvocation = false;
                    return "you can call toString during wrapping";
                } else {
                    throw new AssertionError("toString() of real object haven't be called");
                }
            }
        };
        final Object wrapped = Swap.doWrap(realObject, Object.class);

        assertThat(wrapped.toString(), equalTo("E{Proxy{id=0-0, c=Object}}"));
//        verify(realObject, never()).toString();
    }
}
