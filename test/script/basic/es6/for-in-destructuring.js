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
 * ES6 destructuring patterns as the loop variable of a for-in loop. The key a
 * for-in hands over is a string, so an array pattern reads it out character by
 * character.
 *
 * @test
 * @run
 * @option --language=es6
 */

for (var [i, j, k] in { qux: 1 }) {
    print(i === "q" && j === "u" && k === "x");
}

// The names a var pattern declares outlive the loop, as a var does.
print(i, j, k);

// An object pattern reads properties of the key string.
for (var { length } in { abcd: 1 }) {
    print(length);
}

// let is fresh on every iteration, so a closure keeps that iteration's key.
var closures = [];
for (let [a, b] in { xy: 1, zw: 2 }) {
    closures.push(function () {
        return a + b;
    });
}
print(closures[0](), closures[1]());
print(typeof a);

// const works the same and cannot be assigned to.
for (const [c] in { m: 1 }) {
    print(c);
}

// Defaults and a rest element.
for (var [head, ...tail] in { hello: 1 }) {
    print(head, tail.join(""));
}
for (var [p, q = "!"] in { z: 1 }) {
    print(p, q);
}

// Without a declaration the leaves are assignment targets.
var x, y, target = {};
for ([x, y] in { ab: 1 }) {
    print(x, y);
}
for ([target.first, target.second] in { cd: 1 }) {
    print(target.first, target.second);
}

// break and continue still work, and the collection is only read once.
var reads = 0;
function source() {
    reads++;
    return { ab: 1, cd: 2, ef: 3 };
}
var seen = [];
for (var [s] in source()) {
    if (s === "c") {
        continue;
    }
    if (s === "e") {
        break;
    }
    seen.push(s);
}
print(seen.join(","), reads);

// Called more than once so that the lazily compiled body is re-parsed.
function collect(o) {
    var out = [];
    for (let [first, second] in o) {
        out.push(first + second);
    }
    return out.join(",");
}
print(collect({ ab: 1, cd: 2 }), collect({ ef: 3 }));

// Nothing to iterate.
for (var [none] in {}) {
    print("unreachable");
}
print(typeof none);
