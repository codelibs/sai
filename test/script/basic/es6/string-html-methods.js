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
 * Annex B B.2.3 String.prototype HTML methods.
 *
 * These are a pure addition - no method of these names existed before - so they
 * are installed at every language level. This test deliberately carries no
 * --language=es6 option, which is what pins that.
 *
 * @test
 * @run
 */

var names = ['anchor', 'big', 'blink', 'bold', 'fixed', 'fontcolor', 'fontsize',
             'italics', 'link', 'small', 'strike', 'sub', 'sup'];

// -- all thirteen exist, are not enumerable, and report the spec's arity

for (var i = 0; i < names.length; i++) {
    var f = String.prototype[names[i]];
    print(names[i] + ' ' + (typeof f) + ' arity=' + f.length +
          ' enumerable=' + Object.getOwnPropertyDescriptor(String.prototype, names[i]).enumerable);
}

// -- the markup each one produces

print('foo'.anchor('bar'));
print('foo'.big());
print('foo'.blink());
print('foo'.bold());
print('foo'.fixed());
print('foo'.fontcolor('red'));
print('foo'.fontsize(3));
print('foo'.italics());
print('foo'.link('http://example.com/'));
print('foo'.small());
print('foo'.strike());
print('foo'.sub());
print('foo'.sup());

// -- tag names are lower case, even under the Turkish locale the tests run in

print((function () {
    for (var i = 0; i < names.length; i++) {
        var html = ''[names[i]]();
        if (html.toLowerCase() !== html) {
            return 'FAILED: ' + names[i] + ' -> ' + html;
        }
    }
    return 'all tags lower case';
})());

// -- a double quote in an attribute value is escaped

print('foo'.anchor('"bar"'));
print('foo'.link('a"b'));
print('foo'.fontcolor('"'));
print('foo'.fontsize('"'));
print((function () {
    var attributed = ['anchor', 'fontcolor', 'fontsize', 'link'];
    for (var i = 0; i < attributed.length; i++) {
        if (''[attributed[i]]('"') !== ''[attributed[i]]('&' + 'quot;')) {
            return 'FAILED: ' + attributed[i] + ' did not escape the quote';
        }
    }
    return 'all quotes escaped';
})());

// -- nothing else is escaped: Annex B says so, however unsafe that is

print('<b>'.bold());
print('foo'.anchor('a&b<c>'));

// -- the empty string, and a missing argument

print('[' + ''.big() + ']');
print('[' + ''.anchor('x') + ']');
print('foo'.anchor());
print('foo'.fontsize());

// -- the receiver goes through ToString, and null/undefined are rejected

print(String.prototype.bold.call(5));
print(String.prototype.anchor.call(true, 1));
print(String.prototype.big.call(new String('wrapped')));

for (var i = 0; i < names.length; i++) {
    for (var j = 0, receivers = [null, undefined]; j < receivers.length; j++) {
        try {
            String.prototype[names[i]].call(receivers[j], 'x');
            print('FAILED: ' + names[i] + ' accepted ' + String(receivers[j]) + ' as this');
        } catch (e) {
            if (!(e instanceof TypeError)) {
                print('FAILED: ' + names[i] + ' threw ' + e);
            }
        }
    }
}
print('null and undefined receivers all threw TypeError');

// -- the receiver is coerced before the attribute value is

try {
    String.prototype.anchor.call(null, { toString: function () { print('FAILED: value coerced first'); return ''; } });
} catch (e) {
    print('receiver checked first: ' + (e instanceof TypeError));
}
