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

package org.codelibs.sai.internal.objects.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for NativeMath JavaScript built-in methods.
 *
 * @test
 * @run testng org.codelibs.sai.internal.objects.test.NativeMathTest
 */
public class NativeMathTest {

    private ScriptEngine engine;
    private static final double DELTA = 0.000001;

    @BeforeClass
    public void setupEngine() {
        final ScriptEngineManager manager = new ScriptEngineManager();
        engine = manager.getEngineByName("sai");
        assertNotNull(engine, "Sai engine should be available");
    }

    @Test
    public void testMathConstants() throws ScriptException {
        // Test Math.PI
        Object result = engine.eval("Math.PI;");
        assertEquals(((Number) result).doubleValue(), Math.PI, DELTA);

        // Test Math.E
        result = engine.eval("Math.E;");
        assertEquals(((Number) result).doubleValue(), Math.E, DELTA);

        // Test Math.LN2
        result = engine.eval("Math.LN2;");
        assertEquals(((Number) result).doubleValue(), Math.log(2), DELTA);

        // Test Math.LN10
        result = engine.eval("Math.LN10;");
        assertEquals(((Number) result).doubleValue(), Math.log(10), DELTA);

        // Test Math.LOG2E
        result = engine.eval("Math.LOG2E;");
        assertEquals(((Number) result).doubleValue(), 1.0 / Math.log(2), DELTA);

        // Test Math.LOG10E
        result = engine.eval("Math.LOG10E;");
        assertEquals(((Number) result).doubleValue(), 1.0 / Math.log(10), DELTA);

        // Test Math.SQRT1_2
        result = engine.eval("Math.SQRT1_2;");
        assertEquals(((Number) result).doubleValue(), Math.sqrt(0.5), DELTA);

        // Test Math.SQRT2
        result = engine.eval("Math.SQRT2;");
        assertEquals(((Number) result).doubleValue(), Math.sqrt(2), DELTA);
    }

    @Test
    public void testAbs() throws ScriptException {
        // Test abs positive
        Object result = engine.eval("Math.abs(5);");
        assertEquals(result, 5);

        // Test abs negative
        result = engine.eval("Math.abs(-5);");
        assertEquals(result, 5);

        // Test abs zero
        result = engine.eval("Math.abs(0);");
        assertEquals(result, 0);

        // Test abs float
        result = engine.eval("Math.abs(-3.14);");
        assertEquals(((Number) result).doubleValue(), 3.14, DELTA);
    }

    @Test
    public void testCeil() throws ScriptException {
        // Test ceil positive
        Object result = engine.eval("Math.ceil(4.3);");
        assertEquals(result, 5);

        // Test ceil negative
        result = engine.eval("Math.ceil(-4.3);");
        assertEquals(result, -4);

        // Test ceil integer
        result = engine.eval("Math.ceil(5);");
        assertEquals(result, 5);

        // Test ceil zero
        result = engine.eval("Math.ceil(0);");
        assertEquals(result, 0);
    }

    @Test
    public void testFloor() throws ScriptException {
        // Test floor positive
        Object result = engine.eval("Math.floor(4.7);");
        assertEquals(result, 4);

        // Test floor negative
        result = engine.eval("Math.floor(-4.3);");
        assertEquals(result, -5);

        // Test floor integer
        result = engine.eval("Math.floor(5);");
        assertEquals(result, 5);

        // Test floor zero
        result = engine.eval("Math.floor(0);");
        assertEquals(result, 0);
    }

    @Test
    public void testRound() throws ScriptException {
        // Test round up
        Object result = engine.eval("Math.round(4.6);");
        assertEquals(result, 5);

        // Test round down
        result = engine.eval("Math.round(4.4);");
        assertEquals(result, 4);

        // Test round half
        result = engine.eval("Math.round(4.5);");
        assertEquals(result, 5);

        // Test round negative
        result = engine.eval("Math.round(-4.6);");
        assertEquals(result, -5);

        // Test round zero
        result = engine.eval("Math.round(0);");
        assertEquals(result, 0);
    }

