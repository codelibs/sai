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
 * ES6 object literal shorthand properties.
 *
 * @test
 * @run
 * @option --language=es6
 */

var x = 1, y = 2, z = 3;

var o = { x, y };
print(o.x + "," + o.y);

// Shorthand mixes with ordinary properties.
var o2 = { x, z: z * 2, y };
print(o2.x + "," + o2.z + "," + o2.y);

// The value is whatever the variable holds, functions included.
function fn() {
    return 5;
}
print({ fn }.fn());

print(JSON.stringify({ x }));
print(JSON.stringify({ y, x }));

// The value is read once, when the literal is evaluated.
var w = 1;
var o4 = { w };
w = 9;
print(o4.w);

// get and set are ordinary names in shorthand position.
var get = 10, set = 11;
var o5 = { get, set };
print(o5.get + "," + o5.set);

// An accessor still parses next to a shorthand.
var o6 = { x, get b() { return 7; } };
print(o6.x + "," + o6.b);

// Duplicate keys behave as they always did: the last one wins.
print(JSON.stringify({ x, x: 2 }));
