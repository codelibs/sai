/*
 * Copyright (c) 2010, 2013, Oracle and/or its affiliates. All rights reserved.
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

package org.codelibs.sai.internal.runtime.regexp;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import org.codelibs.sai.internal.runtime.ParserException;
import org.codelibs.sai.internal.runtime.options.Options;

/**
 * Factory class for regular expressions. This class creates instances of {@link JdkRegExp}.
 * An alternative factory can be installed using the {@code sai.regexp.impl} system property.
 */
public class RegExpFactory {

    private final static RegExpFactory instance;

    private final static String JDK = "org.codelibs";
    private final static String JONI = "joni";

    /** Weak cache of already validated regexps - when reparsing, we don't, for example
     *  need to recompile (reverify) all regexps that have previously been parsed by this
     *  RegExpFactory in a previous compilation. This saves significant time in e.g. avatar
     *  startup
     */
    private static final Map<String, RegExp> REGEXP_CACHE = Collections.synchronizedMap(new WeakHashMap<String, RegExp>());

    static {
        final String impl = Options.getStringProperty("sai.regexp.impl", JONI);
        switch (impl) {
        case JONI:
            instance = new JoniRegExp.Factory();
            break;
        case JDK:
            instance = new RegExpFactory();
            break;
        default:
            instance = null;
            throw new InternalError("Unsupported RegExp factory: " + impl);
        }
    }

    /**
     * Creates a Regular expression from the given {@code pattern} and {@code flags} strings.
     *
     * @param pattern RegExp pattern string
     * @param flags   RegExp flags string
     * @param es6     whether the ES6 flags are accepted
     * @return new RegExp
     * @throws ParserException if flags is invalid or pattern string has syntax error.
     */
    public RegExp compile(final String pattern, final String flags, final boolean es6) throws ParserException {
        return new JdkRegExp(pattern, flags, es6);
    }

    /**
     * Compile a regexp with the given {@code source} and {@code flags}.
     *
     * @param pattern RegExp pattern string
     * @param flags   flag string
     * @param es6     whether the ES6 flags are accepted
     * @return new RegExp
     * @throws ParserException if invalid source or flags
     */
    public static RegExp create(final String pattern, final String flags, final boolean es6) {
        // The cache is static and outlives any single context, so the ES6 flags have to be
        // part of the key. Sharing one entry would hand a /x/y compiled under --language=es6
        // to a context that must reject it.
        final String key = es6 ? pattern + "/" + flags + "/es6" : pattern + "/" + flags;
        RegExp regexp = REGEXP_CACHE.get(key);
        if (regexp == null) {
            // Joni matches UTF-16 code units, so it cannot give the u flag its meaning, while
            // java.util.regex matches code points, which is what the flag asks for. A pattern
            // that carries u therefore goes to the JDK engine whichever factory is installed.
            regexp = flags.indexOf('u') >= 0
                    ? new JdkRegExp(pattern, flags, es6)
                    : instance.compile(pattern, flags, es6);
            REGEXP_CACHE.put(key, regexp);
        }
        return regexp;
    }

    /**
     * Validate a regexp with the given {@code source} and {@code flags}.
     *
     * @param pattern RegExp pattern string
     * @param flags  flag string
     * @param es6    whether the ES6 flags are accepted
     *
     * @throws ParserException if invalid source or flags
     */
    public static void validate(final String pattern, final String flags, final boolean es6) throws ParserException {
        create(pattern, flags, es6);
    }
}
