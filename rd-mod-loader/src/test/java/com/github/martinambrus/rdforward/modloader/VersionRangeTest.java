package com.github.martinambrus.rdforward.modloader;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VersionRangeTest {

    @Test
    void wildcardMatchesAnything() {
        assertTrue(VersionRange.matches("*", "1.0"));
        assertTrue(VersionRange.matches("*", "99.99.99"));
        assertTrue(VersionRange.matches("*", ""));
    }

    @Test
    void blankOrNullConstraintMatchesAnything() {
        assertTrue(VersionRange.matches("", "2.1"));
        assertTrue(VersionRange.matches(null, "2.1"));
        assertTrue(VersionRange.matches("   ", "2.1"));
    }

    @Test
    void greaterOrEqualCompareNumeric() {
        assertTrue(VersionRange.matches(">=1.0", "1.0"));
        assertTrue(VersionRange.matches(">=1.0", "1.0.1"));
        assertTrue(VersionRange.matches(">=1.0", "2.0"));
        assertFalse(VersionRange.matches(">=1.0", "0.9"));
    }

    @Test
    void strictGreaterRejectsEqual() {
        assertFalse(VersionRange.matches(">1.0", "1.0"));
        assertTrue(VersionRange.matches(">1.0", "1.0.1"));
    }

    @Test
    void lessOrEqualAndLessCompareNumeric() {
        assertTrue(VersionRange.matches("<=2.0", "2.0"));
        assertTrue(VersionRange.matches("<=2.0", "1.9"));
        assertFalse(VersionRange.matches("<=2.0", "2.0.1"));

        assertFalse(VersionRange.matches("<2.0", "2.0"));
        assertTrue(VersionRange.matches("<2.0", "1.9"));
    }

    @Test
    void exactEqualsRequiresIdenticalSegments() {
        assertTrue(VersionRange.matches("=1.2.3", "1.2.3"));
        assertFalse(VersionRange.matches("=1.2.3", "1.2.4"));
    }

    @Test
    void bareConstraintDoesPrefixMatch() {
        assertTrue(VersionRange.matches("1.0", "1.0"));
        assertTrue(VersionRange.matches("1.0", "1.0.3"));
        assertFalse(VersionRange.matches("1.0", "1.10"));
        assertFalse(VersionRange.matches("1.0", "2.0"));
    }

    @Test
    void missingTrailingSegmentsTreatedAsZero() {
        assertTrue(VersionRange.matches(">=1.2", "1.2.0"));
        assertTrue(VersionRange.matches("<=1.2.1", "1.2"));
    }

    @Test
    void nonNumericSegmentsFallBackToLex() {
        assertTrue(VersionRange.matches(">=1.0-beta", "1.0-rc"));
        assertFalse(VersionRange.matches(">=1.0-rc", "1.0-beta"));
    }
}
