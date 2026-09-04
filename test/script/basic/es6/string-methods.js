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
 * ES2015 String methods: codePointAt, normalize, repeat, startsWith, endsWith,
 * includes and the static fromCodePoint.
 *
 * These are pure additions, so they are installed unconditionally and this test
 * deliberately runs without --language=es6.
 *
 * @test
 * @run
 */

function expectError(name, f) {
    try {
        f();
        print(name + ": no error");
    } catch (e) {
        print(name + ": " + e.name);
    }
}

// ---- codePointAt ----
// "𠮷" is U+20BB7, a character outside the BMP.
var surrogatePair = "𠮷";
print(surrogatePair.length);
print(surrogatePair.codePointAt(0));
print(surrogatePair.codePointAt(1));
print((surrogatePair + "a").codePointAt(2));
print("abc".codePointAt(0));
print("abc".codePointAt(3));
print("abc".codePointAt(-1));
print("abc".codePointAt(1.7));
print("abc".codePointAt(NaN));

// ---- normalize ----
// "c" followed by a combining cedilla normalizes to the single character U+00E7.
var decomposed = "ç";
print(decomposed.length);
print(decomposed.normalize().length);
print(decomposed.normalize() === "ç");
print(decomposed.normalize("NFC").length);
print("ç".normalize("NFD").length);
print("ﬁ".normalize("NFKC"));
print("ﬁ".normalize("NFKD").length);
expectError("normalize XYZ", function () { "a".normalize("XYZ"); });
expectError("normalize nfc", function () { "a".normalize("nfc"); });

// ---- repeat ----
print(JSON.stringify("ab".repeat(0)));
print("ab".repeat(1));
print("ab".repeat(3));
print(JSON.stringify("".repeat(5)));
print(JSON.stringify("a".repeat(NaN)));
print(JSON.stringify("a".repeat(-0.5)));
print("ab".repeat(2.9));
expectError("repeat -1", function () { "a".repeat(-1); });
expectError("repeat Infinity", function () { "a".repeat(Infinity); });
expectError("repeat -Infinity", function () { "a".repeat(-Infinity); });

// ---- startsWith ----
print("abcdef".startsWith("abc"));
print("abcdef".startsWith("bcd"));
print("abcdef".startsWith("bcd", 1));
print("abcdef".startsWith("abc", -5));
print("abcdef".startsWith(""));
print("abcdef".startsWith("f", 10));
expectError("startsWith regexp", function () { "abcdef".startsWith(/abc/); });

// ---- endsWith ----
print("abcdef".endsWith("def"));
print("abcdef".endsWith("cde"));
// The second argument is where the string is taken to end, not where to start.
print("abcdef".endsWith("cde", 5));
print("abcdef".endsWith("abc", 3));
print("abcdef".endsWith(""));
print("abcdef".endsWith("abc", 0));
expectError("endsWith regexp", function () { "abcdef".endsWith(/def/); });

// ---- includes ----
print("abcdef".includes("cde"));
print("abcdef".includes("cdf"));
print("abcdef".includes("abc", 1));
print("abcdef".includes("bcd", 1));
print("abcdef".includes(""));
expectError("includes regexp", function () { "abcdef".includes(/cde/); });

// A RegExp subclass instance is a regular expression too.
var re = new RegExp("a");
expectError("includes RegExp object", function () { "abc".includes(re); });
// A plain object that merely looks like one is not.
print("a/b/".includes({ toString: function () { return "/b/"; } }));

// ---- String.fromCodePoint ----
print(JSON.stringify(String.fromCodePoint()));
print(String.fromCodePoint(97, 98, 99));
print(String.fromCodePoint(0x20BB7) === surrogatePair);
print(String.fromCodePoint(0x20BB7).length);
print(String.fromCodePoint(0x10FFFF).length);
print(String.fromCodePoint("97"));
expectError("fromCodePoint -1", function () { String.fromCodePoint(-1); });
expectError("fromCodePoint too big", function () { String.fromCodePoint(0x110000); });
expectError("fromCodePoint fraction", function () { String.fromCodePoint(1.5); });
expectError("fromCodePoint NaN", function () { String.fromCodePoint(NaN); });
expectError("fromCodePoint Infinity", function () { String.fromCodePoint(Infinity); });

// ---- this coercion ----
var methods = ["codePointAt", "normalize", "repeat", "startsWith", "endsWith", "includes"];
for (var i = 0; i < methods.length; i++) {
    var name = methods[i];
    expectError("null this " + name, (function (m) {
        return function () { String.prototype[m].call(null, "a"); };
    })(name));
    expectError("undefined this " + name, (function (m) {
        return function () { String.prototype[m].call(undefined, "a"); };
    })(name));
}

// Non-string receivers are coerced with ToString.
print(String.prototype.repeat.call(12, 2));
print(String.prototype.startsWith.call(new String("abc"), "ab"));
print(String.prototype.includes.call(true, "ru"));

// ---- property attributes ----
var names = ["codePointAt", "normalize", "repeat", "startsWith", "endsWith", "includes"];
for (var i = 0; i < names.length; i++) {
    var d = Object.getOwnPropertyDescriptor(String.prototype, names[i]);
    print(names[i] + " " + d.writable + " " + d.enumerable + " " + d.configurable +
          " " + String.prototype[names[i]].length);
}
var fcp = Object.getOwnPropertyDescriptor(String, "fromCodePoint");
print("fromCodePoint " + fcp.writable + " " + fcp.enumerable + " " + fcp.configurable +
      " " + String.fromCodePoint.length);
