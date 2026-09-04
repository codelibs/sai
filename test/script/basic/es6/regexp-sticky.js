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
 * ES6 RegExp "y" (sticky) flag.
 *
 * @test
 * @run
 * @option --language=es6
 */

// The flag is accepted both in a literal and in the constructor.
print(/a/y.sticky, new RegExp("a", "y").sticky, /a/.sticky);
print(String(/a/y), String(/a/gimy));

// exec matches at lastIndex and moves it on, exactly like a global regexp does.
var re = new RegExp('\\w', 'y');
re.exec('xy');
print(re.exec('xy')[0]);

// It never scans forward: a match that a plain regexp would find is missed.
var re2 = new RegExp('yy', 'y');
re2.lastIndex = 3;
print(re2.exec('xxxyyxx')[0], re2.lastIndex);
re2.lastIndex = 2;
print(re2.exec('xxxyyxx'), re2.lastIndex);

// A failed match resets lastIndex, so the next attempt starts over.
print(re2.exec('xxxyyxx'), re2.lastIndex);

// test() goes through the same path.
var re3 = /a/y;
print(re3.test('ba'), re3.lastIndex);
print(re3.test('ab'), re3.lastIndex);

// A lastIndex past the end of the input fails and resets.
var re4 = /a/y;
re4.lastIndex = 10;
print(re4.exec('a'), re4.lastIndex);

// String.prototype.match follows exec, for both the single and the global form.
print("xy".match(/y/y));
print("aab".match(/a/gy));
print("bab".match(/a/gy));

// String.prototype.replace does too.
print("xy".replace(/y/y, "z"));
print("yx".replace(/y/y, "z"));
print("aab".replace(/a/gy, "c"));
print("bab".replace(/a/gy, "c"));

// String.prototype.search runs exec from lastIndex 0.
print("xy".search(/y/y), "xy".search(/y/));

// split walks the string one position at a time, so the flag makes no difference.
print("a,b,c".split(/,/y).join("|"), "a,b,c".split(/,/).join("|"));
print("abc".split(/(?:)/y).join("|"));

// Anchors and lookaround still see the whole input, only the start is pinned.
print(/^y/y.test("xy"));
var re5 = /y$/y;
re5.lastIndex = 1;
print(re5.exec("xy")[0]);
var re6 = /y(?=z)/y;
re6.lastIndex = 1;
print(re6.exec("xyz")[0]);

// A repeated flag is still an error.
try {
    // Regexp flags are an early error, so eval is the only way to catch one.
    eval("/a/yy");
    print("no error");
} catch (e) {
    print(e instanceof SyntaxError, e.message.indexOf("Repeated RegExp flag: y") >= 0);
}
