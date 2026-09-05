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
 * ES2015 9.4.1.3 BoundFunctionCreate step 2: a bound function sits on whatever the
 * function it was made from sits on, not on Function.prototype. It matters for a
 * subclass of Function, and for anything that has had its prototype changed: a
 * bound method of a subclass instance kept none of the subclass.
 *
 * This is a change to what bind already did, but one that ES5.1 15.3.4.5 asked for
 * too -- it built the bound function from the standard function prototype only
 * because nothing said otherwise -- so it is not gated.
 *
 * @test
 * @run
 * @option --language=es6
 */

function show(label, value) {
    print(label + ": " + value);
}

function correctProtoBound(make, proto) {
    var target = make();
    Object.setPrototypeOf(target, proto);
    return Object.getPrototypeOf(Function.prototype.bind.call(target, null)) === proto;
}

[["a function", function () { return function () { }; }],
 ["an arrow function", function () { return () => 5; }],
 ["a class", function () { return class { }; }],
 ["a subclass", function () { return class extends Array { }; }]].forEach(function (pair) {
    show(pair[0], [correctProtoBound(pair[1], Function.prototype),
                   correctProtoBound(pair[1], {}),
                   correctProtoBound(pair[1], null)].join(","));
});

// A function whose prototype was never touched still binds onto Function.prototype.
function ordinary() { }
show("an untouched function", Object.getPrototypeOf(ordinary.bind(null)) === Function.prototype);

// Binding twice follows the prototype of the bound function, not of the original.
var twice = ordinary.bind(null);
Object.setPrototypeOf(twice, null);
show("binding a bound function", Object.getPrototypeOf(Function.prototype.bind.call(twice, null)));

// Everything bind already did, it still does.
function greet(greeting, name) {
    return greeting + ", " + name + ", from " + this.who;
}
var bound = greet.bind({ who: "here" }, "hello");
show("the bound this and arguments", bound("world"));
show("length and name", bound.length + "," + bound.name);
show("no prototype of its own", bound.hasOwnProperty("prototype"));

function Point(x, y) {
    this.x = x;
    this.y = y;
}
var BoundPoint = Point.bind(null, 1);
var point = new BoundPoint(2);
show("used with new", point.x + "," + point.y + "," + (point instanceof Point));

// The prototype of a bound function is not its target's "prototype" property, so
// instances built through it still come out of the target.
show("instances come from the target", Object.getPrototypeOf(point) === Point.prototype);

// A bound method of a subclass instance keeps the subclass, which is what the
// change is for.
class Base {
    constructor() { this.value = 1; }
}
class Derived extends Base { }
var derivedBound = Function.prototype.bind.call(Derived, null);
show("a bound subclass", Object.getPrototypeOf(derivedBound) === Base);
