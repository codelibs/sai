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
 * ES2015 11.6: identifiers are read by code point, and an escape in one is
 * resolved before the reserved word check.
 *
 * Neither half is gated. A letter above the basic plane was already a letter in
 * ES5.1's own categories, and ES5.1 7.6 already said an identifier may not be a
 * reserved word however it was written, so both are the older reading too.
 *
 * @test
 * @run
 */

function show(label, value) {
    print(label + ": " + value);
}

function evaluate(source) {
    try {
        return eval(source);
    } catch (e) {
        return e.name;
    }
}

// A character above the basic plane arrives as a surrogate pair, and neither
// half is a letter on its own, which is why these used to be rejected.
show("astral name", evaluate("var 𐋀 = 5; 𐋀"));
show("astral inside a name", evaluate("var a𐋀b = 7; a𐋀b"));
show("astral property", evaluate("var o = { 𐋀: 1 }; o.𐋀"));
show("astral declaration", evaluate("var 𐌀; 'declared'"));

// A lone surrogate is still not a letter.
show("lone high surrogate", evaluate("var \\uD800; 1"));

// An escape may still write an ordinary letter.
show("escaped letter", evaluate("var \\u0061 = 3; a"));
show("escaped letters in a longer name", evaluate("var v\\u0061riable = 9; variable"));
show("a name that merely starts like a keyword", evaluate("var v\\u0061rs = 4; vars"));

// But it may not write a keyword, in any position where a keyword would be one.
show("escaped var as a name", evaluate("var v\\u0061r"));
show("fully escaped var as a name", evaluate("var \\u0076\\u0061\\u0072"));
show("escaped if as a name", evaluate("var \\u0069f"));
show("escaped true as a name", evaluate("var tru\\u0065"));
show("unescaped keyword as a name", evaluate("var var"));

// A property name may be any IdentifierName, so a keyword is fine there whether
// or not it was written with an escape.
show("keyword as a property", evaluate("var o = { var: 1 }; o.var"));
show("escaped keyword as a property", evaluate("var o = { v\\u0061r: 1 }; o.var"));
show("escaped keyword after a dot", evaluate("var o = { 'if': 2 }; o.\\u0069f"));

// Ordinary names are unaffected.
show("ordinary", evaluate("var $x_1 = 2; $x_1"));
show("basic plane letter", evaluate("var あ = 6; あ"));
