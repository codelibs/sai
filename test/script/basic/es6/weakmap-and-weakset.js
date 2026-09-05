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
 * ES2015 23.3 and 23.4, the WeakMap and WeakSet built-ins.
 *
 * These are new global names rather than changes to existing operations, so they
 * are installed unconditionally and this test deliberately runs without
 * --language=es6.
 *
 * @test
 * @run
 */

function show(label, value) {
    print(label + ": " + value);
}

// 23.3.1.1: the constructor requires new, accepts nothing or null, and routes
// every pair through whatever WeakMap.prototype.set currently is.
try {
    WeakMap();
    print("FAILED: WeakMap() without new was allowed");
} catch (e) {
    show("WeakMap without new", e instanceof TypeError);
}
new WeakMap(null);
new WeakMap(undefined);
show("WeakMap(null) and WeakMap(undefined)", "built");

var seen = 0;
var realSet = WeakMap.prototype.set;
WeakMap.prototype.set = function (k, v) {
    seen++;
    return realSet.call(this, k, v);
};
var first = {};
var second = {};
var built = new WeakMap([[first, 1], [second, 2]]);
WeakMap.prototype.set = realSet;
show("constructor goes through prototype.set", seen);
show("built from pairs", built.get(first) + "," + built.get(second));

// 23.3.3: the basic operations.
var key = {};
var map = new WeakMap();
show("set returns this", map.set(key, 123) === map);
show("has", map.has(key));
show("get", map.get(key));
show("delete", map.delete(key) + "," + map.delete(key));
show("has after delete", map.has(key));

// A frozen object is still usable as a key: the entry lives in the map, not in
// the object.
var frozen = Object.freeze({});
var frozenKeyed = new WeakMap();
frozenKeyed.set(frozen, 42);
show("frozen object as key", frozenKeyed.get(frozen));

// A function is an object too.
var fn = function () {};
show("function as key", new WeakMap().set(fn, "f").get(fn));

// A stored null has to stay distinguishable from a missing entry.
var nulls = new WeakMap();
nulls.set(key, null);
show("null value", nulls.has(key) + "," + (nulls.get(key) === null));
show("null value deletes", nulls.delete(key));

// 23.3.3.5: only an object can be a key, and offering a primitive is an error.
try {
    new WeakMap().set(1, "x");
    print("FAILED: a primitive was accepted as a WeakMap key");
} catch (e) {
    show("primitive key rejected", e instanceof TypeError);
}

// Asking after a primitive is not an error, though; it simply misses.
var lookups = new WeakMap();
show("primitive lookups miss quietly",
        lookups.has(1) + "," + lookups.get(1) + "," + lookups.delete(1));

// 23.3.3: there is no way to ask a WeakMap what it holds.
show("no size", "size" in WeakMap.prototype);
show("no clear", "clear" in WeakMap.prototype);
show("no iterators", ("keys" in WeakMap.prototype) + "," + ("forEach" in WeakMap.prototype));

try {
    WeakMap.prototype.has({});
    print("FAILED: WeakMap.prototype.has was allowed on the prototype");
} catch (e) {
    show("prototype is not an instance", e instanceof TypeError);
}

show("class name", Object.prototype.toString.call(new WeakMap()));

// 23.4: WeakSet is the same store with nothing kept alongside the object.
try {
    WeakSet();
    print("FAILED: WeakSet() without new was allowed");
} catch (e) {
    show("WeakSet without new", e instanceof TypeError);
}
new WeakSet(null);
show("WeakSet(null)", "built");

var adds = 0;
var realAdd = WeakSet.prototype.add;
WeakSet.prototype.add = function (v) {
    adds++;
    return realAdd.call(this, v);
};
var one = {};
var two = {};
var seeded = new WeakSet([one, two]);
WeakSet.prototype.add = realAdd;
show("constructor goes through prototype.add", adds);
show("built from values", seeded.has(one) + "," + seeded.has(two));

var member = {};
var set = new WeakSet();
show("add returns this", set.add(member) === set);
set.add(member);
show("added twice, held once", set.has(member) + "," + set.has({}));
show("delete", set.delete(member) + "," + set.delete(member));

try {
    new WeakSet().add("x");
    print("FAILED: a primitive was accepted as a WeakSet member");
} catch (e) {
    show("primitive member rejected", e instanceof TypeError);
}

var weakSetLookups = new WeakSet();
show("primitive lookups miss quietly", weakSetLookups.has(1) + "," + weakSetLookups.delete(1));

show("no size", "size" in WeakSet.prototype);
show("no clear", "clear" in WeakSet.prototype);

try {
    WeakSet.prototype.has({});
    print("FAILED: WeakSet.prototype.has was allowed on the prototype");
} catch (e) {
    show("WeakSet prototype is not an instance", e instanceof TypeError);
}

show("class name", Object.prototype.toString.call(new WeakSet()));
