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
 * ES5.1 reading of Array.prototype.splice with a single argument: the absent
 * deleteCount is undefined, so nothing is deleted. ES6 deletes through to the end
 * instead, and that change is gated behind --language=es6 - see
 * test/script/basic/es6/array-iterator-methods.js.
 *
 * @test
 * @run
 */

var one_arg = [1, 2, 3, 4];
print(JSON.stringify(one_arg.splice(1)) + " " + JSON.stringify(one_arg));

var negative = [1, 2, 3, 4];
print(JSON.stringify(negative.splice(-2)) + " " + JSON.stringify(negative));

var whole = [1, 2, 3, 4];
print(JSON.stringify(whole.splice(0)) + " " + JSON.stringify(whole));

var no_args = [1, 2, 3, 4];
print(JSON.stringify(no_args.splice()) + " " + JSON.stringify(no_args));

var two_args = [1, 2, 3, 4];
print(JSON.stringify(two_args.splice(1, 2)) + " " + JSON.stringify(two_args));

var like = { 0: "a", 1: "b", 2: "c", length: 3 };
print(JSON.stringify(Array.prototype.splice.call(like, 1)) + " " + like.length + "," + like[0]);
