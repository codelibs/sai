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
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for PropertyMap functionality through JavaScript execution.
 *
 * @test
 * @run testng org.codelibs.sai.internal.runtime.test.PropertyMapTest
 */
public class PropertyMapTest {

    private ScriptEngine engine;

    @BeforeClass
    public void setupEngine() {
        final ScriptEngineManager manager = new ScriptEngineManager();
        engine = manager.getEngineByName("sai");
        assertNotNull(engine, "Sai engine should be available");
    }

    @Test
    public void testPropertyAddition() throws ScriptException {
        // Test adding properties to object
        Object result = engine.eval("var obj = {}; obj.a = 1; obj.b = 2; obj.c = 3; obj.a;");
        assertEquals(((Number) result).intValue(), 1);

        // Test property count
        result = engine.eval("var obj = {}; obj.a = 1; obj.b = 2; Object.keys(obj).length;");
        assertEquals(((Number) result).intValue(), 2);
    }

    @Test
    public void testPropertyAccess() throws ScriptException {
        // Test dot notation access
        Object result = engine.eval("var obj = {x: 10, y: 20}; obj.x;");
        assertEquals(((Number) result).intValue(), 10);

        // Test bracket notation access
        result = engine.eval("var obj = {x: 10, y: 20}; obj['y'];");
        assertEquals(((Number) result).intValue(), 20);

        // Test dynamic property access
        result = engine.eval("var obj = {x: 10, y: 20}; var key = 'x'; obj[key];");
        assertEquals(((Number) result).intValue(), 10);
    }

    @Test
    public void testPropertyUpdate() throws ScriptException {
        // Test property update
        Object result = engine.eval("var obj = {x: 10}; obj.x = 20; obj.x;");
        assertEquals(((Number) result).intValue(), 20);

        // Test multiple updates
        result = engine.eval("var obj = {x: 10}; obj.x = 20; obj.x = 30; obj.x;");
        assertEquals(((Number) result).intValue(), 30);
    }

    @Test
    public void testPropertyDeletion() throws ScriptException {
        // Test property deletion
        Object result = engine.eval("var obj = {x: 10, y: 20}; delete obj.x; obj.x;");
        assertEquals(result, null);

        // Test deletion reduces property count
        result = engine.eval("var obj = {x: 10, y: 20}; delete obj.x; Object.keys(obj).length;");
        assertEquals(((Number) result).intValue(), 1);

        // Test deletion returns true
        result = engine.eval("var obj = {x: 10}; delete obj.x;");
        assertEquals(result, true);
    }

    @Test
    public void testPropertyEnumeration() throws ScriptException {
        // Test for-in loop
        Object result = engine.eval("var obj = {a: 1, b: 2, c: 3}; var count = 0; for (var k in obj) count++; count;");
        assertEquals(((Number) result).intValue(), 3);

        // Test Object.keys
        result = engine.eval("var obj = {a: 1, b: 2, c: 3}; Object.keys(obj).length;");
        assertEquals(((Number) result).intValue(), 3);

        // Test property order
        result = engine.eval("var obj = {a: 1, b: 2, c: 3}; Object.keys(obj)[0];");
        assertEquals(result, "a");
    }

    @Test
    public void testPropertyDescriptors() throws ScriptException {
        // Test writable property
        Object result = engine.eval("var obj = {}; Object.defineProperty(obj, 'x', {value: 10, writable: true}); obj.x = 20; obj.x;");
        assertEquals(((Number) result).intValue(), 20);

        // Test non-writable property
        result = engine.eval("var obj = {}; Object.defineProperty(obj, 'x', {value: 10, writable: false}); obj.x = 20; obj.x;");
        assertEquals(((Number) result).intValue(), 10);

        // Test enumerable property
        result = engine.eval(
                "var obj = {}; Object.defineProperty(obj, 'x', {value: 10, enumerable: true}); Object.keys(obj).length;");
        assertEquals(((Number) result).intValue(), 1);

        // Test non-enumerable property
        result = engine.eval(
                "var obj = {}; Object.defineProperty(obj, 'x', {value: 10, enumerable: false}); Object.keys(obj).length;");
        assertEquals(((Number) result).intValue(), 0);
    }

    @Test
    public void testPropertyConfigurability() throws ScriptException {
        // Test configurable property can be deleted
        Object result = engine.eval(
                "var obj = {}; Object.defineProperty(obj, 'x', {value: 10, configurable: true}); delete obj.x; obj.x;");
        assertEquals(result, null);

        // Test non-configurable property cannot be deleted
        result = engine.eval(
                "var obj = {}; Object.defineProperty(obj, 'x', {value: 10, configurable: false}); delete obj.x; obj.x;");
        assertEquals(((Number) result).intValue(), 10);
    }

    @Test
    public void testPropertyInheritance() throws ScriptException {
        // Test prototype property access
        Object result = engine.eval("var proto = {x: 10}; var obj = Object.create(proto); obj.x;");
        assertEquals(((Number) result).intValue(), 10);

        // Test own property shadows prototype
        result = engine.eval("var proto = {x: 10}; var obj = Object.create(proto); obj.x = 20; obj.x;");
        assertEquals(((Number) result).intValue(), 20);

        // Test hasOwnProperty
        result = engine.eval("var proto = {x: 10}; var obj = Object.create(proto); obj.hasOwnProperty('x');");
        assertEquals(result, false);

        // Test hasOwnProperty with own property
        result = engine.eval("var proto = {x: 10}; var obj = Object.create(proto); obj.x = 20; obj.hasOwnProperty('x');");
        assertEquals(result, true);
    }

