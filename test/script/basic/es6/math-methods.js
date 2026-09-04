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
 * The ES2015 Math methods. They are plain additions to an existing object, so like
 * Object.setPrototypeOf they are installed unconditionally - this test deliberately runs
 * without --language=es6 to pin that down.
 *
 * @test
 * @run
 */

// -0 and +0 print the same, so ask for the sign of the infinity you get back by dividing
function z(x) {
    return (x === 0 && 1 / x < 0) ? "-0" : String(x);
}

var names = ["clz32", "imul", "sign", "log10", "log2", "log1p", "expm1", "cosh", "sinh",
             "tanh", "acosh", "asinh", "atanh", "trunc", "fround", "cbrt", "hypot"];

for (var i = 0; i < names.length; i++) {
    if (typeof Math[names[i]] !== "function") {
        print("missing: Math." + names[i]);
    }
}
print("all 17 present");
print("arities " + names.map(function(n) { return Math[n].length; }).join(","));
print("enumerable " + Object.keys(Math).length);

print("-- clz32");
print(Math.clz32(0), Math.clz32(1), Math.clz32(2), Math.clz32(-1), Math.clz32(0x80000000));
print(Math.clz32(NaN), Math.clz32(Infinity), Math.clz32(undefined), Math.clz32("1"));
print(Math.clz32(4294967296), Math.clz32(4294967297));

print("-- imul");
print(Math.imul(2, 4), Math.imul(-1, 8), Math.imul(-2, -2));
print(Math.imul(0xffffffff, 5), Math.imul(0xfffffffe, 5));
print(Math.imul(2, "4"), Math.imul(NaN, 3), Math.imul(1e10, 1e10));

print("-- sign");
print(Math.sign(3), Math.sign(-3), z(Math.sign(0)), z(Math.sign(-0)), Math.sign(NaN));
print(Math.sign(-Infinity), Math.sign("-5"), Math.sign(undefined));

print("-- trunc");
print(Math.trunc(13.37), Math.trunc(-13.37), Math.trunc(42.84));
print(z(Math.trunc(0.5)), z(Math.trunc(-0.3)), z(Math.trunc(-0)), z(Math.trunc(-0.999)));
print(Math.trunc(NaN), Math.trunc(Infinity), Math.trunc(-Infinity));
print(Math.trunc(1e300) === 1e300, Math.trunc(-1e300) === -1e300);

print("-- fround");
print(Math.fround(1.1), Math.fround(5.5), Math.fround(5.05));
print(z(Math.fround(-0)), Math.fround(NaN), Math.fround(Infinity), Math.fround(-Infinity));
print(Math.fround(1e300), Math.fround(-1e300));

print("-- hypot");
print(Math.hypot(), Math.hypot(1), Math.hypot(3, 4), Math.hypot(9, 12, 20), Math.hypot(27, 36, 60, 100));
print(Math.hypot(-3, -4), Math.hypot(1e200, 1e200), Math.hypot(NaN, 1));
print(Math.hypot(Infinity, NaN), Math.hypot(NaN, -Infinity), z(Math.hypot(-0, -0)));

print("-- log10 / log2 / log1p / expm1");
print(Math.log10(1000), Math.log10(1), Math.log10(0), Math.log10(-1));
print(Math.log2(8), Math.log2(1024), Math.log2(0.5), Math.log2(1));
print(Math.log2(Math.pow(2, -1006)), Math.log2(0), Math.log2(-1), Math.log2(Infinity));
print(Math.log1p(0) === 0, Math.log1p(-1), Math.log1p(Math.E - 1));
print(Math.expm1(0) === 0, Math.expm1(-Infinity), Math.expm1(1) === Math.E - 1);

print("-- cbrt");
print(Math.cbrt(8), Math.cbrt(-8), Math.cbrt(27), z(Math.cbrt(-0)), Math.cbrt(Infinity));

print("-- cosh / sinh / tanh");
print(Math.cosh(0), z(Math.sinh(-0)), z(Math.tanh(-0)));
print(Math.cosh(Infinity), Math.sinh(-Infinity), Math.tanh(Infinity), Math.tanh(-Infinity));
print(Math.cosh(1), Math.sinh(1), Math.tanh(1));

print("-- acosh / asinh / atanh");
print(Math.acosh(1), Math.acosh(0.5), Math.acosh(Infinity), Math.acosh(2));
print(z(Math.asinh(-0)), Math.asinh(0) === 0, Math.asinh(Infinity), Math.asinh(-Infinity));
print(Math.asinh(1), Math.asinh(-1) === -Math.asinh(1));
print(z(Math.atanh(-0)), Math.atanh(1), Math.atanh(-1), Math.atanh(1.5), Math.atanh(0.5));

// The hand written ones must survive a round trip through their java.lang.Math inverse over a
// range where the naive closed forms would overflow or cancel.
print("-- round trips");
var xs = [1e-15, 1e-8, 0.25, 1, 3, 1e8, 1e150, 1e300];
print(xs.every(function(x) { return Math.abs(Math.sinh(Math.asinh(x)) / x - 1) < 1e-12; }));
print(xs.every(function(x) { return Math.abs(Math.cosh(Math.acosh(x + 1)) / (x + 1) - 1) < 1e-12; }));
var ts = [1e-15, 1e-8, 0.25, 0.5, 0.9, 0.999999];
print(ts.every(function(x) { return Math.abs(Math.tanh(Math.atanh(x)) / x - 1) < 1e-12; }));
// every exact power of two must give back an exact integer
var pows = true;
for (var e = -1074; e <= 1023; e++) {
    if (Math.log2(Math.pow(2, e)) !== e) {
        pows = false;
        print("log2 not exact at 2^" + e);
    }
}
print(pows);

print("-- writable and deletable like the other Math methods");
var saved = Math.trunc;
Math.trunc = 17;
print(Math.trunc);
Math.trunc = saved;
print(delete Math.trunc, typeof Math.trunc);
