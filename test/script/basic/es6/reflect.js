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
 * ES2015 26.1, the Reflect object.
 *
 * Reflect is a new global name rather than a change to an existing operation, so
 * it is installed unconditionally and this test deliberately runs without
 * --language=es6.
 *
 * @test
 * @run
 */

function show(label, value) {
    print(label + ": " + value);
}

// 26.1.6, 26.1.13, 26.1.9, 26.1.4: the property operations.
show("get", Reflect.get({ qux: 987 }, "qux"));
show("get misses", Reflect.get({}, "nope"));

var written = {};
show("set", Reflect.set(written, "quux", 654) + "," + written.quux);
show("has own", Reflect.has({ qux: 987 }, "qux"));
show("has inherited", Reflect.has(Object.create({ up: 1 }), "up"));
show("has missing", Reflect.has({}, "nope"));

var deletable = { bar: 456 };
show("deleteProperty", Reflect.deleteProperty(deletable, "bar") + "," + ("bar" in deletable));

// 26.1.7: the descriptor comes back as an object, or undefined when there is
// nothing to describe.
var described = Reflect.getOwnPropertyDescriptor({ baz: 789 }, "baz");
show("getOwnPropertyDescriptor", [described.value, described.configurable, described.writable,
        described.enumerable].join(","));
show("getOwnPropertyDescriptor misses", Reflect.getOwnPropertyDescriptor({}, "nope"));

// 26.1.3: this is where Reflect and Object part company. Object.defineProperty
// throws when it cannot do as it was asked; Reflect.defineProperty says so.
var defined = {};
show("defineProperty", Reflect.defineProperty(defined, "foo", { value: 123 }) + "," + defined.foo);
show("defineProperty refused", Reflect.defineProperty(Object.freeze({}), "foo", { value: 123 }));

// 26.1.8 and 26.1.14: the prototype.
show("getPrototypeOf", Reflect.getPrototypeOf([]) === Array.prototype);
var reparented = {};
Reflect.setPrototypeOf(reparented, Array.prototype);
show("setPrototypeOf", reparented instanceof Array);

// 26.1.10 and 26.1.12: extensibility.
show("isExtensible", Reflect.isExtensible({}));
var sealed = {};
show("preventExtensions", Reflect.preventExtensions(sealed) + "," + Reflect.isExtensible(sealed));

// 26.1.11: every own key, enumerable or not, and nothing inherited.
var listed = Object.create({ C: true });
listed.A = true;
Object.defineProperty(listed, "B", { value: true, enumerable: false });
show("ownKeys", Reflect.ownKeys(listed).sort().join(","));

// The order is the one 9.1.12 lays down: array indices ascending, then the rest
// in the order they were added.
var ordered = { 2: true, 0: true, 1: true, " ": true, 9: true, D: true, B: true, "-1": true };
ordered.A = true;
ordered[3] = true;
Object.defineProperty(ordered, "C", { value: true, enumerable: true });
Object.defineProperty(ordered, "4", { value: true, enumerable: true });
delete ordered[2];
ordered[2] = true;
show("ownKeys order", Reflect.ownKeys(ordered).join(""));

// 26.1.1 and 26.1.2: calling and constructing.
show("apply", Reflect.apply(Array.prototype.push, [1, 2], [3, 4, 5]));
show("apply passes this", Reflect.apply(function () { return this.tag; }, { tag: "T" }, []));
show("construct", Reflect.construct(function (a, b, c) {
    this.qux = a + b + c;
}, ["foo", "bar", "baz"]).qux);
show("construct without arguments", Reflect.construct(function () {
    this.z = 9;
}, []).z);

// The third argument to construct names the constructor the result should be an
// instance of, which is how a parent's constructor builds a subclass instance.
function Sub() {}
var built = Reflect.construct(function () {
    this.y = 1;
}, [], Sub);
show("construct newTarget", built.y + "," + (built instanceof Sub));

// A refusal is reported, never thrown.
show("set on a frozen object", Reflect.set(Object.freeze({ a: 1 }), "a", 2));
var readOnlyProto = {};
Object.defineProperty(readOnlyProto, "x", { value: 1, writable: false });
show("set through a read-only prototype", Reflect.set(Object.create(readOnlyProto), "x", 2));

// The receiver argument decides what an accessor runs against.
var withGetter = Object.create({ get v() { return this.tag; } });
show("get with a receiver", Reflect.get(withGetter, "v", { tag: "R" }));

var seen = null;
var withSetter = Object.create({ set v(x) { seen = this.tag + x; } });
Reflect.set(withSetter, "v", 1, { tag: "R" });
show("set with a receiver", seen);
show("get a data property with a receiver", Reflect.get({ a: 5 }, "a", {}));

// A primitive has no operations to reflect on, and a non-function cannot be
// applied.
try {
    Reflect.get(1, "x");
    print("FAILED: Reflect.get accepted a primitive");
} catch (e) {
    show("primitive target rejected", e instanceof TypeError);
}
try {
    Reflect.apply({}, null, []);
    print("FAILED: Reflect.apply accepted a non-function");
} catch (e) {
    show("non-function rejected", e instanceof TypeError);
}

// Reflect is an ordinary object: it is not callable and not a constructor.
show("typeof Reflect", typeof Reflect);
try {
    new Reflect();
    print("FAILED: Reflect was constructed");
} catch (e) {
    show("not a constructor", e instanceof TypeError);
}
