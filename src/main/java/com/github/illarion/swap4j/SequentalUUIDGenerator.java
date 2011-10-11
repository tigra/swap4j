package com.github.illarion.swap4j;

import com.github.illarion.swap4j.swap.UUIDGenerator;

import java.util.UUID;

/**
 * TODO Describe class
 *
 * @author Alexey Tigarev tigra@agile-algorithms.com
 */
public class SequentalUUIDGenerator implements UUIDGenerator {
    long currentUuid = 0;
    long maxUuid = -1;

    public SequentalUUIDGenerator(long maxUuid) {
        this.maxUuid = maxUuid;
    }

    public SequentalUUIDGenerator() {
        currentUuid = 0;
        maxUuid = -1;
    }

    @Override
    public UUID createUUID() {
        if (maxUuid != -1 && currentUuid > maxUuid) {
            throw new IllegalStateException("Too much createUUID() calls");
        }
        return new UUID(0, currentUuid++);
    }
}
