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
 * ES6 method definitions in object literals.
 *
 * @test
 * @run
 * @option --language=es6
 */

var o = {
    v: 10,
    add(a, b) {
        return this.v + a + b;
    },
    name() {
        return "o";
    },
    withDefault(a = 5) {
        return a;
    },
    withRest(...r) {
        return r.length;
    },
    withPattern({ p }) {
        return p;
    },
    callsAnother() {
        return this.name() + "!";
    }
};

print(o.add(1, 2));
print(o.name());
print(o.withDefault());
print(o.withRest(1, 2, 3));
print(o.withPattern({ p: 7 }));
print(o.callsAnother());
print(typeof o.add);
print(o.add.length);

// get and set are ordinary method names when a parameter list follows.
var g = {
    get() { return "method"; },
    set(v) { return v; }
};
print(g.get());
print(g.set(1));

// An accessor is still an accessor.
var a = {
    _v: 1,
    get v() { return this._v; },
    set v(x) { this._v = x * 2; }
};
print(a.v);
a.v = 5;
print(a.v);

// Methods sit beside ordinary properties, shorthand and computed keys.
var x = 1;
var k = "computed";
var mixed = {
    x,
    plain: 2,
    [k]: 3,
    m() { return this.x + this.plain + this[k]; }
};
print(mixed.m());

// Methods nest.
var outer = {
    inner: {
        deep() { return "deep"; }
    }
};
print(outer.inner.deep());
