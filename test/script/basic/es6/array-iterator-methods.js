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
 * ES6 Array.prototype.keys / values / entries, and the ES6 reading of a
 * one-argument splice.
 *
 * The three iterator methods hand back an object with a next method. This engine
 * has no iterator protocol, so that object does not drive for-of, spread or
 * destructuring; the test below pins that limitation down as well.
 *
 * @test
 * @run
 * @option --language=es6
 */

function step(result) {
    return result.done ? "done" : JSON.stringify(result.value);
}

print([[].keys.length, [].values.length, [].entries.length].join(","));
print([typeof [].keys, typeof [].values, typeof [].entries].join(","));

var array = ["a", "b"];

var keys = array.keys();
print(typeof keys.next);
print([step(keys.next()), step(keys.next()), step(keys.next()), step(keys.next())].join(" "));

var values = array.values();
print([step(values.next()), step(values.next()), step(values.next())].join(" "));

var entries = array.entries();
print([step(entries.next()), step(entries.next()), step(entries.next())].join(" "));

// the result object really carries value and done as own properties
var first = ["x"].entries().next();
print(first.hasOwnProperty("value") + "," + first.hasOwnProperty("done") + "," + first.done);
var last = [].values().next();
print(last.hasOwnProperty("value") + "," + (last.value === undefined) + "," + last.done);

// two iterators over the same array walk independently
var shared = [1, 2];
var one = shared.values();
var two = shared.values();
print(step(one.next()) + " " + step(two.next()) + " " + step(one.next()));

// an iterator sees elements appended before it is exhausted
var growing = [1];
var walk = growing.values();
print(step(walk.next()));
growing.push(2);
print(step(walk.next()));

// but an exhausted iterator stays exhausted
var exhausted = [1];
var spent = exhausted.values();
spent.next();
print(step(spent.next()));
exhausted.push(2);
print(step(spent.next()));

// holes are visited as undefined
var holey = [, 1];
var holes = holey.entries();
print(step(holes.next()) + " " + step(holes.next()));

// array-likes work too
print(step(Array.prototype.entries.call({ 0: "z", length: 1 }).next()));

// LIMITATION: the returned object is not iterable, so for-of does not read it.
// for-of is desugared to an index loop in the parser and the iterator object has
// no length, so the loop body never runs.
var reached = 0;
for (var ignored of [1, 2, 3].values()) {
    reached++;
}
print("for-of reached " + reached);

// null or undefined this is a TypeError
["keys", "values", "entries"].forEach(function (name) {
    [null, undefined].forEach(function (bad) {
        try {
            Array.prototype[name].call(bad);
            print("no TypeError from " + name);
        } catch (e) {
            print(name + " " + (e instanceof TypeError));
        }
    });
});

// ES6 splice with only a start index deletes through to the end
var one_arg = [1, 2, 3, 4];
print(JSON.stringify(one_arg.splice(1)) + " " + JSON.stringify(one_arg));

var negative = [1, 2, 3, 4];
print(JSON.stringify(negative.splice(-2)) + " " + JSON.stringify(negative));

var past_end = [1, 2, 3, 4];
print(JSON.stringify(past_end.splice(10)) + " " + JSON.stringify(past_end));

var whole = [1, 2, 3, 4];
print(JSON.stringify(whole.splice(0)) + " " + JSON.stringify(whole));

// no arguments at all still deletes nothing, in ES6 as in ES5
var no_args = [1, 2, 3, 4];
print(JSON.stringify(no_args.splice()) + " " + JSON.stringify(no_args));

// and an explicit undefined deleteCount deletes nothing under both editions
var explicit = [1, 2, 3, 4];
print(JSON.stringify(explicit.splice(1, undefined)) + " " + JSON.stringify(explicit));

// two arguments are unaffected
var two_args = [1, 2, 3, 4];
print(JSON.stringify(two_args.splice(1, 2)) + " " + JSON.stringify(two_args));

// as is a splice that only inserts
var insert = [1, 4];
print(JSON.stringify(insert.splice(1, 0, 2, 3)) + " " + JSON.stringify(insert));

// the one-argument form on an array-like
var like = { 0: "a", 1: "b", 2: "c", length: 3 };
print(JSON.stringify(Array.prototype.splice.call(like, 1)) + " " + like.length + "," + like[0]);
