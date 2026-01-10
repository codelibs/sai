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

package org.codelibs.sai.internal.parser.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Unit tests for Lexer token generation through script execution.
 *
 * @test
 * @run testng org.codelibs.sai.internal.parser.test.LexerTest
 */
@SuppressWarnings("javadoc")
public class LexerTest {

    private ScriptEngine engine;

    @BeforeClass
    public void setupEngine() {
        final ScriptEngineManager manager = new ScriptEngineManager();
        engine = manager.getEngineByName("sai");
        assertNotNull(engine, "Sai engine should be available");
    }

    // Number literal tests

    @Test
    public void testDecimalLiterals() throws ScriptException {
        assertEquals(engine.eval("0"), 0);
        assertEquals(engine.eval("42"), 42);
        assertEquals(engine.eval("123456789"), 123456789);
    }

    @Test
    public void testFloatingPointLiterals() throws ScriptException {
        assertEquals(((Number) engine.eval("3.14")).doubleValue(), 3.14, 0.0001);
        assertEquals(((Number) engine.eval(".5")).doubleValue(), 0.5, 0.0001);
        assertEquals(((Number) engine.eval("0.123")).doubleValue(), 0.123, 0.0001);
        assertEquals(((Number) engine.eval("10.0")).doubleValue(), 10.0, 0.0001);
    }

    @Test
    public void testScientificNotation() throws ScriptException {
        assertEquals(((Number) engine.eval("1e10")).doubleValue(), 1e10, 0.0);
        assertEquals(((Number) engine.eval("1E10")).doubleValue(), 1E10, 0.0);
        assertEquals(((Number) engine.eval("1e+10")).doubleValue(), 1e+10, 0.0);
        assertEquals(((Number) engine.eval("1e-10")).doubleValue(), 1e-10, 0.0);
        assertEquals(((Number) engine.eval("2.5e3")).doubleValue(), 2500.0, 0.0);
        assertEquals(((Number) engine.eval("2.5E-3")).doubleValue(), 0.0025, 0.0001);
    }

    @Test
    public void testHexadecimalLiterals() throws ScriptException {
        assertEquals(engine.eval("0x0"), 0);
        assertEquals(engine.eval("0x10"), 16);
        assertEquals(engine.eval("0X10"), 16);
        assertEquals(engine.eval("0xff"), 255);
        assertEquals(engine.eval("0xFF"), 255);
        assertEquals(engine.eval("0xDEADBEEF"), 0xDEADBEEF);
    }

    @Test
    public void testOctalLiterals() throws ScriptException {
        // In ES5 strict mode, octal is not allowed
        // But in non-strict mode, legacy octal support
        assertEquals(engine.eval("010"), 8);
        assertEquals(engine.eval("0777"), 511);
    }

    // String literal tests

    @Test
    public void testStringLiterals() throws ScriptException {
        assertEquals(engine.eval("'hello'"), "hello");
        assertEquals(engine.eval("\"hello\""), "hello");
        assertEquals(engine.eval("''"), "");
        assertEquals(engine.eval("\"\""), "");
    }

    @Test
    public void testStringEscapes() throws ScriptException {
        assertEquals(engine.eval("'hello\\nworld'"), "hello\nworld");
        assertEquals(engine.eval("'hello\\tworld'"), "hello\tworld");
        assertEquals(engine.eval("'hello\\rworld'"), "hello\rworld");
        assertEquals(engine.eval("'hello\\'world'"), "hello'world");
        assertEquals(engine.eval("\"hello\\\"world\""), "hello\"world");
        assertEquals(engine.eval("'hello\\\\world'"), "hello\\world");
    }

    @Test
    public void testStringUnicodeEscapes() throws ScriptException {
        assertEquals(engine.eval("'\\u0041'"), "A");
        assertEquals(engine.eval("'\\u4e2d\\u6587'"), "中文");
        assertEquals(engine.eval("'\\x41'"), "A");
    }

