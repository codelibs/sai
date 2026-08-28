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
 * ES6 spread in call arguments and array literals.
 *
 * @test
 * @run
 * @option --language=es6
 */

function add3(a, b, c) {
    return a + b + c;
}
var nums = [1, 2, 3];

print(add3(...nums));
print(add3(10, ...[20, 30]));
print(add3(...[100], 200, 300));
print(add3(...[1, 2], ...[3]));

// Strings and array-like objects spread by index.
print(add3(..."abc"));

// A method call keeps its receiver.
var obj = {
    base: 10,
    sum: function (a, b) {
        return this.base + a + b;
    }
};
print(obj.sum(...[1, 2]));

// The receiver is evaluated once.
var receiverCalls = 0;
function receiver() {
    receiverCalls++;
    return obj;
}
print(receiver().sum(...[1, 2]) + ";" + receiverCalls);

// Array literals.
print([...nums].join(","));
print([0, ...nums, 4].join(","));
print([...nums, ...nums].join(","));
print([..."ab"].join(","));
print([...{ length: 2, 0: "x", 1: "y" }].join(","));

// A spread copies rather than aliases.
var copy = [...nums];
copy.push(4);
print(nums.length + ":" + copy.length);

// The spread source is evaluated once.
var calls = 0;
function src() {
    calls++;
    return [1, 2];
}
print([...src()].join(",") + ";" + calls);

// Spreading the result of a call.
print(add3(...nums.map(function (x) { return x * 2; })));

// Empty sources contribute nothing.
print([...[]].length);
print(add3(...[1, 2, 3], ...[]));
print([1, ...[], 2].join(","));

// Spreading a Java array copies it. The result must not share storage with the
// source, and must not keep the source's component type either: both would show
// up as an ArrayStoreException out of an ordinary assignment.
var StringArray = Java.type("java.lang.String[]");
var javaArray = new StringArray(2);
javaArray[0] = "x";
javaArray[1] = "y";

var copy = [...javaArray];
print(copy.join(","));

copy[0] = "changed";
print(javaArray[0] + "," + javaArray[1]);

copy[0] = 42;
copy[1] = true;
print(copy.join(","));

// A Java array of a primitive type goes through reflection and was never shared.
var IntArray = Java.type("int[]");
var primitives = new IntArray(2);
primitives[0] = 1;
primitives[1] = 2;

var primitiveCopy = [...primitives];
primitiveCopy[0] = "s";
print(primitiveCopy.join(","));

// A List is copied by toArray, and the copy is detached from it.
var list = new (Java.type("java.util.ArrayList"))();
list.add("q");

var listCopy = [...list];
listCopy[0] = 9;
print(listCopy[0] + "," + list.get(0));
