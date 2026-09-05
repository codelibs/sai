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
 * ES2015 dropped the ES5 early error for a duplicate data property in strict
 * mode (11.1.5 restriction removed), and 13.3 gives an accessor no [[Construct]].
 *
 * @test
 * @run
 * @option --language=es6
 */

// A repeated data property is no longer an early error; the last one wins.
function duplicates() {
    "use strict";
    return ({ a: 1, a: 2 }).a;
}
print("duplicate data property wins last: " + duplicates());

// The relaxation is only about repeated data properties. A data property and an
// accessor of the same name still clash in strict mode. The clash is an early
// error, so it has to be compiled separately via eval to be observable at all --
// written inline it would stop this file from compiling.
try {
    eval("'use strict'; ({ b: 1, get b() { return 2; } })");
    print("data and accessor clash: not reported");
} catch (e) {
    print("data and accessor clash: " + (e instanceof SyntaxError));
}

// An accessor is not a constructor.
var desc = Object.getOwnPropertyDescriptor({ get a() { return 1; }, set a(v) {} }, 'a');
["get", "set"].forEach(function (which) {
    try {
        new desc[which]();
        print(which + " is a constructor: yes");
    } catch (e) {
        print(which + " is a constructor: " + !(e instanceof TypeError));
    }
});

// ...and so has no own prototype property to hand out.
print("get has own prototype: " + desc.get.hasOwnProperty("prototype"));

// An ordinary function still constructs.
function Ordinary() { this.x = 1; }
print("ordinary still constructs: " + (new Ordinary().x === 1));

// An accessor is still callable.
var o = { get a() { return 42; } };
print("accessor still reads: " + o.a);