    @Test
    public void testMax() throws ScriptException {
        // Test max two values
        Object result = engine.eval("Math.max(5, 10);");
        assertEquals(result, 10);

        // Test max multiple values
        result = engine.eval("Math.max(1, 3, 2, 5, 4);");
        assertEquals(result, 5);

        // Test max single value
        result = engine.eval("Math.max(42);");
        assertEquals(result, 42);

        // Test max no values
        result = engine.eval("Math.max();");
        assertEquals(((Number) result).doubleValue(), Double.NEGATIVE_INFINITY, DELTA);

        // Test max with negative
        result = engine.eval("Math.max(-5, -10, -1);");
        assertEquals(result, -1);
    }

    @Test
    public void testMin() throws ScriptException {
        // Test min two values
        Object result = engine.eval("Math.min(5, 10);");
        assertEquals(result, 5);

        // Test min multiple values
        result = engine.eval("Math.min(1, 3, 2, 5, 4);");
        assertEquals(result, 1);

        // Test min single value
        result = engine.eval("Math.min(42);");
        assertEquals(result, 42);

        // Test min no values
        result = engine.eval("Math.min();");
        assertEquals(((Number) result).doubleValue(), Double.POSITIVE_INFINITY, DELTA);

        // Test min with negative
        result = engine.eval("Math.min(-5, -10, -1);");
        assertEquals(result, -10);
    }

    @Test
    public void testPow() throws ScriptException {
        // Test pow basic
        Object result = engine.eval("Math.pow(2, 3);");
        assertEquals(((Number) result).doubleValue(), 8.0, DELTA);

        // Test pow zero exponent
        result = engine.eval("Math.pow(5, 0);");
        assertEquals(((Number) result).doubleValue(), 1.0, DELTA);

        // Test pow negative exponent
        result = engine.eval("Math.pow(2, -2);");
        assertEquals(((Number) result).doubleValue(), 0.25, DELTA);

        // Test pow fractional exponent
        result = engine.eval("Math.pow(4, 0.5);");
        assertEquals(((Number) result).doubleValue(), 2.0, DELTA);
    }

    @Test
    public void testSqrt() throws ScriptException {
        // Test sqrt
        Object result = engine.eval("Math.sqrt(4);");
        assertEquals(((Number) result).doubleValue(), 2.0, DELTA);

        // Test sqrt large number
        result = engine.eval("Math.sqrt(16);");
        assertEquals(((Number) result).doubleValue(), 4.0, DELTA);

        // Test sqrt zero
        result = engine.eval("Math.sqrt(0);");
        assertEquals(((Number) result).doubleValue(), 0.0, DELTA);

        // Test sqrt one
        result = engine.eval("Math.sqrt(1);");
        assertEquals(((Number) result).doubleValue(), 1.0, DELTA);

        // Test sqrt negative returns NaN
        result = engine.eval("isNaN(Math.sqrt(-1));");
        assertEquals(result, true);
    }

    @Test
    public void testTrigonometric() throws ScriptException {
        // Test sin
        Object result = engine.eval("Math.sin(0);");
        assertEquals(((Number) result).doubleValue(), 0.0, DELTA);

        // Test cos
        result = engine.eval("Math.cos(0);");
        assertEquals(((Number) result).doubleValue(), 1.0, DELTA);

        // Test tan
        result = engine.eval("Math.tan(0);");
        assertEquals(((Number) result).doubleValue(), 0.0, DELTA);

        // Test sin(PI/2)
        result = engine.eval("Math.sin(Math.PI / 2);");
        assertEquals(((Number) result).doubleValue(), 1.0, DELTA);

        // Test cos(PI)
        result = engine.eval("Math.cos(Math.PI);");
        assertEquals(((Number) result).doubleValue(), -1.0, DELTA);
    }

