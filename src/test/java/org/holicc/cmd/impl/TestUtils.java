package org.holicc.cmd.impl;

import org.holicc.db.DataEntry;
import org.junit.jupiter.api.Assertions;

public class TestUtils {

    static void equalsEntry(DataEntry except, DataEntry entry) {
        Assertions.assertEquals(except.getKey(), entry.getKey());
        if (entry.getTtl() != null) {
            Assertions.assertTrue(except.getTtl().isAfter(entry.getTtl()));
        }
        Object o = except.getValue();
        Object b = entry.getValue();
        Assertions.assertEquals(o, b);
        Assertions.assertEquals(except.getPolicy(), entry.getPolicy());
    }
}
