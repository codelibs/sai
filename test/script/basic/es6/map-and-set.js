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
 * ES2015 23.1 and 23.2, the Map and Set built-ins.
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

// 23.1.1.1: the constructor requires new, accepts nothing, null, or an iterable
// of pairs, and routes every pair through whatever Map.prototype.set currently is.
try {
    Map();
    print("FAILED: Map() without new was allowed");
} catch (e) {
    show("Map without new", e instanceof TypeError);
}
show("Map(null)", new Map(null).size);
show("Map(undefined)", new Map(undefined).size);

var seen = [];
var realSet = Map.prototype.set;
Map.prototype.set = function (k, v) {
    seen.push(k);
    return realSet.call(this, k, v);
};
var fromPairs = new Map([["a", 1], ["b", 2]]);
Map.prototype.set = realSet;
show("constructor goes through prototype.set", seen.join(","));
show("built from pairs", fromPairs.get("a") + "," + fromPairs.get("b"));

// 23.1.3: the basic operations.
var key = {};
var map = new Map();
show("set returns this", map.set(key, 123) === map);
show("has", map.has(key));
show("get", map.get(key));
show("size", map.size);
show("delete", map.delete(key) + "," + map.delete(key));
show("size after delete", map.size);

// SameValueZero: -0 and +0 are one key, and NaN matches itself. Both differ from
// the === the rest of the language uses.
var zero = new Map();
zero.set(-0, "minus");
show("-0 reads back as +0", zero.get(+0));
zero.forEach(function (value, k) {
    show("-0 stored as +0", 1 / k === Infinity);
});
var nan = new Map();
nan.set(NaN, "nan");
show("NaN is its own key", nan.get(NaN));
var mixed = new Map();
mixed.set(1, "int");
mixed.set(1.0, "double");
show("1 and 1.0 are one key", mixed.size + "," + mixed.get(1));

// Insertion order survives replacement but not re-insertion.
var ordered = new Map();
ordered.set("b", 1);
ordered.set("a", 2);
ordered.set("c", 3);
ordered.set("b", 9);
var order = [];
ordered.forEach(function (value, k) {
    order.push(k + "=" + value);
});
show("insertion order", order.join(" "));

// 23.1.3.4, .8, .11: the three iterators.
var walk = new Map();
walk.set("x", 10);
walk.set("y", 20);
var keys = walk.keys();
show("keys", keys.next().value + "," + keys.next().value + "," + keys.next().done);
var values = walk.values();
show("values", values.next().value + "," + values.next().value + "," + values.next().done);
var entries = walk.entries();
show("entries", entries.next().value.join(":"));

// An entry deleted before the cursor reaches it is never handed out.
var live = new Map();
live.set(1, "one");
live.set(2, "two");
live.set(3, "three");
var cursor = live.entries();
cursor.next();
live.delete(2);
show("delete during iteration skips", cursor.next().value[0]);

// An exhausted iterator stays exhausted.
var spent = new Map().entries();
spent.next();
show("exhausted stays exhausted", spent.next().done);

// 23.1.3.5: forEach hands over value, key and the map itself.
var args = null;
var one = new Map();
one.set("k", "v");
one.forEach(function (value, k, self) {
    args = [value, k, self === one].join(",");
});
show("forEach arguments", args);

// clear empties the map.
var wiped = new Map();
wiped.set(1, 1);
wiped.clear();
show("clear", wiped.size + "," + wiped.has(1));

// 23.1.3: Map.prototype is not itself a Map, so its methods reject it.
try {
    Map.prototype.has({});
    print("FAILED: Map.prototype.has was allowed on the prototype");
} catch (e) {
    show("prototype is not an instance", e instanceof TypeError);
}

show("class name", Object.prototype.toString.call(new Map()));

// 23.2: Set is the same store with the value standing in for the key.
try {
    Set();
    print("FAILED: Set() without new was allowed");
} catch (e) {
    show("Set without new", e instanceof TypeError);
}
show("Set(null)", new Set(null).size);

var adds = [];
var realAdd = Set.prototype.add;
Set.prototype.add = function (v) {
    adds.push(v);
    return realAdd.call(this, v);
};
var built = new Set(["p", "q"]);
Set.prototype.add = realAdd;
show("constructor goes through prototype.add", adds.join(","));
show("built from values", built.has("p") + "," + built.has("q"));

var dup = new Set();
var only = {};
show("add returns this", dup.add(only) === dup);
dup.add(only);
show("added twice, stored once", dup.size);
show("Set has", dup.has(only) + "," + dup.has({}));
show("Set delete", dup.delete(only) + "," + dup.size);

var setOrder = new Set();
setOrder.add(3);
setOrder.add(1);
setOrder.add(2);
var collected = [];
setOrder.forEach(function (value, alsoValue, self) {
    collected.push(value + "/" + alsoValue + "/" + (self === setOrder));
});
show("Set forEach", collected.join(" "));

var setEntries = new Set(["z"]).entries().next().value;
show("Set entries pairs the value with itself", setEntries.join(":"));
var setKeys = new Set(["z"]).keys();
show("Set keys is Set values", setKeys.next().value);

try {
    Set.prototype.has({});
    print("FAILED: Set.prototype.has was allowed on the prototype");
} catch (e) {
    show("Set prototype is not an instance", e instanceof TypeError);
}

show("Set class name", Object.prototype.toString.call(new Set()));

// A Map or a Set can seed another collection, because both iterate their contents.
show("Set from a Set", new Set(new Set([1, 2, 2])).size);
show("Map from a Map", new Map(new Map([["k", "v"]])).get("k"));
