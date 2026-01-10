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

package org.codelibs.sai.internal.ir.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import org.codelibs.sai.internal.ir.IdentNode;
import org.codelibs.sai.internal.ir.LiteralNode;
import org.codelibs.sai.internal.ir.Symbol;
import org.codelibs.sai.internal.parser.Token;
import org.codelibs.sai.internal.parser.TokenType;
import org.testng.annotations.Test;

/**
 * Unit tests for IR (Intermediate Representation) nodes.
 *
 * @test
 * @run testng org.codelibs.sai.internal.ir.test.IRNodeTest
 */
@SuppressWarnings("javadoc")
public class IRNodeTest {

    // IdentNode tests

    @Test
    public void testIdentNodeCreation() {
        final long token = Token.toDesc(TokenType.IDENT, 0, 3);
        final IdentNode ident = new IdentNode(token, 3, "foo");

        assertEquals(ident.getName(), "foo");
        assertNull(ident.getSymbol());
        assertFalse(ident.isPropertyName());
        assertFalse(ident.isFunction());
        assertFalse(ident.isInitializedHere());
        assertFalse(ident.isDead());
        assertFalse(ident.isDeclaredHere());
    }

    @Test
    public void testIdentNodeCopyConstructor() {
        final long token = Token.toDesc(TokenType.IDENT, 0, 3);
        final IdentNode original = new IdentNode(token, 3, "bar");
        final IdentNode copy = new IdentNode(original);

        assertEquals(copy.getName(), original.getName());
        assertEquals(copy.getToken(), original.getToken());
        assertEquals(copy.getFinish(), original.getFinish());
    }

    @Test
    public void testIdentNodePropertyName() {
        final long token = Token.toDesc(TokenType.IDENT, 0, 3);
        final IdentNode ident = new IdentNode(token, 3, "prop");

        assertFalse(ident.isPropertyName());

        final IdentNode propIdent = ident.setIsPropertyName();
        assertTrue(propIdent.isPropertyName());

        // Original should be unchanged (immutability)
        assertFalse(ident.isPropertyName());
    }

    @Test
    public void testIdentNodeFunction() {
        final long token = Token.toDesc(TokenType.IDENT, 0, 4);
        final IdentNode ident = new IdentNode(token, 4, "func");

        assertFalse(ident.isFunction());

        final IdentNode funcIdent = ident.setIsFunction();
        assertTrue(funcIdent.isFunction());

        final IdentNode notFuncIdent = funcIdent.setIsNotFunction();
        assertFalse(notFuncIdent.isFunction());
    }

    @Test
    public void testIdentNodeInitializedHere() {
        final long token = Token.toDesc(TokenType.IDENT, 0, 1);
        final IdentNode ident = new IdentNode(token, 1, "x");

        assertFalse(ident.isInitializedHere());

        final IdentNode initialized = ident.setIsInitializedHere();
        assertTrue(initialized.isInitializedHere());

        // Original unchanged
        assertFalse(ident.isInitializedHere());
    }

    @Test
    public void testIdentNodeDead() {
        final long token = Token.toDesc(TokenType.IDENT, 0, 1);
        final IdentNode ident = new IdentNode(token, 1, "x");

        assertFalse(ident.isDead());

        final IdentNode dead = ident.markDead();
        assertTrue(dead.isDead());
    }

    @Test
    public void testIdentNodeDeclaredHere() {
        final long token = Token.toDesc(TokenType.IDENT, 0, 1);
        final IdentNode ident = new IdentNode(token, 1, "x");

        assertFalse(ident.isDeclaredHere());

        final IdentNode declared = ident.setIsDeclaredHere();
        assertTrue(declared.isDeclaredHere());
    }

    @Test
    public void testIdentNodeFutureStrictName() {
        final long token = Token.toDesc(TokenType.IDENT, 0, 4);
        final IdentNode ident = new IdentNode(token, 4, "eval");

        assertFalse(ident.isFutureStrictName());

        final IdentNode futureStrict = ident.setIsFutureStrictName();
        assertTrue(futureStrict.isFutureStrictName());
    }

    @Test
    public void testIdentNodeInternal() {
        final long token = Token.toDesc(TokenType.IDENT, 0, 5);

        final IdentNode normalIdent = new IdentNode(token, 5, "normal");
        assertFalse(normalIdent.isInternal());

        final IdentNode internalIdent = new IdentNode(token, 5, ":internal");
        assertTrue(internalIdent.isInternal());
    }

    @Test
    public void testIdentNodeSymbol() {
        final long token = Token.toDesc(TokenType.IDENT, 0, 1);
        final IdentNode ident = new IdentNode(token, 1, "x");

        assertNull(ident.getSymbol());

        final Symbol symbol = new Symbol("x", 0);
        final IdentNode withSymbol = ident.setSymbol(symbol);

        assertNotNull(withSymbol.getSymbol());
        assertEquals(withSymbol.getSymbol().getName(), "x");

        // Original unchanged
        assertNull(ident.getSymbol());
    }

    @Test
    public void testIdentNodeCompileTimeProperty() {
        final long token = Token.toDesc(TokenType.IDENT, 0, 8);

        final IdentNode dirIdent = new IdentNode(token, 8, "__DIR__");
        assertTrue(dirIdent.isCompileTimePropertyName());

        final IdentNode fileIdent = new IdentNode(token, 8, "__FILE__");
        assertTrue(fileIdent.isCompileTimePropertyName());

        final IdentNode lineIdent = new IdentNode(token, 8, "__LINE__");
        assertTrue(lineIdent.isCompileTimePropertyName());

        final IdentNode normalIdent = new IdentNode(token, 5, "normal");
        assertFalse(normalIdent.isCompileTimePropertyName());
    }

