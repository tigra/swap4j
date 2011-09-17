package com.github.illarion.swap4j.swap;

import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

import static junit.framework.Assert.assertEquals;

/**
 * TODO Describe class
 * <p/>
 * <p/>
 * Created at: Sep 16, 2011 2:12:00 PM
 *
 * @author Alexey Tigarev
 */
@RunWith(JMock.class)
public class UUIDGeneratorTest {
    private Mockery context = new JUnit4Mockery() {{
            setImposteriser(ClassImposteriser.INSTANCE);
    }};

    @Test
    public void testSpecificUUIDs() {
        final UUIDGenerator uuidGenerator = context.mock(UUIDGenerator.class);

        context.checking(new UUIDSequenceExpectations(context, uuidGenerator) {{
            expectUUID("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
            expectUUID("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
            expectUUID("cccccccc-cccc-cccc-cccc-cccccccccccc");
        }});

        assertEquals("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa", uuidGenerator.createUUID().toString());
        assertEquals("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb", uuidGenerator.createUUID().toString());
        assertEquals("cccccccc-cccc-cccc-cccc-cccccccccccc", uuidGenerator.createUUID().toString());
    }

    @Test
    public void testSequentalUUIDs() {
        final UUIDGenerator uuidGenerator = context.mock(UUIDGenerator.class);

        context.checking(new UUIDSequenceExpectations(context, uuidGenerator) {{
            expectSequentalUUIDs(5);
        }});

        assertEquals(new UUID(0, 0), uuidGenerator.createUUID());
        assertEquals(new UUID(0, 1), uuidGenerator.createUUID());
        assertEquals(new UUID(0, 2), uuidGenerator.createUUID());
        assertEquals(new UUID(0, 3), uuidGenerator.createUUID());
        assertEquals(new UUID(0, 4), uuidGenerator.createUUID());
    }
}


