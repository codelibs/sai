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

package org.codelibs.sai.internal.runtime.arrays.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for array data structures and type transitions.
 *
 * @test
 * @run testng org.codelibs.sai.internal.runtime.arrays.test.ArrayDataTest
 */
@SuppressWarnings("javadoc")
public class ArrayDataTest {

    private ScriptEngine engine;

    @BeforeClass
    public void setupEngine() {
        final ScriptEngineManager manager = new ScriptEngineManager();
        engine = manager.getEngineByName("sai");
        assertNotNull(engine, "Sai engine should be available");
    }

    // Type transition tests

    @Test
    public void testIntToDoubleTransition() throws ScriptException {
        // Start with int array, transition to double
        Object result = engine.eval(
                "var arr = [1, 2, 3]; " +
                        "arr[0] = 1.5; " +
                        "arr[0]");
        assertEquals(((Number) result).doubleValue(), 1.5, 0.0);
    }

    @Test
    public void testIntToObjectTransition() throws ScriptException {
        // Start with int array, transition to object
        Object result = engine.eval(
                "var arr = [1, 2, 3]; " +
                        "arr[0] = 'string'; " +
                        "arr[0]");
        assertEquals(result, "string");
    }

    @Test
    public void testDoubleToObjectTransition() throws ScriptException {
        // Start with double array, transition to object
        Object result = engine.eval(
                "var arr = [1.1, 2.2, 3.3]; " +
                        "arr[0] = {x: 1}; " +
                        "arr[0].x");
        assertEquals(result, 1);
    }

    @Test
    public void testMixedTypeArrayCreation() throws ScriptException {
        // Create array with mixed types
        Object result = engine.eval(
                "var arr = [1, 'two', 3.0, true, null]; " +
                        "arr.length");
        assertEquals(result, 5);
    }

    // Sparse array tests

    @Test
    public void testSparseArrayCreation() throws ScriptException {
        Object result = engine.eval(
                "var arr = []; " +
                        "arr[1000] = 'x'; " +
                        "arr.length");
        assertEquals(result, 1001);
    }

    @Test
    public void testSparseArrayUndefinedElements() throws ScriptException {
        Object result = engine.eval(
                "var arr = []; " +
                        "arr[10] = 'x'; " +
                        "arr[5] === undefined");
        assertTrue((Boolean) result);
    }

    @Test
    public void testSparseArrayIteration() throws ScriptException {
        Object result = engine.eval(
                "var arr = []; " +
                        "arr[0] = 'a'; " +
                        "arr[100] = 'b'; " +
                        "var count = 0; " +
                        "for (var i in arr) count++; " +
                        "count");
        assertEquals(((Number) result).intValue(), 2);
    }

    @Test
    public void testSparseToContiguousConversion() throws ScriptException {
        Object result = engine.eval(
                "var arr = []; " +
                        "arr[5] = 'x'; " +
                        "arr[0] = 'a'; arr[1] = 'b'; arr[2] = 'c'; " +
                        "arr[3] = 'd'; arr[4] = 'e'; " +
                        "arr.join('')");
        assertEquals(result, "abcdex");
    }

    // Frozen array tests

    @Test
    public void testFrozenArrayCannotModify() throws ScriptException {
        Object result = engine.eval(
                "var arr = [1, 2, 3]; " +
                        "Object.freeze(arr); " +
                        "arr[0] = 99; " +
                        "arr[0]");
        assertEquals(result, 1);
    }

    @Test
    public void testFrozenArrayCannotAdd() throws ScriptException {
        Object result = engine.eval(
                "var arr = [1, 2, 3]; " +
                        "Object.freeze(arr); " +
                        "arr[10] = 'x'; " +
                        "arr.length");
        assertEquals(result, 3);
    }

    @Test
    public void testFrozenArrayCannotDelete() throws ScriptException {
        Object result = engine.eval(
                "var arr = [1, 2, 3]; " +
                        "Object.freeze(arr); " +
                        "delete arr[0]; " +
                        "arr[0]");
        assertEquals(result, 1);
    }

