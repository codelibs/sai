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
 * ES6 arrow functions.
 *
 * @test
 * @run
 * @option --language=es6
 */

var inc = x => x + 1;
print(inc(1));

var add = (a, b) => a + b;
print(add(2, 3));

var zero = () => 0;
print(zero());

var block = a => { var t = a * 2; return t; };
print(block(4));

print([1, 2, 3].map(x => x * x).join(","));

print(typeof (x => x));

print((x => x).length);

// The body is an AssignmentExpression, so the comma binds outside the arrow.
print((x => x, 9));

// An object literal body needs parentheses.
var mk = a => ({ a: a });
print(mk(1).a);

// Arrows close over variables the ordinary way.
function counter() {
    var n = 0;
    return () => ++n;
}
var c = counter();
c();
c();
print(c());

// this is taken from the enclosing function, not from the call.
var obj = {
    v: 42,
    get: function () {
        var f = () => this.v;
        return f();
    }
};
print(obj.get());

// Nested arrows share that same this.
var obj2 = {
    v: 7,
    get: function () {
        var f = () => () => this.v;
        return f()();
    }
};
print(obj2.get());

// this inside an arrow passed to a callback.
var obj3 = {
    vals: [1, 2, 3],
    factor: 10,
    scaled: function () {
        return this.vals.map(x => x * this.factor).join(",");
    }
};
print(obj3.scaled());

// A plain function still gets its own this.
var obj4 = {
    v: 1,
    get: function () {
        var f = function () { return typeof this.v; };
        return f();
    }
};
print(obj4.get());

// A function holding an arrow still recompiles correctly when optimistic typing
// deoptimizes it: acc starts out an int and widens to a double.
var obj5 = {
    v: 3,
    run: function (n) {
        var acc = 0;
        var add = () => this.v;
        for (var i = 0; i < n; i++) {
            acc += add();
        }
        acc += 0.5;
        return acc;
    }
};
print(obj5.run(4));
