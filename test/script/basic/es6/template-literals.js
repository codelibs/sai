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
 * ES6 template literals without a tag.
 *
 * @test
 * @run
 * @option --language=es6
 */

var name = "world";
var n = 5;

print(`hello`);
print(`hello ${name}`);
print(`${name}!`);
print(`${1 + 2}`);
print(`a${1}b${2}c`);
print(`${1}${2}`);
print(`${n} + ${n} = ${n + n}`);
print(`nested ${`inner ${n}`} done`);
print(typeof `x`);
print(`escaped \` backtick`);
print(`dollar \${notsub}`);
print(`a\nb`.length);
print(`line1
line2`);
print(`${undefined}`);
print(`${[1, 2]}`);
print(`${"}"}`);
print(`${"{"}`);
print(`${'}'}`);
print(`${`}`}`);
print(`` === "");

// A function whose last token is a template. It is compiled on first call, which
// re-parses it from its own source range, so that range has to cover the closing
// backquote.
var tail = function () `plain`;
print(tail());
var tailSub = function () `sum ${1 + 1}`;
print(tailSub());

// A substitution is lexed like any other expression, so a slash in it is division
// or a regular expression exactly as it would be outside the template.
var a = 6, b = 3;
print(`${a / b}`);
print(`${1 / 2}`);
print(`${a / b} tail`);
print(`${1} ${a / b}`);
print(`${/c+/.source}`);
print(`${"aa".replace(/a/g, "b")}`);

// A brace inside a comment, a string or a regular expression does not end the
// substitution.
print(`x${ 1 // }
}y`);
print(`x${ /* } */ 1 }y`);
print(`${ /}/.source }`);
print(`${ "}" }`);

// An escaped line terminator still advances the line count, so a later error
// points at the right line.
var escaped = `a\
b`;
print(escaped.length);
