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
 * A regular expression literal in the positions the ES6 lookahead passes over.
 *
 * A "/" is ambiguous until the grammar resolves it, so the lexer stops right
 * after one and lets the parser rescan it. The arrow and destructuring-assignment
 * lookaheads run before that happens, so each of these positions is one the
 * lookahead has to leave untouched.
 *
 * @test
 * @run
 * @option --language=es6
 */

// A parenthesised expression - where the arrow lookahead starts.
print((/a+/).source);
print((/ab+c/.test("abbc")));

// An array literal and an object literal - where the destructuring-assignment
// lookahead starts.
print([/b+/][0].source);
print({ re: /c+/ }.re.source);
print([1, /d+/][1].source);

// Not the first token in the brackets, so the lookahead has already scanned past
// something else by the time it reaches the slash.
print({ a: 1, re: /e+/ }.re.source);

// Inside a conditional, which the arrow lookahead scans through.
var cond = true;
print((cond ? /f+/ : 1).source);

// Nested one function deeper, so the enclosing function is re-parsed on demand.
print([1].map(function () { return (/g+/).source; })[0]);

// A default value in arrow parameters. Division there has always worked; a regular
// expression is the same position and now works too.
var half = (a = 1 / 2) => a;
print(half());

var re = (a = /h+/) => a;
print(re().source);

var sum = (a, b = 4 / 2) => a + b;
print(sum(1));

// Ordinary division is still division, not the start of a literal.
print(10 / 2 / 1);

// A body the lexer cannot tokenise as ordinary JavaScript. The lookahead walks token
// types, so it reaches the body before the grammar has resolved the "/" - and a quote,
// a template tick or a number prefix makes that speculative scan fail. The failure
// belongs to the probe, not to the program, so it must not surface.
print((function () { return /"/.source; })());
print((function () { return /'/.source; })());
print((function () { return /`/.source; })());
print((function () { return /0x/.source; })());
print((function () { return /\u1/.source; })());

// The same through the destructuring-assignment lookahead, which starts at "{".
var quoting = { escape: function (s) { return s.replace(/"/g, "&quot;"); } };
print(quoting.escape('a"b'));

// And through the for-of lookahead, which starts at "(".
for (var m of ['x"y'.match(/"/)]) {
    print(m[0]);
}
