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
 * Annex B.3.1: only the colon form `__proto__: value` in an object literal sets
 * the prototype. A computed key, a shorthand property and a shorthand method
 * that happen to spell __proto__ define an ordinary own property, and two
 * colon forms in one literal are an early error.
 *
 * @test
 * @run
 * @option --language=es6
 */

// The colon form sets the prototype, and defines no own property.
var arr = [];
var byColon = { __proto__: arr };
print("colon sets proto: " + (Object.getPrototypeOf(byColon) === arr));
print("colon defines no own: " + !byColon.hasOwnProperty("__proto__"));

// null works too, and so does a value that cannot be a prototype, which is
// ignored rather than rejected.
print("colon accepts null: " + (Object.getPrototypeOf({ __proto__: null }) === null));
print("colon ignores a number: " + (Object.getPrototypeOf({ __proto__: 1 }) === Object.prototype));

// A computed key is not the colon form.
var key = "__proto__";
var byComputed = { [key]: arr };
print("computed keeps proto: " + (Object.getPrototypeOf(byComputed) === Object.prototype));
print("computed defines own: " + byComputed.hasOwnProperty("__proto__"));
print("computed own value: " + (byComputed.__proto__ === arr));

// A computed key defines the property rather than assigning it, so an inherited
// setter of the same name does not run.
var ran = false;
Object.defineProperty(Object.prototype, "trap", { set: function () { ran = true; }, configurable: true });
var trapped = { ["trap"]: 1 };
print("computed does not call inherited setter: " + (!ran && trapped.hasOwnProperty("trap")));
delete Object.prototype.trap;

// A shorthand property is not the colon form.
var __proto__ = arr;
var byShorthand = { __proto__ };
print("shorthand keeps proto: " + (Object.getPrototypeOf(byShorthand) === Object.prototype));
print("shorthand defines own: " + byShorthand.hasOwnProperty("__proto__"));

// A shorthand method is not the colon form.
var byMethod = { __proto__() { return 1; } };
print("method keeps proto: " + (Object.getPrototypeOf(byMethod) === Object.prototype));
print("method defines own: " + (typeof byMethod.__proto__ === "function"));

// An accessor named __proto__ is not the colon form either.
var byAccessor = { get __proto__() { return 1; } };
print("accessor keeps proto: " + (Object.getPrototypeOf(byAccessor) === Object.prototype));
print("accessor defines own: " + (byAccessor.__proto__ === 1));

// A string key is still the colon form.
print("string key sets proto: " + (Object.getPrototypeOf({ "__proto__": arr }) === arr));

// Two colon forms in one literal are an early error. It has to be compiled on
// its own to be observable, so eval defers it.
try {
    eval("({ __proto__: [], __proto__: {} })");
    print("duplicate proto: not reported");
} catch (e) {
    print("duplicate proto: " + (e instanceof SyntaxError));
}

// One colon form beside other spellings is fine: the colon form sets the
// prototype and the other two define own properties on top of it.
var k = "__proto__";
var mixed = { __proto__: arr, [k]: 1, };
print("mixed forms set proto: " + (Object.getPrototypeOf(mixed) === arr));
print("mixed forms define own: " + (mixed.hasOwnProperty("__proto__") && mixed.__proto__ === 1));

// Assignment through the accessor still works; only the literal form changed.
var assigned = {};
assigned.__proto__ = arr;
print("assignment still sets proto: " + (Object.getPrototypeOf(assigned) === arr));
