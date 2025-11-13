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
 * Tests for NativeDate JavaScript built-in methods.
 *
 * @test
 * @run testng org.codelibs.sai.internal.objects.test.NativeDateTest
 */
public class NativeDateTest {

    private ScriptEngine engine;

    @BeforeClass
    public void setupEngine() {
        final ScriptEngineManager manager = new ScriptEngineManager();
        engine = manager.getEngineByName("sai");
        assertNotNull(engine, "Sai engine should be available");
    }

    @Test
    public void testDateConstructor() throws ScriptException {
        // Test Date constructor with no args returns Date object
        Object result = engine.eval("typeof new Date();");
        assertEquals(result, "object");

        // Test Date constructor with milliseconds
        result = engine.eval("var d = new Date(0); d.getTime();");
        assertEquals(((Number) result).intValue(), 0);

        // Test Date constructor with date string
        result = engine.eval("var d = new Date('2020-01-01'); d.getFullYear();");
        assertEquals(((Number) result).intValue(), 2020);

        // Test Date constructor with year, month
        result = engine.eval("var d = new Date(2020, 0); d.getFullYear();");
        assertEquals(((Number) result).intValue(), 2020);

        // Test Date constructor with year, month, day
        result = engine.eval("var d = new Date(2020, 0, 15); d.getDate();");
        assertEquals(((Number) result).intValue(), 15);
    }

    @Test
    public void testDateNow() throws ScriptException {
        // Test Date.now() returns number
        Object result = engine.eval("typeof Date.now();");
        assertEquals(result, "number");

        // Test Date.now() returns positive value
        result = engine.eval("Date.now() > 0;");
        assertEquals(result, true);

        // Test Date.now() increases over time
        result = engine.eval("var t1 = Date.now(); var t2 = Date.now(); t2 >= t1;");
        assertEquals(result, true);
    }

    @Test
    public void testDateParse() throws ScriptException {
        // Test Date.parse with ISO string
        Object result = engine.eval("typeof Date.parse('2020-01-01');");
        assertEquals(result, "number");

        // Test Date.parse returns number
        result = engine.eval("!isNaN(Date.parse('2020-01-01'));");
        assertEquals(result, true);

        // Test Date.parse invalid string returns NaN
        result = engine.eval("isNaN(Date.parse('invalid'));");
        assertEquals(result, true);
    }

    @Test
    public void testDateUTC() throws ScriptException {
        // Test Date.UTC
        Object result = engine.eval("typeof Date.UTC(2020, 0, 1);");
        assertEquals(result, "number");

        // Test Date.UTC returns milliseconds
        result = engine.eval("Date.UTC(1970, 0, 1);");
        assertEquals(((Number) result).intValue(), 0);

        // Test Date.UTC with full parameters
        result = engine.eval("typeof Date.UTC(2020, 0, 1, 12, 30, 45, 500);");
        assertEquals(result, "number");
    }

    @Test
    public void testGetTime() throws ScriptException {
        // Test getTime
        Object result = engine.eval("var d = new Date(1000); d.getTime();");
        assertEquals(((Number) result).intValue(), 1000);

        // Test getTime returns number
        result = engine.eval("typeof new Date().getTime();");
        assertEquals(result, "number");
    }

    @Test
    public void testSetTime() throws ScriptException {
        // Test setTime
        Object result = engine.eval("var d = new Date(); d.setTime(1000); d.getTime();");
        assertEquals(((Number) result).intValue(), 1000);

        // Test setTime returns timestamp
        result = engine.eval("var d = new Date(); d.setTime(2000);");
        assertEquals(((Number) result).intValue(), 2000);
    }

    @Test
    public void testGetFullYear() throws ScriptException {
        // Test getFullYear
        Object result = engine.eval("var d = new Date('2020-06-15'); d.getFullYear();");
        assertEquals(((Number) result).intValue(), 2020);

        // Test getFullYear returns number
        result = engine.eval("typeof new Date().getFullYear();");
        assertEquals(result, "number");
    }

    @Test
    public void testSetFullYear() throws ScriptException {
        // Test setFullYear
        Object result = engine.eval("var d = new Date('2020-06-15'); d.setFullYear(2021); d.getFullYear();");
        assertEquals(((Number) result).intValue(), 2021);

        // Test setFullYear with month and day
        result = engine.eval("var d = new Date(); d.setFullYear(2020, 5, 15); d.getFullYear();");
        assertEquals(((Number) result).intValue(), 2020);
    }

