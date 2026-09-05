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

package org.codelibs.sai.internal.ir;

import org.codelibs.sai.internal.ir.annotations.Immutable;
import org.codelibs.sai.internal.ir.visitor.NodeVisitor;

/**
 * IR representation of an object literal property.
 */
@Immutable
@SuppressWarnings("serial")
public final class PropertyNode extends Node {
    private static final long serialVersionUID = 1L;

    /** Property key. */
    private final PropertyKey key;

    /** Property value. */
    private final Expression value;

    /** Property getter. */
    private final FunctionNode getter;

    /** Property getter. */
    private final FunctionNode setter;

    /** Is this the {@code __proto__ : value} form that sets the prototype? */
    private final boolean protoAssignment;

    /**
     * Constructor
     *
     * @param token   token
     * @param finish  finish
     * @param key     the key of this property
     * @param value   the value of this property
     * @param getter  getter function body
     * @param setter  setter function body
     * @param protoAssignment true if this is {@code __proto__ : value}, the one form of
     *                        property definition that sets the prototype instead of
     *                        defining a property
     */
    public PropertyNode(final long token, final int finish, final PropertyKey key, final Expression value, final FunctionNode getter,
            final FunctionNode setter, final boolean protoAssignment) {
        super(token, finish);
        this.key = key;
        this.value = value;
        this.getter = getter;
        this.setter = setter;
        this.protoAssignment = protoAssignment;
    }

    private PropertyNode(final PropertyNode propertyNode, final PropertyKey key, final Expression value, final FunctionNode getter,
            final FunctionNode setter, final boolean protoAssignment) {
        super(propertyNode);
        this.key = key;
        this.value = value;
        this.getter = getter;
        this.setter = setter;
        this.protoAssignment = protoAssignment;
    }

    /**
     * Is this the {@code __proto__ : value} form of Annex B.3.1, which sets the
     * prototype rather than defining a property?
     *
     * Only that form does. A computed key, a shorthand property, a shorthand method and
     * an accessor all define an ordinary own property, even when they spell the name the
     * same way, so the name alone cannot decide it -- the parser marks the one form that
     * counts.
     *
     * @return true if this sets the prototype
     */
    public boolean isProtoAssignment() {
        return protoAssignment;
    }

    /**
     * Get the name of the property key
     * @return key name
     */
    public String getKeyName() {
        return key.getPropertyName();
    }

    @Override
    public Node accept(final NodeVisitor<? extends LexicalContext> visitor) {
        if (visitor.enterPropertyNode(this)) {
            return visitor.leavePropertyNode(setKey((PropertyKey) ((Node) key).accept(visitor))
                    .setValue(value == null ? null : (Expression) value.accept(visitor))
                    .setGetter(getter == null ? null : (FunctionNode) getter.accept(visitor))
                    .setSetter(setter == null ? null : (FunctionNode) setter.accept(visitor)));
        }

        return this;
    }

    @Override
    public void toString(final StringBuilder sb, final boolean printType) {
        if (value instanceof FunctionNode && ((FunctionNode) value).getIdent() != null) {
            value.toString(sb);
        }

        if (value != null) {
            ((Node) key).toString(sb, printType);
            sb.append(": ");
            value.toString(sb, printType);
        }

        if (getter != null) {
            sb.append(' ');
            getter.toString(sb, printType);
        }

        if (setter != null) {
            sb.append(' ');
            setter.toString(sb, printType);
        }
    }

    /**
     * Get the getter for this property
     * @return getter or null if none exists
     */
    public FunctionNode getGetter() {
        return getter;
    }

    /**
     * Set the getter of this property, null if none
     * @param getter getter
     * @return same node or new node if state changed
     */
    public PropertyNode setGetter(final FunctionNode getter) {
        if (this.getter == getter) {
            return this;
        }
        return new PropertyNode(this, key, value, getter, setter, protoAssignment);
    }

    /**
     * Return the key for this property node
     * @return the key
     */
    public Expression getKey() {
        return (Expression) key;
    }

    private PropertyNode setKey(final PropertyKey key) {
        if (this.key == key) {
            return this;
        }
        return new PropertyNode(this, key, value, getter, setter, protoAssignment);
    }

    /**
     * Get the setter for this property
     * @return setter or null if none exists
     */
    public FunctionNode getSetter() {
        return setter;
    }

    /**
     * Set the setter for this property, null if none
     * @param setter setter
     * @return same node or new node if state changed
     */
    public PropertyNode setSetter(final FunctionNode setter) {
        if (this.setter == setter) {
            return this;
        }
        return new PropertyNode(this, key, value, getter, setter, protoAssignment);
    }

    /**
     * Get the value of this property
     * @return property value
     */
    public Expression getValue() {
        return value;
    }

    /**
     * Set the value of this property
     * @param value new value
     * @return same node or new node if state changed
     */
    public PropertyNode setValue(final Expression value) {
        if (this.value == value) {
            return this;
        }
        return new PropertyNode(this, key, value, getter, setter, protoAssignment);
    }
}
