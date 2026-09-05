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
 * ES2015 19.2.4.1 and 19.2.4.2: a function's length and name are configurable.
 *
 * Neither property was standard before ES6, so making them configurable takes
 * nothing away from ES5 and this test deliberately runs without --language=es6.
 *
 * Redefining one of them exercises a path that was unreachable while they were
 * fixed: both are built-in accessors with a getter and no setter, so a value
 * given to defineProperty has nowhere to go and the property is replaced instead.
 *
 * @test
 * @run
 */

function show(label, value) {
    print(label + ": " + value);
}

function two(a, b) {}

var nameDesc = Object.getOwnPropertyDescriptor(two, "name");
show("name descriptor", [nameDesc.value, nameDesc.writable, nameDesc.enumerable,
        nameDesc.configurable].join(","));

var lengthDesc = Object.getOwnPropertyDescriptor(two, "length");
show("length descriptor", [lengthDesc.value, lengthDesc.writable, lengthDesc.enumerable,
        lengthDesc.configurable].join(","));

// Configurable means the value can be replaced.
function renamed() {}
Object.defineProperty(renamed, "name", { value: "chosen" });
show("renamed", renamed.name);
Object.defineProperty(renamed, "length", { value: 7 });
show("relengthed", renamed.length);

// Redefining a value leaves the attributes that were not mentioned alone, so the
// property stays non-writable, non-enumerable and configurable.
var afterRedefine = Object.getOwnPropertyDescriptor(renamed, "name");
show("attributes after redefine", [afterRedefine.value, afterRedefine.writable,
        afterRedefine.enumerable, afterRedefine.configurable].join(","));

// The attributes can be changed outright too.
Object.defineProperty(renamed, "name", { value: "locked", configurable: false, writable: true });
var locked = Object.getOwnPropertyDescriptor(renamed, "name");
show("attributes replaced", [locked.value, locked.writable, locked.configurable].join(","));

// Configurable also means deletable. Function.prototype has its own name and
// length, so deleting a function's own pair uncovers those rather than nothing.
function deletable(a) {}
show("delete name", delete deletable.name);
show("name after delete", "'" + deletable.name + "'");
show("length after delete", deletable.length);
show("name is no longer own, length still is",
        deletable.hasOwnProperty("name") + "," + deletable.hasOwnProperty("length"));

// Neither is writable, so a plain assignment is still ignored outside strict mode.
function assigned() {}
assigned.name = "ignored";
assigned.length = 99;
show("assignment ignored", "'" + assigned.name + "'," + assigned.length);

// prototype is a writable accessor and keeps taking values through its setter.
function withPrototype() {}
var replacement = {};
withPrototype.prototype = replacement;
show("prototype still settable", withPrototype.prototype === replacement);

// The ordinary readings are unchanged.
show("length reads arity", [function () {}.length, function (a) {}.length,
        function (a, b, c) {}.length].join(","));
show("name reads the identifier", function named() {}.name);
