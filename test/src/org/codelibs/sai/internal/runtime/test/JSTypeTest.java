/*
 * Copyright (c) 2010, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package org.codelibs.sai.internal.runtime.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.codelibs.sai.internal.runtime.JSType;
import org.codelibs.sai.internal.runtime.ScriptRuntime;
import org.testng.annotations.Test;

/**
 * Tests for JSType methods.
 *
 * @test
 * @run testng org.codelibs.sai.internal.runtime.test.JSTypeTest
 */
public class JSTypeTest {
    /**
     * Test of isPrimitive method, of class Runtime.
     */
    @Test
    public void testIsPrimitive() {
        assertTrue(JSType.isPrimitive(null));
        assertTrue(JSType.isPrimitive(ScriptRuntime.UNDEFINED));
        assertTrue(JSType.isPrimitive(Double.NaN));
        assertTrue(JSType.isPrimitive(Double.NEGATIVE_INFINITY));
        assertTrue(JSType.isPrimitive(Double.POSITIVE_INFINITY));
        assertTrue(JSType.isPrimitive(0.0));
        assertTrue(JSType.isPrimitive(3.14));
        assertTrue(JSType.isPrimitive("hello"));
        assertTrue(JSType.isPrimitive(""));
        assertFalse(JSType.isPrimitive(new Object()));
    }

    /**
     * Test of toBoolean method, of class Runtime.
     */
    @Test
    public void testToBoolean() {
        assertFalse(JSType.toBoolean(ScriptRuntime.UNDEFINED));
        assertFalse(JSType.toBoolean(null));
        assertFalse(JSType.toBoolean(Boolean.FALSE));
        assertTrue(JSType.toBoolean(Boolean.TRUE));
        assertFalse(JSType.toBoolean(-0.0));
        assertFalse(JSType.toBoolean(0.0));
        assertFalse(JSType.toBoolean(Double.NaN));
        assertTrue(JSType.toBoolean(3.14));
        assertFalse(JSType.toBoolean(""));
        assertTrue(JSType.toBoolean("javascript"));
        assertTrue(JSType.toBoolean(new Object()));
    }

    /**
     * Test of toNumber method, of class Runtime.
     */
    @Test
    public void testToNumber_Object() {
        assertTrue(Double.isNaN(JSType.toNumber(ScriptRuntime.UNDEFINED)));
        assertEquals(JSType.toNumber((Object) null), 0.0, 0.0);
        assertEquals(JSType.toNumber(Boolean.TRUE), 1.0, 0.0);
        assertEquals(JSType.toNumber(Boolean.FALSE), 0.0, 0.0);
        assertEquals(JSType.toNumber(3.14), 3.14, 0.0);
    }

    /**
     * Test of toNumber with String input - covers various string to number conversions.
     */
    @Test
    public void testToNumber_String() {
        // Empty string should be 0
        assertEquals(JSType.toNumber(""), 0.0, 0.0);

        // Whitespace only should be 0
        assertEquals(JSType.toNumber("   "), 0.0, 0.0);
        assertEquals(JSType.toNumber("\t\n"), 0.0, 0.0);

        // Integer strings
        assertEquals(JSType.toNumber("42"), 42.0, 0.0);
        assertEquals(JSType.toNumber("-42"), -42.0, 0.0);
        assertEquals(JSType.toNumber("+42"), 42.0, 0.0);

        // Floating point strings
        assertEquals(JSType.toNumber("3.14"), 3.14, 0.0001);
        assertEquals(JSType.toNumber("-3.14"), -3.14, 0.0001);
        assertEquals(JSType.toNumber(".5"), 0.5, 0.0);
        assertEquals(JSType.toNumber("-.5"), -0.5, 0.0);

        // Scientific notation
        assertEquals(JSType.toNumber("1e10"), 1e10, 0.0);
        assertEquals(JSType.toNumber("1E10"), 1E10, 0.0);
        assertEquals(JSType.toNumber("1e-10"), 1e-10, 0.0);
        assertEquals(JSType.toNumber("2.5e3"), 2500.0, 0.0);

        // Hexadecimal
        assertEquals(JSType.toNumber("0x10"), 16.0, 0.0);
        assertEquals(JSType.toNumber("0X10"), 16.0, 0.0);
        assertEquals(JSType.toNumber("0xFF"), 255.0, 0.0);
        assertEquals(JSType.toNumber("0x0"), 0.0, 0.0);

        // Octal (ES5 strict mode does not support octal literals, but legacy support)
        assertEquals(JSType.toNumber("010"), 10.0, 0.0); // Treated as decimal in ES5

        // Infinity
        assertEquals(JSType.toNumber("Infinity"), Double.POSITIVE_INFINITY, 0.0);
        assertEquals(JSType.toNumber("-Infinity"), Double.NEGATIVE_INFINITY, 0.0);
        assertEquals(JSType.toNumber("+Infinity"), Double.POSITIVE_INFINITY, 0.0);

        // NaN cases
        assertTrue(Double.isNaN(JSType.toNumber("NaN")));
        assertTrue(Double.isNaN(JSType.toNumber("hello")));
        assertTrue(Double.isNaN(JSType.toNumber("12abc")));
        assertTrue(Double.isNaN(JSType.toNumber("abc12")));

        // Strings with leading/trailing whitespace
        assertEquals(JSType.toNumber("  42  "), 42.0, 0.0);
        assertEquals(JSType.toNumber("\t42\n"), 42.0, 0.0);
    }

