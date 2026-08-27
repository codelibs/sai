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
 * ES6 destructuring patterns as function parameters.
 *
 * @test
 * @run
 * @option --language=es6
 */

function f({ a, b }) {
    return a + "," + b;
}
print(f({ a: 1, b: 2 }));

function g([x, y]) {
    return x + "," + y;
}
print(g([3, 4]));

// Renaming and a default inside the pattern.
function h({ a: p, b: q = 9 }) {
    return p + "," + q;
}
print(h({ a: 5 }));

// A default on the parameter itself, applied before the pattern is read.
function i({ a } = { a: 7 }) {
    return a;
}
print(i());
print(i({ a: 8 }));

// Patterns sit among ordinary parameters, in order.
function j(m, { n }, [o]) {
    return m + "," + n + "," + o;
}
print(j(1, { n: 2 }, [3]));

// Patterns nest.
function k({ a: [b, c] }) {
    return b + "," + c;
}
print(k({ a: [1, 2] }));

// Arrow functions take them too.
var arrow = ({ a, b }) => a + b;
print(arrow({ a: 1, b: 2 }));

// A pattern counts as one parameter.
print(f.length);
print(j.length);

// arguments still sees what was passed.
function l({ a }) {
    return arguments.length + ":" + a;
}
print(l({ a: 1 }, 2));

// A missing property is undefined rather than an error.
function m({ a, z }) {
    return a + "," + z;
}
print(m({ a: 1 }));

// Patterns work in a nested function and close over the outer scope.
function outer(scale) {
    function inner({ v }) {
        return v * scale;
    }
    return inner({ v: 3 });
}
print(outer(4));
