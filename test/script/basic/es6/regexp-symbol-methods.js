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
 * ES2015 21.2.5.6, .8, .9 and .11: the four operations String.prototype.match,
 * replace, search and split hand to a regular expression, now reachable on
 * RegExp.prototype under Symbol.match, Symbol.replace, Symbol.search and
 * Symbol.split -- and read off the argument first, so that any object can say
 * how it is to be matched, replaced, searched or split.
 *
 * 7.2.8 IsRegExp goes with them: Symbol.match now decides whether a value counts
 * as a regular expression, which is what startsWith, endsWith and includes ask
 * before refusing one, and what the RegExp constructor asks before handing a
 * pattern straight back.
 *
 * An object without these symbols behaves exactly as before, so this is an
 * addition and the test deliberately runs without --language=es6.
 *
 * @test
 * @run
 */

function show(label, value) {
    print(label + ": " + value);
}

// 21.2.5: RegExp.prototype carries all four.
show("RegExp.prototype", [typeof RegExp.prototype[Symbol.match], typeof RegExp.prototype[Symbol.replace],
                          typeof RegExp.prototype[Symbol.search], typeof RegExp.prototype[Symbol.split]].join(","));
show("they are not enumerable",
     Object.getOwnPropertyDescriptor(RegExp.prototype, Symbol.match).enumerable);
show("they are named", RegExp.prototype[Symbol.match].name);

// 21.1.3.11, .14, .15 and .17: the argument is asked first, whatever it is.
var told = {};
told[Symbol.match] = function (string) { return "matched " + string; };
told[Symbol.replace] = function (string, replacement) { return "replaced " + string + " with " + replacement; };
told[Symbol.search] = function (string) { return "searched " + string; };
told[Symbol.split] = function (string, limit) { return "split " + string + " at most " + limit; };

show("match", "abc".match(told));
show("replace", "abc".replace(told, "x"));
show("search", "abc".search(told));
show("split", "abc".split(told, 2));

// The receiver of the call is the argument, not the string.
var receiver = null;
var records = {};
records[Symbol.search] = function () { receiver = this; return 0; };
"abc".search(records);
show("called on the argument", receiver === records);

// 7.3.9 GetMethod: a symbol naming something that cannot be called is an error,
// while a value that carries no symbol at all says nothing.
var notCallable = {};
notCallable[Symbol.match] = 5;
try {
    "abc".match(notCallable);
    show("a non callable symbol", "no error");
} catch (e) {
    show("a non callable symbol", e instanceof TypeError);
}
show("null and undefined say nothing", "a,b".split(undefined).length + "," + ("abc".match(null) === null));

// The four go on working as they did.
show("match", "abc".match(/b/)[0]);
show("match global", "aba".match(/a/g).join(","));
show("match no result", "abc".match(/z/));
show("match a string", "abc".match("b")[0]);
show("replace a string", "abc".replace("b", "X"));
show("replace a regexp", "abcabc".replace(/b/g, "X"));
show("replace with a function", "abc".replace(/b/, function (m) { return m.toUpperCase(); }));
show("search", "abc".search(/c/) + "," + "abc".search(/z/));
show("split a string", "a,b,c".split(",").join("|"));
show("split a regexp", "a1b2c".split(/\d/).join("|"));
show("split with a limit", "a,b,c".split(",", 2).join("|"));
show("split on captures", "a1b".split(/(\d)/).join("|"));

// 7.2.8: Symbol.match decides, so a regular expression can opt out of being one.
var opted = /./;
try {
    "/./".startsWith(opted);
    show("a regexp is refused", "no error");
} catch (e) {
    show("a regexp is refused", e instanceof TypeError);
}
opted[Symbol.match] = false;
show("startsWith after opting out", "/./".startsWith(opted));
show("endsWith after opting out", "/./".endsWith(opted));
show("includes after opting out", "/./".includes(opted));

// And an ordinary object can opt in.
var pretender = { constructor: RegExp };
pretender[Symbol.match] = true;
try {
    "abc".startsWith(pretender);
    show("an object that opted in is refused", "no error");
} catch (e) {
    show("an object that opted in is refused", e instanceof TypeError);
}

// 21.2.3.1 step 3: called without new, a pattern that says it is a regular
// expression and names RegExp as its constructor is handed straight back.
show("RegExp of a pattern that opted in", RegExp(pretender) === pretender);
show("RegExp of one that opted out", RegExp(opted) === opted);
var ordinary = /a/;
show("RegExp of an ordinary regexp", RegExp(ordinary) === ordinary);

var copied = /ab/gi;
show("new RegExp copies", (new RegExp(copied) !== copied) + "," + new RegExp(copied).source + "," + new RegExp(copied).flags);

// 21.2.5.11 reads a species, so a subclass splits with its own kind.
var asked = 0;
var splitter = { constructor: {} };
splitter[Symbol.split] = RegExp.prototype[Symbol.split];
splitter.constructor[Symbol.species] = function () { asked++; return /,/; };
show("split through a species", "a,b".split(splitter).join("|") + "," + asked);
