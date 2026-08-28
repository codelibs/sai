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
 * ES6 destructuring in variable declarations.
 *
 * @test
 * @run
 * @option --language=es6
 */

// Array patterns.
var [a, b] = [1, 2];
print(a + "," + b);

var [c] = [3, 4];
print(c);

var [, d] = [5, 6];
print(d);

var [e, f] = [7];
print(e + "," + f);

// Defaults apply when the element is missing or undefined.
var [g = 9] = [];
print(g);
var [h = 9] = [1];
print(h);
var [i = 9] = [undefined];
print(i);

// Object patterns.
var { x, y } = { x: 1, y: 2 };
print(x + "," + y);

var { p: q } = { p: 3 };
print(q);

var { r = 4 } = {};
print(r);

var { s = 4 } = { s: 5 };
print(s);

// Nesting, both ways round.
var { t: [u, v] } = { t: [6, 7] };
print(u + "," + v);

var [{ w }] = [{ w: 8 }];
print(w);

// A pattern sits among ordinary declarators.
var m = 0, [n, o] = [1, 2], p2 = 3;
print(m + "," + n + "," + o + "," + p2);

// Array patterns read by index, so a string works.
var [s1, s2] = "hi";
print(s1 + "," + s2);

// let and const take patterns too.
let [la, lb] = [1, 2];
print(la + "," + lb);
const [ca] = [9];
print(ca);

// The right hand side is evaluated once.
var calls = 0;
function src() {
    calls++;
    return [1, 2];
}
var [z1, z2] = src();
print(calls + ":" + z1 + "," + z2);

// Destructuring undefined is a TypeError, as reading a property of it would be.
try {
    var [q1] = undefined;
    print("no error, got " + q1);
} catch (err) {
    print(err instanceof TypeError);
}

// A pattern inside a function, and inside a nested block, so that the temporaries
// are declared in the right function body.
function inner(o) {
    var { a: aa, b: bb } = o;
    if (aa < bb) {
        var [cc] = [aa + bb];
        return cc;
    }
    return -1;
}
print(inner({ a: 1, b: 2 }));

// Two patterns in one function get temporaries of their own.
function two(o) {
    var { a: a1 } = o;
    var { b: b1 } = o;
    return a1 + "," + b1;
}
print(two({ a: 3, b: 4 }));

// A const element whose default is actually taken. The default has to be part of
// the initializer, since a const binding cannot be assigned to a second time.
const [constDefault = 7] = [];
print(constDefault);

const { constKey = 8 } = {};
print(constKey);

const [constGiven = 1, constMissing = 2] = [9];
print(constGiven + "," + constMissing);

const [{ constNested = 3 } = {}] = [];
print(constNested);

// let and var take the same path.
let [letDefault = 10] = [];
var [varDefault = 11] = [];
print(letDefault + "," + varDefault);

// Only undefined takes the default.
const [fromUndefined = 12] = [undefined];
const [fromNull = 13] = [null];
const [fromZero = 14] = [0];
print(fromUndefined + "," + fromNull + "," + fromZero);

// The value is read once, whether or not the default is taken.
var reads = 0;
var counted = { get p() { reads++; return undefined; } };
const { p: taken = 15 } = counted;
print(taken + " reads=" + reads);

reads = 0;
var supplied = { get p() { reads++; return 16; } };
const { p: notTaken = 17 } = supplied;
print(notTaken + " reads=" + reads);
