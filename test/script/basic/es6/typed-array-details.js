/*
 * Copyright (c) 2026, CodeLibs Project and the Others. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * ES6 24.2.2.1: DataView is a constructor and requires new, like the typed
 * array constructors and ArrayBuffer beside it.
 *
 * @test
 * @run
 */

var TYPED = ['Int8Array', 'Uint8Array', 'Uint8ClampedArray', 'Int16Array', 'Uint16Array',
             'Int32Array', 'Uint32Array', 'Float32Array', 'Float64Array'];

// Every constructor in 22.2/24.1/24.2 rejects a plain call.
var buffer = new ArrayBuffer(64);
var callable = [];
['ArrayBuffer', 'DataView'].concat(TYPED).forEach(function (name) {
    try {
        this[name](name === 'ArrayBuffer' ? 64 : buffer);
        callable.push(name);
    } catch (e) {
        if (!(e instanceof TypeError)) {
            callable.push(name + "(" + e + ")");
        }
    }
});
print("callable without new: " + (callable.length === 0 ? "none" : callable.join(',')));

// All three DataView arities behave the same way; the check has to sit in each,
// because the two- and three-argument forms are specialized entry points of
// their own rather than paths through the varargs one.
var arities = [];
try { DataView(buffer); } catch (e) { arities.push(e instanceof TypeError); }
try { DataView(buffer, 0); } catch (e) { arities.push(e instanceof TypeError); }
try { DataView(buffer, 0, 8); } catch (e) { arities.push(e instanceof TypeError); }
print("every DataView arity requires new: " + (arities.length === 3 && arities.every(Boolean)));

// new still works at every arity, and still validates its argument.
print("new DataView 1-arg: " + (new DataView(buffer).byteLength === 64));
print("new DataView 2-arg: " + (new DataView(buffer, 8).byteLength === 56));
print("new DataView 3-arg: " + (new DataView(buffer, 0, 8).byteLength === 8));
try {
    new DataView({});
    print("new DataView validates: no");
} catch (e) {
    print("new DataView validates: " + (e instanceof TypeError));
}

// A view built without new would have been unusable anyway; one built with it reads.
var view = new DataView(buffer, 0, 8);
view.setInt32(0, 1234);
print("round trip: " + view.getInt32(0));