    @Test
    public void testInverseTrigonometric() throws ScriptException {
        // Test asin
        Object result = engine.eval("Math.asin(0);");
        assertEquals(((Number) result).doubleValue(), 0.0, DELTA);

        // Test acos
        result = engine.eval("Math.acos(1);");
        assertEquals(((Number) result).doubleValue(), 0.0, DELTA);

        // Test atan
        result = engine.eval("Math.atan(0);");
        assertEquals(((Number) result).doubleValue(), 0.0, DELTA);

        // Test atan2
        result = engine.eval("Math.atan2(0, 1);");
        assertEquals(((Number) result).doubleValue(), 0.0, DELTA);

        // Test atan2 quadrants
        result = engine.eval("Math.atan2(1, 1);");
        assertEquals(((Number) result).doubleValue(), Math.PI / 4, DELTA);
    }

    @Test
    public void testExp() throws ScriptException {
        // Test exp zero
        Object result = engine.eval("Math.exp(0);");
        assertEquals(((Number) result).doubleValue(), 1.0, DELTA);

        // Test exp one
        result = engine.eval("Math.exp(1);");
        assertEquals(((Number) result).doubleValue(), Math.E, DELTA);

        // Test exp two
        result = engine.eval("Math.exp(2);");
        assertEquals(((Number) result).doubleValue(), Math.E * Math.E, 0.0001);
    }

    @Test
    public void testLog() throws ScriptException {
        // Test log E
        Object result = engine.eval("Math.log(Math.E);");
        assertEquals(((Number) result).doubleValue(), 1.0, DELTA);

        // Test log 1
        result = engine.eval("Math.log(1);");
        assertEquals(((Number) result).doubleValue(), 0.0, DELTA);

        // Test log 10
        result = engine.eval("Math.log(10);");
        assertEquals(((Number) result).doubleValue(), Math.log(10), DELTA);
    }

    @Test
    public void testRandom() throws ScriptException {
        // Test random returns number
        Object result = engine.eval("typeof Math.random();");
        assertEquals(result, "number");

        // Test random in range [0, 1)
        result = engine.eval("var r = Math.random(); r >= 0 && r < 1;");
        assertEquals(result, true);

        // Test random different values (probabilistic)
        result = engine.eval("Math.random() !== Math.random();");
        // This could theoretically fail, but probability is extremely low
        assertTrue((Boolean) result);
    }

    @Test
    public void testSpecialValues() throws ScriptException {
        // Test Infinity
        Object result = engine.eval("Math.pow(2, 1024);");
        assertEquals(((Number) result).doubleValue(), Double.POSITIVE_INFINITY, DELTA);

        // Test NaN
        result = engine.eval("isNaN(Math.sqrt(-1));");
        assertEquals(result, true);

        // Test NaN propagation
        result = engine.eval("isNaN(Math.max(NaN, 5));");
        assertEquals(result, true);
    }

    @Test
    public void testEdgeCases() throws ScriptException {
        // Test abs infinity
        Object result = engine.eval("Math.abs(-Infinity);");
        assertEquals(((Number) result).doubleValue(), Double.POSITIVE_INFINITY, DELTA);

        // Test max with infinity
        result = engine.eval("Math.max(Infinity, 100);");
        assertEquals(((Number) result).doubleValue(), Double.POSITIVE_INFINITY, DELTA);

        // Test min with infinity
        result = engine.eval("Math.min(-Infinity, 100);");
        assertEquals(((Number) result).doubleValue(), Double.NEGATIVE_INFINITY, DELTA);

        // Test pow special cases
        result = engine.eval("Math.pow(0, 0);");
        assertEquals(((Number) result).doubleValue(), 1.0, DELTA);

        // Test log of zero
        result = engine.eval("Math.log(0);");
        assertEquals(((Number) result).doubleValue(), Double.NEGATIVE_INFINITY, DELTA);
    }
}
