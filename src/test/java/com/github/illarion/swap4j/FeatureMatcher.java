package com.github.illarion.swap4j;

import com.github.illarion.swap4j.store.scan.FieldRecord;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.internal.matchers.TypeSafeMatcher;

/**
 * Supporting class for matching a feature of an object. Implement <code>featureValueOf()</code>
 * in a subclass to pull out the feature to be matched against.
 *
 * @param <T> The type of the object to be matched
 * @param <U> The type of the feature to be matched
 */
public abstract class FeatureMatcher<T, U> extends BaseMatcher<T> {
//    private static final ReflectiveTypeFinder TYPE_FINDER = new ReflectiveTypeFinder("featureValueOf", 1, 0);
    private final Matcher<? super U> subMatcher;
    private final String featureDescription;
    private final String featureName;

    /**
     * Constructor
     *
     * @param subMatcher         The matcher to apply to the feature
     * @param featureDescription Descriptive text to use in describeTo
     * @param featureName        Identifying text for mismatch message
     */
    public FeatureMatcher(Matcher<? super U> subMatcher, String featureDescription, String featureName) {
        super();
        this.subMatcher = subMatcher;
        this.featureDescription = featureDescription;
        this.featureName = featureName;
    }

    public FeatureMatcher<T, U> and(final Matcher<T> matcher) {
        return new FeatureMatcher<T,U>(null, "@", "$") {
            /**
             * Implement this to extract the interesting feature.
             *
             * @param actual the target object
             * @return the feature to be matched
             */
            @Override
            protected U featureValueOf(T actual) {
                return FeatureMatcher.this.featureValueOf(actual);
            }

            @Override
            public boolean matches(Object item) {
                return FeatureMatcher.this.matches(item) && matcher.matches(item);
            }
            @Override
            public void describeTo(Description description) {
                description.appendDescriptionOf(FeatureMatcher.this).appendText("\n    and ").appendDescriptionOf(matcher);
            }

        };
    }

    /**
     * Implement this to extract the interesting feature.
     *
     * @param actual the target object
     * @return the feature to be matched
     */
    protected abstract U featureValueOf(T actual);


    public boolean matches(Object actual) {
        final U featureValue = featureValueOf((T)actual);
        if (!subMatcher.matches(featureValue)) {
//            mismatchDescription.appendText(featureName).appendText(" ");
//      subMatcher.describeMismatch(featureValue, mismatchDescription);
//            subMatcher.describeTo(mismatchDescription);
            return false;
        }
        return true;
    }

    public void describeTo(Description description) {
        description.appendText(featureDescription).appendText("=")
                .appendDescriptionOf(subMatcher);
    }
}