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
 * ES2015 19.4.3.4 Symbol.prototype [ @@toPrimitive ]: a symbol put in a wrapper
 * object reduces back to the symbol, whatever primitive is asked for. Without it
 * a wrapper reduces through toString, which spells the symbol out as a string, so
 * the wrapper was usable nowhere the symbol was.
 *
 * 7.2.12 goes with it: a symbol sits beside a number and a string in the rule that
 * reduces the object on the other side of ==, so a symbol compares equal to the
 * wrapper it was put in.
 *
 * These are additions, so the test deliberately runs without --language=es6.
 *
 * @test
 * @run
 */

function show(label, value) {
    print(label + ": " + value);
}

var symbol = Symbol("wrapped");
var wrapper = Object(symbol);

// 19.4.3.4 is on the prototype, not on the wrapper.
show("Symbol.prototype has it", typeof Symbol.prototype[Symbol.toPrimitive]);
show("the wrapper does not", wrapper.hasOwnProperty(Symbol.toPrimitive));
var descriptor = Object.getOwnPropertyDescriptor(Symbol.prototype, Symbol.toPrimitive);
show("the descriptor", [typeof descriptor.value, descriptor.enumerable, descriptor.configurable].join(","));

// 19.4.2.1: the wrapper is an object that is not the symbol, but stands for it.
show("it is an object", typeof wrapper);
show("it is a Symbol", wrapper instanceof Symbol);
show("it is not the symbol", wrapper !== symbol);
show("but it values as one", wrapper.valueOf() === symbol);
show("and reduces to one", Symbol.prototype[Symbol.toPrimitive].call(wrapper) === symbol);
show("the hint makes no difference",
     [Symbol.prototype[Symbol.toPrimitive].call(wrapper, "number") === symbol,
      Symbol.prototype[Symbol.toPrimitive].call(wrapper, "string") === symbol,
      Symbol.prototype[Symbol.toPrimitive].call(wrapper, "default") === symbol].join(","));

// 7.2.12: == reduces the object, so the two compare equal both ways round.
show("wrapper == symbol", wrapper == symbol);
show("symbol == wrapper", symbol == wrapper);
show("but not strictly", (wrapper === symbol) + "," + (symbol === wrapper));
show("a different symbol is not equal", Object(Symbol("wrapped")) == symbol);

// 7.1.14: a wrapper used as a key names the symbol it stands for, not the string
// that spells the symbol out.
var object = {};
object[wrapper] = 1;
show("used as a key", object[symbol]);
show("it left no name behind", Object.getOwnPropertyNames(object).length);
show("it is among the symbols", Object.getOwnPropertySymbols(object)[0] === symbol);

// 24.3.2: JSON leaves both the symbol and its wrapper out, the wrapper being an
// ordinary object with nothing in it to write.
show("a symbol valued property", JSON.stringify({ foo: symbol }));
show("a symbol in an array", JSON.stringify([symbol]));
show("a symbol on its own", JSON.stringify(symbol));
show("a wrapper valued property", JSON.stringify({ foo: wrapper }));
show("a wrapper in an array", JSON.stringify([wrapper]));
show("a wrapper on its own", JSON.stringify(wrapper));

// A wrapper with a toJSON that cannot be called is still written as an object,
// since nothing tries to call it.
var noToJSON = Object(Symbol());
Object.defineProperty(noToJSON, "toJSON", { enumerable: false, value: null });
show("a wrapper with a null toJSON", JSON.stringify({ foo: noToJSON }));

// Everything a symbol refused before, it still refuses.
try {
    "" + symbol;
    show("a symbol still refuses to be a string", "no error");
} catch (e) {
    show("a symbol still refuses to be a string", e instanceof TypeError);
}
show("its description still reads", symbol.toString() + "," + String(symbol));
