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

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for NativeJSON JavaScript built-in methods.
 *
 * @test
 * @run testng org.codelibs.sai.internal.objects.test.NativeJSONTest
 */
public class NativeJSONTest {

    private ScriptEngine engine;

    @BeforeClass
    public void setupEngine() {
        final ScriptEngineManager manager = new ScriptEngineManager();
        engine = manager.getEngineByName("sai");
        assertNotNull(engine, "Sai engine should be available");
    }

    @Test
    public void testJSONParseSimpleObject() throws ScriptException {
        // Test parse simple object
        Object result = engine.eval("var obj = JSON.parse('{\"a\":1,\"b\":2}'); obj.a;");
        assertEquals(result, 1);

        // Test parse returns object
        result = engine.eval("typeof JSON.parse('{}');");
        assertEquals(result, "object");
    }

    @Test
    public void testJSONParseSimpleArray() throws ScriptException {
        // Test parse array
        Object result = engine.eval("var arr = JSON.parse('[1,2,3]'); arr.length;");
        assertEquals(result, 3);

        // Test parse array values
        result = engine.eval("var arr = JSON.parse('[1,2,3]'); arr[0];");
        assertEquals(result, 1);
    }

    @Test
    public void testJSONParsePrimitives() throws ScriptException {
        // Test parse string
        Object result = engine.eval("JSON.parse('\"hello\"');");
        assertEquals(result, "hello");

        // Test parse number
        result = engine.eval("JSON.parse('42');");
        assertEquals(result, 42);

        // Test parse true
        result = engine.eval("JSON.parse('true');");
        assertEquals(result, true);

        // Test parse false
        result = engine.eval("JSON.parse('false');");
        assertEquals(result, false);

        // Test parse null
        result = engine.eval("JSON.parse('null');");
        assertEquals(result, null);
    }

    @Test
    public void testJSONParseNestedObject() throws ScriptException {
        // Test parse nested object
        Object result = engine.eval("var obj = JSON.parse('{\"a\":{\"b\":1}}'); obj.a.b;");
        assertEquals(result, 1);

        // Test parse deeply nested
        result = engine.eval("var obj = JSON.parse('{\"a\":{\"b\":{\"c\":42}}}'); obj.a.b.c;");
        assertEquals(result, 42);
    }

    @Test
    public void testJSONParseArrayOfObjects() throws ScriptException {
        // Test parse array of objects
        Object result = engine.eval("var arr = JSON.parse('[{\"a\":1},{\"a\":2}]'); arr.length;");
        assertEquals(result, 2);

        // Test parse array of objects values
        result = engine.eval("var arr = JSON.parse('[{\"a\":1},{\"a\":2}]'); arr[0].a;");
        assertEquals(result, 1);
    }

    @Test
    public void testJSONParseWithReviver() throws ScriptException {
        // Test parse with reviver function
        Object result = engine.eval("var obj = JSON.parse('{\"a\":1,\"b\":2}', function(k,v){return typeof v === 'number' ? v * 2 : v;}); obj.a;");
        assertEquals(result, 2);

        // Test parse with reviver that filters
        result = engine.eval(
                "var obj = JSON.parse('{\"a\":1,\"b\":2}', function(k,v){return k === 'a' ? undefined : v;}); obj.a;");
        assertEquals(result, null);
    }

    @Test
    public void testJSONStringifySimpleObject() throws ScriptException {
        // Test stringify simple object
        Object result = engine.eval("JSON.stringify({a:1,b:2});");
        assertEquals(result, "{\"a\":1,\"b\":2}");

        // Test stringify empty object
        result = engine.eval("JSON.stringify({});");
        assertEquals(result, "{}");
    }

    @Test
    public void testJSONStringifySimpleArray() throws ScriptException {
        // Test stringify array
        Object result = engine.eval("JSON.stringify([1,2,3]);");
        assertEquals(result, "[1,2,3]");

        // Test stringify empty array
        result = engine.eval("JSON.stringify([]);");
        assertEquals(result, "[]");
    }

    @Test
    public void testJSONStringifyPrimitives() throws ScriptException {
        // Test stringify string
        Object result = engine.eval("JSON.stringify('hello');");
        assertEquals(result, "\"hello\"");

        // Test stringify number
        result = engine.eval("JSON.stringify(42);");
        assertEquals(result, "42");

        // Test stringify true
        result = engine.eval("JSON.stringify(true);");
        assertEquals(result, "true");

        // Test stringify false
        result = engine.eval("JSON.stringify(false);");
        assertEquals(result, "false");

        // Test stringify null
        result = engine.eval("JSON.stringify(null);");
        assertEquals(result, "null");
    }

    @Test
    public void testJSONStringifyNestedObject() throws ScriptException {
        // Test stringify nested object
        Object result = engine.eval("JSON.stringify({a:{b:1}});");
        assertEquals(result, "{\"a\":{\"b\":1}}");

        // Test stringify deeply nested
        result = engine.eval("JSON.stringify({a:{b:{c:42}}});");
        assertEquals(result, "{\"a\":{\"b\":{\"c\":42}}}");
    }

    @Test
    public void testJSONStringifyArrayOfObjects() throws ScriptException {
        // Test stringify array of objects
        Object result = engine.eval("JSON.stringify([{a:1},{a:2}]);");
        assertEquals(result, "[{\"a\":1},{\"a\":2}]");
    }

    @Test
    public void testJSONStringifyWithReplacer() throws ScriptException {
        // Test stringify with replacer function
        Object result = engine.eval("JSON.stringify({a:1,b:2}, function(k,v){return typeof v === 'number' ? v * 2 : v;});");
        assertEquals(result, "{\"a\":2,\"b\":4}");

        // Test stringify with replacer array
        result = engine.eval("JSON.stringify({a:1,b:2,c:3}, ['a','c']);");
        assertEquals(result, "{\"a\":1,\"c\":3}");
    }

