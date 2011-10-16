package com.github.illarion.swap4j.store.scan;

import com.github.illarion.swap4j.CustomAssertions;
import org.junit.Test;

import java.util.List;

import static com.github.illarion.swap4j.CustomAssertions.containsElements;
import static com.github.illarion.swap4j.CustomAssertions.containsOneElement;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

/**
 * TODO Describe class
 *
 * @author Alexey Tigarev tigra@agile-algorithms.com
 */
public class LocatorTest {
    @Test
    public void testGetParsedPath1() {
        List<String> path = new Locator(0, ".[0").getParsedPath();

        assertThat(path, containsOneElement("[0"));
    }

    @Test
    public void testGetParsedPath2() {
        List<String> path = new Locator(0, "./field").getParsedPath();

        assertThat(path, containsOneElement("field"));
    }

    @Test
    public void testGetParsedPath3() {
        List<String> path = new Locator(0, "./field[3/subField1/subField2[0").getParsedPath();

        assertThat(path, containsElements("field", "[3", "subField1", "subField2", "[0"));
    }

}
