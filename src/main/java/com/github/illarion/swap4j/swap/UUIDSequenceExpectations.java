package com.github.illarion.swap4j.swap;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;

import java.util.UUID;

/**
* JMock's "Expectations" extension allowing to have UUIDs generated in known sequence (instead of random sequence).
*
* @author Alexey Tigarev
*/
public class UUIDSequenceExpectations extends Expectations {
    private Sequence uuidSequence;
    private UUIDGenerator uuidGenerator;

    public UUIDSequenceExpectations(UUIDGenerator uuidGenerator, Sequence uuidSequence) {
        this.uuidGenerator = uuidGenerator;
        this.uuidSequence = uuidSequence;
    }

    public UUIDSequenceExpectations(Mockery mockery) {
        this.uuidGenerator = mockery.mock(UUIDGenerator.class);
        this.uuidSequence = createSequence(mockery);
    }

    private Sequence createSequence(Mockery mockery) {
        return mockery.sequence("uuid");
    }

    public UUIDSequenceExpectations(Mockery mockery, UUIDGenerator uuidGenerator) {
        this.uuidGenerator = uuidGenerator;
        this.uuidSequence = createSequence(mockery);

    }

    public void expectUUID(String uuidStr) {
        expectUUID(UUID.fromString(uuidStr));
    }

    public void expectSequentalUUIDs(int count) {
        for (int i = 0; i < count; i++) {
            expectUUID(new UUID(0, i));
        }
    }

    public void expectUUID(UUID result) {
        one(uuidGenerator).createUUID();
        will(returnValue(result));
        inSequence(uuidSequence);
    }
}
