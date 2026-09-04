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
 * A code point escape naming an identifier.
 *
 * \u{...} was already understood inside a string. An identifier is the other
 * place the grammar allows an escape, and it is the place where the code point
 * can be above the basic plane, so the name is two chars long while the escape
 * that wrote it is one code point.
 *
 * @test
 * @run
 * @option --language=es6
 */

// A declaration, and a read of the same name written the same way.
var \u{102C0} = 2;
print(\u{102C0});

\u{102C0} = 3;
print(\u{102C0});

// The four-digit form is unchanged.
var \u0061bc = "latin";
print(abc);

// A property key defined by escape, read as the string the escape names. U+102C0
// is above the basic plane, so that string is the surrogate pair for it.
var byEscape = { \u{102C0}: "defined by escape" };
print(byEscape['\ud800\udec0']);

// And the other way round: defined as that string, read by escape.
var byString = { '\ud800\udec0': "defined by string" };
print(byString.\u{102C0});

// The name is the code point, so it is two UTF-16 units long.
print(Object.keys(byString)[0].length);
print(Object.keys(byEscape)[0] === '\ud800\udec0');

// An escape need not start the name.
var a\u{102C0}b = "middle";
print(a\u{102C0}b);

// A code point that is not an identifier character is still rejected.
try {
    eval("var \\u{20} = 1;");
    print("no error");
} catch (e) {
    print(e instanceof SyntaxError);
}
