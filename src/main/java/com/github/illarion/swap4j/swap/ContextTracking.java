package com.github.illarion.swap4j.swap;

import de.huxhorn.lilith.logback.classic.NDC;

/**
 * TODO Describe class
 *
 * @author Alexey Tigarev tigra@agile-algorithms.com
 */
public abstract class ContextTracking {
    protected void enter(String context) {
        NDC.push(getContextInfo(context));
    }

    protected abstract String getContextInfo(String context);

    protected void exit() {
        NDC.pop();
    }

    protected void enter(String pattern, Object... args) {
        enter(String.format(pattern, args));
    }
}
