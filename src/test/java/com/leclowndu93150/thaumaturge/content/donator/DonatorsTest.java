package com.leclowndu93150.thaumaturge.content.donator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DonatorsTest {
    @Test
    void missingProfileIdDoesNotGrantDonatorStatus() {
        assertFalse(Donators.is(null, "Chubby_Boi"));
    }

    @Test
    void recognizedProfileIdRetainsDonatorStatusAfterNameChange() {
        assertTrue(Donators.is(Donators.donators().get("Chubby_Boi"), "RenamedPlayer"));
    }
}
