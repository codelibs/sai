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
 * Members of a class body are not enumerable, they are defined without naming
 * anything in the enclosing scope, and the body is strict code.
 *
 * @test
 * @run
 * @option --language=es6
 */

class C {
    constructor() { this.own = 1; }
    method() { return "method"; }
    static staticMethod() { return "static"; }
    get accessor() { return "get"; }
    set accessor(value) { this.written = value; }
}

print(C.prototype.propertyIsEnumerable("method"));
print(C.propertyIsEnumerable("staticMethod"));
print(C.prototype.propertyIsEnumerable("accessor"));
print(Object.keys(C.prototype).length);
print(Object.getOwnPropertyNames(C.prototype).sort().join(","));

// The one thing a for-in over an instance sees is the instance's own field.
var instance = new C();
var enumerated = [];
for (var key in instance) {
    enumerated.push(key);
}
print(enumerated.join(","));

// A member is still writable and configurable, as a class member is in ES6.
var method = Object.getOwnPropertyDescriptor(C.prototype, "method");
print(method.writable + "," + method.configurable + "," + method.enumerable);

var accessor = Object.getOwnPropertyDescriptor(C.prototype, "accessor");
print(typeof accessor.get + "," + typeof accessor.set + "," + accessor.configurable + "," + accessor.enumerable);

// The two halves of a pair end up on the same property.
instance.accessor = "written";
print(instance.accessor + "," + instance.written);

// Members are ordinary properties, so they can be replaced and deleted.
C.prototype.method = function () { return "replaced"; };
print(new C().method());
print(delete C.prototype.method);

// A local named Object no longer breaks a class defined under it. eval, so that
// the class is compiled with that local in scope.
(function () {
    var Object = 1;
    eval("class Local { get x() { return 'local get'; } m() { return 'local method'; } }" +
         "print(new Local().x); print(new Local().m());");
})();

// The body is strict code even though this file is not.
class Strict {
    static receiver() { return this === undefined; }
    static assignUndeclared() {
        try {
            undeclaredGlobalName = 1;
            return "assigned";
        } catch (e) {
            return e.name;
        }
    }
}
print((0, Strict.receiver)());
print(Strict.assignUndeclared());

// Which makes the forms strict mode rejects syntax errors inside a class body.
["class Octal { m() { return 010; } }",
 "class With { m(o) { with (o) {} } }",
 "class Eval { m() { eval = 1; } }"].forEach(function (source) {
    try {
        eval(source);
        print("accepted: " + source);
    } catch (e) {
        print(e.name);
    }
});

// Inheritance is unaffected.
class Base {
    hello() { return "base"; }
    static shared() { return "shared"; }
}
class Derived extends Base {
    hello() { return "derived/" + super.hello(); }
}
print(new Derived().hello());
print(Derived.shared());
print(new Derived() instanceof Base);
print(Derived.prototype.propertyIsEnumerable("hello"));

// The last definition of a name wins, as it does for any property.
class Last {
    m() { return 1; }
    m() { return 2; }
}
print(new Last().m());
