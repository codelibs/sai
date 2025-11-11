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
 * Tests for NativeString JavaScript built-in methods.
 *
 * @test
 * @run testng org.codelibs.sai.internal.objects.test.NativeStringTest
 */
public class NativeStringTest {

    private ScriptEngine engine;

    @BeforeClass
    public void setupEngine() {
        final ScriptEngineManager manager = new ScriptEngineManager();
        engine = manager.getEngineByName("sai");
        assertNotNull(engine, "Sai engine should be available");
    }

    @Test
    public void testStringConstructor() throws ScriptException {
        // Test string literal
        Object result = engine.eval("var s = 'hello'; s.length;");
        assertEquals(result, 5);

        // Test String constructor
        result = engine.eval("var s = new String('hello'); s.length;");
        assertEquals(result, 5);

        // Test String conversion
        result = engine.eval("String(123);");
        assertEquals(result, "123");
    }

    @Test
    public void testCharAt() throws ScriptException {
        // Test charAt
        Object result = engine.eval("'hello'.charAt(0);");
        assertEquals(result, "h");

        // Test charAt middle
        result = engine.eval("'hello'.charAt(2);");
        assertEquals(result, "l");

        // Test charAt last
        result = engine.eval("'hello'.charAt(4);");
        assertEquals(result, "o");

        // Test charAt out of bounds
        result = engine.eval("'hello'.charAt(10);");
        assertEquals(result, "");
    }

    @Test
    public void testCharCodeAt() throws ScriptException {
        // Test charCodeAt
        Object result = engine.eval("'hello'.charCodeAt(0);");
        assertEquals(result, 104); // 'h'

        // Test charCodeAt various positions
        result = engine.eval("'ABC'.charCodeAt(0);");
        assertEquals(result, 65); // 'A'

        // Test charCodeAt out of bounds returns NaN
        result = engine.eval("isNaN('hello'.charCodeAt(10));");
        assertEquals(result, true);
    }

    @Test
    public void testConcat() throws ScriptException {
        // Test concat single string
        Object result = engine.eval("'hello'.concat(' world');");
        assertEquals(result, "hello world");

        // Test concat multiple strings
        result = engine.eval("'a'.concat('b', 'c', 'd');");
        assertEquals(result, "abcd");

        // Test concat empty string
        result = engine.eval("'hello'.concat('');");
        assertEquals(result, "hello");

        // Test concat doesn't modify original
        result = engine.eval("var s = 'hello'; s.concat(' world'); s;");
        assertEquals(result, "hello");
    }

    @Test
    public void testIndexOf() throws ScriptException {
        // Test indexOf found
        Object result = engine.eval("'hello world'.indexOf('world');");
        assertEquals(result, 6);

        // Test indexOf not found
        result = engine.eval("'hello world'.indexOf('xyz');");
        assertEquals(result, -1);

        // Test indexOf with fromIndex
        result = engine.eval("'hello hello'.indexOf('hello', 1);");
        assertEquals(result, 6);

        // Test indexOf single character
        result = engine.eval("'hello'.indexOf('l');");
        assertEquals(result, 2);

        // Test indexOf at start
        result = engine.eval("'hello'.indexOf('h');");
        assertEquals(result, 0);
    }

    @Test
    public void testLastIndexOf() throws ScriptException {
        // Test lastIndexOf
        Object result = engine.eval("'hello hello'.lastIndexOf('hello');");
        assertEquals(result, 6);

        // Test lastIndexOf not found
        result = engine.eval("'hello world'.lastIndexOf('xyz');");
        assertEquals(result, -1);

        // Test lastIndexOf with fromIndex
        result = engine.eval("'hello hello'.lastIndexOf('hello', 5);");
        assertEquals(result, 0);

        // Test lastIndexOf single character
        result = engine.eval("'hello'.lastIndexOf('l');");
        assertEquals(result, 3);
    }

    @Test
    public void testSlice() throws ScriptException {
        // Test slice with start
        Object result = engine.eval("'hello world'.slice(6);");
        assertEquals(result, "world");

        // Test slice with start and end
        result = engine.eval("'hello world'.slice(0, 5);");
        assertEquals(result, "hello");

        // Test slice with negative indices
        result = engine.eval("'hello world'.slice(-5);");
        assertEquals(result, "world");

        // Test slice negative start and end
        result = engine.eval("'hello world'.slice(-5, -1);");
        assertEquals(result, "worl");
    }

    @Test
    public void testSubstring() throws ScriptException {
        // Test substring with start
        Object result = engine.eval("'hello world'.substring(6);");
        assertEquals(result, "world");

        // Test substring with start and end
        result = engine.eval("'hello world'.substring(0, 5);");
        assertEquals(result, "hello");

        // Test substring swaps if start > end
        result = engine.eval("'hello'.substring(3, 1);");
        assertEquals(result, "el");

        // Test substring negative treated as 0
        result = engine.eval("'hello'.substring(-2, 3);");
        assertEquals(result, "hel");
    }

    @Test
    public void testSubstr() throws ScriptException {
        // Test substr with start
        Object result = engine.eval("'hello world'.substr(6);");
        assertEquals(result, "world");

        // Test substr with start and length
        result = engine.eval("'hello world'.substr(0, 5);");
        assertEquals(result, "hello");

        // Test substr with negative start
        result = engine.eval("'hello world'.substr(-5);");
        assertEquals(result, "world");

        // Test substr with negative start and length
        result = engine.eval("'hello world'.substr(-5, 2);");
        assertEquals(result, "wo");
    }

