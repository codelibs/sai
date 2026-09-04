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
 * ES6 19.1.2: the Object statics that inspect an object run ToObject on a
 * primitive instead of throwing a TypeError. Only null and undefined still
 * throw, because they have no wrapper object.
 *
 * @test
 * @run
 * @option --language=es6
 */

// -- ToObject group: getPrototypeOf / getOwnPropertyDescriptor /
//                    getOwnPropertyNames / keys

print(Object.getPrototypeOf('a') === String.prototype);
print(Object.getPrototypeOf(1) === Number.prototype);
print(Object.getPrototypeOf(1.5) === Number.prototype);
print(Object.getPrototypeOf(true) === Boolean.prototype);
print(Object.getPrototypeOf('a').constructor === String);

print(Object.getOwnPropertyDescriptor('a', 'foo'));
print(JSON.stringify(Object.getOwnPropertyDescriptor('ab', '0')));
print(Object.getOwnPropertyDescriptor(1, 'x'));

print(Object.getOwnPropertyNames('a').sort().join(','));
print(Object.getOwnPropertyNames('').sort().join(','));
print(Object.getOwnPropertyNames(1).join(','));

print(Object.keys('a').join(','));
print(Object.keys('ab').join(','));
print(Object.keys('').length);
print(Object.keys(1).length);
print(Object.keys(true).length);

// -- integrity level group: a primitive is handed back untouched, and reads as
//    sealed, frozen and not extensible

print(Object.seal('a') === 'a');
print(Object.freeze('a') === 'a');
print(Object.preventExtensions('a') === 'a');
print(Object.seal(42) === 42);
print(Object.freeze(false) === false);
print(Object.preventExtensions('') === '');

print(Object.isSealed('a'));
print(Object.isFrozen('a'));
print(Object.isExtensible('a'));
print(Object.isSealed(42));
print(Object.isFrozen(false));
print(Object.isExtensible(''));

// -- null and undefined still throw, for all ten

var methods = ['getPrototypeOf', 'getOwnPropertyDescriptor', 'getOwnPropertyNames',
               'seal', 'freeze', 'preventExtensions',
               'isSealed', 'isFrozen', 'isExtensible', 'keys'];

for (var i = 0; i < methods.length; i++) {
    for (var j = 0, args = [null, undefined]; j < args.length; j++) {
        try {
            Object[methods[i]](args[j], 'x');
            print('FAILED: Object.' + methods[i] + ' accepted ' + String(args[j]));
        } catch (e) {
            print(methods[i] + '(' + String(args[j]) + ') -> ' + (e instanceof TypeError));
        }
    }
}

// -- objects and host objects are unaffected

print(Object.keys({ a: 1, b: 2 }).join(','));
print(Object.isExtensible({}));
print(Object.isSealed(Object.seal({})));
print(Object.getPrototypeOf(new java.lang.Object()));
