package org.holicc.cmd.impl;

import org.holicc.db.DataEntry;
import org.junit.jupiter.api.Assertions;

import java.util.Optional;

public class TestUtils {

    static void equalsEntry(DataEntry except, DataEntry entry) {
        Assertions.assertEquals(except.getKey(), entry.getKey());
        Assertions.assertEquals(except.getTtl().isPresent(), entry.getTtl().isPresent());
        if (except.getTtl().isPresent()) {
            Assertions.assertTrue(except.getTtl().get().isAfter(
                    entry.getTtl().get()
            ));
        }
        Object o = except.getValue();
        Object b = entry.getValue();
        Assertions.assertEquals(o, b);
        Assertions.assertEquals(except.getPolicy(), entry.getPolicy());
    }
}
