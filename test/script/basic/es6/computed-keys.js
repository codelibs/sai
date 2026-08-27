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
 * ES6 computed property names in object literals.
 *
 * @test
 * @run
 * @option --language=es6
 */

var k = "a";
var kb = "b";

print({ [k]: 1 }.a);
print(JSON.stringify({ [k]: 1, b: 2 }));
print(JSON.stringify({ a: 1, [kb]: 2 }));

// Properties are applied in source order, so a later key still wins.
print(JSON.stringify({ a: 1, [kb]: 2, b: 3 }));
print(JSON.stringify({ a: 1, b: 3, [kb]: 2 }));

// Each key expression is evaluated once, before its own value, in source order.
var log = [];
function t(v) {
    log.push(v);
    return v;
}
var d = { [t("p")]: t(1), [t("q")]: t(2) };
print(log.join(","));
print(d.p + "," + d.q);

// Any expression may be a key, and it is converted to a property name.
var n = 2;
var e = { [n]: "two", [n + 1]: "three" };
print(e[2] + "," + e[3]);

// Literals with computed keys nest, each getting its own temporary.
print({ [k]: { [kb]: 5 } }.a.b);

// Computed keys mix with shorthand.
var x = 1;
var g = { x, [k]: 9 };
print(g.x + "," + g.a);

// An accessor before a computed key is kept as an accessor.
var h = { get b() { return 7; }, [k]: 1 };
print(h.a + "," + h.b);

// The literal is still an expression, usable anywhere.
print([{ [k]: 1 }, { [kb]: 2 }].map(function (o) { return o.a || o.b; }).join(","));

// A computed key inside a function body, and inside a loop.
function f(key) {
    var out = [];
    for (var i = 0; i < 3; i++) {
        out.push({ [key + i]: i });
    }
    return out.map(function (o, j) { return o[key + j]; }).join(",");
}
print(f("z"));
