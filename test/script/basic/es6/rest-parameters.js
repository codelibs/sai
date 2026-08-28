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
 * ES6 rest parameters.
 *
 * @test
 * @run
 * @option --language=es6
 */

function f(a, ...rest) {
    return a + ":" + rest.join(",") + ":" + rest.length;
}
print(f(1));
print(f(1, 2, 3));

function g(...all) {
    return all.join(",");
}
print("[" + g() + "]");
print(g(1, 2));

// A rest binding is not a formal parameter.
print(f.length);
print(g.length);

// It is a real array, not the arguments object.
function h(...r) {
    return Array.isArray(r) + ":" + r.slice(1).join(",");
}
print(h(1, 2, 3));

// arguments still sees everything that was passed.
function i(a, ...r) {
    return arguments.length + ":" + arguments[1];
}
print(i(1, 2, 3));

// Arrow functions take rest bindings too.
var arrow = (a, ...r) => a + r.length;
print(arrow(1, 2, 3));

// Rest sits after a default.
function j(a = 5, ...r) {
    return a + ":" + r.length;
}
print(j());
print(j(1, 2, 3));

// Rest sits after a pattern.
function k({ v }, ...r) {
    return v + ":" + r.join(",");
}
print(k({ v: 1 }, 2, 3));

// A rest binding spreads straight back out.
function sum(a, b, c) {
    return a + b + c;
}
function pass(...args) {
    return sum(...args);
}
print(pass(1, 2, 3));
