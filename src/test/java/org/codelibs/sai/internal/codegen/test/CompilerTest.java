/*
 * Copyright (c) 2010, 2013, Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2026, CodeLibs Project and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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

package org.codelibs.sai.internal.codegen.test;

import static org.codelibs.sai.internal.runtime.Source.readFully;
import static org.codelibs.sai.internal.runtime.Source.sourceFor;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.codelibs.sai.internal.objects.Global;
import org.codelibs.sai.internal.runtime.Context;
import org.codelibs.sai.internal.runtime.ErrorManager;
import org.codelibs.sai.internal.runtime.ScriptFunction;
import org.codelibs.sai.internal.runtime.Source;
import org.codelibs.sai.internal.runtime.options.Options;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests to check Sai JS compiler - just compiler and not execution of scripts.
 */
@SuppressWarnings("javadoc")
public class CompilerTest {
    private static final boolean VERBOSE = Boolean.valueOf(System.getProperty("compilertest.verbose"));
    private static final boolean TEST262 = Boolean.valueOf(System.getProperty("compilertest.test262"));
    private static final String TEST_BASIC_DIR = System.getProperty("test.basic.dir");
    private static final String TEST_NODE_DIR = System.getProperty("test.node.dir");
    private static final String TEST262_SUITE_DIR = System.getProperty("test262.suite.dir");

    interface TestFilter {
        public boolean exclude(File file, String content);
    }

    private static void log(final String msg) {
        org.testng.Reporter.log(msg, true);
    }

    private Context context;
    private Global global;
    private Context es6Context;
    private Global es6Global;

    @BeforeClass
    public void setupTest() {
        this.context = newContext(null);
        this.global = context.createGlobal();
        this.es6Context = newContext("es6");
        this.es6Global = es6Context.createGlobal();
    }

    private static Context newContext(final String language) {
        final Options options = new Options("sai");
        options.set("anon.functions", true);
        options.set("compile.only", true);
        options.set("print.ast", true);
        options.set("print.parse", true);
        options.set("scripting", true);
        options.set("const.as.var", true);
        options.set("verify.code", true);
        if (language != null) {
            options.set("language", language);
        }

        final ErrorManager errors = new ErrorManager() {
            @Override
            public void error(final String msg) {
                log(msg);
            }
        };

        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);

        return new Context(options, errors, pw, pw, Thread.currentThread().getContextClassLoader());
    }

    @AfterClass
    public void tearDownTest() {
        this.context = null;
        this.global = null;
        this.es6Context = null;
        this.es6Global = null;
    }

    @Test
    public void compileAllTests() {
        if (TEST262) {
            compileTestSet(new File(TEST262_SUITE_DIR), new TestFilter() {
                @Override
                public boolean exclude(final File file, final String content) {
                    return content != null && content.contains("@negative");
                }
            }, context, global);
        }
        compileTestSet(new File(TEST_BASIC_DIR), new TestFilter() {
            @Override
            public boolean exclude(final File file, final String content) {
                return file.getName().equals("es6");
            }
        }, context, global);

        // The es6 directory is excluded above because the context above is in the
        // default language mode, where its contents are not valid. Sweep it separately
        // in the mode it is written for.
        compileTestSet(new File(TEST_BASIC_DIR, "es6"), null, es6Context, es6Global);

        compileTestSet(new File(TEST_NODE_DIR, "node"), null, context, global);
        compileTestSet(new File(TEST_NODE_DIR, "src"), null, context, global);
    }

    private void compileTestSet(final File testSetDir, final TestFilter filter, final Context compileContext,
            final Global compileGlobal) {
        passed = 0;
        failed = 0;
        skipped = 0;
        if (!testSetDir.isDirectory()) {
            log("WARNING: " + testSetDir + " not found or not a directory");
            return;
        }
        log(testSetDir.getAbsolutePath());
        compileJSDirectory(testSetDir, filter, compileContext, compileGlobal);

        log(testSetDir + " compile done!");
        log("compile ok: " + passed);
        log("compile failed: " + failed);
        log("compile skipped: " + skipped);
        if (failed != 0) {
            Assert.fail(failed + " tests failed to compile in " + testSetDir.getAbsolutePath());
        }
    }

    // number of scripts that compiled fine
    private int passed;
    // number of scripts resulting in compile failure
    private int failed;
    // scripts that were skipped - all tests with @negative are
    // skipped for now.
    private int skipped;

    private void compileJSDirectory(final File dir, final TestFilter filter, final Context compileContext,
            final Global compileGlobal) {
        if (filter != null && filter.exclude(dir, null)) {
            return;
        }
        for (final File f : dir.listFiles()) {
            if (f.isDirectory()) {
                compileJSDirectory(f, filter, compileContext, compileGlobal);
            } else if (f.getName().endsWith(".js")) {
                compileJSFile(f, filter, compileContext, compileGlobal);
            }
        }
    }

    private void compileJSFile(final File file, final TestFilter filter, final Context compileContext,
            final Global compileGlobal) {
        if (VERBOSE) {
            log("Begin compiling " + file.getAbsolutePath());
        }

        final Global oldGlobal = Context.getGlobal();
        final boolean globalChanged = (oldGlobal != compileGlobal);

        try {
            final char[] buffer = readFully(file);
            boolean excluded = false;

            if (filter != null) {
                final String content = new String(buffer);
                excluded = filter.exclude(file, content);
            }

            if (excluded) {
                if (VERBOSE) {
                    log("Skipping " + file.getAbsolutePath());
                }
                skipped++;
                return;
            }

            if (globalChanged) {
                Context.setGlobal(compileGlobal);
            }
            final Source source = sourceFor(file.getAbsolutePath(), buffer);
            final ScriptFunction script = compileContext.compileScript(source, compileGlobal);
            if (script == null || compileContext.getErrorManager().getNumberOfErrors() > 0) {
                log("Compile failed: " + file.getAbsolutePath());
                failed++;
            } else {
                passed++;
            }
        } catch (final Throwable t) {
            log("Compile failed: " + file.getAbsolutePath() + " : " + t);
            if (VERBOSE) {
                t.printStackTrace(System.out);
            }
            failed++;
        } finally {
            if (globalChanged) {
                Context.setGlobal(oldGlobal);
            }
        }

        if (VERBOSE) {
            log("Done compiling " + file.getAbsolutePath());
        }
    }
}