    @Test
    public void testJSONStringifyWithSpace() throws ScriptException {
        // Test stringify with space number
        Object result = engine.eval("JSON.stringify({a:1,b:2}, null, 2);");
        assertNotNull(result);
        assertEquals(((String) result).contains("\n"), true);

        // Test stringify with space string
        result = engine.eval("JSON.stringify({a:1}, null, '  ');");
        assertNotNull(result);
        assertEquals(((String) result).contains("\n"), true);
    }

    @Test
    public void testJSONStringifySpecialValues() throws ScriptException {
        // Test stringify undefined
        Object result = engine.eval("JSON.stringify(undefined);");
        assertEquals(result, null);

        // Test stringify function
        result = engine.eval("JSON.stringify(function(){});");
        assertEquals(result, null);

        // Test stringify object with undefined values
        result = engine.eval("JSON.stringify({a:1,b:undefined,c:2});");
        assertEquals(result, "{\"a\":1,\"c\":2}");

        // Test stringify array with undefined
        result = engine.eval("JSON.stringify([1,undefined,2]);");
        assertEquals(result, "[1,null,2]");
    }

    @Test
    public void testJSONStringifyInfinity() throws ScriptException {
        // Test stringify Infinity
        Object result = engine.eval("JSON.stringify(Infinity);");
        assertEquals(result, "null");

        // Test stringify -Infinity
        result = engine.eval("JSON.stringify(-Infinity);");
        assertEquals(result, "null");

        // Test stringify NaN
        result = engine.eval("JSON.stringify(NaN);");
        assertEquals(result, "null");
    }

    @Test
    public void testJSONRoundTrip() throws ScriptException {
        // Test roundtrip object
        Object result = engine.eval("var obj = {a:1,b:2}; var str = JSON.stringify(obj); var obj2 = JSON.parse(str); obj2.a;");
        assertEquals(result, 1);

        // Test roundtrip array
        result = engine.eval("var arr = [1,2,3]; var str = JSON.stringify(arr); var arr2 = JSON.parse(str); arr2[0];");
        assertEquals(result, 1);

        // Test roundtrip nested
        result = engine.eval(
                "var obj = {a:{b:[1,2,3]}}; var str = JSON.stringify(obj); var obj2 = JSON.parse(str); obj2.a.b[1];");
        assertEquals(result, 2);
    }

    @Test
    public void testJSONStringifyEscaping() throws ScriptException {
        // Test stringify escapes quotes
        Object result = engine.eval("JSON.stringify('hello \"world\"');");
        assertEquals(result, "\"hello \\\"world\\\"\"");

        // Test stringify escapes backslash
        result = engine.eval("JSON.stringify('hello\\\\world');");
        assertNotNull(result);
        assertEquals(((String) result).contains("\\\\"), true);

        // Test stringify escapes newline
        result = engine.eval("JSON.stringify('hello\\nworld');");
        assertEquals(result, "\"hello\\nworld\"");
    }

    @Test
    public void testJSONParseErrors() throws ScriptException {
        // Test parse invalid JSON throws error
        try {
            engine.eval("JSON.parse('{invalid}');");
            assertEquals(true, false, "Should have thrown an error");
        } catch (final ScriptException e) {
            // Expected
        }

        // Test parse unclosed object throws error
        try {
            engine.eval("JSON.parse('{\"a\":1');");
            assertEquals(true, false, "Should have thrown an error");
        } catch (final ScriptException e) {
            // Expected
        }

        // Test parse unclosed array throws error
        try {
            engine.eval("JSON.parse('[1,2,3');");
            assertEquals(true, false, "Should have thrown an error");
        } catch (final ScriptException e) {
            // Expected
        }
    }

    @Test
    public void testJSONStringifyCircularReference() throws ScriptException {
        // Test stringify circular reference throws error
        try {
            engine.eval("var obj = {}; obj.self = obj; JSON.stringify(obj);");
            assertEquals(true, false, "Should have thrown an error");
        } catch (final ScriptException e) {
            // Expected
        }
    }

    @Test
    public void testJSONToJSON() throws ScriptException {
        // Test stringify with toJSON method
        Object result = engine.eval("var obj = {a:1, toJSON: function(){return {b:2};}}; JSON.stringify(obj);");
        assertEquals(result, "{\"b\":2}");

        // Test Date toJSON
        result = engine.eval("var d = new Date(0); JSON.stringify(d);");
        assertEquals(result, "\"1970-01-01T00:00:00.000Z\"");
    }

    @Test
    public void testJSONStringifyEmptyStrings() throws ScriptException {
        // Test stringify empty string
        Object result = engine.eval("JSON.stringify('');");
        assertEquals(result, "\"\"");

        // Test stringify object with empty string
        result = engine.eval("JSON.stringify({a:''});");
        assertEquals(result, "{\"a\":\"\"}");
    }

    @Test
    public void testJSONStringifyNumbers() throws ScriptException {
        // Test stringify integer
        Object result = engine.eval("JSON.stringify(42);");
        assertEquals(result, "42");

        // Test stringify float
        result = engine.eval("JSON.stringify(3.14);");
        assertEquals(result, "3.14");

        // Test stringify negative
        result = engine.eval("JSON.stringify(-42);");
        assertEquals(result, "-42");

        // Test stringify zero
        result = engine.eval("JSON.stringify(0);");
        assertEquals(result, "0");
    }
}
