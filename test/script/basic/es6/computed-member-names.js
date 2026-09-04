/*
 * Copyright (c) 2026, CodeLibs Project and/or its affiliates. All rights reserved.
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
 */

/**
 * ES6 members whose name is a string, a number or an expression, in an object
 * literal and in a class body, and super with an index.
 *
 * Every method here is called more than once, so that the lazily compiled body
 * goes through the re-parse of its own source range.
 *
 * @test
 * @run
 * @option --language=es6
 */

// --- object literal, keys that are not names -------------------------------

var strKeyed = { "foo bar"() { return 4; } };
print(strKeyed["foo bar"](), strKeyed["foo bar"]());

var numKeyed = { 1() { return "one"; }, 2.5() { return "two and a half"; } };
print(numKeyed[1](), numKeyed[2.5]());

var reserved = { "a:b|c"() { return "punctuated"; } };
print(reserved["a:b|c"](), reserved["a:b|c"]());

// --- object literal, computed keys -----------------------------------------

var x = "y";

var computed = { [x]() { return 1; } };
print(computed.y(), computed.y());

var made = { [(function () { return "made"; })()]() { return "mv"; } };
print(made.made(), made.made());

// --- object literal, computed accessors ------------------------------------

var valueSet;
var accessors = {
    get [x]() { return 1; },
    set [x](value) { valueSet = value; }
};
accessors.y = "foo";
print(accessors.y, accessors.y, valueSet);

// A property of an object literal is enumerable and configurable, however it
// was written.
var descriptor = Object.getOwnPropertyDescriptor(accessors, "y");
print(Object.keys(accessors).join(","), descriptor.enumerable, descriptor.configurable);

// An accessor may now follow a computed key: both are applied to the finished
// object, in source order.
var mixed = { [x]: 0, get later() { return "l"; }, set later(v) { valueSet = v; } };
mixed.later = "set";
print(mixed.y, mixed.later, valueSet);

// Keys are evaluated in source order, wherever they are.
var order = [];
function key(n) {
    order.push(n);
    return "k" + n;
}
var ordered = { [key(1)]: 1, [key(2)]() { return 2; }, get [key(3)]() { return 3; } };
print(order.join(","), ordered.k1, ordered.k2(), ordered.k3);

// The forms that were already there are unchanged.
var get = 1, set = 2;
print(JSON.stringify({ get, set }), ({ get: /re/ }).get.source);

// --- class body ------------------------------------------------------------

class StringKeyed {
    "foo bar"() { return 2; }
    static "s m"() { return "sm"; }
    get "g k"() { return "gk"; }
}
print(typeof StringKeyed.prototype["foo bar"], new StringKeyed()["foo bar"](),
      StringKeyed["s m"](), new StringKeyed()["g k"]);

var name = "method";

class Computed {
    [name]() { return 2; }
    static [name]() { return 3; }
}
print(new Computed().method(), new Computed().method(), Computed.method(), Computed.method());

class Accessors {
    get [name]() { return "got"; }
    set [name](v) { this.taken = v; }
    static get ["s" + name]() { return "static got"; }
}
var instance = new Accessors();
instance.method = "given";
print(instance.method, instance.taken, Accessors.smethod);

// No member of a class body is enumerable, accessor or method. What the key was
// written as makes no difference either way.
print("[" + Object.keys(Accessors.prototype).join(",") + "]",
      "[" + Object.keys(Computed.prototype).join(",") + "]");

// A key written as "constructor" is an ordinary member, so it replaces the
// constructor property rather than becoming the constructor.
class Named {
    ["constructor"]() { return "member"; }
}
print(new Named().constructor === Named, Named.prototype.constructor());

// A key may hold a function of its own, which the re-parse of the method has to
// step over rather than mistake for the method.
class Built {
    [(function () { return "built"; })()]() { return "bv"; }
}
print(new Built().built(), new Built().built());

// A class expression's name is not bound in its body, so a computed key that
// reads it is a reference to an undeclared variable.
try {
    var B = class C {
        [C]() {}
    };
    print("no error");
} catch (e) {
    print(e.name);
}

// --- super with an index ---------------------------------------------------

class Base {}
Base.prototype.qux = "foo";
Base.prototype.corge = "baz";

class Derived extends Base {
    quux(a) { return super.qux + a + super["corge"]; }
    lexical() { return (() => super["corge"])(); }
    called() { return super["toString"](); }
}
Derived.prototype.qux = "garply";

var derived = new Derived();
print(derived.quux("bar"), derived.quux("bar"), derived.lexical(), typeof derived.called());
