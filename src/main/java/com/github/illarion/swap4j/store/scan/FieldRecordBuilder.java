package com.github.illarion.swap4j.store.scan;

import java.util.UUID;

public class FieldRecordBuilder<T> {
    private UUID id;
    private String path;
    private T value;
    private Class clazz;
    private RECORD_TYPE recordType;
    private Locator locator;
    private Class elementClass;

    public FieldRecordBuilder() {
    }

    public FieldRecordBuilder(UUID id, String path) {
        this.id = id;
        this.path = path;
    }

    public FieldRecordBuilder(long id, String path) {
        setId(id);
        setPath(path);
    }

    public FieldRecordBuilder(Locator locator) {
        setLocator(locator);
    }

    public FieldRecordBuilder setId(UUID id) {
        this.id = id;
        return this;
    }

    public FieldRecordBuilder setPath(String path) {
        this.path = path;
        return this;
    }

    public FieldRecordBuilder setValue(T value) {
        this.value = value;
        return this;
    }

    public FieldRecordBuilder setClazz(Class clazz) {
        this.clazz = clazz;
        return this;
    }

    public FieldRecordBuilder setRecordType(RECORD_TYPE recordType) {
        this.recordType = recordType;
        return this;
    }

    public FieldRecordBuilder setLocator(Locator locator) {
        this.locator = locator;
        if (null != locator) {
            setId(locator.getId());
            setPath(locator.getPath());
        }
        return this;
    }

    public FieldRecordBuilder setLocator(int uuid, String path) {
        return setLocator(new Locator(uuid, path));
    }

    public FieldRecordBuilder setLocator(UUID uuid, String path) {
        return setLocator(new Locator(uuid, path));
    }

    public FieldRecordBuilder setElementClass(Class elementClass) {
        this.elementClass = elementClass;
        return this;
    }

    public FieldRecordBuilder setId(long id) {
        this.id = new UUID(0, id);
        return this;
    }

    public FieldRecord create() {
        if (null == locator) {
            locator = new Locator(id, path);
        }
        return new FieldRecord(locator, value, clazz, elementClass, recordType);
    }
}