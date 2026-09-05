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
 * ES2015 9.1.12 OrdinaryOwnPropertyKeys: every array index first, in ascending
 * numeric order, then the remaining string keys in the order they were created.
 *
 * @test
 * @run
 */

function build() {
    var obj = {
        2: true, 0: true, 1: true, ' ': true, 9: true, D: true, B: true, '-1': true
    };
    obj.A = true;
    obj[3] = true;
    "EFGHIJKLMNOPQRSTUVWXYZ".split('').forEach(function (key) { obj[key] = true; });
    // An index introduced through defineProperty belongs in the ascending run just
    // as much as one introduced by assignment.
    Object.defineProperty(obj, 'C', { value: true, enumerable: true });
    Object.defineProperty(obj, '4', { value: true, enumerable: true });
    delete obj[2];
    obj[2] = true;
    return obj;
}

var obj = build();
print("getOwnPropertyNames: " + Object.getOwnPropertyNames(obj).join(''));
print("keys: " + Object.keys(obj).join(''));

var forIn = '';
for (var k in obj) { forIn += k; }
print("for-in: " + forIn);

print("JSON.stringify starts sorted: " +
    (JSON.stringify(obj).indexOf('{"0":true,"1":true,"2":true,"3":true,"4":true,"9":true') === 0));

// Object.assign visits the source in the same order.
var seen = '';
var target = {};
"012349 DBACEFGHIJKLMNOPQRST".split('').concat(-1).forEach(function (key) {
    Object.defineProperty(target, key, { set: function () { seen += key; } });
});
Object.assign(target, build());
print("Object.assign order: " + seen);

// An index promoted out of the array store by an attribute change keeps its place.
var promoted = {};
promoted[0] = 1; promoted[1] = 2; promoted[2] = 3;
Object.defineProperty(promoted, 1, { enumerable: false });
Object.defineProperty(promoted, 1, { enumerable: true });
print("promoted index keeps place: " + Object.getOwnPropertyNames(promoted).join(''));

// A plain array is untouched, sparse included.
var sparse = [];
sparse[10000000] = 1; sparse[10] = 1; sparse[20] = 1;
print("sparse array: " + Object.keys(sparse).join(','));

// 2^32-1 is not an array index, so it sorts with the string keys.
var edge = {};
edge[4294967295] = 1;
edge[4294967294] = 1;
edge.z = 1;
edge[0] = 1;
print("index edge: " + Object.getOwnPropertyNames(edge).join(','));
