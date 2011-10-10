package com.github.illarion.swap4j.store.scan;

import com.github.illarion.swap4j.AnyObject;
import com.github.illarion.swap4j.swap.Utils;

import javax.xml.stream.events.EntityDeclaration;
import java.lang.reflect.Field;
import java.util.List;
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
        final StringBuilder sb = new StringBuilder();
        sb.append("SF{");
        sb.append("@").append(locator);
        sb.append(" =").append(value);
        sb.append(" c=").append(shortName(clazz));
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
        if (value != null ? !valuesEqual(that) : that.value != null) return false;

        return true;
    }

    private boolean valuesEqual(FieldRecord that) {
//        if (this.value instanceof AnyObject || that.value instanceof AnyObject) {
//            return true; // TODO remove this dirty hack, use e.g. hamcrest matchers instead
//        }
        return value.equals(that.value);
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
    public void writeTo(Object object) throws NoSuchFieldException, IllegalAccessException {
        if (getLocator().isRoot(this)) {
            return; // can't set object itself, ignoring
        }
        List<String> pathComponents = locator.getParsedPath();
        writeTo(object, pathComponents, getValue());
    }

    public void writeTo(Object object, List<String> pathComponents, Object value) throws NoSuchFieldException, IllegalAccessException {
        if (pathComponents.size() < 1) {
            throw new IllegalArgumentException("Hm.....");
        }
        String fieldName = pathComponents.get(0);
        Class<?> clazz = object.getClass();

        Field field = Utils.getAccessibleField(fieldName, clazz);
        if (pathComponents.size() == 1) {
            field.set(object, value);
        } else {
            pathComponents.remove(0);
            writeTo(field.get(object), pathComponents, value);
        }
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
        return getValue().toString();
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
}
