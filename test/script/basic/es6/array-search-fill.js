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
 * ES6 Array.prototype.find / findIndex / fill / copyWithin and Array.of.
 *
 * @test
 * @run
 * @option --language=es6
 */

// arity
print([Array.prototype.find.length, Array.prototype.findIndex.length,
       Array.prototype.fill.length, Array.prototype.copyWithin.length,
       Array.of.length].join(","));

// find / findIndex, normal cases
print([1, 2, 3, 4].find(function (x) { return x > 2; }));
print([1, 2, 3, 4].findIndex(function (x) { return x > 2; }));
print([1, 2, 3].find(function () { return false; }));
print([1, 2, 3].findIndex(function () { return false; }));
print([].find(function () { return true; }));
print([].findIndex(function () { return true; }));
print(typeof [1].findIndex(function () { return true; }));

// the predicate is called with (element, index, array)
var seen = [];
[10, 20].find(function (v, i, a) { seen.push(v + ":" + i + ":" + (a.length)); return false; });
print(seen.join(" "));

// thisArg
print([1, 2, 3].find(function (x) { return x === this.wanted; }, { wanted: 2 }));

// holes are visited, unlike forEach
var visited = [];
[, 1, , 3].find(function (v, i) { visited.push(i + "=" + v); return false; });
print(visited.join(","));
print([, 1].findIndex(function (v) { return v === undefined; }));

// find works on array-likes
var arrayLike = { 0: "a", 1: "b", length: 2 };
print(Array.prototype.find.call(arrayLike, function (v) { return v === "b"; }));
print(Array.prototype.findIndex.call(arrayLike, function (v) { return v === "b"; }));

// a non-numeric length is ToUint32'd to 0
print(Array.prototype.findIndex.call({ length: "nope" }, function () { return true; }));

// fill
print(JSON.stringify([1, 2, 3, 4].fill(0)));
print(JSON.stringify([1, 2, 3, 4].fill(0, 1, 3)));
print(JSON.stringify([1, 2, 3, 4].fill(0, -2)));
print(JSON.stringify([1, 2, 3, 4].fill(0, -2, -1)));
print(JSON.stringify([1, 2, 3, 4].fill(0, 3, 1)));
print(JSON.stringify([1, 2, 3, 4].fill(0, 10)));
print(JSON.stringify([].fill(1)));
print(JSON.stringify(new Array(3).fill(7)));
var filled = [1, 2];
print(filled.fill(9) === filled);

// copyWithin, including overlapping ranges in both directions
print(JSON.stringify([1, 2, 3, 4, 5].copyWithin(0, 3)));
print(JSON.stringify([1, 2, 3, 4, 5].copyWithin(1, 3)));
print(JSON.stringify([1, 2, 3, 4, 5].copyWithin(0, 1)));
print(JSON.stringify([1, 2, 3, 4, 5].copyWithin(2, 0)));
print(JSON.stringify([1, 2, 3, 4, 5].copyWithin(-2, -3, -1)));
print(JSON.stringify([1, 2, 3, 4, 5].copyWithin(0, 0)));
print(JSON.stringify([].copyWithin(0, 1)));
var copied = [1, 2];
print(copied.copyWithin(0, 1) === copied);

// copyWithin propagates holes as deletions
var withHole = [1, , 3];
withHole.copyWithin(0, 1);
print(withHole.hasOwnProperty(0) + "," + JSON.stringify(withHole));

// Array.of does not treat a lone number as a length
print(Array.of(2).length + "," + Array.of(2)[0]);
print(JSON.stringify(Array.of(1, "a", true)));
print(JSON.stringify(Array.of()));
print(Array.isArray(Array.of(1)));

// null or undefined this is a TypeError for every new prototype function
["find", "findIndex", "fill", "copyWithin"].forEach(function (name) {
    [null, undefined].forEach(function (bad) {
        try {
            Array.prototype[name].call(bad, function () { return true; });
            print("no TypeError from " + name);
        } catch (e) {
            print(name + " " + (e instanceof TypeError));
        }
    });
});

// a non-callable predicate is a TypeError even when the array is empty
[1, undefined, null, {}].forEach(function (bad) {
    try {
        [].find(bad);
        print("no TypeError from find");
    } catch (e) {
        print("find callable " + (e instanceof TypeError));
    }
});
