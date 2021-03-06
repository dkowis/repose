/*
 * _=_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_=
 * Repose
 * _-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-
 * Copyright (C) 2010 - 2015 Rackspace US, Inc.
 * _-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_=_
 */
package org.openrepose.commons.utils;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.*;

/**
 *
 *
 */
@RunWith(Enclosed.class)
public class StringUtilitiesTest {

    public static class WhenJoiningStrings {

        @Test
        public void shouldJoinTwoStrings() {
            String expected, actual;

            expected = "thing one, thing two";
            actual = StringUtilities.join("thing one, ", "thing two");

            assertEquals(expected, actual);
        }

        @Test
        public void shouldManyThings() {
            String expected, actual;

            expected = "1duck2.5";
            actual = StringUtilities.join(1, "duck", 2.5);

            assertEquals(expected, actual);
        }
    }

    public static class WhenCheckingIfAStringIsBlank {

        @Test
        public void shouldHandleNulls() {
            assertTrue(StringUtilities.isBlank(null));
        }

        @Test
        public void shouldHandleEmptyStrings() {
            assertTrue(StringUtilities.isBlank(null));
        }

        @Test
        public void shouldHandleBlankStrings() {
            assertTrue(StringUtilities.isBlank("     "));
        }

        @Test
        public void shouldHandleBlankStringsWithNewLines() {
            assertTrue(StringUtilities.isBlank("\n\n"));
        }

        @Test
        public void shouldHandleBlankStringsWithTabs() {
            assertTrue(StringUtilities.isBlank("\t\t"));
        }

        @Test
        public void shouldHandleComplexBlankStrings() {
            assertTrue(StringUtilities.isBlank("\n\n  \t  \t\n  \t\n   \n\t"));
        }

        @Test
        public void shouldRejectComplexNonBlankStrings() {
            assertFalse(StringUtilities.isBlank("\n\n  \t abc123 \t\n  \t\n   \n\t"));
        }

        @Test
        public void shouldRejectNonBlankStrings() {
            assertFalse(StringUtilities.isBlank("zf-adapter"));
        }
    }

    public static class WhenCheckingIfAStringIsNotBlank {

        @Test
        public void shouldHandleNulls() {
            assertFalse(StringUtilities.isNotBlank(null));
        }

        @Test
        public void shouldHandleEmptyStrings() {
            assertFalse(StringUtilities.isNotBlank(null));
        }

        @Test
        public void shouldHandleBlankStrings() {
            assertFalse(StringUtilities.isNotBlank("     "));
        }

        @Test
        public void shouldHandleBlankStringsWithNewLines() {
            assertFalse(StringUtilities.isNotBlank("\n\n"));
        }

        @Test
        public void shouldHandleBlankStringsWithTabs() {
            assertFalse(StringUtilities.isNotBlank("\t\t"));
        }

        @Test
        public void shouldHandleComplexBlankStrings() {
            assertFalse(StringUtilities.isNotBlank("\n\n  \t  \t\n  \t\n   \n\t"));
        }

        @Test
        public void shouldRejectComplexNonBlankStrings() {
            assertTrue(StringUtilities.isNotBlank("\n\n  \t abc123 \t\n  \t\n   \n\t"));
        }

        @Test
        public void shouldRejectNonBlankStrings() {
            assertTrue(StringUtilities.isNotBlank("zf-adapter"));
        }
    }

    public static class WhenTrimmingStrings {

        public static final String TEST_STRING = "%testing!complexMatch",
                BEGINNING_TRIM = "%",
                END_TRIM = "!complexMatch";

        @Test
        public void shouldStripFromBeginningOfString() {
            final String actual = StringUtilities.trim(TEST_STRING, BEGINNING_TRIM);

            assertThat(actual, not(containsString(BEGINNING_TRIM)));
            assertThat(actual.length(),  equalTo(TEST_STRING.length() - BEGINNING_TRIM.length()));
        }

        @Test
        public void shouldStripFromEndOfString() {
            final String actual = StringUtilities.trim(TEST_STRING, END_TRIM);

            assertThat(actual, not(containsString(END_TRIM)));
            assertThat(actual.length(), equalTo(TEST_STRING.length() - END_TRIM.length()));
        }

        @Test
        public void shouldNotStripIfMatchIsNotFound() {
            final String actual = StringUtilities.trim(TEST_STRING, "NEVER FIND ME");

            assertThat(actual.length(), equalTo(TEST_STRING.length()));
        }

        @Test
        public void shouldDoNothingIfTrimCharsAreLongerThanTargetString() {
            String expected, actual;

            expected = "string";
            actual = StringUtilities.trim("string", "strings");

            assertEquals(expected, actual);
        }
    }

    public static class WhenPerformingNullSafeEquals {

        @Test
        public void shouldReturnFalseIfFirstStringIsNull() {
            String one = null;
            String two = "abc";

            assertFalse(StringUtilities.nullSafeEqualsIgnoreCase(one, two));
        }

        @Test
        public void shouldReturnFalseIfSecondStringIsNull() {
            String one = "abc";
            String two = null;

            assertFalse(StringUtilities.nullSafeEqualsIgnoreCase(one, two));
        }

        @Test
        public void shouldReturnFalseIfNonNullStringsAreDifferent() {
            String one = "abc";
            String two = "def";

            assertFalse(StringUtilities.nullSafeEqualsIgnoreCase(one, two));
        }

        @Test
        public void shouldReturnTrueIfBothStringsAreNull() {
            String one = null;
            String two = null;

            assertTrue(StringUtilities.nullSafeEqualsIgnoreCase(one, two));
        }

        @Test
        public void shouldReturnTrueIfNonNullStringsAreSame() {
            String one = "abc";
            String two = "AbC";

            assertTrue(StringUtilities.nullSafeEqualsIgnoreCase(one, two));
        }
    }

    public static class WhenPerformingNullSafeStartsWith {

        @Test
        public void shouldReturnTrueIfBothStringsAreBlank() {
            final String one = "";
            final String two = "";

            assertTrue(StringUtilities.nullSafeStartsWith(one, two));
        }

        @Test
        public void shouldReturnTrueIfFirstStringStartsWithSecondString() {
            final String one = "hello my friend hello";
            final String two = "hello";

            assertTrue(StringUtilities.nullSafeStartsWith(one, two));
        }

        @Test
        public void shouldReturnFalseIfFirstStringIsNull() {
            final String one = null;
            final String two = "hello";

            assertFalse(StringUtilities.nullSafeStartsWith(one, two));
        }

        @Test
        public void shouldReturnFalseIfSecondStringIsNull() {
            final String one = "hello";
            final String two = null;

            assertFalse(StringUtilities.nullSafeStartsWith(one, two));
        }

        @Test
        public void shouldReturnFalseIfBothStringsAreNull() {
            final String one = null;
            final String two = null;

            assertFalse(StringUtilities.nullSafeStartsWith(one, two));
        }

        @Test
        public void shouldReturnFalseIfFirstStringDoesNotStartWithSecondString() {
            final String one = "this is a tribute to neil diamond, hello my friend hello";
            final String two = "hello my friend hello";

            assertFalse(StringUtilities.nullSafeStartsWith(one, two));
        }
    }
}
