/*
 * Copyright (c) 2010, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * Test to check that sai "internal" classes in codegen, parser, ir
 * packages cannot * be accessed from sandbox scripts.
 *
 * @test
 * @run
 * @security
 */

function checkClass(name) {
    try {
        Java.type(name);
        fail("should have thrown exception for: " + name);
    } catch (e) {
        if (! (e instanceof java.lang.SecurityException)) {
            fail("Expected SecurityException, but got " + e);
        }
    }
}

// Not exhaustive - but a representative list of classes
checkClass("org.codelibs.sai.internal.codegen.Compiler");
checkClass("org.codelibs.sai.internal.codegen.types.Type");
checkClass("org.codelibs.sai.internal.ir.Node");
checkClass("org.codelibs.sai.internal.ir.FunctionNode");
checkClass("org.codelibs.sai.internal.ir.debug.JSONWriter");
checkClass("org.codelibs.sai.internal.ir.visitor.NodeVisitor");
checkClass("org.codelibs.sai.internal.lookup.MethodHandleFactory");
checkClass("org.codelibs.sai.internal.objects.Global");
checkClass("org.codelibs.sai.internal.parser.AbstractParser");
checkClass("org.codelibs.sai.internal.parser.Parser");
checkClass("org.codelibs.sai.internal.parser.JSONParser");
checkClass("org.codelibs.sai.internal.parser.Lexer");
checkClass("org.codelibs.sai.internal.parser.Scanner");
checkClass("org.codelibs.sai.internal.runtime.Context");
checkClass("org.codelibs.sai.internal.runtime.arrays.ArrayData");
checkClass("org.codelibs.sai.internal.runtime.linker.Bootstrap");
checkClass("org.codelibs.sai.internal.runtime.options.Option");
checkClass("org.codelibs.sai.internal.runtime.regexp.RegExp");
checkClass("org.codelibs.sai.internal.scripts.JO");
checkClass("org.codelibs.sai.tools.Shell");
checkClass("jdk.internal.dynalink.CallSiteDescriptor");
checkClass("jdk.internal.dynalink.beans.StaticClass");
checkClass("jdk.internal.dynalink.linker.LinkRequest");
checkClass("jdk.internal.dynalink.support.AbstractRelinkableCallSite");
