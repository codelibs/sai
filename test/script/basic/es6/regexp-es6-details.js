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
 * ES2015 21.2.3.1: the RegExp constructor takes flags alongside a RegExp pattern.
 * ES2015 21.2.5.14: RegExp.prototype.toString is generic.
 * Annex B 21.2.2.1: a legacy octal escape in a pattern.
 *
 * The constructor change is gated: it alters an existing operation rather than
 * adding a name, so ES5 keeps rejecting the pair (see JDK-8025486.js). The
 * generic toString and the octal escape are additions and need no flag.
 *
 * @test
 * @run
 * @option --language=es6
 */

// 21.2.3.1: a second argument replaces the pattern's own flags rather than
// being rejected.
var re = new RegExp(/./im, "g");
print("source: " + re.source);
print("global: " + re.global);
print("ignoreCase: " + re.ignoreCase);
print("multiline: " + re.multiline);

// Without flags the original flags are kept.
print("no flags keeps flags: " + new RegExp(/a/gi).flags);

// A string pattern is unaffected.
print("string pattern: " + new RegExp("a", "m").flags);

// 21.2.5.14: toString reads source and flags off this, whatever this is.
print("generic toString: " + RegExp.prototype.toString.call({ source: 'foo', flags: 'bar' }));
print("own toString: " + /ab+c/ig.toString());

// Annex B: a legacy octal escape, outside and inside a character class.
print("octal outside class: " + /\041/.exec("!")[0]);
print("octal inside class: " + /[\041]/.exec("!")[0]);
print("octal two digit: " + /\101/.exec("A")[0]);

// A back reference is still a back reference, not an octal escape.
print("back reference: " + /(a)\1/.exec("aa")[0]);
