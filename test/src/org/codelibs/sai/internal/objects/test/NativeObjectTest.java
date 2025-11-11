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

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for NativeObject JavaScript built-in methods.
 *
 * @test
 * @run testng org.codelibs.sai.internal.objects.test.NativeObjectTest
 */
public class NativeObjectTest {

    private ScriptEngine engine;

    @BeforeClass
    public void setupEngine() {
        final ScriptEngineManager manager = new ScriptEngineManager();
        engine = manager.getEngineByName("sai");
        assertNotNull(engine, "Sai engine should be available");
    }

    @Test
    public void testObjectConstructor() throws ScriptException {
        // Test object literal
        Object result = engine.eval("var obj = {a: 1, b: 2}; obj.a;");
        assertEquals(result, 1);

        // Test Object constructor
        result = engine.eval("var obj = new Object(); obj.x = 5; obj.x;");
        assertEquals(result, 5);

        // Test Object constructor with value
        result = engine.eval("var obj = new Object(123); typeof obj;");
        assertEquals(result, "object");
    }

    @Test
    public void testObjectCreate() throws ScriptException {
        // Test Object.create with null
        Object result = engine.eval("var obj = Object.create(null); obj.__proto__ === undefined;");
        assertEquals(result, true);

        // Test Object.create with prototype
        result = engine.eval("var proto = {x: 1}; var obj = Object.create(proto); obj.x;");
        assertEquals(result, 1);

        // Test Object.create with properties
        result = engine.eval(
                "var obj = Object.create({}, {a: {value: 1, writable: true, enumerable: true, configurable: true}}); obj.a;");
        assertEquals(result, 1);
    }

    @Test
    public void testObjectKeys() throws ScriptException {
        // Test Object.keys
        Object result = engine.eval("var obj = {a: 1, b: 2, c: 3}; Object.keys(obj).length;");
        assertEquals(result, 3);

        // Test Object.keys order
        result = engine.eval("var obj = {a: 1, b: 2, c: 3}; Object.keys(obj)[0];");
        assertEquals(result, "a");

        // Test Object.keys empty object
        result = engine.eval("Object.keys({}).length;");
        assertEquals(result, 0);

        // Test Object.keys ignores prototype properties
        result = engine.eval("var proto = {x: 1}; var obj = Object.create(proto); obj.a = 2; Object.keys(obj).length;");
        assertEquals(result, 1);
    }

    @Test
    public void testObjectGetOwnPropertyNames() throws ScriptException {
        // Test getOwnPropertyNames
        Object result = engine.eval("var obj = {a: 1, b: 2}; Object.getOwnPropertyNames(obj).length;");
        assertEquals(result, 2);

        // Test getOwnPropertyNames includes non-enumerable
        result = engine.eval(
                "var obj = {}; Object.defineProperty(obj, 'x', {value: 1, enumerable: false}); Object.getOwnPropertyNames(obj).length;");
        assertEquals(result, 1);
    }

    @Test
    public void testObjectDefineProperty() throws ScriptException {
        // Test defineProperty
        Object result = engine.eval(
                "var obj = {}; Object.defineProperty(obj, 'x', {value: 42, writable: true, enumerable: true, configurable: true}); obj.x;");
        assertEquals(result, 42);

        // Test defineProperty non-writable
        result = engine.eval("var obj = {}; Object.defineProperty(obj, 'x', {value: 42, writable: false}); obj.x = 99; obj.x;");
        assertEquals(result, 42);

        // Test defineProperty non-enumerable
        result = engine.eval(
                "var obj = {}; Object.defineProperty(obj, 'x', {value: 42, enumerable: false}); Object.keys(obj).length;");
        assertEquals(result, 0);
    }

    @Test
    public void testObjectDefineProperties() throws ScriptException {
        // Test defineProperties
        Object result = engine.eval("var obj = {}; Object.defineProperties(obj, {"
                + "a: {value: 1, writable: true, enumerable: true, configurable: true},"
                + "b: {value: 2, writable: true, enumerable: true, configurable: true}" + "}); obj.a + obj.b;");
        assertEquals(result, 3);
    }

