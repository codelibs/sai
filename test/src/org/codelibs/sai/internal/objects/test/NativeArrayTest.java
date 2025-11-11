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
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for NativeArray JavaScript built-in methods.
 *
 * @test
 * @run testng org.codelibs.sai.internal.objects.test.NativeArrayTest
 */
public class NativeArrayTest {

    private ScriptEngine engine;

    @BeforeClass
    public void setupEngine() {
        final ScriptEngineManager manager = new ScriptEngineManager();
        engine = manager.getEngineByName("sai");
        assertNotNull(engine, "Sai engine should be available");
    }

    @Test
    public void testArrayConstructor() throws ScriptException {
        // Test array literal
        Object result = engine.eval("var arr = [1, 2, 3]; arr.length;");
        assertEquals(result, 3);

        // Test Array constructor with length
        result = engine.eval("var arr = new Array(5); arr.length;");
        assertEquals(result, 5);

        // Test Array constructor with elements
        result = engine.eval("var arr = new Array(1, 2, 3); arr.length;");
        assertEquals(result, 3);
    }

    @Test
    public void testIsArray() throws ScriptException {
        assertTrue((Boolean) engine.eval("Array.isArray([])"));
        assertTrue((Boolean) engine.eval("Array.isArray([1, 2, 3])"));
        assertTrue((Boolean) engine.eval("Array.isArray(new Array())"));
        assertFalse((Boolean) engine.eval("Array.isArray({})"));
        assertFalse((Boolean) engine.eval("Array.isArray('hello')"));
        assertFalse((Boolean) engine.eval("Array.isArray(123)"));
        assertFalse((Boolean) engine.eval("Array.isArray(null)"));
        assertFalse((Boolean) engine.eval("Array.isArray(undefined)"));
    }

    @Test
    public void testPush() throws ScriptException {
        // Test push single element
        Object result = engine.eval("var arr = [1, 2]; arr.push(3); arr.length;");
        assertEquals(result, 3);

        // Test push multiple elements
        result = engine.eval("var arr = [1]; arr.push(2, 3, 4); arr.length;");
        assertEquals(result, 4);

        // Test push returns new length
        result = engine.eval("var arr = [1, 2]; arr.push(3);");
        assertEquals(result, 3);

        // Test array content after push
        result = engine.eval("var arr = [1, 2]; arr.push(3); arr[2];");
        assertEquals(result, 3);
    }

    @Test
    public void testPop() throws ScriptException {
        // Test pop returns last element
        Object result = engine.eval("var arr = [1, 2, 3]; arr.pop();");
        assertEquals(result, 3);

        // Test pop reduces length
        result = engine.eval("var arr = [1, 2, 3]; arr.pop(); arr.length;");
        assertEquals(result, 2);

        // Test pop on empty array
        result = engine.eval("var arr = []; arr.pop();");
        assertEquals(result, null);

        // Test multiple pops
        result = engine.eval("var arr = [1, 2, 3]; arr.pop(); arr.pop(); arr.pop(); arr.length;");
        assertEquals(result, 0);
    }

    @Test
    public void testShift() throws ScriptException {
        // Test shift returns first element
        Object result = engine.eval("var arr = [1, 2, 3]; arr.shift();");
        assertEquals(result, 1);

        // Test shift reduces length
        result = engine.eval("var arr = [1, 2, 3]; arr.shift(); arr.length;");
        assertEquals(result, 2);

        // Test remaining elements shift down
        result = engine.eval("var arr = [1, 2, 3]; arr.shift(); arr[0];");
        assertEquals(result, 2);

        // Test shift on empty array
        result = engine.eval("var arr = []; arr.shift();");
        assertEquals(result, null);
    }

    @Test
    public void testUnshift() throws ScriptException {
        // Test unshift single element
        Object result = engine.eval("var arr = [2, 3]; arr.unshift(1); arr.length;");
        assertEquals(result, 3);

        // Test unshift multiple elements
        result = engine.eval("var arr = [3]; arr.unshift(1, 2); arr.length;");
        assertEquals(result, 3);

        // Test unshift returns new length
        result = engine.eval("var arr = [2, 3]; arr.unshift(1);");
        assertEquals(result, 3);

        // Test array content after unshift
        result = engine.eval("var arr = [2, 3]; arr.unshift(1); arr[0];");
        assertEquals(result, 1);
    }