    @Test
    public void testStringWithQuotes() throws ScriptException {
        assertEquals(engine.eval("'say \"hello\"'"), "say \"hello\"");
        assertEquals(engine.eval("\"say 'hello'\""), "say 'hello'");
    }

    // Keyword tests

    @Test
    public void testKeywords() throws ScriptException {
        assertEquals(engine.eval("var x = 1; x"), 1);
        assertEquals(engine.eval("function f() { return 42; } f()"), 42);
        assertEquals(engine.eval("if (true) { 1 } else { 2 }"), 1);
        assertEquals(engine.eval("if (false) { 1 } else { 2 }"), 2);
    }

    @Test
    public void testBooleanLiterals() throws ScriptException {
        assertEquals(engine.eval("true"), true);
        assertEquals(engine.eval("false"), false);
    }

    @Test
    public void testNullLiteral() throws ScriptException {
        assertEquals(engine.eval("null"), null);
    }

    // Operator tests

    @Test
    public void testArithmeticOperators() throws ScriptException {
        assertEquals(engine.eval("1 + 2"), 3);
        assertEquals(engine.eval("5 - 3"), 2);
        assertEquals(engine.eval("4 * 3"), 12);
        assertEquals(engine.eval("10 / 2"), 5.0);
        assertEquals(engine.eval("10 % 3"), 1.0);
    }

    @Test
    public void testComparisonOperators() throws ScriptException {
        assertEquals(engine.eval("1 < 2"), true);
        assertEquals(engine.eval("2 > 1"), true);
        assertEquals(engine.eval("1 <= 1"), true);
        assertEquals(engine.eval("1 >= 1"), true);
        assertEquals(engine.eval("1 == 1"), true);
        assertEquals(engine.eval("1 === 1"), true);
        assertEquals(engine.eval("1 != 2"), true);
        assertEquals(engine.eval("1 !== '1'"), true);
    }

    @Test
    public void testLogicalOperators() throws ScriptException {
        assertEquals(engine.eval("true && true"), true);
        assertEquals(engine.eval("true && false"), false);
        assertEquals(engine.eval("true || false"), true);
        assertEquals(engine.eval("false || false"), false);
        assertEquals(engine.eval("!true"), false);
        assertEquals(engine.eval("!false"), true);
    }

    @Test
    public void testBitwiseOperators() throws ScriptException {
        assertEquals(engine.eval("5 & 3"), 1);
        assertEquals(engine.eval("5 | 3"), 7);
        assertEquals(engine.eval("5 ^ 3"), 6);
        assertEquals(engine.eval("~5"), -6);
        assertEquals(engine.eval("5 << 1"), 10);
        assertEquals(engine.eval("5 >> 1"), 2);
        assertEquals(engine.eval("-1 >>> 0"), 4294967295L);
    }

    @Test
    public void testAssignmentOperators() throws ScriptException {
        assertEquals(engine.eval("var x = 10; x"), 10);
        assertEquals(engine.eval("var x = 10; x += 5; x"), 15);
        assertEquals(engine.eval("var x = 10; x -= 5; x"), 5);
        assertEquals(engine.eval("var x = 10; x *= 2; x"), 20);
        assertEquals(engine.eval("var x = 10; x /= 2; x"), 5.0);
        assertEquals(engine.eval("var x = 10; x %= 3; x"), 1.0);
    }

    @Test
    public void testIncrementDecrementOperators() throws ScriptException {
        assertEquals(engine.eval("var x = 5; ++x"), 6);
        assertEquals(engine.eval("var x = 5; x++"), 5);
        assertEquals(engine.eval("var x = 5; --x"), 4);
        assertEquals(engine.eval("var x = 5; x--"), 5);
    }

    @Test
    public void testTernaryOperator() throws ScriptException {
        assertEquals(engine.eval("true ? 1 : 2"), 1);
        assertEquals(engine.eval("false ? 1 : 2"), 2);
        assertEquals(engine.eval("5 > 3 ? 'yes' : 'no'"), "yes");
    }

    // Comment tests

