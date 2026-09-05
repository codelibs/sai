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
 * ES2015 9.2.12: a function whose parameter list is anything other than a plain
 * list of names gets an unmapped arguments object.
 *
 * A mapped arguments object keeps each element and its parameter in step, so
 * assigning to one is seen through the other. That mapping is what a default, a
 * pattern or a rest parameter takes away, because with them there is no longer a
 * one to one correspondence to keep.
 *
 * Every parameter form involved needs --language=es6 to parse at all, so the
 * change is only reachable there.
 *
 * @test
 * @run
 * @option --language=es6
 */

function show(label, value) {
    print(label + ": " + value);
}

// A plain parameter list still maps, in both directions.
show("plain, parameter to arguments", (function (a) {
    a = "written";
    return arguments[0];
}("passed")));
show("plain, arguments to parameter", (function (a) {
    arguments[0] = "written";
    return a;
}("passed")));

// A default takes the mapping away, again in both directions.
show("default, parameter to arguments", (function (a = 1) {
    a = "written";
    return arguments[0];
}("passed")));
show("default, arguments to parameter", (function (a = 1) {
    arguments[0] = "written";
    return a;
}("passed")));

// So does a rest parameter and so does a pattern.
show("rest, parameter to arguments", (function (a, ...rest) {
    a = "written";
    return arguments[0];
}("passed", "more")));
show("pattern, parameter to arguments", (function ([a]) {
    a = "written";
    return arguments[0][0];
}(["passed"])));

// arguments still reports what was actually passed, not the arity, and the
// parameters still get their defaults.
show("length is the count passed", (function (a = 1, b = 2) {
    return arguments.length;
}("only")));
show("defaults still apply", (function (a, b = 2) {
    return a + "," + b;
}(1)));
show("rest still collects", (function (a, ...rest) {
    return rest.join(",");
}("a", "b", "c")));
show("arguments is still array-like", (function (a = 1) {
    return Array.prototype.join.call(arguments, ",");
}("p", "q")));

// Strict functions were already unmapped and are unchanged.
show("strict", (function () {
    "use strict";
    return (function (a) {
        a = "written";
        return arguments[0];
    }("passed"));
}()));

// The whole kangax subtest this is for.
show("assigning to a defaulted parameter", (function (a = "baz", b = "qux", c = "quux") {
    a = "corge";
    return arguments.length === 2 && arguments[0] === "foo" && arguments[1] === "bar";
}("foo", "bar")));
