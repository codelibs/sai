/*
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

/*
 * This file is available under and governed by the GNU General Public
 * License version 2 only, as published by the Free Software Foundation.
 * However, the following notice accompanied the original version of this
 * file:
 *
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000-2011 INRIA, France Telecom
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.codelibs.sai.org.objectweb.asm.util;

import org.codelibs.sai.org.objectweb.asm.AnnotationVisitor;
import org.codelibs.sai.org.objectweb.asm.Attribute;
import org.codelibs.sai.org.objectweb.asm.FieldVisitor;
import org.codelibs.sai.org.objectweb.asm.Opcodes;
import org.codelibs.sai.org.objectweb.asm.TypePath;

/**
 * A {@link FieldVisitor} that prints the fields it visits with a
 * {@link Printer}.
 *
 * @author Eric Bruneton
 */
public final class TraceFieldVisitor extends FieldVisitor {

    public final Printer p;

    public TraceFieldVisitor(final Printer p) {
        this(null, p);
    }

    public TraceFieldVisitor(final FieldVisitor fv, final Printer p) {
        super(Opcodes.ASM5, fv);
        this.p = p;
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String desc,
            final boolean visible) {
        Printer p = this.p.visitFieldAnnotation(desc, visible);
        AnnotationVisitor av = fv == null ? null : fv.visitAnnotation(desc,
                visible);
        return new TraceAnnotationVisitor(av, p);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef,
            TypePath typePath, String desc, boolean visible) {
        Printer p = this.p.visitFieldTypeAnnotation(typeRef, typePath, desc,
                visible);
        AnnotationVisitor av = fv == null ? null : fv.visitTypeAnnotation(
                typeRef, typePath, desc, visible);
        return new TraceAnnotationVisitor(av, p);
    }

    @Override
    public void visitAttribute(final Attribute attr) {
        p.visitFieldAttribute(attr);
        super.visitAttribute(attr);
    }

    @Override
    public void visitEnd() {
        p.visitFieldEnd();
        super.visitEnd();
    }
}