    @Test
    public void testObjectGetOwnPropertyDescriptor() throws ScriptException {
        // Test getOwnPropertyDescriptor
        Object result = engine.eval("var obj = {x: 42}; var desc = Object.getOwnPropertyDescriptor(obj, 'x'); desc.value;");
        assertEquals(result, 42);

        // Test getOwnPropertyDescriptor writable
        result = engine.eval("var obj = {x: 42}; var desc = Object.getOwnPropertyDescriptor(obj, 'x'); desc.writable;");
        assertEquals(result, true);

        // Test getOwnPropertyDescriptor enumerable
        result = engine.eval("var obj = {x: 42}; var desc = Object.getOwnPropertyDescriptor(obj, 'x'); desc.enumerable;");
        assertEquals(result, true);

        // Test getOwnPropertyDescriptor configurable
        result = engine.eval("var obj = {x: 42}; var desc = Object.getOwnPropertyDescriptor(obj, 'x'); desc.configurable;");
        assertEquals(result, true);

        // Test getOwnPropertyDescriptor undefined property
        result = engine.eval("var obj = {x: 42}; Object.getOwnPropertyDescriptor(obj, 'y');");
        assertEquals(result, null);
    }

    @Test
    public void testObjectGetPrototypeOf() throws ScriptException {
        // Test getPrototypeOf array
        Object result = engine.eval("Object.getPrototypeOf([]) === Array.prototype;");
        assertEquals(result, true);

        // Test getPrototypeOf object
        result = engine.eval("Object.getPrototypeOf({}) === Object.prototype;");
        assertEquals(result, true);

        // Test getPrototypeOf custom
        result = engine.eval("var proto = {x: 1}; var obj = Object.create(proto); Object.getPrototypeOf(obj) === proto;");
        assertEquals(result, true);
    }

    @Test
    public void testObjectIsExtensible() throws ScriptException {
        // Test isExtensible default
        Object result = engine.eval("Object.isExtensible({});");
        assertEquals(result, true);

        // Test isExtensible after preventExtensions
        result = engine.eval("var obj = {}; Object.preventExtensions(obj); Object.isExtensible(obj);");
        assertEquals(result, false);
    }

    @Test
    public void testObjectPreventExtensions() throws ScriptException {
        // Test preventExtensions
        Object result = engine.eval("var obj = {a: 1}; Object.preventExtensions(obj); obj.b = 2; obj.b;");
        assertEquals(result, null);

        // Test preventExtensions allows modification
        result = engine.eval("var obj = {a: 1}; Object.preventExtensions(obj); obj.a = 99; obj.a;");
        assertEquals(result, 99);

        // Test preventExtensions allows deletion
        result = engine.eval("var obj = {a: 1}; Object.preventExtensions(obj); delete obj.a; obj.a;");
        assertEquals(result, null);
    }

    @Test
    public void testObjectSeal() throws ScriptException {
        // Test seal prevents new properties
        Object result = engine.eval("var obj = {a: 1}; Object.seal(obj); obj.b = 2; obj.b;");
        assertEquals(result, null);

        // Test seal allows modification
        result = engine.eval("var obj = {a: 1}; Object.seal(obj); obj.a = 99; obj.a;");
        assertEquals(result, 99);

        // Test seal prevents deletion
        result = engine.eval("var obj = {a: 1}; Object.seal(obj); delete obj.a; obj.a;");
        assertEquals(result, 1);

        // Test isSealed
        result = engine.eval("var obj = {}; Object.seal(obj); Object.isSealed(obj);");
        assertEquals(result, true);
    }

    @Test
    public void testObjectFreeze() throws ScriptException {
        // Test freeze prevents new properties
        Object result = engine.eval("var obj = {a: 1}; Object.freeze(obj); obj.b = 2; obj.b;");
        assertEquals(result, null);

        // Test freeze prevents modification
        result = engine.eval("var obj = {a: 1}; Object.freeze(obj); obj.a = 99; obj.a;");
        assertEquals(result, 1);

        // Test freeze prevents deletion
        result = engine.eval("var obj = {a: 1}; Object.freeze(obj); delete obj.a; obj.a;");
        assertEquals(result, 1);

        // Test isFrozen
        result = engine.eval("var obj = {}; Object.freeze(obj); Object.isFrozen(obj);");
        assertEquals(result, true);
    }

