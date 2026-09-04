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
 * The Function constructor takes the same parameter list the grammar does.
 *
 * Function builds its result by evaluating a function expression, which has
 * always understood ES6 parameters. Only the separate check of the parameter
 * string did not, so these forms were rejected before the function was ever
 * built.
 *
 * @test
 * @run
 * @option --language=es6
 */

// Default values.
print(new Function("a = 1, b = 2", "return a + ':' + b;")());
print(new Function("a, b = a * 2", "return a + ':' + b;")(3));
print(new Function("a = 1", "return a;")(9));

// Patterns.
print(new Function("{a, x: b}", "return a + ':' + b;")({ a: 1, x: 2 }));
print(new Function("[a, b]", "return a + ':' + b;")([3, 4]));
print(new Function("{a, x: b}, [c, d]", "return [a, b, c, d].join(':');")({ a: 1, x: 2 }, [3, 4]));

// Patterns with defaults.
print(new Function("{a = 1, x: b = 2}", "return a + ':' + b;")({}));
print(new Function("[a = 5, b = 6]", "return a + ':' + b;")([]));

// Rest.
print(new Function("...r", "return r.length + ':' + r.join(',');")(1, 2, 3));
print(new Function("a, ...r", "return a + ':' + r.join(',');")(1, 2, 3));

// Still a syntax error when it really is one.
try {
    new Function("a b", "return a;");
    print("no error");
} catch (e) {
    print(e instanceof SyntaxError);
}

try {
    new Function("...r, a", "return a;");
    print("no error");
} catch (e) {
    print(e instanceof SyntaxError);
}