    /**
     * Test of toNumber with special double values.
     */
    @Test
    public void testToNumber_SpecialDoubles() {
        // Positive and negative zero
        assertEquals(JSType.toNumber(0.0), 0.0, 0.0);
        assertEquals(JSType.toNumber(-0.0), -0.0, 0.0);

        // Infinity
        assertEquals(JSType.toNumber(Double.POSITIVE_INFINITY), Double.POSITIVE_INFINITY, 0.0);
        assertEquals(JSType.toNumber(Double.NEGATIVE_INFINITY), Double.NEGATIVE_INFINITY, 0.0);

        // NaN
        assertTrue(Double.isNaN(JSType.toNumber(Double.NaN)));

        // Max and min values
        assertEquals(JSType.toNumber(Double.MAX_VALUE), Double.MAX_VALUE, 0.0);
        assertEquals(JSType.toNumber(Double.MIN_VALUE), Double.MIN_VALUE, 0.0);
    }

    /**
     * Test of toString method, of class Runtime.
     */
    @Test
    public void testToString_Object() {
        assertEquals(JSType.toString(ScriptRuntime.UNDEFINED), "undefined");
        assertEquals(JSType.toString(null), "null");
        assertEquals(JSType.toString(Boolean.TRUE), "true");
        assertEquals(JSType.toString(Boolean.FALSE), "false");
        assertEquals(JSType.toString(""), "");
        assertEquals(JSType.toString("sai"), "sai");
        assertEquals(JSType.toString(Double.NaN), "NaN");
        assertEquals(JSType.toString(Double.POSITIVE_INFINITY), "Infinity");
        assertEquals(JSType.toString(Double.NEGATIVE_INFINITY), "-Infinity");
        assertEquals(JSType.toString(0.0), "0");
    }

    /**
     * Test of toString with various number types.
     */
    @Test
    public void testToString_Numbers() {
        // Integers
        assertEquals(JSType.toString(1), "1");
        assertEquals(JSType.toString(42), "42");
        assertEquals(JSType.toString(-42), "-42");
        assertEquals(JSType.toString(0), "0");

        // Negative zero
        assertEquals(JSType.toString(-0.0), "0");

        // Large integers
        assertEquals(JSType.toString(1000000), "1000000");
        assertEquals(JSType.toString(Integer.MAX_VALUE), String.valueOf(Integer.MAX_VALUE));
        assertEquals(JSType.toString(Integer.MIN_VALUE), String.valueOf(Integer.MIN_VALUE));

        // Floating point
        assertEquals(JSType.toString(3.14), "3.14");
        assertEquals(JSType.toString(-3.14), "-3.14");
        assertEquals(JSType.toString(0.5), "0.5");
        assertEquals(JSType.toString(0.1), "0.1");

        // Very small numbers use exponential notation
        assertEquals(JSType.toString(1e-7), "1e-7");
        assertEquals(JSType.toString(0.0000001), "1e-7");

        // Very large numbers use exponential notation
        assertEquals(JSType.toString(1e20), "100000000000000000000");
        assertEquals(JSType.toString(1e21), "1e+21");

        // Integer-valued doubles
        assertEquals(JSType.toString(1.0), "1");
        assertEquals(JSType.toString(42.0), "42");
        assertEquals(JSType.toString(100.0), "100");
    }