    @Test
    public void testJoin() throws ScriptException {
        // Test default separator (comma)
        Object result = engine.eval("var arr = [1, 2, 3]; arr.join();");
        assertEquals(result, "1,2,3");

        // Test custom separator
        result = engine.eval("var arr = [1, 2, 3]; arr.join('-');");
        assertEquals(result, "1-2-3");

        // Test empty separator
        result = engine.eval("var arr = [1, 2, 3]; arr.join('');");
        assertEquals(result, "123");

        // Test single element
        result = engine.eval("var arr = [1]; arr.join(',');");
        assertEquals(result, "1");

        // Test empty array
        result = engine.eval("var arr = []; arr.join(',');");
        assertEquals(result, "");
    }

    @Test
    public void testConcat() throws ScriptException {
        // Test concat single array
        Object result = engine.eval("var arr = [1, 2]; var result = arr.concat([3, 4]); result.length;");
        assertEquals(result, 4);

        // Test concat multiple arrays
        result = engine.eval("var arr = [1]; var result = arr.concat([2], [3, 4]); result.length;");
        assertEquals(result, 4);

        // Test concat values
        result = engine.eval("var arr = [1, 2]; var result = arr.concat(3, 4); result.length;");
        assertEquals(result, 4);

        // Test concat doesn't modify original
        result = engine.eval("var arr = [1, 2]; arr.concat([3, 4]); arr.length;");
        assertEquals(result, 2);

        // Test concat result values
        result = engine.eval("var arr = [1, 2]; var result = arr.concat([3, 4]); result[2];");
        assertEquals(result, 3);
    }

    @Test
    public void testSlice() throws ScriptException {
        // Test slice with start
        Object result = engine.eval("var arr = [1, 2, 3, 4, 5]; var sliced = arr.slice(2); sliced.length;");
        assertEquals(result, 3);

        // Test slice with start and end
        result = engine.eval("var arr = [1, 2, 3, 4, 5]; var sliced = arr.slice(1, 4); sliced.length;");
        assertEquals(result, 3);

        // Test slice with negative indices
        result = engine.eval("var arr = [1, 2, 3, 4, 5]; var sliced = arr.slice(-2); sliced.length;");
        assertEquals(result, 2);

        // Test slice doesn't modify original
        result = engine.eval("var arr = [1, 2, 3]; arr.slice(1); arr.length;");
        assertEquals(result, 3);

        // Test slice result values
        result = engine.eval("var arr = [1, 2, 3, 4, 5]; var sliced = arr.slice(1, 3); sliced[0];");
        assertEquals(result, 2);
    }

    @Test
    public void testSplice() throws ScriptException {
        // Test splice remove elements
        Object result = engine.eval("var arr = [1, 2, 3, 4, 5]; arr.splice(2, 2); arr.length;");
        assertEquals(result, 3);

        // Test splice returns removed elements
        result = engine.eval("var arr = [1, 2, 3, 4, 5]; var removed = arr.splice(1, 2); removed.length;");
        assertEquals(result, 2);

        // Test splice add elements
        result = engine.eval("var arr = [1, 2, 5]; arr.splice(2, 0, 3, 4); arr.length;");
        assertEquals(result, 5);

        // Test splice replace elements
        result = engine.eval("var arr = [1, 2, 3, 4, 5]; arr.splice(2, 1, 99); arr[2];");
        assertEquals(result, 99);
    }

    @Test
    public void testReverse() throws ScriptException {
        // Test reverse
        Object result = engine.eval("var arr = [1, 2, 3, 4, 5]; arr.reverse(); arr[0];");
        assertEquals(result, 5);

        // Test reverse returns array
        result = engine.eval("var arr = [1, 2, 3]; var reversed = arr.reverse(); reversed[2];");
        assertEquals(result, 1);

        // Test reverse modifies original
        result = engine.eval("var arr = [1, 2, 3]; arr.reverse(); arr[0];");
        assertEquals(result, 3);
    }

