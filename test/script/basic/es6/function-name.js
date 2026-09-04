/*
 * Copyright (c) 2010, 2014, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
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

/**
 * ES2015 function "name": inferred from the binding an anonymous function
 * expression is assigned to, and "bound " on a bound function.
 *
 * See function-name-es5.js for the same probes at the default language level,
 * where none of this applies.
 *
 * @test
 * @run
 * @option --language=es6
 */

// a declaration and a named expression keep their own name, as before
function declared() {}
var named = function realName() {};
print("declaration       = '" + declared.name + "'");
print("named expression  = '" + named.name + "'");

// an anonymous expression takes the name of the binding it lands in
var fromVar = function() {};
let fromLet = function() {};
const fromConst = function() {};
assigned = function() {};
print("var               = '" + fromVar.name + "'");
print("let               = '" + fromLet.name + "'");
print("const             = '" + fromConst.name + "'");
print("assignment        = '" + assigned.name + "'");

var arrow = () => 1;
print("arrow             = '" + arrow.name + "'");

var literal = { key: function() {}, other: function inner() {} };
print("object literal    = '" + literal.key.name + "' '" + literal.other.name + "'");

// approximation: ES6 infers a name only for an assignment to an identifier, but
// sai's parser derives one from a property access too, and that same derived name
// is what has always shown up in stack traces
literal.viaAccess = function() {};
print("property access   = '" + literal.viaAccess.name + "'");

// nothing to infer from: still anonymous
print("expression stmt   = '" + (function() {}).name + "'");
print("returned          = '" + (function() { return function() {}; })().name + "'");
print("argument          = '" + (function(f) { return f.name; })(function() {}) + "'");

// bound functions
print("bound             = '" + declared.bind({}).name + "'");
print("bound anonymous   = '" + (function() {}).bind({}).name + "'");
print("bound twice       = '" + declared.bind({}).bind({}).name + "'");
print("bound inferred    = '" + fromVar.bind({}).name + "'");

// the descriptor is unchanged - ES6 also makes name configurable, which sai does
// not do yet, so this records what is actually there
var desc = Object.getOwnPropertyDescriptor(function f() {}, "name");
print("value             = '" + desc.value + "'");
print("writable          = " + desc.writable);
print("enumerable        = " + desc.enumerable);
print("configurable      = " + desc.configurable);

// name is not writable, inferred or not
var target = function() {};
target.name = "ignored";
print("assignment kept   = '" + target.name + "'");

// a name is inferred once, when the function object is created, so two closures
// made from the same expression agree
function make() {
    var inner = function() {};
    return inner;
}
print("stable            = '" + make().name + "' '" + make().name + "'");
