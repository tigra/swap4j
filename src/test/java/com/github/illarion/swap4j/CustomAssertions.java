package com.github.illarion.swap4j;

import com.github.illarion.swap4j.store.StorageException;
import com.github.illarion.swap4j.store.scan.*;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.internal.matchers.TypeSafeMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

/**
 * TODO Describe class
 * <p/>
 * <p/>
 * Created at: Sep 29, 2011 2:38:10 PM
 *
 * @author Alexey Tigarev
 */
public class CustomAssertions {
    private static Logger log = LoggerFactory.getLogger("CustomAssertions");

    public static void assertGreaterOrEqual(int a, int b) {
        assertTrue(a >= b);
    }

    public static void assertGreaterOrEqual(String message, int a, int b) {
        assertThat(message, a, greaterOrEqualTo(b));
    }

    private static Matcher<Integer> greaterOrEqualTo(final int number) {
        return new BaseMatcher<Integer>() {
            @Override
            public boolean matches(Object item) {
                return (Integer) item >= number;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(">=").appendValue(number);
            }
        };
    }

    /**
     * Verifies that store contains ONLY of specified FieldRecord instances
     *
     * @param fieldStorage
     * @param expected
     * @throws com.github.illarion.swap4j.store.StorageException
     *
     */
    @Deprecated
    public static void assertStorageContains(FieldStorage fieldStorage, FieldRecord... expected) throws StorageException {
        Set<Locator> locators = new HashSet<Locator>();
        for (FieldRecord fieldRecord : expected) {
            final Locator locator = fieldRecord.getLocator();
            FieldRecord restored = fieldStorage.read(locator);
            if (fieldRecord == null) {
                fail("Don't pass nulls to assertStorageContains!\n" + dumpStoreContents(fieldStorage, "Present objects in storage"));
            }
            if (restored == null) {
                fail("Not found: " + fieldRecord + "\n" + dumpStoreContents(fieldStorage, "Present objects in storage"));
            }
            assertFieldRecordsMatch("Other object found in store than expected:\n" + dumpStoreContents(fieldStorage, "Present objects in storage"),
                    fieldRecord, restored);
            locators.add(locator);
        }
        assertThat("Make sure you don't have repeating IDs when calling assertStoreContains()",
                expected.length, greaterOrEqualTo(locators.size()));
        assertEquals("Extra expected found in storage", expected.length, locators.size());
    }

    public static FeatureMatcher<FieldRecord, RECORD_TYPE> recordTypeIsProxyList() {
        return recordTypeIs(RECORD_TYPE.PROXY_LIST);
    }

    public static FeatureMatcher<FieldRecord, RECORD_TYPE> recordTypeIsPrimitiveField() {
        return recordTypeIs(RECORD_TYPE.PRIMITIVE_FIELD);
    }

    public static Matcher<FieldRecord> recordTypeIsProxiedValue() {
        return recordTypeIs(RECORD_TYPE.PROXIED_VALUE);
    }

    public static FeatureMatcher<FieldRecord, RECORD_TYPE> recordTypeIsListElement() {
        return recordTypeIs(RECORD_TYPE.LIST_ELEMENT);
    }

    public static FeatureMatcher<FieldRecord, RECORD_TYPE> recordTypeIsListField() {
        return recordTypeIs(RECORD_TYPE.LIST_FIELD);
    }

    public static FeatureMatcher<FieldRecord, String> pathIs(String expectedPath) {
        return new FeatureMatcher<FieldRecord, String>(equalTo(expectedPath), "path", "path") {
            @Override
            protected String featureValueOf(FieldRecord actual) {
                return actual.getLocator().getPath();
            }
        };
    }

    public static FeatureMatcher<FieldRecord, UUID> idIs(int expectedId) {
        return new FeatureMatcher<FieldRecord, UUID>(equalTo(new UUID(0, expectedId)), "id", "id") {
            @Override
            protected UUID featureValueOf(FieldRecord actual) {
                return actual.getId();
            }
        };
    }

    public static FeatureMatcher<FieldRecord, Object> valueIsUuidStr(long uuid) {
        return valueIsUuidStr(new UUID(0, uuid));
    }

    static FeatureMatcher<FieldRecord, Object> valueIsUuidStr(UUID uuid) {
        return valueIs(uuid.toString());
    }

    public static <T> Matcher<? super Object> containsOneElement() {
        return new TypeSafeMatcher<Object>() {
            @Override
            public boolean matchesSafely(Object item) {
                if (item instanceof List) {
                    List<T> list = (List<T>) item;
                    return null != list && list.size() == 1;
                } else {
                    return false;
                }
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("list containing one and only one element");
            }

            @Override
            public void describeMismatch(Object item, Description description) {
                describeListContents((List<T>) item, description);
            }

        };
    }