    @Test
    public void testSort() throws ScriptException {
        // Test sort default (lexicographic)
        Object result = engine.eval("var arr = [3, 1, 4, 1, 5]; arr.sort(); arr[0];");
        assertEquals(result, 1);

        // Test sort with compare function
        result = engine.eval("var arr = [3, 1, 4, 1, 5]; arr.sort(function(a,b){return a-b;}); arr[0];");
        assertEquals(result, 1);

        // Test sort modifies original
        result = engine.eval("var arr = [3, 2, 1]; arr.sort(); arr[0];");
        assertEquals(result, 1);

        // Test sort returns array
        result = engine.eval("var arr = [3, 2, 1]; var sorted = arr.sort(); sorted[2];");
        assertEquals(result, 3);
    }

    @Test
    public void testIndexOf() throws ScriptException {
        // Test indexOf found
        Object result = engine.eval("var arr = [1, 2, 3, 2, 1]; arr.indexOf(2);");
        assertEquals(result, 1);

        // Test indexOf not found
        result = engine.eval("var arr = [1, 2, 3]; arr.indexOf(5);");
        assertEquals(result, -1);

        // Test indexOf with fromIndex
        result = engine.eval("var arr = [1, 2, 3, 2, 1]; arr.indexOf(2, 2);");
        assertEquals(result, 3);

        // Test indexOf exact match (no type coercion)
        result = engine.eval("var arr = [1, 2, 3]; arr.indexOf('2');");
        assertEquals(result, -1);
    }

    @Test
    public void testLastIndexOf() throws ScriptException {
        // Test lastIndexOf found
        Object result = engine.eval("var arr = [1, 2, 3, 2, 1]; arr.lastIndexOf(2);");
        assertEquals(result, 3);

        // Test lastIndexOf not found
        result = engine.eval("var arr = [1, 2, 3]; arr.lastIndexOf(5);");
        assertEquals(result, -1);

        // Test lastIndexOf with fromIndex
        result = engine.eval("var arr = [1, 2, 3, 2, 1]; arr.lastIndexOf(2, 2);");
        assertEquals(result, 1);
    }

    @Test
    public void testEvery() throws ScriptException {
        // Test every true
        Object result = engine.eval("var arr = [2, 4, 6, 8]; arr.every(function(x){return x % 2 === 0;});");
        assertTrue((Boolean) result);

        // Test every false
        result = engine.eval("var arr = [2, 4, 5, 8]; arr.every(function(x){return x % 2 === 0;});");
        assertFalse((Boolean) result);

        // Test every empty array
        result = engine.eval("var arr = []; arr.every(function(x){return false;});");
        assertTrue((Boolean) result);
    }

    @Test
    public void testSome() throws ScriptException {
        // Test some true
        Object result = engine.eval("var arr = [1, 2, 3, 4]; arr.some(function(x){return x % 2 === 0;});");
        assertTrue((Boolean) result);

        // Test some false
        result = engine.eval("var arr = [1, 3, 5, 7]; arr.some(function(x){return x % 2 === 0;});");
        assertFalse((Boolean) result);

        // Test some empty array
        result = engine.eval("var arr = []; arr.some(function(x){return true;});");
        assertFalse((Boolean) result);
    }

    @Test
    public void testForEach() throws ScriptException {
        // Test forEach executes callback
        Object result = engine.eval("var sum = 0; var arr = [1, 2, 3]; arr.forEach(function(x){sum += x;}); sum;");
        assertEquals(result, 6);

        // Test forEach with index
        result = engine.eval(
                "var result = ''; var arr = ['a', 'b', 'c']; arr.forEach(function(x, i){result += i + x;}); result;");
        assertEquals(result, "0a1b2c");
    }

    @Test
    public void testMap() throws ScriptException {
        // Test map
        Object result = engine.eval("var arr = [1, 2, 3]; var mapped = arr.map(function(x){return x * 2;}); mapped[0];");
        assertEquals(result, 2);

        // Test map length
        result = engine.eval("var arr = [1, 2, 3]; var mapped = arr.map(function(x){return x * 2;}); mapped.length;");
        assertEquals(result, 3);

        // Test map doesn't modify original
        result = engine.eval("var arr = [1, 2, 3]; arr.map(function(x){return x * 2;}); arr[0];");
        assertEquals(result, 1);
    }

