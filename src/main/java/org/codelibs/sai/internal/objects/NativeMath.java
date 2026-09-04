/*
 * Copyright (c) 2010, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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

package org.codelibs.sai.internal.objects;

import org.codelibs.sai.internal.objects.annotations.Attribute;
import org.codelibs.sai.internal.objects.annotations.Function;
import org.codelibs.sai.internal.objects.annotations.Property;
import org.codelibs.sai.internal.objects.annotations.ScriptClass;
import org.codelibs.sai.internal.objects.annotations.SpecializedFunction;
import org.codelibs.sai.internal.objects.annotations.Where;
import org.codelibs.sai.internal.runtime.JSType;
import org.codelibs.sai.internal.runtime.PropertyMap;
import org.codelibs.sai.internal.runtime.ScriptObject;

/**
 * ECMA 15.8 The Math Object
 *
 */
@ScriptClass("Math")
public final class NativeMath extends ScriptObject {

    // initialized by saigen
    @SuppressWarnings("unused")
    private static PropertyMap $saigenmap$;

    private NativeMath() {
        // don't create me!
        throw new UnsupportedOperationException();
    }

    /** ECMA 15.8.1.1 - E, always a double constant. Not writable or configurable */
    @Property(attributes = Attribute.NON_ENUMERABLE_CONSTANT, where = Where.CONSTRUCTOR)
    public static final double E = Math.E;

    /** ECMA 15.8.1.2 - LN10, always a double constant. Not writable or configurable */
    @Property(attributes = Attribute.NON_ENUMERABLE_CONSTANT, where = Where.CONSTRUCTOR)
    public static final double LN10 = 2.302585092994046;

    /** ECMA 15.8.1.3 - LN2, always a double constant. Not writable or configurable */
    @Property(attributes = Attribute.NON_ENUMERABLE_CONSTANT, where = Where.CONSTRUCTOR)
    public static final double LN2 = 0.6931471805599453;

    /** ECMA 15.8.1.4 - LOG2E, always a double constant. Not writable or configurable */
    @Property(attributes = Attribute.NON_ENUMERABLE_CONSTANT, where = Where.CONSTRUCTOR)
    public static final double LOG2E = 1.4426950408889634;

    /** ECMA 15.8.1.5 - LOG10E, always a double constant. Not writable or configurable */
    @Property(attributes = Attribute.NON_ENUMERABLE_CONSTANT, where = Where.CONSTRUCTOR)
    public static final double LOG10E = 0.4342944819032518;

    /** ECMA 15.8.1.6 - PI, always a double constant. Not writable or configurable */
    @Property(attributes = Attribute.NON_ENUMERABLE_CONSTANT, where = Where.CONSTRUCTOR)
    public static final double PI = Math.PI;

    /** ECMA 15.8.1.7 - SQRT1_2, always a double constant. Not writable or configurable */
    @Property(attributes = Attribute.NON_ENUMERABLE_CONSTANT, where = Where.CONSTRUCTOR)
    public static final double SQRT1_2 = 0.7071067811865476;

    /** ECMA 15.8.1.8 - SQRT2, always a double constant. Not writable or configurable */
    @Property(attributes = Attribute.NON_ENUMERABLE_CONSTANT, where = Where.CONSTRUCTOR)
    public static final double SQRT2 = 1.4142135623730951;