    public static <T> void describeListContents(List<T> list, Description description) {
        description.appendText("list contains ").appendValue(list.size()).appendText(" elements: [");
        for (T element : list) {
            description.appendValue(element).appendText(",");
        }
        description.appendText("]");
    }

    public static <T> Matcher<? super Object> containsOneElement(final T expectedElement) {
        return new TypeSafeMatcher<Object>() {
            @Override
            public boolean matchesSafely(Object item) {
                List<T> list = (List<T>) item;
                return null != list && list.size() == 1 && list.contains(expectedElement);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("list containing one and only one element: ").appendValue(expectedElement);
            }

            @Override
            public void describeMismatch(Object item, Description description) {
                describeListContents((List<T>) item, description);
            }
        };
    }

    public static <T> Matcher<List<? extends T>> isEmpty() {
        return new TypeSafeMatcher<List<? extends T>>() {
            @Override
            public boolean matchesSafely(List<? extends T> item) {
                return ((List<T>) item).isEmpty();
            }

            @Override
            public void describeMismatch(Object item, Description description) {
                description.appendText("list is non empty: ");
                describeListContents((List<T>)item, description);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("empty list");
            }
        };
    }

    public static <T> Matcher<List<T>> containsElements(final T... expected) {
        return new BaseMatcher<List<T>>() {
            @Override
            public boolean matches(Object item) {
                if (!(item instanceof List)) {
                    return false;
                }
                List list = (List) item;
                if (expected.length != list.size()) {
                    return false;
                }
                for (int i = 0; i < expected.length; i++) {
                    T expectedElement = expected[i];
                    Object actualElement = list.get(i);
                    if (not(equalTo(expectedElement)).matches(actualElement)) {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("List [");
                for (T e : expected) {
                    description.appendValue(e).appendText(", ");
                }
                description.appendText("]");
            }
        };
    }

    public static Matcher<? super FieldRecord> locatedAt(final int uuid, final String path) {
        return new TypeSafeMatcher<FieldRecord>() {
            @Override
            public boolean matchesSafely(FieldRecord item) {
                if (null == item) {
                    return false;
                }
                Locator locator = item.getLocator();
                if (null == locator) {
                    return false;
                }
                return uuid == locator.getIdLong() && equalTo(path).matches(locator.getPath());
            }
            @Override
            public void describeTo(Description description) {
                description.appendText("at(").appendValue(uuid).appendText(", ").appendValue(path).appendText(")");
            }
        };
    }


    static class MethodCallResult<T> {
        T returnValue;
        Throwable thrown;

        MethodCallResult(T returnValue, Throwable thrown) {
            this.returnValue = returnValue;
            this.thrown = thrown;
        }

        MethodCallResult(Throwable thrown) {
            this.returnValue = null;
            this.thrown = thrown;
        }

        MethodCallResult(T returnValue) {
            this.returnValue = returnValue;
            this.thrown = null;
        }

        public T getReturnValue() {
            return returnValue;
        }

        public Throwable getThrown() {
            return thrown;
        }

        public boolean isExceptionThrown() {
            return null != thrown;
        }
    }

    
    public static <O, T> Matcher<O> getterValue(final String fieldName, final Matcher<T> subMatcher) {
        return new TypeSafeMatcher<O>() {
            @Override
            public boolean matchesSafely(O item) {
                if (null == item) {
                    return false;
                }
                Method getter = getGetter(item, fieldName);
                if (null == getter) {
                    return false;
                }
                MethodCallResult<T> result = callGetter(item, getter);
                if (result.isExceptionThrown()) {
                    return false;
                }
                return subMatcher.matches(result.getReturnValue());
            }

            private MethodCallResult<T> callGetter(O item, Method getter) {
                try {
                    Object result = getter.invoke(item);
                    return new MethodCallResult<T>((T)result);
                } catch (IllegalAccessException e) {
                    return new MethodCallResult<T>(e);
                } catch (InvocationTargetException e) {
                    return new MethodCallResult<T>(e);
                } catch (ClassCastException e) {
                    return new MethodCallResult<T>(e);
                }
            }

            private <O> Method getGetter(O item, String fieldName) {
                String methodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                try {
                    return item.getClass().getMethod(methodName);
                } catch (NoSuchMethodException e) {
                    return null;
                }
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("object whose field ").appendValue(fieldName).appendText(" accessed using getter returns ");
                description.appendDescriptionOf(subMatcher);
            }

            @Override
            public void describeMismatch(Object item, Description description) {
                if (null == item) {
                    description.appendText("was null");
                } else {
                    description.appendText("was ").appendValue(item);
                    Method getter = getGetter(item, fieldName);
                    if (null == getter) {
                        description.appendText(" getter not found");
                    } else {
                        MethodCallResult result = callGetter((O)item, getter);
                        if (result.isExceptionThrown()) {
                            description.appendValue(" exception thrown calling getter: ").appendValue(result.getThrown());
                        } else {
                            description.appendDescriptionOf(subMatcher);
                        }
                    }
                }

            }
        };
    }

    public static <O, T> Matcher<O> fieldValue(final String fieldName, final Matcher<T> subMatcher) {
        return new TypeSafeMatcher<O>() { // TODO refactor, don't call too much times
            @Override
            public boolean matchesSafely(O item) {
                if (null == item) {
                    return false;
                }
                Field field = getField(item, fieldName);
                if (field == null) {
                    return false;
                }
                if (null != getValueException(item, fieldName)) {
                    return false;
                }
                return subMatcher.matches(getValue(item, field));
            }

            private <O> Exception getValueException(O item, String fieldName) {
                try {
                    getField(item, fieldName).get(item);
                    return null;
                } catch (IllegalAccessException iae) {
                    return iae;
                }
            }

            private Object getValue(Object item, Field field) {
                Object value = null;
                try {
                    value = field.get(item);
                } catch (IllegalAccessException e) {
                    System.err.println("Unexpected error (should already had checked for this): " + e);
                    e.printStackTrace();
                }
                return value;
            }


            @Override
            public void describeTo(Description description) {
                description.appendText("object with field ").appendValue(fieldName).appendText(" matching: ")
                        .appendDescriptionOf(subMatcher);
            }

            @Override
            public void describeMismatch(Object item, Description description) {
                if (null == item) {
                    description.appendText("was null");
                } else {
                    description.appendText("was ").appendValue(item);
                    if (null == getField(item, fieldName)) {
                        description.appendText(" field '").appendValue(fieldName).appendText("' was not found");
                    } else if (null != getValueException(item, fieldName)) {
                        description.appendText("exception thrown accessing field: ").appendValue(getValueException(item, fieldName));
                    } else {
                        subMatcher.describeMismatch(item, description);
                    }
                }
            }

            private Field getField(Object item, String fieldName) {
                Field field;
                try {
                    field = item.getClass().getField(fieldName);
                } catch (NoSuchFieldException e) {
                    field = null;
                }
                return field;
            }
        };
    }

    public static class PositionalMatcher<T> extends BaseMatcher<T> {
        Locator locator;
        Matcher<T> subMatcher;

        protected PositionalMatcher(Locator locator, Matcher<T> subMatcher) {
            this.locator = locator;
            this.subMatcher = subMatcher;
        }

        public Locator getLocator() {
            return locator;
        }

        @Override
        public boolean matches(Object item) {
            return subMatcher.matches(item);
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("at ").appendText(locator.toString()).appendText(": ").appendDescriptionOf(subMatcher);
//            subMatcher.describeTo(description);
        }
    }

    public static <T> PositionalMatcher<T> at(Locator locator, Matcher<T> matcher) {
        return new PositionalMatcher<T>(locator, matcher);
    }

    public static <T> PositionalMatcher<T> at(int uuid, String path, Matcher<T> matcher) {
        return new PositionalMatcher<T>(new Locator(uuid, path), matcher);
    }

    /**
     * Verifies that store contains ONLY of specified FieldRecord instances
     *
     * @param fieldStorage
     * @param expected
     * @throws com.github.illarion.swap4j.store.StorageException
     *
     */
    public static void assertStorageContains(FieldStorage fieldStorage, PositionalMatcher<FieldRecord>... expected) throws StorageException {
        Set<Locator> seenLocators = new HashSet<Locator>();
        String expectedRecords = "Expected records:\n" + dumpExpectedRecords(expected);
        String actualRecords = dumpStoreContents(fieldStorage, "Actual FieldRecords");

        for (PositionalMatcher<FieldRecord> matcher : expected) {
            final Locator locator = matcher.getLocator();
            FieldRecord actual = fieldStorage.read(locator);
            if (matcher == null) {
                fail("Don't pass nulls to assertStorageContains!\n" + dumpStoreContents(fieldStorage, "Present objects in storage"));
            }
            if (actual == null) {
                fail("Not found: " + matcher + "\n" + dumpStoreContents(fieldStorage, "Present objects in storage"));
            }
            assertThat("Other object found in store than expected.\n" + expectedRecords + actualRecords, actual, matcher);
            seenLocators.add(locator);
        }
        assertThat("Make sure you don't have repeating locators when calling assertStoreContains()",
                expected.length, greaterOrEqualTo(seenLocators.size()));
//        assertEquals("Extra expected found in storage", expected.length, seenLocators.size());
        String extraRecords = dumpStoreContents(fieldStorage, seenLocators, "Extra FieldRecords");
        assertEquals("Extra records found in storage - " + extraRecords + "\n"
                + expectedRecords + actualRecords
                , expected.length, fieldStorage.getRecordCount());
    }

    private static String dumpExpectedRecords(PositionalMatcher<FieldRecord>[] expected) {
        Description description = new StringDescription();
        for (Matcher<FieldRecord> matcher : expected) {
            description.appendDescriptionOf(matcher).appendText("\n");
        }
        return description.toString();
    }


    private static void assertFieldRecordsMatch(String message, FieldRecord expected, FieldRecord actual) {
        assertThat(message, expected,
                locatorIs(actual.getLocator()).and(valueIs(actual.getValue()))
                        .and(clazzIs(actual.getClazz())).and(elementClassIs(actual.getElementClass()))
                        .and(recordTypeIs(actual.getRecordType())));
    }


    private static <T> Matcher<T> and(final Matcher<T>... matchers) {
        return new BaseMatcher<T>() {
            @Override
            public boolean matches(Object item) {
                for (Matcher<T> matcher : matchers) {
                    if (!matcher.matches(item)) {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("conjunction of matchers:");
                for (Matcher<T> matcher : matchers) {
                    description.appendDescriptionOf(matcher).appendText(" and ");
                }
            }
        };
    }

    public static FeatureMatcher<FieldRecord, Class> clazzIs(Class expected) {
        return new FeatureMatcher<FieldRecord, Class>(equalTo(expected), "class", "class") {
            @Override
            protected Class featureValueOf(FieldRecord actual) {
                return actual.getClazz();
            }
        };
    }

    public static FeatureMatcher<FieldRecord, Class> elementClassIs(Class expected) {
        return new FeatureMatcher<FieldRecord, Class>(equalTo(expected), "elementClass", "elementClass") {
            @Override
            protected Class featureValueOf(FieldRecord actual) {
                return actual.getElementClass();
            }
        };
    }

    public static FeatureMatcher<FieldRecord, RECORD_TYPE> recordTypeIs(RECORD_TYPE expected) {
        return new FeatureMatcher<FieldRecord, RECORD_TYPE>(equalTo(expected), "recordType", "recordType") {
            @Override
            protected RECORD_TYPE featureValueOf(FieldRecord actual) {
                return actual.getRecordType();
            }
        };
    }

    public static FeatureMatcher<FieldRecord, Object> valueIs(final Object expected) {
        return new FeatureMatcher<FieldRecord, Object>(equalTo(expected), "value", "value") {
            @Override
            protected Object featureValueOf(FieldRecord actual) {
                return actual.getValue();
            }
        };
    }

    private static FeatureMatcher<FieldRecord, Locator> locatorIs(final Locator expected) {
        return new FeatureMatcher<FieldRecord, Locator>(equalTo(expected), "locator", "locator") {
            @Override
            protected Locator featureValueOf(FieldRecord actual) {
                return actual.getLocator();
            }
        };
    }

    private static String dumpStoreContents(FieldStorage fieldStorage, String title) {
        return dumpStoreContents(fieldStorage, new HashSet<Locator>(), title);
    }

    private static String dumpStoreContents(FieldStorage fieldStorage, Set<Locator> except, String title) {
        StringBuilder builder = new StringBuilder(title).append(": [\n");
        for (Locator locator : fieldStorage) {
            if (!except.contains(locator)) {
                FieldRecord record = null;
                String entry;
                try {
                    record = fieldStorage.read(locator);
                    entry = null == record ? "null" : record.toString();
                } catch (StorageException e) {
                    log.error("", e);
                    entry = e.getMessage();
                }
                builder.append(locator).append(" => ").append(record).append("\n");
            }
        }
        return builder.append("]").toString();
    }

    public static FieldRecord obj(int id, String path, Object value, Class clazz, RECORD_TYPE recordType) {
        return new FieldRecordBuilder(id, path).setValue(value).setClazz(clazz).setRecordType(recordType).create();
    }

    public static FieldRecord obj(int id, String path, Object value, Class clazz, Class elementClass, RECORD_TYPE type) {
        return new FieldRecordBuilder(id, path).setValue(value).setClazz(clazz).setElementClass(elementClass).setRecordType(type).create();
    }
}
