package org.holicc.cmd.impl;

import org.holicc.db.DataEntry;
import org.junit.jupiter.api.Assertions;

public class TestUtils {

    static void equalsEntry(DataEntry except, DataEntry entry) {
        Assertions.assertEquals(except.getKey(), entry.getKey());
        if (entry.getTtl() != 0) {
            System.out.println(Math.abs(except.getTtl() - entry.getTtl()));
            Assertions.assertTrue(Math.abs(except.getTtl() - entry.getTtl()) < 1000);
        }
        Assertions.assertEquals(except.getValue(), entry.getValue());
        Assertions.assertEquals(except.getPolicy(), entry.getPolicy());
    }
}
