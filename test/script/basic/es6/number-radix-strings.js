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
 * ES2015 7.1.3.1: a numeric string may carry a 0o or 0b prefix, the way 0x
 * already could.
 *
 * The two new prefixes change how an existing conversion reads its input, so
 * they are gated the same way the 0o and 0b literals themselves are.
 *
 * The sign is a separate matter and is not gated: none of the three prefixed
 * forms may carry one, in ES5.1 as much as in ES2015, because only
 * StrDecimalLiteral admits a sign. Number("-0x10") was reading as -16 here.
 *
 * @test
 * @run
 * @option --language=es6
 */

function show(label, value) {
    print(label + ": " + value);
}

show("0o1", Number("0o1"));
show("0o777", Number("0o777"));
show("0O17", Number("0O17"));
show("0b1", Number("0b1"));
show("0b1011", Number("0b1011"));
show("0B11", Number("0B11"));
show("0x10", Number("0x10"));
show("0X1f", Number("0X1f"));

// A digit the radix does not have, and a prefix with nothing after it.
show("0o8", Number("0o8"));
show("0b2", Number("0b2"));
show("0o", Number("0o"));
show("0b", Number("0b"));

// Surrounding whitespace is trimmed first, as it is for every numeric string.
show("padded", Number("  0o17  "));

// A sign is not part of any of the three grammars.
show("-0x10", Number("-0x10"));
show("+0x10", Number("+0x10"));
show("-0o1", Number("-0o1"));
show("+0b1", Number("+0b1"));

// Every other conversion that reads a string reads it the same way.
show("unary plus", +"0o10");
show("multiplication", "0b11" * 1);
show("comparison", "0o10" == 8);

// The decimal forms are untouched.
show("decimal", [Number("10"), Number("-10"), Number("+1.5e2"), Number("0.5"),
        Number("-Infinity"), Number("")].join(","));

// parseInt has its own grammar and keeps it.
show("parseInt", parseInt("0x10") + "," + parseInt("-0x10") + "," + parseInt("0o17"));

// The literals the prefixes were added for.
show("literals", [0o17, 0b101, 0x1f].join(","));
