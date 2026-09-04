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
 * ES6 tagged template literals, and String.raw.
 *
 * @test
 * @run
 * @option --language=es6
 */

function id(s) {
    return s;
}

// The literal parts arrive as data, cooked and raw, with the substitutions
// after them.
function tag(s, a, b) {
    print(s instanceof Array, s.length, s[0], JSON.stringify(s[1]));
    print(JSON.stringify(s.raw[1]));
    print(a, b);
    return "tagged";
}
print(tag`foo${123}bar\n${456}`);

// The strings object and its raw are frozen, and raw is not enumerable.
var strings = id`a${1}b`;
print(Object.isFrozen(strings), Object.isFrozen(strings.raw));
print(Object.keys(strings).join(","), JSON.stringify(strings));
var raw = Object.getOwnPropertyDescriptor(strings, "raw");
print(raw.enumerable, raw.writable, raw.configurable);

// A call site hands over the same object every time it runs, and keeps doing so
// for the life of the Global, so a tag can key off it.
function site() {
    return id`site`;
}
var first = site();
print(first === site(), first === new site(), first !== id`site`);

// The identity survives a recompilation of the function around it.
function deoptimised(n) {
    var sum = n + 1;
    return { strings: id`cached`, sum: sum };
}
var early = deoptimised(1);
var late = deoptimised("x");
print(early.strings === late.strings, early.sum, late.sum);

// An escape that is invalid anywhere else is allowed here: the tag is called,
// and only the cooked value of the part it is in goes missing.
var bad = id`\1\xz\uz\u{110000}\u{z}`;
print(bad.length, bad[0] === undefined, bad.raw[0]);
// The lexer has to be back where it was afterwards, or nothing after this parses.
print(id`\uz${1}ok`.raw[1]);

// A method keeps its receiver.
var receiver = {
    name: "receiver",
    tag: function (s) { return this.name + s[0]; }
};
print(receiver.tag`!`);

// A function inside a substitution, read more than once so that the lazily
// compiled body goes through a re-parse.
function substituted() {
    return id`x${(function () { return 1; })()}y`;
}
print(substituted().length, substituted().length);

// A template binds to the member expression before it, so a tag can be the
// result of a call or a property.
function outer() {
    return id;
}
print(outer()`chained`[0], ({ t: id }).t`held`[0]);

// String.raw puts the parts back together as they were written.
print(String.raw`a\nb${1}c`);
print(String.raw({ raw: ["a", "b"] }, 1));
print(String.raw({ raw: ["only"] }));
print(typeof String.raw, String.raw.length);

// A template reports a line ending as a line feed, however it was written, both
// cooked and raw. eval is what gets a carriage return into the source here.
var cr = eval("`x" + String.fromCharCode(13) + "y`");
var crlf = eval("`x" + String.fromCharCode(13, 10) + "y`");
print(cr.length, cr.charCodeAt(1), crlf.length, crlf.charCodeAt(1));
var rawCr = eval("(function (s) { return s.raw[0]; })`x" + String.fromCharCode(13) + "y`");
print(rawCr.length, rawCr.charCodeAt(1));

// Two evals are two call sites even though their sources are both called
// <eval>, so the key cannot be the source name.
var seen = [];
for (var i = 0; i < 2; i++) {
    seen.push(eval("id`" + i + "`")[0]);
}
print(seen.join(","));

// Two Globals do not share a strings object.
var script = "function t(s) { return s; } t`shared`;";
var a = loadWithNewGlobal({ name: "a", script: script });
var b = loadWithNewGlobal({ name: "b", script: script });
print(a[0], b[0], a === b);

// An untagged template is unaffected.
print(`plain`, `a${1}b`);
