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
 * ES2015 RegExp.prototype.flags (21.2.5.3).
 *
 * @test
 * @run
 * @option --language=es6
 */

print("no flags          = '" + /x/.flags + "'");
print("g                 = '" + /x/g.flags + "'");
print("i                 = '" + /x/i.flags + "'");
print("m                 = '" + /x/m.flags + "'");
print("gim               = '" + /x/gim.flags + "'");

// the order is fixed by the spec, not by the order the flags were written in
print("igm -> gim        = '" + /x/igm.flags + "'");
print("mig -> gim        = '" + /x/mig.flags + "'");
print("new RegExp('x','mg') = '" + new RegExp("x", "mg").flags + "'");

// like source, global, ignoreCase and multiline, it lives on the regexp itself
// rather than on RegExp.prototype - sai keeps the ES5 placement for all of them
print("own on instance   = " + /x/g.hasOwnProperty("flags"));

// ES6 makes flags configurable, unlike its neighbours
var desc = Object.getOwnPropertyDescriptor(/x/gi, "flags");
print("value             = '" + desc.value + "'");
print("writable          = " + desc.writable);
print("enumerable        = " + desc.enumerable);
print("configurable      = " + desc.configurable);

// reading it off RegExp.prototype falls back to the default regexp, the same
// way source and global already do
print("proto.flags       = '" + RegExp.prototype.flags + "'");

// there is no setter, so a sloppy assignment is silently ignored
RegExp.prototype.flags = "zzz";
var re = /x/gi;
re.flags = "zzz";
print("after assignment  = '" + re.flags + "' '" + RegExp.prototype.flags + "'");

// and throws in strict mode
try {
    (function() {
        "use strict";
        /x/g.flags = "zzz";
    })();
    print("no error in strict mode");
} catch (e) {
    print("strict assignment = " + (e instanceof TypeError));
}
