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
 * ES2015 Object statics: Object.assign (19.1.2.1) and Object.is (19.1.2.10).
 *
 * @test
 * @run
 * @option --language=es6
 */

// --- Object.is ---------------------------------------------------------

print("is(NaN, NaN)      = " + Object.is(NaN, NaN));
print("is(0, -0)         = " + Object.is(0, -0));
print("is(-0, -0)        = " + Object.is(-0, -0));
print("is(0, 0)          = " + Object.is(0, 0));
print("is(1, 1)          = " + Object.is(1, 1));
print("is(1, '1')        = " + Object.is(1, "1"));
print("is('a', 'a')      = " + Object.is("a", "a"));
print("is(null, undef)   = " + Object.is(null, undefined));
print("is(null, null)    = " + Object.is(null, null));
print("is(undef, undef)  = " + Object.is());

var obj = {};
print("is(obj, obj)      = " + Object.is(obj, obj));
print("is({}, {})        = " + Object.is({}, {}));
print("is.length         = " + Object.is.length);

// --- Object.assign -----------------------------------------------------

print("assign.length     = " + Object.assign.length);

var target = { a: 1 };
var result = Object.assign(target, { b: 2 }, { c: 3, a: 4 });
print("returns target    = " + (result === target));
print("merged            = " + result.a + " " + result.b + " " + result.c);

// null and undefined sources are skipped rather than rejected
var skipped = Object.assign({}, null, undefined, { x: 1 });
print("skips nullish     = " + skipped.x);

// only own enumerable properties are copied
var src = {};
Object.defineProperty(src, "hidden", { value: 1, enumerable: false });
src.shown = 2;
var copied = Object.assign({}, src);
print("skips hidden      = " + ("hidden" in copied) + " " + copied.shown);

function Parent() {}
Parent.prototype.inherited = 1;
var child = new Parent();
child.own = 2;
copied = Object.assign({}, child);
print("skips inherited   = " + ("inherited" in copied) + " " + copied.own);

// a getter on the source is read once, as a value
var reads = 0;
var withGetter = { get v() { reads++; return 42; } };
copied = Object.assign({}, withGetter);
print("getter read       = " + copied.v + " " + reads);
print("copied as data    = " + Object.getOwnPropertyDescriptor(copied, "v").value);

// a setter on the target is invoked
var seen;
var accessor = {};
Object.defineProperty(accessor, "s", {
    get: function() { return "from getter"; },
    set: function(v) { seen = v; },
    configurable: true
});
Object.assign(accessor, { s: 7 });
print("setter called     = " + seen + " / " + accessor.s);

// a primitive source goes through ToObject
copied = Object.assign({}, "ab");
print("string source     = " + copied[0] + copied[1]);

// a primitive target goes through ToObject too
var wrapped = Object.assign(1, { a: 2 });
print("primitive target  = " + typeof wrapped + " " + wrapped.a);

// a nullish target is a TypeError
try {
    Object.assign(null, { a: 1 });
    print("no error for null");
} catch (e) {
    print("null target       = " + (e instanceof TypeError));
}

try {
    Object.assign();
    print("no error for undefined");
} catch (e) {
    print("undefined target  = " + (e instanceof TypeError));
}

// --- property attributes ----------------------------------------------

var desc = Object.getOwnPropertyDescriptor(Object, "assign");
print("assign attrs      = " + desc.writable + " " + desc.enumerable + " " + desc.configurable);
desc = Object.getOwnPropertyDescriptor(Object, "is");
print("is attrs          = " + desc.writable + " " + desc.enumerable + " " + desc.configurable);
