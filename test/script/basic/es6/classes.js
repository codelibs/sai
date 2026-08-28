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
 * ES6 class syntax.
 *
 * @test
 * @run
 * @option --language=es6
 */

class Point {
    constructor(x, y) {
        this.x = x;
        this.y = y;
    }
    sum() {
        return this.x + this.y;
    }
    static origin() {
        return new Point(0, 0);
    }
}

var p = new Point(1, 2);
print(p.sum());
print(Point.origin().sum());
print(p instanceof Point);
print(typeof Point);
print(Point.name);
print(p.constructor === Point);

class Point3 extends Point {
    constructor(x, y, z) {
        super(x, y);
        this.z = z;
    }
    sum() {
        return super.sum() + this.z;
    }
}

var q = new Point3(1, 2, 3);
print(q.sum());
print(q instanceof Point3);
print(q instanceof Point);

// Statics are inherited.
print(Point3.origin().sum());

// A class with no constructor of its own.
class Empty {
    m() {
        return "m";
    }
}
print(new Empty().m());

// A derived class with no constructor forwards its arguments.
class Derived extends Point {
}
var d = new Derived(3, 4);
print(d.sum());
print(d instanceof Point);

// Classes are expressions too.
var C = class {
    m() {
        return "expr";
    }
};
print(new C().m());

// Methods take the same parameter forms as any other function.
class Args {
    withDefault(a = 1) {
        return a;
    }
    withRest(...r) {
        return r.length;
    }
    withPattern({ v }) {
        return v;
    }
}
var a = new Args();
print(a.withDefault());
print(a.withRest(1, 2));
print(a.withPattern({ v: 9 }));

// Two class hierarchies in one scope keep their own superclass bindings.
class A {
    m() {
        return "A";
    }
}
class B {
    m() {
        return "B";
    }
}
class CA extends A {
    m() {
        return super.m() + "C";
    }
}
class DB extends B {
    m() {
        return super.m() + "D";
    }
}
print(new CA().m() + new DB().m());

// A class at the very start of a scope, where the program itself starts.
function scope() {
    class First {
        m() {
            return "first";
        }
    }
    return new First().m();
}
print(scope());

// A method reads super more than once, and after the class is long built.
class Base {
    twice() {
        return "base";
    }
}
class Twice extends Base {
    twice() {
        return super.twice() + super.twice();
    }
}
print(new Twice().twice());

// Accessors. A method is a plain assignment, but an accessor is not expressible as
// one, so it is defined on the prototype - or on the class itself when static.
class WithAccessors {
    get readOnly() {
        return 2;
    }

    set writeOnly(v) {
        this._written = v;
    }

    get paired() {
        return this._paired;
    }

    set paired(v) {
        this._paired = v * 2;
    }

    method() {
        return 10;
    }
}

var withAccessors = new WithAccessors();
print(withAccessors.readOnly);
withAccessors.writeOnly = 5;
print(withAccessors._written);
withAccessors.paired = 3;
print(withAccessors.paired);
print(withAccessors.method());

// A getter and a setter for the same name are defined one at a time, and a
// descriptor only changes the fields it names, so the pair survives.
print(typeof Object.getOwnPropertyDescriptor(WithAccessors.prototype, "paired").get);
print(typeof Object.getOwnPropertyDescriptor(WithAccessors.prototype, "paired").set);

// Static accessors live on the class.
class StaticAccessors {
    static get value() {
        return 3;
    }

    static set value(v) {
        StaticAccessors.stored = v;
    }
}

print(StaticAccessors.value);
StaticAccessors.value = 7;
print(StaticAccessors.stored);

// Accessors are inherited and can be overridden.
class AccessorBase {
    get which() {
        return "base";
    }
}

class AccessorDerived extends AccessorBase {
}

class AccessorOverride extends AccessorBase {
    get which() {
        return "override";
    }
}

print(new AccessorDerived().which);
print(new AccessorOverride().which);

// An accessor is not enumerable, as ES6 requires.
class NotEnumerable {
    get hidden() {
        return 1;
    }
}

var seen = [];
for (var key in new NotEnumerable()) {
    seen.push(key);
}
print(seen.length);

// "get" and "set" are ordinary names when a parameter list follows.
class NamedGetSet {
    get() {
        return "method get";
    }

    set(v) {
        return v;
    }

    static get() {
        return "static get";
    }
}

var namedGetSet = new NamedGetSet();
print(namedGetSet.get());
print(namedGetSet.set(9));
print(NamedGetSet.get());
