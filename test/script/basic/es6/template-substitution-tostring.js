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
 * A template substitution goes through ToString, which asks an object for its
 * toString, rather than through the conversion "+" does, which asks for valueOf.
 *
 * @test
 * @run
 * @option --language=es6
 */

var both = {
    toString: function () { return "toString"; },
    valueOf: function () { return "valueOf"; }
};

print(`${both}`);
print(String(both));
print("" + both);

// An object with no usable toString still falls back to valueOf, as ToString
// does.
var onlyValueOf = Object.create(null);
onlyValueOf.valueOf = function () { return "valueOf only"; };
print(`${onlyValueOf}`);

// Every other conversion is what it was.
print(`${1}${2}`, `${1 + 2}`);
print(`${null}|${undefined}|${true}|${0.5}|${-0}`);
print(`${[1, 2]}|${({})}`);
print(`a${"b"}c`);
print(`${new java.lang.StringBuilder("java")}`);

// A local named String does not break a template, because nothing looks the
// name up. eval, so that the template is compiled with the local in scope.
(function () {
    var String = 1;
    eval("print(`${both}`);");
})();

// Nested templates convert at each level.
print(`outer ${`inner ${both}`} end`);

// A throwing conversion propagates.
try {
    print(`${ { toString: function () { throw new Error("from toString"); } } }`);
} catch (e) {
    print(e.message);
}

// A tagged template hands the values over unconverted; only the tag decides.
function tag(strings) {
    return strings.raw.join("|") + "/" + typeof arguments[1];
}
print(tag`a${both}b`);

// The substitution of a lazily compiled function converts the same way, which
// is the path a re-parse of the body goes through.
function lazy() {
    return `${both}`;
}
print(lazy(), lazy());
