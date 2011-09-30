package com.github.illarion.swap4j.swap;

import com.github.illarion.swap4j.store.scan.FieldStorage;
import com.github.illarion.swap4j.store.scan.SerializedField;
import com.github.illarion.swap4j.store.scan.TYPE;
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
    private Sequence serializationSequence;
    private UUIDGenerator uuidGenerator;
    private FieldStorage objectSerializer;

    public UUIDSequenceExpectations(UUIDGenerator uuidGenerator, Sequence uuidSequence, FieldStorage objectSerializer) {
        this.uuidGenerator = uuidGenerator;
        this.uuidSequence = uuidSequence;
        this.objectSerializer = objectSerializer;
    }

    public UUIDSequenceExpectations(UUIDGenerator uuidGenerator, Mockery mockery) {
        this.uuidGenerator = uuidGenerator;
        this.uuidSequence = createSequence(mockery, "uuid");
        this.serializationSequence = createSequence(mockery, "serialization");
    }

    public UUIDSequenceExpectations(Mockery mockery) {
        this.uuidGenerator = mockery.mock(UUIDGenerator.class);
        this.uuidSequence = createSequence(mockery, "uuid");
        this.serializationSequence = createSequence(mockery, "serialization");
    }


    public UUIDSequenceExpectations(Mockery mockery, UUIDGenerator uuidGenerator, FieldStorage objectSerializer) {
        this.uuidGenerator = uuidGenerator;
        this.uuidSequence = createSequence(mockery, "uuid");
        this.objectSerializer = objectSerializer;
        this.serializationSequence = createSequence(mockery, "serialization");
    }

    private Sequence createSequence(Mockery mockery, String name) {
        return mockery.sequence(name);
    }

    public void expectUUID(String uuidStr) {
        expectUUID(UUID.fromString(uuidStr));
    }

    public void expectSequentalUUIDs(int count) {
        expectSequentalUUIDs(0, count);
    }

    public void expectSequentalUUIDs(int firstElement, int lastElement) {
        for (int i = firstElement; i <= lastElement; i++) {
            expectUUID(new UUID(0, i));
        }
    }

    public void expectUUID(UUID result) {
        one(uuidGenerator).createUUID();
        will(returnValue(result));
        inSequence(uuidSequence);
    }

    protected <T> void expectWrite(int id, String path, T value, Class clazz, TYPE recordType) {
        expectWrite(new UUID(0, id), path, value, clazz, recordType);
    }

    protected <T> void expectWrite(UUID id, String path, T value, Class clazz, TYPE recordType) {
        one(objectSerializer).serialize(with(equal(new SerializedField(id, path, value, clazz, recordType))));
        inSequence(serializationSequence);
    }
}
