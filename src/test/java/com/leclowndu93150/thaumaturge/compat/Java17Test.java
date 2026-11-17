package com.leclowndu93150.thaumaturge.compat;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

class Java17Test {
    @Test
    void firstAndLastRetainOrderAndNullElements() {
        assertEquals(1, Java17.first(List.of(1, 2, 3)));
        assertEquals(3, Java17.last(List.of(1, 2, 3)));
        assertEquals(4, Java17.first(List.of(4)));
        assertEquals(4, Java17.last(List.of(4)));
        assertNull(Java17.first(Arrays.asList(null, 1)));
        assertNull(Java17.last(Arrays.asList(1, null)));
    }

    @Test
    void emptyListsRetainTheSequencedCollectionsExceptionContract() {
        assertThrows(NoSuchElementException.class, () -> Java17.first(List.of()));
        assertThrows(NoSuchElementException.class, () -> Java17.last(List.of()));
        assertThrows(NullPointerException.class, () -> Java17.first(null));
        assertThrows(NullPointerException.class, () -> Java17.last(null));
    }

    @Test
    void reverseIsABackedViewIncludingMutationAndInsertion() {
        var source = new ArrayList<>(List.of(1, 2, 3));
        var view = Java17.reversed(source);
        assertEquals(List.of(3, 2, 1), view);
        source.set(2, 30);
        assertEquals(30, view.get(0));
        view.set(2, 10);
        assertEquals(10, source.get(0));
        view.add(0, 4);
        assertEquals(List.of(10, 2, 30, 4), source);
        assertEquals(30, view.remove(1));
        assertEquals(List.of(10, 2, 4), source);
        assertSame(source, Java17.reversed(view));
    }

    @Test
    void immutableAndEmptyReverseViewsStayImmutableAndEmpty() {
        assertTrue(Java17.reversed(List.of()).isEmpty());
        var view = Java17.reversed(List.of(1, 2));
        assertEquals(List.of(2, 1), view);
        assertThrows(UnsupportedOperationException.class, () -> view.add(3));
        assertThrows(UnsupportedOperationException.class, () -> view.set(0, 3));
    }

    @Test
    void intClampSaturatesLongInputsBeforeNarrowing() {
        assertEquals(10, Java17.clamp(Long.MAX_VALUE, -10, 10));
        assertEquals(-10, Java17.clamp(Long.MIN_VALUE, -10, 10));
        assertEquals(Integer.MAX_VALUE, Java17.clamp(Long.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE));
        assertEquals(Integer.MIN_VALUE, Java17.clamp(Long.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE));
        assertEquals(3, Java17.clamp(3, -10, 10));
        assertEquals(5, Java17.clamp(3, 5, 5));
        assertThrows(IllegalArgumentException.class, () -> Java17.clamp(1, 3, 2));
    }

    @Test
    void longClampDoesNotOverflowAtEitherExtreme() {
        assertEquals(10L, Java17.clamp(Long.MAX_VALUE, -10L, 10L));
        assertEquals(-10L, Java17.clamp(Long.MIN_VALUE, -10L, 10L));
        assertEquals(Long.MIN_VALUE, Java17.clamp(Long.MIN_VALUE, Long.MIN_VALUE, Long.MAX_VALUE));
        assertEquals(Long.MAX_VALUE, Java17.clamp(Long.MAX_VALUE, Long.MIN_VALUE, Long.MAX_VALUE));
        assertThrows(IllegalArgumentException.class, () -> Java17.clamp(0L, 1L, -1L));
    }

    @Test
    void floatClampPreservesNaNValuesAndRejectsInvalidBounds() {
        assertTrue(Float.isNaN(Java17.clamp(Float.NaN, 0F, 1F)));
        assertEquals(0F, Java17.clamp(Float.NEGATIVE_INFINITY, 0F, 1F));
        assertEquals(1F, Java17.clamp(Float.POSITIVE_INFINITY, 0F, 1F));
        assertEquals(0.25F, Java17.clamp(0.25F, 0F, 1F));
        assertThrows(IllegalArgumentException.class, () -> Java17.clamp(0F, Float.NaN, 1F));
        assertThrows(IllegalArgumentException.class, () -> Java17.clamp(0F, 0F, Float.NaN));
        assertThrows(IllegalArgumentException.class, () -> Java17.clamp(0F, 1F, -1F));
    }

    @Test
    void floatClampRetainsSignedZeroOrdering() {
        assertEquals(Float.floatToRawIntBits(-0F), Float.floatToRawIntBits(Java17.clamp(-1F, -0F, 0F)));
        assertEquals(Float.floatToRawIntBits(0F), Float.floatToRawIntBits(Java17.clamp(1F, -0F, 0F)));
        assertThrows(IllegalArgumentException.class, () -> Java17.clamp(0F, 0F, -0F));
    }

    @Test
    void doubleClampPreservesNaNValuesAndRejectsInvalidBounds() {
        assertTrue(Double.isNaN(Java17.clamp(Double.NaN, 0D, 1D)));
        assertEquals(0D, Java17.clamp(Double.NEGATIVE_INFINITY, 0D, 1D));
        assertEquals(1D, Java17.clamp(Double.POSITIVE_INFINITY, 0D, 1D));
        assertEquals(0.25D, Java17.clamp(0.25D, 0D, 1D));
        assertThrows(IllegalArgumentException.class, () -> Java17.clamp(0D, Double.NaN, 1D));
        assertThrows(IllegalArgumentException.class, () -> Java17.clamp(0D, 0D, Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> Java17.clamp(0D, 1D, -1D));
    }

    @Test
    void doubleClampRetainsSignedZeroOrdering() {
        assertEquals(Double.doubleToRawLongBits(-0D), Double.doubleToRawLongBits(Java17.clamp(-1D, -0D, 0D)));
        assertEquals(Double.doubleToRawLongBits(0D), Double.doubleToRawLongBits(Java17.clamp(1D, -0D, 0D)));
        assertThrows(IllegalArgumentException.class, () -> Java17.clamp(0D, 0D, -0D));
    }
}
