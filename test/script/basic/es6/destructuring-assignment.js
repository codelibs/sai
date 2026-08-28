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
 * ES6 destructuring assignment, as an expression rather than a declaration.
 *
 * @test
 * @run
 * @option --language=es6
 */

var a, b, c;

[a, b] = [1, 2];
print(a + "," + b);

// The assignment yields the right hand side.
var r = ([a, b] = [3, 4]);
print(r.join(",") + ";" + a + "," + b);

// An object pattern needs parentheses where a statement could start with a block.
var x, y;
({ x, y } = { x: 5, y: 6 });
print(x + "," + y);

({ x: c } = { x: 7 });
print(c);

// Defaults.
[a = 9] = [];
print(a);
({ y = 8 } = {});
print(y);

// Elisions and missing elements.
[, b] = [10, 11];
print(b);
[a, b] = [12];
print(a + "," + b);

// Nesting, both ways round.
[a, [b, c]] = [1, [2, 3]];
print(a + "," + b + "," + c);
({ x: { y: a } } = { x: { y: 4 } });
print(a);

// Any assignment target works, not just a plain name.
var o = {};
var arr = [];
[o.p, arr[0]] = [5, 6];
print(o.p + "," + arr[0]);

// The right hand side is read before anything is assigned, so a swap works.
a = 1;
b = 2;
[a, b] = [b, a];
print(a + "," + b);

// The right hand side is evaluated once.
var calls = 0;
function src() {
    calls++;
    return [1, 2];
}
[a, b] = src();
print(calls);

// Array patterns read by index, so a string works.
[a, b] = "hi";
print(a + b);

// A computed key works in the assignment form too, where the leaves are targets.
var assignKey = "x";
var computedTarget;
({ [assignKey]: computedTarget } = { x: 32 });
print(computedTarget);

var assignEvaluations = 0;
function countingAssignKey() {
    assignEvaluations++;
    return "x";
}
var countedTarget;
({ [countingAssignKey()]: countedTarget } = { x: 33 });
print(countedTarget + " evals=" + assignEvaluations);
