package com.github.illarion.swap4j.store.scan;

import com.github.illarion.swap4j.store.StorageException;
import com.github.illarion.swap4j.swap.Swap;

import java.util.UUID;

/**
 * TODO Describe class
 * <p/>
 * <p/>
 * Created at: Sep 16, 2011 2:53:26 AM
 *
 * @author Alexey Tigarev
 */
public class FieldRecord<T> implements Comparable<FieldRecord<T>> {
    private T value;
    private RECORD_TYPE recordType;
    private Class clazz;
    Locator locator;
    private Class elementClass = null; // for collections
    transient Swap swap;


    public FieldRecord() {
    }

    public FieldRecord(UUID id, String path, T value, Class clazz, RECORD_TYPE recordType) {
        checkValue(value);
        this.clazz = clazz;
        this.locator = new Locator(id, path);
        this.recordType = recordType;
        this.value = value;
    }

    public FieldRecord(Locator locator, T value, Class clazz, Class elementClass, RECORD_TYPE recordType) {
        checkValue(value);
        this.clazz = clazz;
        this.locator = locator;
        this.recordType = recordType;
        this.value = value;
        this.elementClass = elementClass;
    }

    public FieldRecord(int id, String path, T value, Class clazz, Class elementClass, RECORD_TYPE record_type) {
        this(new Locator(id, path), value, clazz, elementClass, record_type);
    }


    private void checkValue(T value) {
        if (null != value && value instanceof FieldRecord) {
            throw new IllegalArgumentException("Nested SerializedFields are wrong + " + this + ", " + value);
        }
    }

    public FieldRecord(int idNumber, String path, T value, Class clazz, RECORD_TYPE recordType) {
        this(new UUID(0, idNumber), path, value, clazz, recordType);
    }

    public FieldRecord(Locator locator, T value, Class clazz, RECORD_TYPE recordType) {
        checkValue(value);
        this.clazz = clazz;
        this.locator = locator;
        this.recordType = recordType;
        this.value = value;
    }

    public UUID getId() {
        return locator.getId();
    }

    public Class getClazz() {
        return clazz;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
//        throw new IllegalArgumentException("zzzz");
        final StringBuilder sb = new StringBuilder();
        sb.append("FR{");
        sb.append("@").append(locator);
        sb.append(" =").append(value);
        sb.append(" c=").append(shortName(clazz));
        sb.append(" e=").append(shortName(elementClass));
        sb.append(" t=").append(recordType);
        sb.append('}');
        return sb.toString();
    }

    private String shortName(Class clazz) {
        if (clazz == null) {
            return null;
        } else {
            return clazz.getSimpleName();
        }
    }

    @SuppressWarnings({"RedundantIfStatement"})
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FieldRecord that = (FieldRecord) o;

        if (recordType != that.recordType) return false;
        if (clazz != null ? !clazz.equals(that.clazz) : that.clazz != null) return false;
        if (locator != null ? !locator.equals(that.locator) : that.locator != null) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = value != null ? value.hashCode() : 0;
        result = 31 * result + (recordType != null ? recordType.hashCode() : 0);
        result = 31 * result + (clazz != null ? clazz.hashCode() : 0);
        result = 31 * result + (locator != null ? locator.hashCode() : 0);
        return result;
    }

    public Locator getLocator() {
        return locator;
    }

    @Override
    public int compareTo(FieldRecord<T> that) {
        if (this.locator == null) {
            return that.locator == null? 0 : -1;
        }
        if (that.locator == null) {
            return 1;
        }
        return this.locator.compareTo(that.locator);
    }


    /**
     * Write the value of this field to specified object into place identified by path.
     *
     * @param object
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    public Object writeTo(Object object) throws NoSuchFieldException, IllegalAccessException, StorageException {
        if (locator.isRoot()) {
            return null; // can't set object itself, ignoring
        }
        ObjectStructure structure = getObjectStructure(object);
        return structure.writeTo(object, this);
    }

    private ObjectStructure getObjectStructure(Object object) {
        return new ObjectStructure(object);
    }

    public RECORD_TYPE getRecordType() {
        return recordType;
    }

    String getPath() {
        return getLocator().getPath();
    }

    String getIdString() {
        return getId().toString();
    }

    String getValueString() {
        return null == value ? null : value.toString();
    }

    int getTypeOrdinal() {
        return getRecordType().ordinal();
    }

    String getClassName() {
        return getClazz().getName();
    }

    public String getElementClassName() {
        return null == elementClass ? null : elementClass.getName();
    }

    public Class getElementClass() {
        return elementClass;
    }

    boolean isListElement() {
        return RECORD_TYPE.LIST_ELEMENT == getRecordType();
    }

    UUID getValueAsUuid() {
        return UUID.fromString((String) getValue());
    }

    public void setValue(T value) {
        this.value = value;
    }

    boolean isProxiedField() {
        return getRecordType() == RECORD_TYPE.PROXIED_FIELD;
    }

    public boolean isCompoundField() {
        return recordType == RECORD_TYPE.COMPOUND_FIELD;
    }

    public long getIdLong() {
        return locator.getIdLong();
    }
}