    @Test
    public void testIsFrozen() throws ScriptException {
        Object result = engine.eval(
                "var arr = [1, 2, 3]; " +
                        "Object.freeze(arr); " +
                        "Object.isFrozen(arr)");
        assertTrue((Boolean) result);
    }

    // Sealed array tests

    @Test
    public void testSealedArrayCanModify() throws ScriptException {
        Object result = engine.eval(
                "var arr = [1, 2, 3]; " +
                        "Object.seal(arr); " +
                        "arr[0] = 99; " +
                        "arr[0]");
        assertEquals(result, 99);
    }

    @Test
    public void testSealedArrayCannotAdd() throws ScriptException {
        Object result = engine.eval(
                "var arr = [1, 2, 3]; " +
                        "Object.seal(arr); " +
                        "arr[10] = 'x'; " +
                        "arr.length");
        assertEquals(result, 3);
    }

    @Test
    public void testSealedArrayCannotDelete() throws ScriptException {
        Object result = engine.eval(
                "var arr = [1, 2, 3]; " +
                        "Object.seal(arr); " +
                        "delete arr[0]; " +
                        "arr[0]");
        assertEquals(result, 1);
    }

    @Test
    public void testIsSealed() throws ScriptException {
        Object result = engine.eval(
                "var arr = [1, 2, 3]; " +
                        "Object.seal(arr); " +
                        "Object.isSealed(arr)");
        assertTrue((Boolean) result);
    }

    // Non-extensible array tests

    @Test
    public void testNonExtensibleArrayCannotAdd() throws ScriptException {
        Object result = engine.eval(
                "var arr = [1, 2, 3]; " +
                        "Object.preventExtensions(arr); " +
                        "arr[10] = 'x'; " +
                        "arr.length");
        assertEquals(result, 3);
    }

    @Test
    public void testNonExtensibleArrayCanModify() throws ScriptException {
        Object result = engine.eval(
                "var arr = [1, 2, 3]; " +
                        "Object.preventExtensions(arr); " +
                        "arr[0] = 99; " +
                        "arr[0]");
        assertEquals(result, 99);
    }

    @Test
    public void testNonExtensibleArrayCanDelete() throws ScriptException {
        Object result = engine.eval(
                "var arr = [1, 2, 3]; " +
                        "Object.preventExtensions(arr); " +
                        "delete arr[0]; " +
                        "arr[0]");
        assertEquals(result, null);
    }

    // Large array tests

    @Test
    public void testLargeArrayPush() throws ScriptException {
        Object result = engine.eval(
                "var arr = []; " +
                        "for (var i = 0; i < 10000; i++) arr.push(i); " +
                        "arr.length");
        assertEquals(result, 10000);
    }

    @Test
    public void testLargeArrayAccess() throws ScriptException {
        Object result = engine.eval(
                "var arr = []; " +
                        "for (var i = 0; i < 10000; i++) arr[i] = i; " +
                        "arr[5000]");
        assertEquals(result, 5000);
    }

    @Test
    public void testLargeArraySlice() throws ScriptException {
        Object result = engine.eval(
                "var arr = []; " +
                        "for (var i = 0; i < 1000; i++) arr[i] = i; " +
                        "var sliced = arr.slice(500, 510); " +
                        "sliced.length");
        assertEquals(result, 10);
    }

    // Delete element tests

    @Test
    public void testDeleteCreatesHole() throws ScriptException {
        Object result = engine.eval(
                "var arr = [1, 2, 3, 4, 5]; " +
                        "delete arr[2]; " +
                        "arr[2] === undefined");
        assertTrue((Boolean) result);
    }

    @Test
    public void testDeletePreservesLength() throws ScriptException {
        Object result = engine.eval(
                "var arr = [1, 2, 3, 4, 5]; " +
                        "delete arr[2]; " +
                        "arr.length");
        assertEquals(result, 5);
    }

    @Test
    public void testDeleteInIteration() throws ScriptException {
        Object result = engine.eval(
                "var arr = [1, 2, 3, 4, 5]; " +
                        "delete arr[2]; " +
                        "var count = 0; " +
                        "for (var i in arr) count++; " +
                        "count");
        assertEquals(((Number) result).intValue(), 4);
    }

    // Array method tests on modified arrays

