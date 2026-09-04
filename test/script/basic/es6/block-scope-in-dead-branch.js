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
 * A block-scoped declaration in a branch that constant folding drops.
 *
 * Folding a decidable "if" splices the surviving branch into the enclosing
 * block, and lifts the declarations out of the dropped one so that hoisted var
 * names still exist. A let or a const does not hoist, so lifting it puts a
 * second declaration of the same name in the same block - and the two branches
 * of an if very often use the same name.
 *
 * @test
 * @run
 * @option --language=es6
 */

// The same name declared in both branches, with the test decidable either way.
if (true) { let q = 1; print(q); } else { let q = 2; print(q); }
if (false) { let q = 3; print(q); } else { let q = 4; print(q); }
if (1) { const c = 5; print(c); } else { const c = 6; print(c); }
if ("x") { let s = 7; print(s); } else { let s = 8; print(s); }

// An else-if chain, where the dropped branch is itself an if.
if (true) { let q = 9; print(q); } else if (false) { let q = 10; print(q); }

// Nested one level down.
if (true) { { let n = 11; print(n); } } else { { let n = 12; print(n); } }

// A class is block scoped too.
if (true) { class K { m() { return 13; } } print(new K().m()); }
else { class K { m() { return 14; } } print(new K().m()); }

// var still hoists out of the dropped branch, initializer dropped, so the name
// exists and is undefined rather than a ReferenceError.
if (false) { var hoisted = 15; }
print(typeof hoisted);

if (true) { var alsoHoisted = 16; } else { var alsoHoisted = 17; }
print(alsoHoisted);

// Unreachable code after a return is pruned by the same helper.
function afterReturn() {
    return 18;
    let unreachable = 19;
}
print(afterReturn());

function afterReturnVar() {
    return typeof later;
    var later = 20;
}
print(afterReturnVar());
