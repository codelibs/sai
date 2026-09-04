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

/**
 * super inside an arrow function.
 *
 * An arrow has no this and no super of its own, so both come from the method
 * around it. The parent methods here all read this, because a super call that
 * forwards the wrong receiver still returns something - just the wrong thing.
 *
 * Runs with lazy compilation, which is the point: the arrow body is re-parsed
 * the first time it is called, and that re-parse has to accept super too.
 *
 * @test
 * @run
 * @option --language=es6
 */

class Base {
    constructor() {
        this.v = "base";
    }
    who() {
        return this.v;
    }
    greet(greeting) {
        return greeting + " " + this.v;
    }
}

class Derived extends Base {
    constructor() {
        super();
        this.v = "derived";
    }

    // The method form, for comparison.
    direct() {
        return super.who();
    }

    // A concise arrow body.
    concise() {
        var f = () => super.who();
        return f();
    }

    // A brace arrow body.
    braced() {
        var f = () => { return super.who(); };
        return f();
    }

    // Two arrows deep.
    nested() {
        var f = () => () => super.who();
        return f()();
    }

    // With arguments, so the receiver is not the only thing forwarded.
    withArgs() {
        var f = (greeting) => super.greet(greeting);
        return f("hello");
    }

    // A property access rather than a call.
    property() {
        var f = () => super.who;
        return typeof f();
    }

    // In an arrow inside the constructor, after super().
    static madeInConstructor() {
        return new WithArrowInConstructor().fromConstructor();
    }
}

class WithArrowInConstructor extends Base {
    constructor() {
        super();
        this.v = "constructed";
        this.fromConstructor = () => super.who();
    }
}

var d = new Derived();
print(d.direct());
print(d.concise());
print(d.braced());
print(d.nested());
print(d.withArgs());
print(d.property());
print(Derived.madeInConstructor());
