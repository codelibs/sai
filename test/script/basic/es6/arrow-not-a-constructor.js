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
 * An arrow function is not a constructor: it has no prototype property and new
 * rejects it. It also has no arguments object of its own, so arguments inside
 * one means the arguments of the function around it.
 *
 * @test
 * @run
 * @option --language=es6
 */

// --- not a constructor -----------------------------------------------------

var arrow = () => 5;
print(Object.getOwnPropertyNames(arrow).sort().join(","));
print(arrow.hasOwnProperty("prototype"), arrow.prototype);

try {
    new arrow();
    print("constructed");
} catch (e) {
    print(e.name);
}

// A strict arrow is the same, minus the prototype it never had.
function strictly() {
    "use strict";
    return () => 6;
}
var strictArrow = strictly();
print(Object.getOwnPropertyNames(strictArrow).sort().join(","));
try {
    new strictArrow();
    print("constructed");
} catch (e) {
    print(e.name);
}

// An ordinary function, a method and a class are untouched.
function plain() {}
print(plain.hasOwnProperty("prototype"), typeof new plain());

var literal = { m() { return 1; } };
print(literal.m.hasOwnProperty("prototype"));

class C { m() { return 2; } }
print(new C().m(), C.prototype.m.hasOwnProperty("prototype"));

// Binding and currying an arrow still work; a bound function has no prototype
// either.
var curried = x => y => x + y;
print(curried(1)(2));
var bound = arrow.bind(null);
print(bound(), bound.hasOwnProperty("prototype"));

// So do call and apply.
print(arrow.call(null), arrow.apply(null, []));

// --- lexical arguments -----------------------------------------------------

function count() {
    return (() => arguments.length)();
}
print(count(1, 2, 3));

// Arrows nested in arrows all reach the same function.
function deep() {
    return (() => (() => arguments[0])())();
}
print(deep("deep"));

// The arrow's own parameters are separate from the outer arguments.
function both(a, b) {
    return ((x) => x + ":" + arguments.length + ":" + arguments[0])("own");
}
print(both(7, 8));

// The arrow is only called after its enclosing body has been re-parsed, which
// is the path the binding has to survive.
function lazily() {
    return () => arguments[0];
}
print(lazily("late")());

// A strict enclosing function, and a method.
function strictOuter() {
    "use strict";
    return (() => arguments.length)();
}
print(strictOuter(1, 2));

class Method {
    m() { return (() => arguments[0])(); }
}
print(new Method().m("method"));

// An ordinary function inside an arrow gets an arguments object of its own.
function mixed() {
    return (() => (function () { return arguments[0]; })("inner"))();
}
print(mixed("outer"));

// The binding is the arguments object itself, so writing through it is visible.
function writable() {
    return (() => { arguments[0] = "changed"; return arguments[0]; })();
}
print(writable("original"));

// The shorthand form reads it too.
function shorthand() {
    return (() => ({ arguments }))().arguments.length;
}
print(shorthand(1, 2, 3));

// Code passed to eval finds the arguments of the function the eval runs in.
function evalled() {
    return eval("(() => arguments[0])()");
}
print(evalled("from eval"));
