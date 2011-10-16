package com.github.illarion.swap4j.store.scan;

import java.util.UUID;

/**
 * TODO Describe class
 *
 * @author Alexey Tigarev tigra@agile-algorithms.com
 */
public class ID {
    public static String shortRepresentation(UUID id) {
        if (null == id) {
            return "null";
        } else {
            return Long.toHexString(id.getMostSignificantBits()) + "-" + Long.toHexString(id.getLeastSignificantBits());
        }
    }
}
