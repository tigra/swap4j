package com.github.illarion.swap4j;

import com.github.illarion.swap4j.store.StoreException;
import com.github.illarion.swap4j.store.scan.*;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.hamcrest.CoreMatchers.equalTo;
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
                return (Integer)item >= number;
            }
            @Override
            public void describeTo(Description description) {
                description.appendText(">=").appendValue(number);
            }
        };
    }

    /**
     * Verifies that store contains ONLY of specified FieldRecord instances
     * @param fieldStorage
     * @param expected
     * @throws com.github.illarion.swap4j.store.StoreException
     */
    @Deprecated
    public static void assertStorageContains(FieldStorage fieldStorage, FieldRecord... expected) throws StoreException {
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
     * @param fieldStorage
     * @param expected
     * @throws com.github.illarion.swap4j.store.StoreException
     */
    public static void assertStorageContains(FieldStorage fieldStorage, PositionalMatcher<FieldRecord>... expected) throws StoreException {
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
        for (Locator locator: fieldStorage) {
            if (!except.contains(locator)) {
                builder.append(locator).append(" => ").append(fieldStorage.read(locator)).append("\n");
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
