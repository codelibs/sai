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

package org.codelibs.sai.internal.runtime;

import java.util.HashMap;
import java.util.Map;

/**
 * An insertion ordered map with the key equality ES6 calls SameValueZero, used to
 * back {@code Map} and {@code Set}.
 *
 * <p>The entries live in a doubly linked list and a hash map indexes it, so a
 * cursor can keep walking while the map is modified underneath it. That is what
 * the specification asks of the {@code Map} and {@code Set} iterators: an entry
 * deleted before the cursor reaches it is never visited, and one appended while
 * the cursor is running is.
 */
public final class LinkedMap {

    /** An entry, and at the same time a link in the iteration order. */
    public static final class Node {
        private final Object key;
        private Object value;
        private Node prev;
        private Node next;
        /** Cleared on removal so that a cursor parked here knows to walk forward. */
        private boolean linked = true;

        private Node(final Object key, final Object value) {
            this.key = key;
            this.value = value;
        }

        /**
         * Returns the key, in the normalized form the map stores it in.
         * @return the key
         */
        public Object getKey() {
            return key;
        }

        /**
         * Returns the value.
         * @return the value
         */
        public Object getValue() {
            return value;
        }
    }

    /**
     * A cursor over the entries that tolerates the map being modified while it runs.
     */
    public static final class Cursor {
        private Node current;

        private Cursor(final Node head) {
            this.current = head;
        }

        /**
         * Advances to the next live entry and returns it, or null once the map is
         * exhausted. A removed entry keeps the forward link it had, so a cursor
         * parked on one still finds its way to the entries that follow.
         * @return the next entry, or null
         */
        public Node next() {
            Node node = current.next;
            while (node != null && !node.linked) {
                node = node.next;
            }
            if (node != null) {
                current = node;
            }
            return node;
        }
    }

    /** Stands in for the null key, which a HashMap would otherwise treat specially. */
    private static final Object NULL_KEY = new Object();

    private final Map<Object, Node> index = new HashMap<>();
    private final Node head = new Node(null, null);
    private Node tail = head;

    /**
     * Reduces a key to the form the index is keyed on, so that SameValueZero
     * decides equality: every number becomes a Double, -0 becomes +0, NaN equals
     * itself, and a ConsString is flattened to the String it stands for.
     * @param key the key as the script supplied it
     * @return the normalized key
     */
    public static Object normalizeKey(final Object key) {
        if (key == null) {
            return NULL_KEY;
        }
        if (key instanceof ConsString) {
            return key.toString();
        }
        if (key instanceof Number) {
            final double d = ((Number) key).doubleValue();
            // +0 and -0 are one key; Double.equals already makes NaN equal itself.
            return d == 0.0 ? Double.valueOf(0.0) : Double.valueOf(d);
        }
        return key;
    }

    /**
     * Returns the key in the form a script should see it in, undoing
     * {@link #normalizeKey(Object)}'s stand-in for null.
     * @param key a normalized key
     * @return the key as a script value
     */
    public static Object denormalizeKey(final Object key) {
        return key == NULL_KEY ? null : key;
    }

    /**
     * Returns the number of entries.
     * @return the number of entries
     */
    public int size() {
        return index.size();
    }

    /**
     * Looks an entry up.
     * @param key a normalized key
     * @return the entry, or null
     */
    public Node get(final Object key) {
        return index.get(key);
    }

    /**
     * Adds an entry, or replaces the value of the one already stored under the key.
     * An existing entry keeps its place in the iteration order.
     * @param key a normalized key
     * @param value the value
     */
    public void set(final Object key, final Object value) {
        final Node existing = index.get(key);
        if (existing != null) {
            existing.value = value;
            return;
        }
        final Node node = new Node(key, value);
        node.prev = tail;
        tail.next = node;
        tail = node;
        index.put(key, node);
    }

    /**
     * Removes an entry.
     * @param key a normalized key
     * @return true if there was one to remove
     */
    public boolean delete(final Object key) {
        final Node node = index.remove(key);
        if (node == null) {
            return false;
        }
        unlink(node);
        return true;
    }

    /**
     * Removes every entry. Cursors already running rewind to the head and so see
     * only what is added afterwards.
     */
    public void clear() {
        for (Node node = head.next; node != null; node = node.next) {
            node.linked = false;
        }
        index.clear();
        head.next = null;
        tail = head;
    }

    /**
     * Returns a cursor positioned before the first entry.
     * @return a cursor
     */
    public Cursor cursor() {
        return new Cursor(head);
    }

    private void unlink(final Node node) {
        node.linked = false;
        node.prev.next = node.next;
        if (node.next != null) {
            node.next.prev = node.prev;
        } else {
            tail = node.prev;
        }
    }
}