    @Test
    public void testToLowerCase() throws ScriptException {
        // Test toLowerCase
        Object result = engine.eval("'HELLO'.toLowerCase();");
        assertEquals(result, "hello");

        // Test toLowerCase mixed case
        result = engine.eval("'HeLLo WoRLd'.toLowerCase();");
        assertEquals(result, "hello world");

        // Test toLowerCase already lowercase
        result = engine.eval("'hello'.toLowerCase();");
        assertEquals(result, "hello");
    }

    @Test
    public void testToUpperCase() throws ScriptException {
        // Test toUpperCase
        Object result = engine.eval("'hello'.toUpperCase();");
        assertEquals(result, "HELLO");

        // Test toUpperCase mixed case
        result = engine.eval("'HeLLo WoRLd'.toUpperCase();");
        assertEquals(result, "HELLO WORLD");

        // Test toUpperCase already uppercase
        result = engine.eval("'HELLO'.toUpperCase();");
        assertEquals(result, "HELLO");
    }

    @Test
    public void testTrim() throws ScriptException {
        // Test trim leading and trailing spaces
        Object result = engine.eval("'  hello  '.trim();");
        assertEquals(result, "hello");

        // Test trim only leading spaces
        result = engine.eval("'  hello'.trim();");
        assertEquals(result, "hello");

        // Test trim only trailing spaces
        result = engine.eval("'hello  '.trim();");
        assertEquals(result, "hello");

        // Test trim no spaces
        result = engine.eval("'hello'.trim();");
        assertEquals(result, "hello");

        // Test trim whitespace characters
        result = engine.eval("'\\t\\n hello \\r\\n'.trim();");
        assertEquals(result, "hello");
    }

    @Test
    public void testSplit() throws ScriptException {
        // Test split with delimiter
        Object result = engine.eval("'a,b,c'.split(',').length;");
        assertEquals(result, 3);

        // Test split result values
        result = engine.eval("'a,b,c'.split(',')[0];");
        assertEquals(result, "a");

        // Test split with limit
        result = engine.eval("'a,b,c,d'.split(',', 2).length;");
        assertEquals(result, 2);

        // Test split empty delimiter
        result = engine.eval("'hello'.split('').length;");
        assertEquals(result, 5);

        // Test split no match
        result = engine.eval("'hello'.split(',').length;");
        assertEquals(result, 1);
    }

    @Test
    public void testReplace() throws ScriptException {
        // Test replace first occurrence
        Object result = engine.eval("'hello hello'.replace('hello', 'hi');");
        assertEquals(result, "hi hello");

        // Test replace not found
        result = engine.eval("'hello'.replace('xyz', 'abc');");
        assertEquals(result, "hello");

        // Test replace with function
        result = engine.eval("'hello'.replace('h', function(match){return match.toUpperCase();});");
        assertEquals(result, "Hello");
    }

    @Test
    public void testMatch() throws ScriptException {
        // Test match found
        Object result = engine.eval("var m = 'hello world'.match(/world/); m !== null;");
        assertEquals(result, true);

        // Test match not found
        result = engine.eval("'hello world'.match(/xyz/);");
        assertEquals(result, null);

        // Test match with groups
        result = engine.eval("var m = 'hello world'.match(/\\w+/); m[0];");
        assertEquals(result, "hello");
    }

    @Test
    public void testSearch() throws ScriptException {
        // Test search found
        Object result = engine.eval("'hello world'.search(/world/);");
        assertEquals(result, 6);

        // Test search not found
        result = engine.eval("'hello world'.search(/xyz/);");
        assertEquals(result, -1);

        // Test search ignores global flag
        result = engine.eval("'hello world'.search(/l/);");
        assertEquals(result, 2);
    }

    @Test
    public void testLength() throws ScriptException {
        // Test length
        Object result = engine.eval("'hello'.length;");
        assertEquals(result, 5);

        // Test length empty string
        result = engine.eval("''.length;");
        assertEquals(result, 0);

        // Test length with unicode
        result = engine.eval("'こんにちは'.length;");
        assertEquals(result, 5);
    }

    @Test
    public void testFromCharCode() throws ScriptException {
        // Test fromCharCode single
        Object result = engine.eval("String.fromCharCode(65);");
        assertEquals(result, "A");

        // Test fromCharCode multiple
        result = engine.eval("String.fromCharCode(72, 101, 108, 108, 111);");
        assertEquals(result, "Hello");
    }

    @Test
    public void testStringComparison() throws ScriptException {
        // Test string equality
        Object result = engine.eval("'hello' === 'hello';");
        assertEquals(result, true);

        // Test string inequality
        result = engine.eval("'hello' === 'world';");
        assertEquals(result, false);

        // Test string comparison
        result = engine.eval("'a' < 'b';");
        assertEquals(result, true);
    }

    @Test
    public void testStringConcatenation() throws ScriptException {
        // Test + operator
        Object result = engine.eval("'hello' + ' ' + 'world';");
        assertEquals(result, "hello world");

        // Test += operator
        result = engine.eval("var s = 'hello'; s += ' world'; s;");
        assertEquals(result, "hello world");

        // Test concatenation with numbers
        result = engine.eval("'hello' + 123;");
        assertEquals(result, "hello123");
    }

    @Test
    public void testStringBracketAccess() throws ScriptException {
        // Test bracket notation
        Object result = engine.eval("'hello'[0];");
        assertEquals(result, "h");

        // Test bracket notation middle
        result = engine.eval("'hello'[2];");
        assertEquals(result, "l");

        // Test bracket notation out of bounds
        result = engine.eval("'hello'[10];");
        assertEquals(result, null);
    }
}
