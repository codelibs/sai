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
 * ES6 code point escapes in string and template literals.
 *
 * @test
 * @run
 * @option --language=es6
 */

print("\u{41}");
print('\u{42}');
print(`\u{43}`);

// A code point above the basic plane becomes a surrogate pair.
print("\u{1F600}".length);
print("\u{1F600}" === "\uD83D\uDE00");
print("\u{1f600}".codePointAt(0));

// Leading zeroes and the shortest form.
print("\u{0041}" + "\u{00041}" + "\u{4}".charCodeAt(0));

// The four digit form still works, alongside the new one.
print("\u0041\u{42}");

// Escapes sit among ordinary characters.
print("a\u{42}c");
print(`x\u{44}${1 + 1}z`);
