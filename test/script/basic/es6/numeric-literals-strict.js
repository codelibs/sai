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
 * ES6 binary and octal literals are legal in strict mode. The legacy
 * leading-zero octal form is not, and stays rejected.
 *
 * @test
 * @run
 * @option --language=es6
 */

"use strict";

print(0o17);
print(0b101);

try {
    eval("017");
    print("legacy octal wrongly accepted in strict mode");
} catch (e) {
    print(e instanceof SyntaxError);
}
