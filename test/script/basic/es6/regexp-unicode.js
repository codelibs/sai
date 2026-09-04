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
 * ES6 RegExp "u" (unicode) flag.
 *
 * @test
 * @run
 * @option --language=es6
 */

// The source is kept plain ASCII so that the file survives any transcoding.
var ASTRAL = String.fromCharCode(0xd842, 0xdfb7);   // U+20BB7
var TETRA = String.fromCharCode(0xd834, 0xdf06);    // U+1D306
var LONG_S = String.fromCharCode(0x017f);
var KELVIN = String.fromCharCode(0x212a);
var NBSP = String.fromCharCode(0x00a0);

// The flag is accepted both in a literal and in the constructor.
print(/a/u.unicode, new RegExp("a", "u").unicode, /a/.unicode);
print(String(/a/u), String(/a/gimuy));

// A pattern matches code points, not UTF-16 code units.
print(ASTRAL.match(/^.$/u)[0].length);
print((ASTRAL + "x").match(/^.x$/u)[0].length);
print((ASTRAL + ASTRAL).match(/^.{2}$/u)[0].length);
print(ASTRAL.match(/^.$/));

// A lone surrogate in the pattern no longer matches half of a pair.
print(/[\ud842]/u.test(ASTRAL), /[\ud842]/.test(ASTRAL));

// Code point escapes.
print(TETRA.match(/\u{1d306}/u)[0].length);
print(TETRA.match(/[\u{1d306}]/u)[0].length);
print((TETRA + TETRA).match(/\u{1d306}{2}/u)[0].length);
print("A".match(/\u{41}/u)[0], "A".match(/\u{00000041}/u)[0]);
print(ASTRAL.match(/[\u{20bb7}]/u)[0].length);

// A literal astral character in the pattern works too.
print(ASTRAL.match(new RegExp(ASTRAL, "u"))[0].length);

// Case folding is full Unicode case folding under u, and ASCII only without it.
print(/S/iu.test(LONG_S), /S/i.test(LONG_S));
print(/k/iu.test(KELVIN), /k/i.test(KELVIN));
print(/I/iu.test("i"), /I/i.test("i"));

// \s keeps its JavaScript meaning even though u picks a different engine.
print(/\s/u.test(NBSP), /[\s]/u.test(NBSP), /\S/u.test(NBSP));

// So do the group and replacement conventions.
print(JSON.stringify("abc".match(/(a)(x)?(c)?/u)));
print(JSON.stringify("a1b2c".split(/(\d)/u)));
print("abc".replace(/(b)/u, "[$1$&$`$']"));

// u composes with the other flags.
var re = /\u{20bb7}/guy;
print(re.unicode, re.sticky, re.global);
print(re.exec("x" + ASTRAL), re.lastIndex);
re.lastIndex = 1;
print(re.exec("x" + ASTRAL)[0].length, re.lastIndex);

// A malformed code point escape is an error rather than a literal, and the brace form
// is only recognised under u. Regexp flags and patterns are early errors, so eval is
// the only way to catch one.
try {
    eval("/\\u{}/u");
    print("no error");
} catch (e) {
    print(e instanceof SyntaxError);
}
try {
    eval("/\\u{110000}/u");
    print("no error");
} catch (e) {
    print(e instanceof SyntaxError);
}
try {
    eval("/\\u{1d306}/");
    print("no error");
} catch (e) {
    print(e instanceof SyntaxError);
}
try {
    eval("/a/uu");
    print("no error");
} catch (e) {
    print(e instanceof SyntaxError, e.message.indexOf("Repeated RegExp flag: u") >= 0);
}
