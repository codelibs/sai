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

package org.codelibs.sai.internal.test.framework;

import java.util.Set;
import org.testng.Assert;
import org.testng.ITest;
import org.testng.annotations.Test;

/**
 * Test case used by JSCompilerTest to complain if test files are marked as
 * neither test nor subtest.
 */
@SuppressWarnings("javadoc")
public final class OrphanTestFinder implements ITest {
    private final Set<String> orphanFiles;

    public OrphanTestFinder(final Set<String> orphanFiles) {
        this.orphanFiles = orphanFiles;
    }

    /**
     * TestNG finds this class by its {@code @Test} method as well as through the
     * factory in ScriptTest, and that factory leaves it out when test.js.includes
     * narrows the run. Without a constructor TestNG can call itself, that
     * combination fails the whole run before a single script test executes. An
     * instance built this way has no orphan set and so has nothing to report.
     */
    public OrphanTestFinder() {
        this(null);
    }

    @Override
    public String getTestName() {
        return getClass().getName();
    }

    @Test
    public void test() {
        if (orphanFiles == null || orphanFiles.isEmpty()) {
            return;
        }

        final StringBuilder sb = new StringBuilder();
        final String NL = System.getProperty("line.separator");
        sb.append(orphanFiles.size());
        sb.append(" files found with neither @test nor @subtest: ");
        sb.append(NL);
        for (final String s : orphanFiles) {
            sb.append("  ");
            sb.append(s);
            sb.append(NL);
        }
        Assert.fail(sb.toString());
    }
}