    @Test
    public void testIdentNodeImmutability() {
        final long token = Token.toDesc(TokenType.IDENT, 0, 3);
        final IdentNode original = new IdentNode(token, 3, "foo");

        final IdentNode modified = original.setIsPropertyName();

        assertNotSame(original, modified);
        assertFalse(original.isPropertyName());
        assertTrue(modified.isPropertyName());
    }

    @Test
    public void testIdentNodeGetPropertyName() {
        final long token = Token.toDesc(TokenType.IDENT, 0, 4);
        final IdentNode ident = new IdentNode(token, 4, "test");

        assertEquals(ident.getPropertyName(), "test");
    }

    @Test
    public void testIdentNodeToString() {
        final long token = Token.toDesc(TokenType.IDENT, 0, 3);
        final IdentNode ident = new IdentNode(token, 3, "foo");

        final StringBuilder sb = new StringBuilder();
        ident.toString(sb, false);

        assertEquals(sb.toString(), "foo");
    }

    // LiteralNode tests

    @Test
    public void testLiteralNodeNumber() {
        final LiteralNode<?> intLiteral = LiteralNode.newInstance(
                Token.toDesc(TokenType.DECIMAL, 0, 2), 2, 42);
        assertEquals(((Number) intLiteral.getValue()).intValue(), 42);

        final LiteralNode<?> doubleLiteral = LiteralNode.newInstance(
                Token.toDesc(TokenType.FLOATING, 0, 4), 4, 3.14);
        assertEquals(((Number) doubleLiteral.getValue()).doubleValue(), 3.14, 0.001);
    }

    @Test
    public void testLiteralNodeString() {
        final LiteralNode<?> stringLiteral = LiteralNode.newInstance(
                Token.toDesc(TokenType.STRING, 0, 5), 5, "hello");
        assertEquals(stringLiteral.getValue(), "hello");
    }

    @Test
    public void testLiteralNodeBoolean() {
        final LiteralNode<?> trueLiteral = LiteralNode.newInstance(
                Token.toDesc(TokenType.TRUE, 0, 4), 4);
        assertTrue((Boolean) trueLiteral.getValue());

        final LiteralNode<?> falseLiteral = LiteralNode.newInstance(
                Token.toDesc(TokenType.FALSE, 0, 5), 5);
        assertFalse((Boolean) falseLiteral.getValue());
    }

    @Test
    public void testLiteralNodeNull() {
        final LiteralNode<?> nullLiteral = LiteralNode.newInstance(
                Token.toDesc(TokenType.NULL, 0, 4), 4);
        assertNull(nullLiteral.getValue());
    }

    @Test
    public void testLiteralNodeIsString() {
        final LiteralNode<?> stringLiteral = LiteralNode.newInstance(
                Token.toDesc(TokenType.STRING, 0, 5), 5, "hello");
        assertTrue(stringLiteral.isString());

        final LiteralNode<?> numLiteral = LiteralNode.newInstance(
                Token.toDesc(TokenType.DECIMAL, 0, 2), 2, 42);
        assertFalse(numLiteral.isString());
    }

    @Test
    public void testLiteralNodeIsNumeric() {
        final LiteralNode<?> intLiteral = LiteralNode.newInstance(
                Token.toDesc(TokenType.DECIMAL, 0, 2), 2, 42);
        assertTrue(intLiteral.isNumeric());

        final LiteralNode<?> floatLiteral = LiteralNode.newInstance(
                Token.toDesc(TokenType.FLOATING, 0, 4), 4, 3.14);
        assertTrue(floatLiteral.isNumeric());

        final LiteralNode<?> stringLiteral = LiteralNode.newInstance(
                Token.toDesc(TokenType.STRING, 0, 5), 5, "hello");
        assertFalse(stringLiteral.isNumeric());
    }

    @Test
    public void testLiteralNodeGetString() {
        final LiteralNode<?> stringLiteral = LiteralNode.newInstance(
                Token.toDesc(TokenType.STRING, 0, 5), 5, "hello");
        assertEquals(stringLiteral.getString(), "hello");
    }

    @Test
    public void testLiteralNodeGetNumber() {
        final LiteralNode<?> intLiteral = LiteralNode.newInstance(
                Token.toDesc(TokenType.DECIMAL, 0, 2), 2, 42);
        assertEquals(intLiteral.getNumber().intValue(), 42);

        final LiteralNode<?> floatLiteral = LiteralNode.newInstance(
                Token.toDesc(TokenType.FLOATING, 0, 4), 4, 3.14);
        assertEquals(floatLiteral.getNumber().doubleValue(), 3.14, 0.001);
    }

    @Test
    public void testLiteralNodePropertyName() {
        final LiteralNode<?> stringLiteral = LiteralNode.newInstance(
                Token.toDesc(TokenType.STRING, 0, 3), 3, "key");
        assertEquals(stringLiteral.getPropertyName(), "key");

        final LiteralNode<?> numLiteral = LiteralNode.newInstance(
                Token.toDesc(TokenType.DECIMAL, 0, 1), 1, 42);
        assertEquals(numLiteral.getPropertyName(), "42");
    }
}
