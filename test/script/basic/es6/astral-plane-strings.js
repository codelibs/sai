/*
 * Copyright (c) 2026, CodeLibs Project and the Others. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * ES6 walks a string by code point, not by UTF-16 code unit, wherever it
 * iterates one: for..of (13.7.5), spread (12.3.6.1) and array destructuring
 * (13.3.3). Indexing a string is unchanged and stays by code unit.
 *
 * @test
 * @run
 * @option --language=es6
 */

// U+20BB7, a surrogate pair: two code units, one code point.
var astral = "𠮷";
var mixed = "a" + astral + "b";

// Indexing and length are untouched -- they are code units, as ever.
print("length: " + mixed.length);
print("index 1: " + (mixed[1] === "\uD842"));
print("charAt: " + (mixed.charAt(1) === "\uD842"));

// Spread, in an array literal and in a call.
print("spread array: " + [...mixed].length);
print("spread array parts: " + [...mixed].join('|'));
print("spread call: " + (function () { return arguments.length; })(...mixed));

// for..of
var out = [];
for (var ch of mixed) { out.push(ch); }
print("for-of: " + out.length + " " + out.join('|'));

// Array destructuring, in a declaration, an assignment and a parameter.
var [d0, d1, d2] = mixed;
print("destructuring declaration: " + (d1 === astral));

var a0, a1, a2;
[a0, a1, a2] = mixed;
print("destructuring assignment: " + (a1 === astral));

print("destructuring parameter: " + (function ([p0, p1]) { return p1 === astral; })(mixed));

// A rest element collects the remaining code points.
var [, ...rest] = mixed;
print("rest: " + rest.length + " " + (rest[0] === astral));

// Object destructuring of a string is not array iteration: it keeps reading
// named properties, so length and a numeric key stay code-unit based.
var { length: len, 1: unit } = mixed;
print("object pattern length: " + len);
print("object pattern index 1: " + (unit === "\uD842"));

// A lone surrogate is still one element; nothing is dropped or merged.
print("lone surrogate: " + [..."\uD842"].length);
print("trailing high surrogate: " + [..."a\uD842"].length);

// Everything that is not a string is unaffected.
print("array spread: " + [...[1, 2, 3]].length);
print("arguments spread: " + (function () { return [...arguments].length; })(1, 2));
print("empty string: " + [...""].length);
