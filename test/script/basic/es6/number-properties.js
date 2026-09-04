/*
 * Copyright (c) 2010, 2014, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
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
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/**
 * The ES2015 Number properties. Like the ES2015 Math methods they are plain additions to an
 * object that already exists, so they are installed unconditionally - this test deliberately
 * runs without --language=es6 to pin that down.
 *
 * @test
 * @run
 */

print("-- all nine are there");
print(typeof Number.isFinite, typeof Number.isInteger, typeof Number.isSafeInteger,
      typeof Number.isNaN, typeof Number.parseFloat, typeof Number.parseInt);
print(typeof Number.EPSILON, typeof Number.MIN_SAFE_INTEGER, typeof Number.MAX_SAFE_INTEGER);
print("enumerable " + Object.keys(Number).length);
print("arities " + [Number.isFinite.length, Number.isInteger.length, Number.isSafeInteger.length,
                    Number.isNaN.length, Number.parseFloat.length, Number.parseInt.length].join(","));

print("-- the constants");
print(Number.EPSILON, Number.MAX_SAFE_INTEGER, Number.MIN_SAFE_INTEGER);
print(1 + Number.EPSILON > 1, 1 + Number.EPSILON / 2 === 1);
print(Number.MAX_SAFE_INTEGER === Math.pow(2, 53) - 1);
print(Number.MIN_SAFE_INTEGER === -Number.MAX_SAFE_INTEGER);
// past the safe range consecutive integers stop being distinguishable
print(Number.MAX_SAFE_INTEGER + 2 === Number.MAX_SAFE_INTEGER + 1);
print(Number.MAX_SAFE_INTEGER + 1 !== Number.MAX_SAFE_INTEGER);

print("-- Number.parseInt and Number.parseFloat are the global functions themselves");
var actualGlobal = Function("return this")();
print(Number.parseInt === actualGlobal.parseInt, Number.parseFloat === actualGlobal.parseFloat);
print(Number.parseInt === parseInt, Number.parseFloat === parseFloat);
print(Number.parseInt("42px"), Number.parseInt("ff", 16), Number.parseInt("  -7  "));
print(Number.parseFloat("3.14abc"), Number.parseFloat(".5"), Number.parseFloat("x"));

print("-- Number.isNaN does not coerce, the global isNaN does");
print(Number.isNaN(NaN), Number.isNaN(0 / 0), Number.isNaN(1));
print(Number.isNaN("NaN"), isNaN("NaN"));
print(Number.isNaN(undefined), isNaN(undefined));
print(Number.isNaN(new Number(NaN)), Number.isNaN({}), Number.isNaN(null), Number.isNaN(true));

print("-- Number.isFinite does not coerce either");
print(Number.isFinite(0), Number.isFinite(-0), Number.isFinite(2e64), Number.isFinite(Number.MIN_VALUE));
print(Number.isFinite(Infinity), Number.isFinite(-Infinity), Number.isFinite(NaN));
print(Number.isFinite("1"), isFinite("1"));
print(Number.isFinite(null), isFinite(null));
print(Number.isFinite(new Number(1)), Number.isFinite(true), Number.isFinite([]));

print("-- Number.isInteger");
print(Number.isInteger(0), Number.isInteger(-0), Number.isInteger(1), Number.isInteger(-100000));
print(Number.isInteger(5.0), Number.isInteger(5.000000000000001), Number.isInteger(0.1));
print(Number.isInteger(1e100), Number.isInteger(Math.pow(2, 53)));
print(Number.isInteger("1"), Number.isInteger("10"), Number.isInteger(new Number(1)));
print(Number.isInteger(Infinity), Number.isInteger(-Infinity), Number.isInteger(NaN));
print(Number.isInteger(true), Number.isInteger(null), Number.isInteger(undefined), Number.isInteger([1]));

print("-- Number.isSafeInteger");
print(Number.isSafeInteger(3), Number.isSafeInteger(-3), Number.isSafeInteger(0), Number.isSafeInteger(-0));
print(Number.isSafeInteger(Math.pow(2, 53)), Number.isSafeInteger(Math.pow(2, 53) - 1));
print(Number.isSafeInteger(-Math.pow(2, 53)), Number.isSafeInteger(1 - Math.pow(2, 53)));
print(Number.isSafeInteger(Number.MAX_SAFE_INTEGER), Number.isSafeInteger(Number.MIN_SAFE_INTEGER));
print(Number.isSafeInteger(3.1), Number.isSafeInteger(NaN), Number.isSafeInteger(Infinity));
print(Number.isSafeInteger("3"), Number.isSafeInteger(new Number(3)));

print("-- attributes match the neighbouring Number members");
print(Object.getOwnPropertyDescriptor(Number, "EPSILON").writable,
      Object.getOwnPropertyDescriptor(Number, "MAX_VALUE").writable);
var d = Object.getOwnPropertyDescriptor(Number, "isNaN");
print(d.writable, d.enumerable, d.configurable);
d = Object.getOwnPropertyDescriptor(Number, "parseInt");
print(d.writable, d.enumerable, d.configurable);
