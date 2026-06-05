package com.numberbook.app;

import org.junit.Test;

import static org.junit.Assert.*;

public class ExampleUnitTest {

    @Test
    public void phoneEntry_storesNameAndNumber() {
        PhoneEntry entry = new PhoneEntry("Alice", "+1 555 0100");
        assertEquals("Alice", entry.getFullName());
        assertEquals("+1 555 0100", entry.getMobileNumber());
    }
}