    @Test
    public void testSingleLineComments() throws ScriptException {
        assertEquals(engine.eval("1 + 2 // this is a comment"), 3);
        assertEquals(engine.eval("// comment at start\n42"), 42);
    }

    @Test
    public void testMultiLineComments() throws ScriptException {
        assertEquals(engine.eval("1 + /* add two */ 2"), 3);
        assertEquals(engine.eval("/* comment\nacross\nlines */ 42"), 42);
    }

    // Identifier tests

    @Test
    public void testIdentifiers() throws ScriptException {
        assertEquals(engine.eval("var x = 1; x"), 1);
        assertEquals(engine.eval("var _x = 2; _x"), 2);
        assertEquals(engine.eval("var $x = 3; $x"), 3);
        assertEquals(engine.eval("var x123 = 4; x123"), 4);
        assertEquals(engine.eval("var camelCase = 5; camelCase"), 5);
    }

    @Test
    public void testUnicodeIdentifiers() throws ScriptException {
        assertEquals(engine.eval("var 変数 = 42; 変数"), 42);
        assertEquals(engine.eval("var π = 3.14159; π"), 3.14159);
    }

    // Bracket and punctuation tests

    @Test
    public void testBrackets() throws ScriptException {
        assertEquals(engine.eval("(1 + 2) * 3"), 9);
        assertEquals(engine.eval("var arr = [1, 2, 3]; arr[1]"), 2);
        assertEquals(engine.eval("var obj = {a: 1}; obj.a"), 1);
    }

    @Test
    public void testSemicolonInsertion() throws ScriptException {
        assertEquals(engine.eval("var x = 1\nx"), 1);
        assertEquals(engine.eval("var x = 1; var y = 2\nx + y"), 3);
    }

    // Regex literal tests

    @Test
    public void testRegexLiterals() throws ScriptException {
        assertTrue((Boolean) engine.eval("/abc/.test('abc')"));
        assertTrue((Boolean) engine.eval("/abc/i.test('ABC')"));
        assertTrue((Boolean) engine.eval("/^hello/.test('hello world')"));
        assertTrue((Boolean) engine.eval("/world$/.test('hello world')"));
    }

    @Test
    public void testRegexWithFlags() throws ScriptException {
        assertTrue((Boolean) engine.eval("/abc/g.global"));
        assertTrue((Boolean) engine.eval("/abc/i.ignoreCase"));
        assertTrue((Boolean) engine.eval("/abc/m.multiline"));
    }

    // Edge case tests

    @Test
    public void testDivisionVsRegex() throws ScriptException {
        // Division context
        assertEquals(((Number) engine.eval("10 / 2")).doubleValue(), 5.0, 0.0);

        // Regex context (after operator or keyword)
        assertTrue((Boolean) engine.eval("/a/.test('a')"));
    }

    @Test
    public void testWhitespaceHandling() throws ScriptException {
        assertEquals(engine.eval("  42  "), 42);
        assertEquals(engine.eval("\t42\t"), 42);
        assertEquals(engine.eval("\n42\n"), 42);
        assertEquals(engine.eval("1    +    2"), 3);
    }

    @Test
    public void testMultipleStatements() throws ScriptException {
        assertEquals(engine.eval("var a = 1; var b = 2; a + b"), 3);
        assertEquals(engine.eval("function f(x) { return x * 2; } f(5)"), 10);
    }

    @Test
    public void testNestedExpressions() throws ScriptException {
        assertEquals(engine.eval("((1 + 2) * (3 + 4))"), 21);
        assertEquals(engine.eval("var arr = [[1, 2], [3, 4]]; arr[0][1]"), 2);
    }

    @Test
    public void testSpecialNumbers() throws ScriptException {
        assertTrue(Double.isNaN(((Number) engine.eval("NaN")).doubleValue()));
        assertEquals(engine.eval("Infinity"), Double.POSITIVE_INFINITY);
        assertEquals(engine.eval("-Infinity"), Double.NEGATIVE_INFINITY);
    }
}