    @Test
    public void testGetMonth() throws ScriptException {
        // Test getMonth (0-based)
        Object result = engine.eval("var d = new Date('2020-06-15'); d.getMonth();");
        assertEquals(((Number) result).intValue(), 5); // June is month 5 (0-based)

        // Test getMonth January
        result = engine.eval("var d = new Date('2020-01-15'); d.getMonth();");
        assertEquals(((Number) result).intValue(), 0);
    }

    @Test
    public void testSetMonth() throws ScriptException {
        // Test setMonth
        Object result = engine.eval("var d = new Date('2020-01-15'); d.setMonth(5); d.getMonth();");
        assertEquals(((Number) result).intValue(), 5);

        // Test setMonth with day
        result = engine.eval("var d = new Date('2020-01-15'); d.setMonth(5, 20); d.getDate();");
        assertEquals(((Number) result).intValue(), 20);
    }

    @Test
    public void testGetDate() throws ScriptException {
        // Test getDate
        Object result = engine.eval("var d = new Date('2020-06-15'); d.getDate();");
        assertEquals(((Number) result).intValue(), 15);

        // Test getDate first of month
        result = engine.eval("var d = new Date('2020-06-01'); d.getDate();");
        assertEquals(((Number) result).intValue(), 1);
    }

    @Test
    public void testSetDate() throws ScriptException {
        // Test setDate
        Object result = engine.eval("var d = new Date('2020-06-15'); d.setDate(20); d.getDate();");
        assertEquals(((Number) result).intValue(), 20);

        // Test setDate first of month
        result = engine.eval("var d = new Date('2020-06-15'); d.setDate(1); d.getDate();");
        assertEquals(((Number) result).intValue(), 1);
    }

    @Test
    public void testGetDay() throws ScriptException {
        // Test getDay (day of week, 0=Sunday)
        Object result = engine.eval("typeof new Date().getDay();");
        assertEquals(result, "number");

        // Test getDay in range
        result = engine.eval("var day = new Date().getDay(); day >= 0 && day <= 6;");
        assertEquals(result, true);
    }

    @Test
    public void testGetHours() throws ScriptException {
        // Test getHours
        Object result = engine.eval("var d = new Date(2020, 0, 1, 15, 30); d.getHours();");
        assertEquals(((Number) result).intValue(), 15);

        // Test getHours returns number
        result = engine.eval("typeof new Date().getHours();");
        assertEquals(result, "number");
    }

    @Test
    public void testSetHours() throws ScriptException {
        // Test setHours
        Object result = engine.eval("var d = new Date(2020, 0, 1, 10, 30); d.setHours(15); d.getHours();");
        assertEquals(((Number) result).intValue(), 15);

        // Test setHours with minutes, seconds, milliseconds
        result = engine.eval("var d = new Date(); d.setHours(15, 30, 45, 500); d.getHours();");
        assertEquals(((Number) result).intValue(), 15);
    }

    @Test
    public void testGetMinutes() throws ScriptException {
        // Test getMinutes
        Object result = engine.eval("var d = new Date(2020, 0, 1, 15, 30); d.getMinutes();");
        assertEquals(((Number) result).intValue(), 30);

        // Test getMinutes returns number
        result = engine.eval("typeof new Date().getMinutes();");
        assertEquals(result, "number");
    }

    @Test
    public void testSetMinutes() throws ScriptException {
        // Test setMinutes
        Object result = engine.eval("var d = new Date(2020, 0, 1, 15, 20); d.setMinutes(45); d.getMinutes();");
        assertEquals(((Number) result).intValue(), 45);

        // Test setMinutes with seconds and milliseconds
        result = engine.eval("var d = new Date(); d.setMinutes(30, 45, 500); d.getMinutes();");
        assertEquals(((Number) result).intValue(), 30);
    }

    @Test
    public void testGetSeconds() throws ScriptException {
        // Test getSeconds
        Object result = engine.eval("var d = new Date(2020, 0, 1, 15, 30, 45); d.getSeconds();");
        assertEquals(((Number) result).intValue(), 45);

        // Test getSeconds returns number
        result = engine.eval("typeof new Date().getSeconds();");
        assertEquals(result, "number");
    }

    @Test
    public void testSetSeconds() throws ScriptException {
        // Test setSeconds
        Object result = engine.eval("var d = new Date(2020, 0, 1, 15, 30, 20); d.setSeconds(45); d.getSeconds();");
        assertEquals(((Number) result).intValue(), 45);

        // Test setSeconds with milliseconds
        result = engine.eval("var d = new Date(); d.setSeconds(30, 500); d.getSeconds();");
        assertEquals(((Number) result).intValue(), 30);
    }