    /**
     * ECMA 15.8.2.1 abs(x)
     *
     * @param self  self reference
     * @param x     argument
     *
     * @return abs of value
     */
    @Function(attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static double abs(final Object self, final Object x) {
        return Math.abs(JSType.toNumber(x));
    }

    /**
     * ECMA 15.8.2.1 abs(x) - specialization for int values
     *
     * @param self  self reference
     * @param x     argument
     *
     * @return abs of argument
     */
    @SpecializedFunction
    public static int abs(final Object self, final int x) {
        return Math.abs(x);
    }

    /**
     * ECMA 15.8.2.1 abs(x) - specialization for long values
     *
     * @param self  self reference
     * @param x     argument
     *
     * @return abs of argument
     */
    @SpecializedFunction
    public static long abs(final Object self, final long x) {
        return Math.abs(x);
    }

    /**
     * ECMA 15.8.2.1 abs(x) - specialization for double values
     *
     * @param self  self reference
     * @param x     argument
     *
     * @return abs of argument
     */
    @SpecializedFunction
    public static double abs(final Object self, final double x) {
        return Math.abs(x);
    }

    /**
     * ECMA 15.8.2.2 acos(x)
     *
     * @param self  self reference
     * @param x     argument
     *
     * @return acos of argument
     */
    @Function(attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static double acos(final Object self, final Object x) {
        return Math.acos(JSType.toNumber(x));
    }

    /**
     * ECMA 15.8.2.2 acos(x) - specialization for double values
     *
     * @param self  self reference
     * @param x     argument
     *
     * @return acos of argument
     */
    @SpecializedFunction
    public static double acos(final Object self, final double x) {
        return Math.acos(x);
    }

    /**
     * ES6 20.2.2.3 acosh(x)
     *
     * @param self  self reference
     * @param x     argument
     *
     * @return inverse hyperbolic cosine of x
     */
    @Function(attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static double acosh(final Object self, final Object x) {
        final double d = JSType.toNumber(x);

        if (Double.isNaN(d) || d < 1.0) {
            return Double.NaN;
        }
        if (d == 1.0) {
            return 0.0;
        }
        if (Double.isInfinite(d)) {
            return d;
        }
        // java.lang.Math has no acosh, so this is fdlibm's __ieee754_acosh split into its ranges.
        // The textbook log(x + sqrt(x*x - 1)) is unusable at both ends: x*x overflows to Infinity
        // once x exceeds about 1.3e154, and as x approaches 1 the subtraction x*x - 1 cancels away
        // the digits that carry the answer.
        if (d >= 0x1p28) {
            // x*x would overflow, and at this magnitude acosh(x) and log(2x) agree to within an ulp
            return Math.log(d) + LN2;
        }
        if (d > 2.0) {
            return Math.log(2.0 * d - 1.0 / (d + Math.sqrt(d * d - 1.0)));
        }
        final double t = d - 1.0;
        return Math.log1p(t + Math.sqrt(2.0 * t + t * t));
    }

    /**
     * ECMA 15.8.2.3 asin(x)
     *
     * @param self  self reference
     * @param x     argument
     *
     * @return asin of argument
     */
    @Function(attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static double asin(final Object self, final Object x) {
        return Math.asin(JSType.toNumber(x));
    }

    /**
     * ECMA 15.8.2.3 asin(x) - specialization for double values
     *
     * @param self  self reference
     * @param x     argument
     *
     * @return asin of argument
     */
    @SpecializedFunction
    public static double asin(final Object self, final double x) {
        return Math.asin(x);
    }

    /**
     * ES6 20.2.2.5 asinh(x)
     *
     * @param self  self reference
     * @param x     argument
     *
     * @return inverse hyperbolic sine of x
     */
    @Function(attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static double asinh(final Object self, final Object x) {
        final double d = JSType.toNumber(x);

        // NaN, +/-Infinity and +/-0 come back unchanged, which is what keeps asinh(-0) === -0
        if (Double.isNaN(d) || Double.isInfinite(d) || d == 0.0) {
            return d;
        }

        // fdlibm's __ieee754_asinh; java.lang.Math has no asinh. asinh is odd, so the sign is
        // taken out first and the magnitude is split the same way acosh is - the plain
        // log(|x| + sqrt(x*x + 1)) overflows for large |x| and cancels for small |x|.
        final double a = Math.abs(d);
        final double r;
        if (a >= 0x1p28) {
            r = Math.log(a) + LN2;
        } else if (a > 2.0) {
            r = Math.log(2.0 * a + 1.0 / (Math.sqrt(a * a + 1.0) + a));
        } else {
            final double t = a * a;
            r = Math.log1p(a + t / (1.0 + Math.sqrt(1.0 + t)));
        }
        return d < 0 ? -r : r;
    }

    /**
     * ECMA 15.8.2.4 atan(x)
     *
     * @param self  self reference
     * @param x     argument
     *
     * @return atan of argument
     */
    @Function(attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static double atan(final Object self, final Object x) {
        return Math.atan(JSType.toNumber(x));
    }

    /**
     * ECMA 15.8.2.4 atan(x) - specialization for double values
     *
     * @param self  self reference
     * @param x     argument
     *
     * @return atan of argument
     */
    @SpecializedFunction
    public static double atan(final Object self, final double x) {
        return Math.atan(x);
    }

    /**
     * ECMA 15.8.2.5 atan2(x,y)
     *
     * @param self  self reference
     * @param x     first argument
     * @param y     second argument
     *
     * @return atan2 of x and y
     */
    @Function(attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static double atan2(final Object self, final Object y, final Object x) {
        return Math.atan2(JSType.toNumber(y), JSType.toNumber(x));
    }

    /**
     * ECMA 15.8.2.5 atan2(x,y) - specialization for double values
     *
     * @param self  self reference
     * @param x     first argument
     * @param y     second argument
     *
     * @return atan2 of x and y
     */
    @SpecializedFunction
    public static double atan2(final Object self, final double y, final double x) {
        return Math.atan2(y, x);
    }

    /**
     * ES6 20.2.2.7 atanh(x)
     *
     * @param self  self reference
     * @param x     argument
     *
     * @return inverse hyperbolic tangent of x
     */
    @Function(attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static double atanh(final Object self, final Object x) {
        final double d = JSType.toNumber(x);
        final double a = Math.abs(d);

        if (Double.isNaN(d) || a > 1.0) {
            return Double.NaN;
        }
        if (d == 0.0) {
            return d; // +/-0 unchanged
        }
        if (a == 1.0) {
            return d > 0 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
        }

        // fdlibm's __ieee754_atanh; java.lang.Math has no atanh. Written as
        // 0.5 * log((1 + x) / (1 - x)) the quotient rounds to 1 for small |x| and the result
        // collapses to 0, so the deviation from 1 is handed to log1p instead.
        final double r;
        if (a < 0.5) {
            final double t = a + a;
            r = 0.5 * Math.log1p(t + t * a / (1.0 - a));
        } else {
            r = 0.5 * Math.log1p((a + a) / (1.0 - a));
        }
        return d < 0 ? -r : r;
    }

    /**
     * ES6 20.2.2.9 cbrt(x)
     *
     * @param self  self reference
     * @param x     argument
     *
     * @return cube root of x
     */
    @Function(attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static double cbrt(final Object self, final Object x) {
        return Math.cbrt(JSType.toNumber(x));
    }

    /**
     * ECMA 15.8.2.6 ceil(x)
     *
     * @param self  self reference
     * @param x     argument
     *
     * @return ceil of argument
     */
    @Function(attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static double ceil(final Object self, final Object x) {
        return Math.ceil(JSType.toNumber(x));
    }

    /**
     * ECMA 15.8.2.6 ceil(x) - specialized version for ints
     *
     * @param self  self reference
     * @param x     argument
     *
     * @return ceil of argument
     */
    @SpecializedFunction
    public static int ceil(final Object self, final int x) {
        return x;
    }

    /**
     * ECMA 15.8.2.6 ceil(x) - specialized version for longs
     *
     * @param self  self reference
     * @param x     argument
     *
     * @return ceil of argument
     */
    @SpecializedFunction
    public static long ceil(final Object self, final long x) {
        return x;
    }

    /**
     * ECMA 15.8.2.6 ceil(x) - specialized version for doubles
     *
     * @param self  self reference
     * @param x     argument
     *
     * @return ceil of argument
     */
    @SpecializedFunction
    public static double ceil(final Object self, final double x) {
        return Math.ceil(x);
    }

    /**
     * ES6 20.2.2.11 clz32(x)
     *
     * @param self  self reference
     * @param x     argument
     *
     * @return number of leading zero bits in the ToUint32 value of x
     */
    @Function(attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static int clz32(final Object self, final Object x) {
        // ToUint32 is a long here only so it can hold values above Integer.MAX_VALUE; the 32 bits
        // it carries are the ones clz32 is defined over, and zero yields 32.
        return Integer.numberOfLeadingZeros((int) JSType.toUint32(x));
    }

    /**
     * ECMA 15.8.2.7 cos(x)
     *
     * @param self  self reference
     * @param x     argument
     *
     * @return cos of argument
     */
    @Function(attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static double cos(final Object self, final Object x) {
        return Math.cos(JSType.toNumber(x));
    }

    /**
     * ECMA 15.8.2.7 cos(x) - specialized version for doubles
     *
     * @param self  self reference
     * @param x     argument
     *
     * @return cos of argument
     */
    @SpecializedFunction
    public static double cos(final Object self, final double x) {
        return Math.cos(x);
    }

    /**
     * ES6 20.2.2.13 cosh(x)
     *
     * @param self  self reference
     * @param x     argument
     *
     * @return hyperbolic cosine of x
     */
    @Function(attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static double cosh(final Object self, final Object x) {
        return Math.cosh(JSType.toNumber(x));
    }

    /**
     * ECMA 15.8.2.8 exp(x)
     *
     * @param self  self reference
     * @param x     argument
     *
     * @return exp of argument
     */
    @Function(attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static double exp(final Object self, final Object x) {
        return Math.exp(JSType.toNumber(x));
    }

    /**
     * ES6 20.2.2.15 expm1(x)
     *
     * @param self  self reference
     * @param x     argument
     *
     * @return e raised to the power of x, minus 1
     */
    @Function(attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static double expm1(final Object self, final Object x) {
        return Math.expm1(JSType.toNumber(x));
    }

    /**
     * ECMA 15.8.2.9 floor(x)
     *
     * @param self  self reference
     * @param x     argument
     *
     * @return floor of argument
     */
    @Function(attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static double floor(final Object self, final Object x) {
        return Math.floor(JSType.toNumber(x));
    }

    /**
     * ECMA 15.8.2.9 floor(x) - specialized version for ints
     *
     * @param self  self reference
     * @param x     argument
     *
     * @return floor of argument
     */
    @SpecializedFunction
    public static int floor(final Object self, final int x) {
        return x;
    }

    /**
     * ECMA 15.8.2.9 floor(x) - specialized version for longs
     *
     * @param self  self reference
     * @param x     argument
     *
     * @return floor of argument
     */
    @SpecializedFunction
    public static long floor(final Object self, final long x) {
        return x;
    }

    /**
     * ECMA 15.8.2.9 floor(x) - specialized version for doubles
     *
     * @param self  self reference
     * @param x     argument
     *
     * @return floor of argument
     */
    @SpecializedFunction
    public static double floor(final Object self, final double x) {
        return Math.floor(x);
    }

    /**
     * ES6 20.2.2.16 fround(x)
     *
     * @param self  self reference
     * @param x     argument
     *
     * @return x rounded to the nearest 32 bit float, widened back to a double
     */
    @Function(attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static double fround(final Object self, final Object x) {
        // Java's double -> float narrowing is IEEE 754 round-to-nearest-even, which is exactly what
        // the spec asks for, and it carries NaN, +/-Infinity and +/-0 through unchanged. A finite
        // value larger than Float.MAX_VALUE becoming +/-Infinity is likewise the specified result.
        return (double) (float) JSType.toNumber(x);
    }

    /**
     * ES6 20.2.2.18 hypot(value1, value2, ...values)
     *
     * @param self  self reference
     * @param args  arguments
     *
     * @return the square root of the sum of the squares of the arguments
     */
    @Function(arity = 2, attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static double hypot(final Object self, final Object... args) {
        // Every argument is coerced, even once an Infinity has been seen, because ToNumber is
        // observable through valueOf.
        final double[] values = new double[args.length];
        double max = 0.0;
        boolean hasNaN = false;
        boolean hasInfinity = false;

        for (int i = 0; i < args.length; i++) {
            final double d = Math.abs(JSType.toNumber(args[i]));
            values[i] = d;
            if (Double.isInfinite(d)) {
                hasInfinity = true;
            } else if (Double.isNaN(d)) {
                hasNaN = true;
            } else if (d > max) {
                max = d;
            }
        }

        // An infinite argument outranks a NaN one - that ordering is spelled out in the spec
        if (hasInfinity) {
            return Double.POSITIVE_INFINITY;
        }
        if (hasNaN) {
            return Double.NaN;
        }
        if (max == 0.0) {
            return 0.0; // no arguments at all, or every argument is +/-0
        }

        // Scaling by the largest magnitude keeps the sum of squares away from both ends of the
        // exponent range; sqrt(x*x + y*y) written out literally reports Infinity for
        // hypot(1e200, 1e200) and 0 for hypot(1e-200, 1e-200).
        double sum = 0.0;
        for (final double d : values) {
            final double r = d / max;
            sum += r * r;
        }
        return max * Math.sqrt(sum);
    }

    /**
     * ES6 20.2.2.19 imul(x, y)
     *
     * @param self  self reference
     * @param x     first argument
     * @param y     second argument
     *
     * @return the C-like 32 bit multiplication of x and y
     */
    @Function(attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static int imul(final Object self, final Object x, final Object y) {
        // int multiplication in Java already wraps modulo 2^32, which is the whole point of imul
        return JSType.toInt32(x) * JSType.toInt32(y);
    }

    /**
     * ECMA 15.8.2.10 log(x)
     *
     * @param self  self reference
     * @param x     argument
     *
     * @return log of argument
     */
    @Function(attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static double log(final Object self, final Object x) {
        return Math.log(JSType.toNumber(x));
    }

    /**
     * ECMA 15.8.2.10 log(x) - specialized version for doubles
     *
     * @param self  self reference
     * @param x     argument
     *
     * @return log of argument
     */
    @SpecializedFunction
    public static double log(final Object self, final double x) {
        return Math.log(x);
    }

    /**
     * ES6 20.2.2.21 log10(x)
     *
     * @param self  self reference
     * @param x     argument
     *
     * @return base 10 logarithm of x
     */
    @Function(attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static double log10(final Object self, final Object x) {
        return Math.log10(JSType.toNumber(x));
    }

    /**
     * ES6 20.2.2.20 log1p(x)
     *
     * @param self  self reference
     * @param x     argument
     *
     * @return natural logarithm of 1 + x
     */
    @Function(attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static double log1p(final Object self, final Object x) {
        return Math.log1p(JSType.toNumber(x));
    }

    /**
     * ES6 20.2.2.22 log2(x)
     *
     * @param self  self reference
     * @param x     argument
     *
     * @return base 2 logarithm of x
     */
    @Function(attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static double log2(final Object self, final Object x) {
        final double d = JSType.toNumber(x);

        if (Double.isNaN(d) || d < 0.0) {
            return Double.NaN;
        }
        if (d == 0.0) {
            return Double.NEGATIVE_INFINITY; // +0 and -0 alike
        }
        if (Double.isInfinite(d)) {
            return d;
        }

        // java.lang.Math has no log2. Dividing log(x) by LN2 misses the exact answer for 441 of
        // the 2098 exact powers of two (log2(2^-1006) comes out -1006.0000000000001), so the
        // exponent is split off first: for d = m * 2^e with m in [1, 2) the term log(m) / LN2
        // vanishes whenever d is a power of two and e is returned untouched.
        double v = d;
        int bias = 0;
        if (v < Double.MIN_NORMAL) {
            // Subnormals carry no implicit leading bit, so getExponent would not describe them;
            // scale into the normal range and take the scaling back out of the exponent.
            v *= 0x1p54;
            bias = -54;
        }
        final int e = Math.getExponent(v);
        return (e + bias) + Math.log(Math.scalb(v, -e)) / LN2;
    }

    /**
     * ECMA 15.8.2.11 max(x)
     *
     * @param self  self reference
     * @param args  arguments
     *
     * @return the largest of the arguments, {@link Double#NEGATIVE_INFINITY} if no args given, or identity if one arg is given
     */
    @Function(arity = 2, attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static double max(final Object self, final Object... args) {
        switch (args.length) {
        case 0:
            return Double.NEGATIVE_INFINITY;
        case 1:
            return JSType.toNumber(args[0]);
        default:
            double res = JSType.toNumber(args[0]);
            for (int i = 1; i < args.length; i++) {
                res = Math.max(res, JSType.toNumber(args[i]));
            }
            return res;
        }
    }

    /**
     * ECMA 15.8.2.11 max(x) - specialized no args version
     *
     * @param self  self reference
     *
     * @return {@link Double#NEGATIVE_INFINITY}
     */
    @SpecializedFunction
    public static double max(final Object self) {
        return Double.NEGATIVE_INFINITY;
    }

    /**
     * ECMA 15.8.2.11 max(x) - specialized version for ints
     *
     * @param self  self reference
     * @param x     first argument
     * @param y     second argument
     *
     * @return largest value of x and y
     */
    @SpecializedFunction
    public static int max(final Object self, final int x, final int y) {
        return Math.max(x, y);
    }

    /**
     * ECMA 15.8.2.11 max(x) - specialized version for longs
     *
     * @param self  self reference
     * @param x     first argument
     * @param y     second argument
     *
     * @return largest value of x and y
     */
    @SpecializedFunction
    public static long max(final Object self, final long x, final long y) {
        return Math.max(x, y);
    }

    /**
     * ECMA 15.8.2.11 max(x) - specialized version for doubles
     *
     * @param self  self reference
     * @param x     first argument
     * @param y     second argument
     *
     * @return largest value of x and y
     */
    @SpecializedFunction
    public static double max(final Object self, final double x, final double y) {
        return Math.max(x, y);
    }

    /**
     * ECMA 15.8.2.11 max(x) - specialized version for two Object args
     *
     * @param self  self reference
     * @param x     first argument
     * @param y     second argument
     *
     * @return largest value of x and y
     */
    @SpecializedFunction
    public static double max(final Object self, final Object x, final Object y) {
        return Math.max(JSType.toNumber(x), JSType.toNumber(y));
    }

    /**
     * ECMA 15.8.2.12 min(x)
     *
     * @param self  self reference
     * @param args  arguments
     *
     * @return the smallest of the arguments, {@link Double#NEGATIVE_INFINITY} if no args given, or identity if one arg is given
     */
    @Function(arity = 2, attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static double min(final Object self, final Object... args) {
        switch (args.length) {
        case 0:
            return Double.POSITIVE_INFINITY;
        case 1:
            return JSType.toNumber(args[0]);
        default:
            double res = JSType.toNumber(args[0]);
            for (int i = 1; i < args.length; i++) {
                res = Math.min(res, JSType.toNumber(args[i]));
            }
            return res;
        }
    }

    /**
     * ECMA 15.8.2.11 min(x) - specialized no args version
     *
     * @param self  self reference
     *
     * @return {@link Double#POSITIVE_INFINITY}
     */
    @SpecializedFunction
    public static double min(final Object self) {
        return Double.POSITIVE_INFINITY;
    }

    /**
     * ECMA 15.8.2.12 min(x) - specialized version for ints
     *
     * @param self  self reference
     * @param x     first argument
     * @param y     second argument
     *
     * @return smallest value of x and y
     */
    @SpecializedFunction
    public static int min(final Object self, final int x, final int y) {
        return Math.min(x, y);
    }

    /**
     * ECMA 15.8.2.12 min(x) - specialized version for longs
     *
     * @param self  self reference
     * @param x     first argument
     * @param y     second argument
     *
     * @return smallest value of x and y
     */
    @SpecializedFunction
    public static long min(final Object self, final long x, final long y) {
        return Math.min(x, y);
    }

    /**
     * ECMA 15.8.2.12 min(x) - specialized version for doubles
     *
     * @param self  self reference
     * @param x     first argument
     * @param y     second argument
     *
     * @return smallest value of x and y
     */
    @SpecializedFunction
    public static double min(final Object self, final double x, final double y) {
        return Math.min(x, y);
    }

    /**
     * ECMA 15.8.2.12 min(x) - specialized version for two Object args
     *
     * @param self  self reference
     * @param x     first argument
     * @param y     second argument
     *
     * @return smallest value of x and y
     */
    @SpecializedFunction
    public static double min(final Object self, final Object x, final Object y) {
        return Math.min(JSType.toNumber(x), JSType.toNumber(y));
    }

    /**
     * ECMA 15.8.2.13 pow(x,y)
     *
     * @param self  self reference
     * @param x     first argument
     * @param y     second argument
     *
     * @return x raised to the power of y
     */
    @Function(attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static double pow(final Object self, final Object x, final Object y) {
        return Math.pow(JSType.toNumber(x), JSType.toNumber(y));
    }

    /**
     * ECMA 15.8.2.13 pow(x,y) - specialized version for doubles
     *
     * @param self  self reference
     * @param x     first argument
     * @param y     second argument
     *
     * @return x raised to the power of y
     */
    @SpecializedFunction
    public static double pow(final Object self, final double x, final double y) {
        return Math.pow(x, y);
    }

    /**
     * ECMA 15.8.2.14 random()
     *
     * @param self  self reference
     *
     * @return random number in the range [0..1)
     */
    @Function(attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static double random(final Object self) {
        return Math.random();
    }

    /**
     * ECMA 15.8.2.15 round(x)
     *
     * @param self  self reference
     * @param x     argument
     *
     * @return x rounded
     */
    @Function(attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static double round(final Object self, final Object x) {
        final double d = JSType.toNumber(x);
        if (Math.getExponent(d) >= 52) {
            return d;
        }
        return Math.copySign(Math.floor(d + 0.5), d);
    }

    /**
     * ES6 20.2.2.29 sign(x)
     *
     * @param self  self reference
     * @param x     argument
     *
     * @return the sign of x as 1, -1 or x itself
     */
    @Function(attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static double sign(final Object self, final Object x) {
        // Math.signum returns its argument for NaN and for both zeros, so sign(-0) stays -0
        return Math.signum(JSType.toNumber(x));
    }

    /**
     * ECMA 15.8.2.16 sin(x)
     *
     * @param self  self reference
     * @param x     argument
     *
     * @return sin of x
     */
    @Function(attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static double sin(final Object self, final Object x) {
        return Math.sin(JSType.toNumber(x));
    }

    /**
     * ECMA 15.8.2.16 sin(x) - specialized version for doubles
     *
     * @param self  self reference
     * @param x     argument
     *
     * @return sin of x
     */
    @SpecializedFunction
    public static double sin(final Object self, final double x) {
        return Math.sin(x);
    }

    /**
     * ES6 20.2.2.31 sinh(x)
     *
     * @param self  self reference
     * @param x     argument
     *
     * @return hyperbolic sine of x
     */
    @Function(attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static double sinh(final Object self, final Object x) {
        return Math.sinh(JSType.toNumber(x));
    }

    /**
     * ECMA 15.8.2.17 sqrt(x)
     *
     * @param self  self reference
     * @param x     argument
     *
     * @return sqrt of x
     */
    @Function(attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static double sqrt(final Object self, final Object x) {
        return Math.sqrt(JSType.toNumber(x));
    }

    /**
     * ECMA 15.8.2.17 sqrt(x) - specialized version for doubles
     *
     * @param self  self reference
     * @param x     argument
     *
     * @return sqrt of x
     */
    @SpecializedFunction
    public static double sqrt(final Object self, final double x) {
        return Math.sqrt(x);
    }

    /**
     * ECMA 15.8.2.18 tan(x)
     *
     * @param self  self reference
     * @param x     argument
     *
     * @return tan of x
     */
    @Function(attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static double tan(final Object self, final Object x) {
        return Math.tan(JSType.toNumber(x));
    }

    /**
     * ECMA 15.8.2.18 tan(x) - specialized version for doubles
     *
     * @param self  self reference
     * @param x     argument
     *
     * @return tan of x
     */
    @SpecializedFunction
    public static double tan(final Object self, final double x) {
        return Math.tan(x);
    }

    /**
     * ES6 20.2.2.34 tanh(x)
     *
     * @param self  self reference
     * @param x     argument
     *
     * @return hyperbolic tangent of x
     */
    @Function(attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static double tanh(final Object self, final Object x) {
        return Math.tanh(JSType.toNumber(x));
    }

    /**
     * ES6 20.2.2.35 trunc(x)
     *
     * @param self  self reference
     * @param x     argument
     *
     * @return the integral part of x, with the fractional digits removed
     */
    @Function(attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static double trunc(final Object self, final Object x) {
        final double d = JSType.toNumber(x);
        // ceil for negatives and floor for positives round towards zero without going through an
        // integral type, so values beyond the long range survive and trunc(-0.3) stays -0 (ceil
        // returns negative zero for anything in (-1, 0]). Both leave NaN and the infinities alone.
        return d < 0 ? Math.ceil(d) : Math.floor(d);
    }
}