    @Test
    public void testFilter() throws ScriptException {
        // Test filter
        Object result = engine.eval("var arr = [1, 2, 3, 4, 5]; var filtered = arr.filter(function(x){return x % 2 === 0;}); filtered.length;");
        assertEquals(result, 2);

        // Test filter result
        result = engine.eval("var arr = [1, 2, 3, 4, 5]; var filtered = arr.filter(function(x){return x % 2 === 0;}); filtered[0];");
        assertEquals(result, 2);

        // Test filter doesn't modify original
        result = engine.eval("var arr = [1, 2, 3]; arr.filter(function(x){return x > 1;}); arr.length;");
        assertEquals(result, 3);
    }

    @Test
    public void testReduce() throws ScriptException {
        // Test reduce sum
        Object result = engine.eval("var arr = [1, 2, 3, 4, 5]; arr.reduce(function(acc, x){return acc + x;}, 0);");
        assertEquals(result, 15);

        // Test reduce without initial value
        result = engine.eval("var arr = [1, 2, 3, 4]; arr.reduce(function(acc, x){return acc + x;});");
        assertEquals(result, 10);

        // Test reduce max
        result = engine.eval("var arr = [1, 5, 3, 9, 2]; arr.reduce(function(max, x){return x > max ? x : max;});");
        assertEquals(result, 9);
    }

    @Test
    public void testReduceRight() throws ScriptException {
        // Test reduceRight
        Object result = engine.eval("var arr = [1, 2, 3, 4]; arr.reduceRight(function(acc, x){return acc - x;}, 0);");
        assertEquals(result, -10);

        // Test reduceRight order
        result = engine.eval("var arr = ['a', 'b', 'c']; arr.reduceRight(function(acc, x){return acc + x;}, '');");
        assertEquals(result, "cba");
    }

    @Test
    public void testToString() throws ScriptException {
        // Test toString
        Object result = engine.eval("var arr = [1, 2, 3]; arr.toString();");
        assertEquals(result, "1,2,3");

        // Test toString empty array
        result = engine.eval("var arr = []; arr.toString();");
        assertEquals(result, "");
    }

    @Test
    public void testLength() throws ScriptException {
        // Test get length
        Object result = engine.eval("var arr = [1, 2, 3]; arr.length;");
        assertEquals(result, 3);

        // Test set length
        result = engine.eval("var arr = [1, 2, 3, 4, 5]; arr.length = 3; arr.length;");
        assertEquals(result, 3);

        // Test set length truncates array
        result = engine.eval("var arr = [1, 2, 3, 4, 5]; arr.length = 3; arr[4];");
        assertEquals(result, null);

        // Test set length extends array
        result = engine.eval("var arr = [1, 2, 3]; arr.length = 5; arr.length;");
        assertEquals(result, 5);
    }

    @Test
    public void testSparseArray() throws ScriptException {
        // Test sparse array creation
        Object result = engine.eval("var arr = []; arr[10] = 'x'; arr.length;");
        assertEquals(result, 11);

        // Test sparse array undefined elements
        result = engine.eval("var arr = []; arr[10] = 'x'; arr[5];");
        assertEquals(result, null);
    }

    @Test
    public void testArrayElementAccess() throws ScriptException {
        // Test bracket notation
        Object result = engine.eval("var arr = [1, 2, 3]; arr[1];");
        assertEquals(result, 2);

        // Test bracket assignment
        result = engine.eval("var arr = [1, 2, 3]; arr[1] = 99; arr[1];");
        assertEquals(result, 99);

        // Test out of bounds read
        result = engine.eval("var arr = [1, 2, 3]; arr[10];");
        assertEquals(result, null);

        // Test out of bounds write extends array
        result = engine.eval("var arr = [1, 2, 3]; arr[5] = 99; arr.length;");
        assertEquals(result, 6);
    }
}