    @Test
    public void testObjectHasOwnProperty() throws ScriptException {
        // Test hasOwnProperty true
        Object result = engine.eval("var obj = {a: 1}; obj.hasOwnProperty('a');");
        assertEquals(result, true);

        // Test hasOwnProperty false
        result = engine.eval("var obj = {a: 1}; obj.hasOwnProperty('b');");
        assertEquals(result, false);

        // Test hasOwnProperty ignores prototype
        result = engine.eval("var proto = {x: 1}; var obj = Object.create(proto); obj.hasOwnProperty('x');");
        assertEquals(result, false);
    }

    @Test
    public void testObjectPropertyIsEnumerable() throws ScriptException {
        // Test propertyIsEnumerable true
        Object result = engine.eval("var obj = {a: 1}; obj.propertyIsEnumerable('a');");
        assertEquals(result, true);

        // Test propertyIsEnumerable false
        result = engine.eval(
                "var obj = {}; Object.defineProperty(obj, 'x', {value: 1, enumerable: false}); obj.propertyIsEnumerable('x');");
        assertEquals(result, false);
    }

    @Test
    public void testObjectToString() throws ScriptException {
        // Test toString object
        Object result = engine.eval("({}).toString();");
        assertEquals(result, "[object Object]");

        // Test toString array
        result = engine.eval("Object.prototype.toString.call([]);");
        assertEquals(result, "[object Array]");

        // Test toString function
        result = engine.eval("Object.prototype.toString.call(function(){});");
        assertEquals(result, "[object Function]");
    }

    @Test
    public void testObjectValueOf() throws ScriptException {
        // Test valueOf returns object
        Object result = engine.eval("var obj = {a: 1}; obj.valueOf() === obj;");
        assertEquals(result, true);
    }

    @Test
    public void testObjectPropertyAccess() throws ScriptException {
        // Test dot notation
        Object result = engine.eval("var obj = {a: 1}; obj.a;");
        assertEquals(result, 1);

        // Test bracket notation
        result = engine.eval("var obj = {a: 1}; obj['a'];");
        assertEquals(result, 1);

        // Test dynamic property access
        result = engine.eval("var obj = {a: 1, b: 2}; var key = 'b'; obj[key];");
        assertEquals(result, 2);

        // Test undefined property
        result = engine.eval("var obj = {a: 1}; obj.b;");
        assertEquals(result, null);
    }

    @Test
    public void testObjectPropertyAssignment() throws ScriptException {
        // Test property assignment
        Object result = engine.eval("var obj = {}; obj.a = 1; obj.a;");
        assertEquals(result, 1);

        // Test property update
        result = engine.eval("var obj = {a: 1}; obj.a = 2; obj.a;");
        assertEquals(result, 2);

        // Test bracket assignment
        result = engine.eval("var obj = {}; obj['a'] = 1; obj.a;");
        assertEquals(result, 1);
    }

    @Test
    public void testObjectPropertyDeletion() throws ScriptException {
        // Test delete property
        Object result = engine.eval("var obj = {a: 1, b: 2}; delete obj.a; obj.a;");
        assertEquals(result, null);

        // Test delete returns true
        result = engine.eval("var obj = {a: 1}; delete obj.a;");
        assertEquals(result, true);

        // Test delete non-existent property
        result = engine.eval("var obj = {}; delete obj.a;");
        assertEquals(result, true);
    }

    @Test
    public void testObjectInOperator() throws ScriptException {
        // Test in operator true
        Object result = engine.eval("var obj = {a: 1}; 'a' in obj;");
        assertEquals(result, true);

        // Test in operator false
        result = engine.eval("var obj = {a: 1}; 'b' in obj;");
        assertEquals(result, false);

        // Test in operator checks prototype
        result = engine.eval("var proto = {x: 1}; var obj = Object.create(proto); 'x' in obj;");
        assertEquals(result, true);
    }

    @Test
    public void testObjectForInLoop() throws ScriptException {
        // Test for-in loop
        Object result = engine.eval("var obj = {a: 1, b: 2, c: 3}; var count = 0; for (var key in obj) count++; count;");
        assertEquals(result, 3);

        // Test for-in loop values
        result = engine.eval("var obj = {a: 1, b: 2}; var sum = 0; for (var key in obj) sum += obj[key]; sum;");
        assertEquals(result, 3);
    }
}
