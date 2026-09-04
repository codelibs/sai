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
 * ES6 destructuring patterns as the parameter of a catch clause.
 *
 * @test
 * @run
 * @option --language=es6
 */

// Array and object patterns, nested the way the compatibility table nests them.
try {
    throw [1, 2];
} catch ([i, j]) {
    try {
        throw { k: 3, l: 4 };
    } catch ({ k, l }) {
        print(i, j, k, l);
    }
}

// Nested patterns, renaming, defaults and a rest element.
try {
    throw { a: { b: 5 }, c: undefined, d: [6, 7, 8] };
} catch ({ a: { b }, c = 9, d: [head, ...tail] }) {
    print(b, c, head, tail.join(","));
}

// A default may read a binding declared before it.
try {
    throw [10];
} catch ([m, n = m * 2]) {
    print(m, n);
}

// The bindings belong to the clause: they neither leak out nor collide with an
// outer name of their own.
var b = "outer";
try {
    throw { b: "inner" };
} catch ({ b }) {
    print(b);
}
print(b);
print(typeof i, typeof k);

// A declaration in the written body still shadows one of them.
try {
    throw [11];
} catch ([p]) {
    let p2 = p + 1;
    print(p, p2);
}

// Each clause gets its own binding, so a closure made in one keeps its values.
function trap(thrown) {
    try {
        throw thrown;
    } catch ([x, y]) {
        return function () {
            return x + "/" + y;
        };
    }
}

// Called more than once so that the lazily compiled body is re-parsed.
print(trap([1, 2])(), trap([3, 4])());

// A pattern in a catch inside a function that is itself re-parsed.
function outer() {
    try {
        throw { q: "q" };
    } catch ({ q }) {
        return q;
    }
}
print(outer(), outer());

// Throwing out of the clause still works, and a finally still runs.
try {
    try {
        throw ["boom"];
    } catch ([message]) {
        throw new Error(message);
    } finally {
        print("finally");
    }
} catch (e) {
    print(e.message);
}

// A plain name and a condition, the sai extension, are untouched.
try {
    throw 12;
} catch (e if e === 12) {
    print("conditional", e);
} catch (e) {
    print("unreachable");
}
