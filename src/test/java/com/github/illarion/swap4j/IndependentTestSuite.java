package com.github.illarion.swap4j;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.AllTests;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * TODO Describe class
 *
 * @author Alexey Tigarev tigra@agile-algorithms.com
 */
@RunWith(AllTests.class)
public class IndependentTestSuite {

    private static Logger log = LoggerFactory.getLogger("IndependentTestSuite");

    public static Test suite() {
        TestSuite suite = new TestSuite("Independent tests");
        suite.addTest(suiteFor(SimpleTestH2FieldStorage.class));
        suite.addTest(suiteFor(SimpleTestMapWriter.class));
        return suite;
    }

    private static Test suiteFor(Class<? extends TestCase> testClass) {
        TestSuite suite = new TestSuite("Independent " + testClass.getSimpleName());
        for (Method method : testClass.getMethods()) {
            if (null != method.getAnnotation(org.junit.Test.class)
                    && null == method.getAnnotation(Ignore.class)) {
                try {
                    suite.addTest(testClass.getConstructor(String.class).newInstance(method.getName()));
                } catch (InstantiationException e) {
                    log.error("", e);
                } catch (IllegalAccessException e) {
                    log.error("", e);
                } catch (InvocationTargetException e) {
                    log.error("", e);
                } catch (NoSuchMethodException e) {
                    log.error("", e);
                }
            }
        }
        return suite;
    }
    
}
