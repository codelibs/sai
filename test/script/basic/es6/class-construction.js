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
 * A class constructor can only be called with new, and a class can extend null.
 *
 * @test
 * @run
 * @option --language=es6
 */

function calling(constructor) {
    try {
        constructor();
        return "called";
    } catch (e) {
        return e.name + ": " + e.message;
    }
}

// The constructor a class did not write out.
class Implicit {}
print(calling(Implicit));
print(new Implicit() instanceof Implicit);

// The one it did.
class Explicit {
    constructor(x) { this.x = x; }
}
print(calling(Explicit));
print(new Explicit(5).x);

// A derived class, whose super call passes the instance along and so goes through.
class Derived extends Explicit {
    constructor(x) { super(x * 2); }
}
print(calling(Derived));
print(new Derived(5).x);

class ImplicitDerived extends Explicit {}
print(calling(ImplicitDerived));
print(new ImplicitDerived(3).x);

// Both kinds keep the check when their body is compiled on demand, which is what
// happens the first time either of these is reached at all.
function lazyExplicit() {
    class Late { constructor() { this.v = "late"; } }
    return Late;
}
var LazyExplicit = lazyExplicit();
print(calling(LazyExplicit));
print(new LazyExplicit().v);

function lazyImplicit() {
    class Late {}
    return Late;
}
var LazyImplicit = lazyImplicit();
print(calling(LazyImplicit));
print(new LazyImplicit() instanceof LazyImplicit);

// An ordinary method named "constructor" is not a class constructor, even after a
// lazy re-parse of the object it belongs to.
function literal() {
    return { constructor() { return "literal"; } };
}
var method = literal().constructor;
print(method());

// class C extends null: the instances' prototype chain ends immediately and the
// class itself keeps Function.prototype.
class Nothing extends null {}
print(Object.getPrototypeOf(Nothing.prototype) === null);
print(Object.getPrototypeOf(Nothing) === Function.prototype);
print(calling(Nothing));

// extends undefined stays a TypeError.
try {
    eval("class Undef extends undefined {}");
    print("undefined accepted");
} catch (e) {
    print(e.name);
}

// The superclass expression is still evaluated exactly once.
var evaluations = 0;
function superclass() {
    evaluations++;
    return Explicit;
}
class Once extends superclass() {}
print(evaluations);
print(new Once(4).x);

// Two levels of super still work.
class L1 { m() { return "1"; } }
class L2 extends L1 { m() { return "2" + super.m(); } }
class L3 extends L2 { m() { return "3" + super.m(); } }
print(new L3().m());
print(new L3() instanceof L1);
