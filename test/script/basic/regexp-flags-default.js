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
 * RegExp.prototype.flags at the default language level.
 *
 * The ES6 flags are gated on --language=es6, so at this level they can never be parsed
 * and can never show up in the string. Nothing about es5 changed when they were added.
 *
 * @test
 * @run
 */

print("no flags = '" + /x/.flags + "'");
print("gim      = '" + /x/gim.flags + "'");
print("mig      = '" + /x/mig.flags + "'");
print("toString = '" + String(/x/gim) + "'");
print("sticky   = " + /x/gim.sticky);

// Regexp flags are an early error, so eval is the only way to catch one.
["y", "u"].forEach(function (flag) {
    try {
        eval("/x/" + flag);
        print(flag + " accepted");
    } catch (e) {
        print(flag + " rejected = " + (e instanceof SyntaxError));
    }
    try {
        new RegExp("x", flag);
        print("new RegExp x/" + flag + " accepted");
    } catch (e) {
        print("new RegExp x/" + flag + " rejected = " + (e instanceof SyntaxError));
    }
});
