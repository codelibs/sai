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
 * ES6 default function parameters.
 *
 * @test
 * @run
 * @option --language=es6
 */

function f(a, b = 1) {
    return a + b;
}
print(f(1));
print(f(1, 5));
print(f(1, undefined));
print(f(1, null));

// A default may refer to a parameter declared before it.
function g(a, b = a * 2) {
    return b;
}
print(g(3));
print(g(3, 4));

// The default expression is evaluated on every call that needs it.
function h(a = []) {
    a.push(1);
    return a.length;
}
print(h());
print(h());

// Several defaults are applied in declaration order.
function i(a = 1, b = a + 1, c = b + 1) {
    return a + "," + b + "," + c;
}
print(i());
print(i(10));
print(i(10, 20));

// Arrow functions take defaults too.
var add = (a, b = 2) => a + b;
print(add(1));
print(add(1, 10));

// A default sees the enclosing scope.
var outer = 100;
function j(a = outer) {
    return a;
}
print(j());

// Known deviation: ES6 counts only the parameters before the first default, so
// this would be 1 there. Defaults are desugared into the body here, so every
// parameter still counts.
print(f.length);
