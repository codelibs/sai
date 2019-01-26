/*
 * Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.
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
 * JDK-8055107: Extension directives to turn on callsite profiling, tracing, AST print and other debug features locally
 *
 * @test
 * @option -Dsai.debug=true
 * @option -scripting
 * @run
 * @fork
 */

function runScriptEngine(code) {
    var imports = new JavaImporter(
        java.io, java.lang, java.util, javax.script);

    with(imports) {
        var m = new ScriptEngineManager();
        // get current System.err
        var oldErr = System.err;
        var baos = new ByteArrayOutputStream();
        var newErr = new PrintStream(baos);
        try {
            // set new standard err
            System.setErr(newErr);
            var engine = m.getEngineByName("sai");
            engine.eval(code);
            newErr.flush();
            return new java.lang.String(baos.toByteArray());
        } finally {
            // restore System.err to old value
            System.setErr(oldErr);
        }
    }
}

// sai callsite trace enterexit
var str = runScriptEngine(<<CODE
function func() {
   "sai callsite trace enterexit";
   k();
}

function k() {
    var x = "hello";
}

func();
CODE);

if (!str.contains(" ENTER ")) {
    fail("expected 'ENTER' in trace mode output");
}

if (!str.contains(" EXIT ")) {
    fail("expected 'EXIT' in trace mode output");
}

// sai callsite trace objects
var str = runScriptEngine(<<CODE
"sai callsite trace objects";
function func(x) {
}

func("hello");
CODE);

if (!str.contains(" ENTER ")) {
    fail("expected 'ENTER' in trace mode output");
}

if (!str.contains(" EXIT ")) {
    fail("expected 'EXIT' in trace mode output");
}

if (!str.contains("hello")) {
    fail("expected argument to be traced in trace objects mode");
}

// sai callsite trace misses
str = runScriptEngine(<<CODE
function f() {
   "sai callsite trace misses";
   k();
}

function k() {}
f();
CODE);

if (!str.contains(" MISS ")) {
    fail("expected callsite MISS trace messages");
}

// sai print lower ast
str = runScriptEngine(<<CODE
function foo() {
    "sai print lower ast";
    var x = 'hello';
}
foo();
CODE);

if (!str.contains("Lower AST for: 'foo'") ||
    !str.contains("sai print lower ast")) {
    fail("expected Lower AST to be printed for 'foo'");
}

// sai print ast
str = runScriptEngine(<<CODE
function foo() {
  "sai print ast";
}
CODE);
if (!str.contains("[function ") ||
    !str.contains("sai print ast")) {
    fail("expected AST to be printed");
}

// sai print symbols
str = runScriptEngine(<<CODE
function bar(a) {
    "sai print symbols";
    if (a) print(a);
}

bar();
CODE)

if (!str.contains("[BLOCK in 'Function bar']")) {
    fail("expected symbols to be printed for 'bar'");
}

// sai print parse
str = runScriptEngine(<<CODE
"sai print parse";

function func() {}
CODE);

if (!str.contains("function func") ||
    !str.contains("sai print parse")) {
    fail("expected sai print parse output");
}

// sai print lower parse
str = runScriptEngine(<<CODE
"sai print lower parse";

function func() {}

func()
CODE);

if (!str.contains("function {U%}func") ||
    !str.contains("sai print lower parse")) {
    fail("expected sai print lower parse output");
}