    @Test
    public void testPropertyWithGetter() throws ScriptException {
        // Test getter property
        Object result = engine.eval("var obj = {get x() { return 42; }}; obj.x;");
        assertEquals(((Number) result).intValue(), 42);

        // Test getter with computation
        result = engine.eval("var obj = {y: 10, get x() { return this.y * 2; }}; obj.x;");
        assertEquals(((Number) result).intValue(), 20);
    }

    @Test
    public void testPropertyWithSetter() throws ScriptException {
        // Test setter property
        Object result = engine.eval("var obj = {_x: 0, set x(v) { this._x = v * 2; }}; obj.x = 10; obj._x;");
        assertEquals(((Number) result).intValue(), 20);

        // Test getter and setter
        result = engine.eval("var obj = {_x: 0, get x() { return this._x; }, set x(v) { this._x = v; }}; obj.x = 10; obj.x;");
        assertEquals(((Number) result).intValue(), 10);
    }

    @Test
    public void testPropertyTypes() throws ScriptException {
        // Test number property
        Object result = engine.eval("var obj = {x: 42}; typeof obj.x;");
        assertEquals(result, "number");

        // Test string property
        result = engine.eval("var obj = {x: 'hello'}; typeof obj.x;");
        assertEquals(result, "string");

        // Test boolean property
        result = engine.eval("var obj = {x: true}; typeof obj.x;");
        assertEquals(result, "boolean");

        // Test object property
        result = engine.eval("var obj = {x: {y: 1}}; typeof obj.x;");
        assertEquals(result, "object");

        // Test function property
        result = engine.eval("var obj = {x: function(){}}; typeof obj.x;");
        assertEquals(result, "function");
    }

    @Test
    public void testPropertyNameTypes() throws ScriptException {
        // Test string property names
        Object result = engine.eval("var obj = {'hello world': 1}; obj['hello world'];");
        assertEquals(result, 1);

        // Test number property names
        result = engine.eval("var obj = {}; obj[42] = 'value'; obj['42'];");
        assertEquals(result, "value");

        // Test computed property names
        result = engine.eval("var key = 'dynamic'; var obj = {}; obj[key] = 10; obj.dynamic;");
        assertEquals(((Number) result).intValue(), 10);
    }

    @Test
    public void testPropertyMapSharing() throws ScriptException {
        // Test objects with same structure share property map (implicit test)
        Object result = engine.eval("var obj1 = {x: 1, y: 2}; var obj2 = {x: 3, y: 4}; obj1.x + obj2.x;");
        assertEquals(((Number) result).intValue(), 4);
    }

    @Test
    public void testPropertyMapTransitions() throws ScriptException {
        // Test property map transitions when adding properties
        Object result = engine.eval("var obj = {}; obj.a = 1; obj.b = 2; obj.c = 3; Object.keys(obj).length;");
        assertEquals(((Number) result).intValue(), 3);

        // Test property map transitions when deleting properties
        result = engine.eval("var obj = {a: 1, b: 2, c: 3}; delete obj.b; Object.keys(obj).length;");
        assertEquals(((Number) result).intValue(), 2);
    }

    @Test
    public void testManyProperties() throws ScriptException {
        // Test object with many properties
        Object result = engine.eval("var obj = {}; for (var i = 0; i < 100; i++) obj['p' + i] = i; obj.p50;");
        assertEquals(((Number) result).intValue(), 50);

        // Test property count
        result = engine.eval("var obj = {}; for (var i = 0; i < 100; i++) obj['p' + i] = i; Object.keys(obj).length;");
        assertEquals(((Number) result).intValue(), 100);
    }

    @Test
    public void testPropertyOverwrite() throws ScriptException {
        // Test property overwrite
        Object result = engine.eval("var obj = {x: 1}; obj.x = 2; obj.x = 3; obj.x;");
        assertEquals(((Number) result).intValue(), 3);

        // Test type change
        result = engine.eval("var obj = {x: 1}; obj.x = 'string'; obj.x;");
        assertEquals(result, "string");
    }

    @Test
    public void testUndefinedProperty() throws ScriptException {
        // Test accessing undefined property
        Object result = engine.eval("var obj = {x: 1}; obj.y;");
        assertEquals(result, null);

        // Test typeof undefined property
        result = engine.eval("var obj = {}; typeof obj.missing;");
        assertEquals(result, "undefined");
    }

    @Test
    public void testPropertyWithUndefinedValue() throws ScriptException {
        // Test property with undefined value
        Object result = engine.eval("var obj = {x: undefined}; obj.x;");
        assertEquals(result, null);

        // Test hasOwnProperty with undefined value
        result = engine.eval("var obj = {x: undefined}; obj.hasOwnProperty('x');");
        assertEquals((Boolean) result, true);
    }

    @Test
    public void testNullPropertyValue() throws ScriptException {
        // Test property with null value
        Object result = engine.eval("var obj = {x: null}; obj.x;");
        assertEquals(result, null);

        // Test hasOwnProperty with null value
        result = engine.eval("var obj = {x: null}; obj.hasOwnProperty('x');");
        assertEquals((Boolean) result, true);
    }

    @Test
    public void testInOperator() throws ScriptException {
        // Test in operator with own property
        Object result = engine.eval("var obj = {x: 1}; 'x' in obj;");
        assertEquals(result, true);

        // Test in operator with missing property
        result = engine.eval("var obj = {x: 1}; 'y' in obj;");
        assertEquals(result, false);

        // Test in operator with inherited property
        result = engine.eval("var proto = {x: 1}; var obj = Object.create(proto); 'x' in obj;");
        assertEquals(result, true);
    }

    @Test
    public void testObjectLiteralShorthand() throws ScriptException {
        // Test property value shorthand (ES6 feature check)
        Object result = engine.eval("var x = 10; var obj = {x: x}; obj.x;");
        assertEquals(((Number) result).intValue(), 10);
    }
}
