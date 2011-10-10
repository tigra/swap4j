package com.github.illarion.swap4j.swap;

import java.util.UUID;

public class RandomUuidGenerator implements UUIDGenerator {
    public RandomUuidGenerator() {
    }

    @Override
    public UUID createUUID() {
        return UUID.randomUUID();
    }
}