    @Test
    public void testGetMilliseconds() throws ScriptException {
        // Test getMilliseconds
        Object result = engine.eval("var d = new Date(2020, 0, 1, 15, 30, 45, 500); d.getMilliseconds();");
        assertEquals(((Number) result).intValue(), 500);

        // Test getMilliseconds returns number
        result = engine.eval("typeof new Date().getMilliseconds();");
        assertEquals(result, "number");
    }

    @Test
    public void testSetMilliseconds() throws ScriptException {
        // Test setMilliseconds
        Object result = engine.eval("var d = new Date(2020, 0, 1, 15, 30, 45, 100); d.setMilliseconds(500); d.getMilliseconds();");
        assertEquals(((Number) result).intValue(), 500);
    }

    @Test
    public void testGetUTCMethods() throws ScriptException {
        // Test getUTCFullYear
        Object result = engine.eval("typeof new Date().getUTCFullYear();");
        assertEquals(result, "number");

        // Test getUTCMonth
        result = engine.eval("typeof new Date().getUTCMonth();");
        assertEquals(result, "number");

        // Test getUTCDate
        result = engine.eval("typeof new Date().getUTCDate();");
        assertEquals(result, "number");

        // Test getUTCHours
        result = engine.eval("typeof new Date().getUTCHours();");
        assertEquals(result, "number");

        // Test getUTCMinutes
        result = engine.eval("typeof new Date().getUTCMinutes();");
        assertEquals(result, "number");

        // Test getUTCSeconds
        result = engine.eval("typeof new Date().getUTCSeconds();");
        assertEquals(result, "number");

        // Test getUTCMilliseconds
        result = engine.eval("typeof new Date().getUTCMilliseconds();");
        assertEquals(result, "number");
    }

    @Test
    public void testToString() throws ScriptException {
        // Test toString returns string
        Object result = engine.eval("typeof new Date().toString();");
        assertEquals(result, "string");

        // Test toString not empty
        result = engine.eval("new Date().toString().length > 0;");
        assertEquals(result, true);
    }

    @Test
    public void testToDateString() throws ScriptException {
        // Test toDateString returns string
        Object result = engine.eval("typeof new Date().toDateString();");
        assertEquals(result, "string");

        // Test toDateString not empty
        result = engine.eval("new Date().toDateString().length > 0;");
        assertEquals(result, true);
    }

    @Test
    public void testToTimeString() throws ScriptException {
        // Test toTimeString returns string
        Object result = engine.eval("typeof new Date().toTimeString();");
        assertEquals(result, "string");

        // Test toTimeString not empty
        result = engine.eval("new Date().toTimeString().length > 0;");
        assertEquals(result, true);
    }

    @Test
    public void testToISOString() throws ScriptException {
        // Test toISOString returns string
        Object result = engine.eval("typeof new Date().toISOString();");
        assertEquals(result, "string");

        // Test toISOString format
        result = engine.eval("var d = new Date(0); d.toISOString();");
        assertEquals(result, "1970-01-01T00:00:00.000Z");
    }

    @Test
    public void testToJSON() throws ScriptException {
        // Test toJSON returns string
        Object result = engine.eval("typeof new Date().toJSON();");
        assertEquals(result, "string");

        // Test toJSON format
        result = engine.eval("var d = new Date(0); d.toJSON();");
        assertEquals(result, "1970-01-01T00:00:00.000Z");
    }

    @Test
    public void testValueOf() throws ScriptException {
        // Test valueOf returns number
        Object result = engine.eval("typeof new Date().valueOf();");
        assertEquals(result, "number");

        // Test valueOf same as getTime
        result = engine.eval("var d = new Date(1000); d.valueOf() === d.getTime();");
        assertEquals(result, true);
    }

    @Test
    public void testDateComparison() throws ScriptException {
        // Test date comparison
        Object result = engine.eval("var d1 = new Date(1000); var d2 = new Date(2000); d1 < d2;");
        assertEquals(result, true);

        // Test date equality
        result = engine.eval("var d1 = new Date(1000); var d2 = new Date(1000); d1.getTime() === d2.getTime();");
        assertEquals(result, true);
    }

    @Test
    public void testInvalidDate() throws ScriptException {
        // Test invalid date
        Object result = engine.eval("isNaN(new Date('invalid').getTime());");
        assertEquals(result, true);

        // Test NaN propagation
        result = engine.eval("var d = new Date(NaN); isNaN(d.getTime());");
        assertEquals(result, true);
    }
}