    /**
     * Test of toString with Long values.
     * Note: Long values go through toString(Object) which converts to double first.
     */
    @Test
    public void testToString_Long() {
        assertEquals(JSType.toString(1L), "1");
        assertEquals(JSType.toString(42L), "42");
        assertEquals(JSType.toString(-42L), "-42");
        assertEquals(JSType.toString(0L), "0");
        // Large longs lose precision when converted to double
        assertEquals(JSType.toString(1000000L), "1000000");
    }

    /**
     * Test isNumber method.
     */
    @Test
    public void testIsNumber() {
        assertTrue(JSType.isNumber(42));
        assertTrue(JSType.isNumber(3.14));
        assertTrue(JSType.isNumber(3.14f));
        assertTrue(JSType.isNumber(Double.NaN));
        assertTrue(JSType.isNumber(Double.POSITIVE_INFINITY));
        assertTrue(JSType.isNumber((short) 1));
        assertTrue(JSType.isNumber((byte) 1));

        // Long is not considered a number in JSType.isNumber
        assertFalse(JSType.isNumber(42L));
        assertFalse(JSType.isNumber("42"));
        assertFalse(JSType.isNumber(null));
        assertFalse(JSType.isNumber(ScriptRuntime.UNDEFINED));
        assertFalse(JSType.isNumber(Boolean.TRUE));
        assertFalse(JSType.isNumber(new Object()));
    }

    /**
     * Test isString method.
     */
    @Test
    public void testIsString() {
        assertTrue(JSType.isString(""));
        assertTrue(JSType.isString("hello"));
        assertTrue(JSType.isString("42"));

        assertFalse(JSType.isString(42));
        assertFalse(JSType.isString(null));
        assertFalse(JSType.isString(ScriptRuntime.UNDEFINED));
        assertFalse(JSType.isString(Boolean.TRUE));
        assertFalse(JSType.isString(new Object()));
    }

    /**
     * Test type name conversions.
     */
    @Test
    public void testTypeOf() {
        assertEquals(JSType.UNDEFINED.typeName(), "undefined");
        assertEquals(JSType.NULL.typeName(), "object");
        assertEquals(JSType.BOOLEAN.typeName(), "boolean");
        assertEquals(JSType.NUMBER.typeName(), "number");
        assertEquals(JSType.STRING.typeName(), "string");
        assertEquals(JSType.OBJECT.typeName(), "object");
        assertEquals(JSType.FUNCTION.typeName(), "function");
    }

    /**
     * Test toInteger conversion.
     */
    @Test
    public void testToInteger() {
        assertEquals(JSType.toInteger(3.7), 3);
        assertEquals(JSType.toInteger(3.2), 3);
        assertEquals(JSType.toInteger(-3.7), -3);
        assertEquals(JSType.toInteger(-3.2), -3);
        assertEquals(JSType.toInteger(0.0), 0);
        assertEquals(JSType.toInteger(-0.0), 0);
        assertEquals(JSType.toInteger(Double.NaN), 0);
        assertEquals(JSType.toInteger(Double.POSITIVE_INFINITY), Integer.MAX_VALUE);
        assertEquals(JSType.toInteger(Double.NEGATIVE_INFINITY), Integer.MIN_VALUE);
    }