    @Test
    public void testMapOnSparseArray() throws ScriptException {
        Object result = engine.eval(
                "var arr = []; " +
                        "arr[0] = 1; arr[2] = 3; " +
                        "var mapped = arr.map(function(x) { return x * 2; }); " +
                        "mapped[0]");
        assertEquals(result, 2);
    }

    @Test
    public void testFilterOnSparseArray() throws ScriptException {
        Object result = engine.eval(
                "var arr = []; " +
                        "arr[0] = 1; arr[2] = 3; arr[4] = 5; " +
                        "var filtered = arr.filter(function(x) { return x > 1; }); " +
                        "filtered.length");
        assertEquals(result, 2);
    }

    @Test
    public void testReduceOnSparseArray() throws ScriptException {
        Object result = engine.eval(
                "var arr = []; " +
                        "arr[0] = 1; arr[2] = 3; arr[4] = 5; " +
                        "arr.reduce(function(acc, x) { return acc + x; }, 0)");
        assertEquals(((Number) result).intValue(), 9);
    }

    // Negative index tests

    @Test
    public void testNegativeIndexSlice() throws ScriptException {
        Object result = engine.eval(
                "var arr = [1, 2, 3, 4, 5]; " +
                        "arr.slice(-2).length");
        assertEquals(result, 2);
    }

    @Test
    public void testNegativeIndexSplice() throws ScriptException {
        Object result = engine.eval(
                "var arr = [1, 2, 3, 4, 5]; " +
                        "arr.splice(-2, 1); " +
                        "arr.length");
        assertEquals(result, 4);
    }

    // Array-like object tests

    @Test
    public void testArrayFromArrayLike() throws ScriptException {
        Object result = engine.eval(
                "var obj = {0: 'a', 1: 'b', 2: 'c', length: 3}; " +
                        "Array.prototype.slice.call(obj).length");
        assertEquals(result, 3);
    }

    @Test
    public void testArrayMethodsOnString() throws ScriptException {
        Object result = engine.eval(
                "Array.prototype.join.call('abc', '-')");
        assertEquals(result, "a-b-c");
    }

    // Edge cases

    @Test
    public void testArrayWithMaxSafeIntegerLength() throws ScriptException {
        // Testing boundary behavior - not creating actual huge array
        Object result = engine.eval(
                "var arr = []; " +
                        "arr.length = Math.pow(2, 32) - 1; " +
                        "arr.length");
        assertEquals(((Number) result).longValue(), 4294967295L);
    }

    @Test
    public void testArrayConcatFlattening() throws ScriptException {
        Object result = engine.eval(
                "var arr = [1, 2]; " +
                        "arr.concat([3, 4], [[5, 6]]).length");
        assertEquals(result, 5);
    }

    @Test
    public void testArrayWithUndefinedElements() throws ScriptException {
        Object result = engine.eval(
                "var arr = [1, undefined, 3]; " +
                        "arr[1] === undefined");
        assertTrue((Boolean) result);
    }

    @Test
    public void testArrayWithNullElements() throws ScriptException {
        Object result = engine.eval(
                "var arr = [1, null, 3]; " +
                        "arr[1] === null");
        assertTrue((Boolean) result);
    }

    @Test
    public void testArraySortStability() throws ScriptException {
        // Test that sort maintains relative order for equal elements
        Object result = engine.eval(
                "var arr = [{v: 1, i: 0}, {v: 1, i: 1}, {v: 1, i: 2}]; " +
                        "arr.sort(function(a, b) { return a.v - b.v; }); " +
                        "arr[0].i + ',' + arr[1].i + ',' + arr[2].i");
        assertEquals(result, "0,1,2");
    }

    @Test
    public void testArrayReverseInPlace() throws ScriptException {
        Object result = engine.eval(
                "var arr = [1, 2, 3, 4, 5]; " +
                        "var reversed = arr.reverse(); " +
                        "arr === reversed");
        assertTrue((Boolean) result);
    }

    @Test
    public void testArraySortInPlace() throws ScriptException {
        Object result = engine.eval(
                "var arr = [3, 1, 2]; " +
                        "var sorted = arr.sort(); " +
                        "arr === sorted");
        assertTrue((Boolean) result);
    }
}
