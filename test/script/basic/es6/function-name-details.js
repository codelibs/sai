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
 * Three more of the ES2015 rules for a function's name, on top of the inference
 * that already landed: CreateDynamicFunction names its result "anonymous"
 * (19.2.1.1.1), an accessor is named "get x"/"set x" (9.2.11), and a concise
 * method does not bind its own name inside its body (14.3.8).
 *
 * @test
 * @run
 * @option --language=es6
 */

// 19.2.1.1.1 splices the identifier "anonymous" into the source it builds.
print("new Function name: " + (new Function).name);
print("new Function with args: " + (new Function("a", "b", "return a + b")).name);
print("new Function still runs: " + (new Function("a", "b", "return a + b"))(1, 2));

// 9.2.11 SetFunctionName prefixes an accessor with its kind.
var desc = Object.getOwnPropertyDescriptor({ get foo() { return 1; }, set foo(v) {} }, 'foo');
print("getter name: " + desc.get.name);
print("setter name: " + desc.set.name);

// 14.3.8: a concise method gets no self-binding, so the outer name stays visible.
var f = "outer";
print("shorthand method sees outer f: " + ({ f() { return f; } }).f());

var g = "outer g";
print("accessor sees outer g: " + ({ get g() { return g; } }).g);

// A named function expression still binds its own name -- that rule is untouched.
print("named function expression self-binds: " + (function me() { return typeof me; })());

// A class is left alone by the change above -- it is still treated as a named
// function expression, so nothing here moves it. That its name is not yet
// visible inside its own body is the separate, known gap in lexical class
// scoping; this line is here to catch the day this starts reporting something
// other than what it did before.
var Named = class Inner { m() { return typeof Inner; } };
print("class expression name in body (known gap): " + new Named().m());

// The method still has the name it always had.
print("shorthand method name: " + ({ f() {} }).f.name);