    /**
     * Test of JSType.toUint32(double)
     */
    @Test
    public void testToUint32() {
        assertEquals(JSType.toUint32(+0.0), 0);
        assertEquals(JSType.toUint32(-0.0), 0);
        assertEquals(JSType.toUint32(Double.NaN), 0);
        assertEquals(JSType.toUint32(Double.POSITIVE_INFINITY), 0);
        assertEquals(JSType.toUint32(Double.NEGATIVE_INFINITY), 0);
        assertEquals(JSType.toUint32(9223372036854775807.0d), 0);
        assertEquals(JSType.toUint32(-9223372036854775807.0d), 0);
        assertEquals(JSType.toUint32(1099511627776.0d), 0);
        assertEquals(JSType.toUint32(-1099511627776.0d), 0);
        assertEquals(JSType.toUint32(4294967295.0d), 4294967295l);
        assertEquals(JSType.toUint32(4294967296.0d), 0);
        assertEquals(JSType.toUint32(4294967297.0d), 1);
        assertEquals(JSType.toUint32(-4294967295.0d), 1);
        assertEquals(JSType.toUint32(-4294967296.0d), 0);
        assertEquals(JSType.toUint32(-4294967297.0d), 4294967295l);
        assertEquals(JSType.toUint32(4294967295.6d), 4294967295l);
        assertEquals(JSType.toUint32(4294967296.6d), 0);
        assertEquals(JSType.toUint32(4294967297.6d), 1);
        assertEquals(JSType.toUint32(-4294967295.6d), 1);
        assertEquals(JSType.toUint32(-4294967296.6d), 0);
        assertEquals(JSType.toUint32(-4294967297.6d), 4294967295l);
    }

    /**
     * Test of JSType.toInt32(double)
     */
    @Test
    public void testToInt32() {
        assertEquals(JSType.toInt32(+0.0), 0);
        assertEquals(JSType.toInt32(-0.0), 0);
        assertEquals(JSType.toInt32(Double.NaN), 0);
        assertEquals(JSType.toInt32(Double.POSITIVE_INFINITY), 0);
        assertEquals(JSType.toInt32(Double.NEGATIVE_INFINITY), 0);
        assertEquals(JSType.toInt32(9223372036854775807.0d), 0);
        assertEquals(JSType.toInt32(-9223372036854775807.0d), 0);
        assertEquals(JSType.toInt32(1099511627776.0d), 0);
        assertEquals(JSType.toInt32(-1099511627776.0d), 0);
        assertEquals(JSType.toInt32(4294967295.0d), -1);
        assertEquals(JSType.toInt32(4294967296.0d), 0);
        assertEquals(JSType.toInt32(4294967297.0d), 1);
        assertEquals(JSType.toInt32(-4294967295.0d), 1);
        assertEquals(JSType.toInt32(-4294967296.0d), 0);
        assertEquals(JSType.toInt32(-4294967297.d), -1);
        assertEquals(JSType.toInt32(4294967295.6d), -1);
        assertEquals(JSType.toInt32(4294967296.6d), 0);
        assertEquals(JSType.toInt32(4294967297.6d), 1);
        assertEquals(JSType.toInt32(-4294967295.6d), 1);
        assertEquals(JSType.toInt32(-4294967296.6d), 0);
        assertEquals(JSType.toInt32(-4294967297.6d), -1);
    }

    /**
     * Test of JSType.toUint16(double)
     */
    @Test
    public void testToUint16() {
        assertEquals(JSType.toUint16(+0.0), 0);
        assertEquals(JSType.toUint16(-0.0), 0);
        assertEquals(JSType.toUint16(Double.NaN), 0);
        assertEquals(JSType.toUint16(Double.POSITIVE_INFINITY), 0);
        assertEquals(JSType.toUint16(Double.NEGATIVE_INFINITY), 0);
        assertEquals(JSType.toUint16(9223372036854775807.0d), 0);
        assertEquals(JSType.toUint16(-9223372036854775807.0d), 0);
        assertEquals(JSType.toUint16(1099511627776.0d), 0);
        assertEquals(JSType.toUint16(-1099511627776.0d), 0);
        assertEquals(JSType.toUint16(4294967295.0d), 65535);
        assertEquals(JSType.toUint16(4294967296.0d), 0);
        assertEquals(JSType.toUint16(4294967297.0d), 1);
        assertEquals(JSType.toUint16(-4294967295.0d), 1);
        assertEquals(JSType.toUint16(-4294967296.0d), 0);
        assertEquals(JSType.toUint16(-4294967297.0d), 65535);
        assertEquals(JSType.toUint16(4294967295.6d), 65535);
        assertEquals(JSType.toUint16(4294967296.6d), 0);
        assertEquals(JSType.toUint16(4294967297.6d), 1);
        assertEquals(JSType.toUint16(-4294967295.6d), 1);
        assertEquals(JSType.toUint16(-4294967296.6d), 0);
        assertEquals(JSType.toUint16(-4294967297.6d), 65535);
    }

}
