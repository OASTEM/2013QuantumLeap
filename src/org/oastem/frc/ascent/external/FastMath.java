/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.oastem.frc.ascent.external;

import java.io.PrintStream;

/**
 * Faster, more accurate, portable alternative to {@link Math} and
 * {@link StrictMath} for large scale computation.
 * <p>
 * FastMath is a drop-in replacement for both Math and StrictMath. This
 * means that for any method in Math (say {@code Math.sin(x)} or
 * {@code Math.cbrt(y)}), user can directly change the class and use the
 * methods as is (using {@code FastMath.sin(x)} or {@code FastMath.cbrt(y)}
 * in the previous example).
 * </p>
 * <p>
 * FastMath speed is achieved by relying heavily on optimizing compilers
 * to native code present in many JVMs today and use of large tables.
 * The larger tables are lazily initialised on first use, so that the setup
 * time does not penalise methods that don't need them.
 * </p>
 * <p>
 * Note that FastMath is
 * extensively used inside Apache Commons Math, so by calling some algorithms,
 * the overhead when the the tables need to be intialised will occur
 * regardless of the end-user calling FastMath methods directly or not.
 * Performance figures for a specific JVM and hardware can be evaluated by
 * running the FastMathTestPerformance tests in the test directory of the source
 * distribution.
 * </p>
 * <p>
 * FastMath accuracy should be mostly independent of the JVM as it relies only
 * on IEEE-754 basic operations and on embedded tables. Almost all operations
 * are accurate to about 0.5 ulp throughout the domain range. This statement,
 * of course is only a rough global observed behavior, it is <em>not</em> a
 * guarantee for <em>every</em> double numbers input (see William Kahan's <a
 * href="http://en.wikipedia.org/wiki/Rounding#The_table-maker.27s_dilemma">Table
 * Maker's Dilemma</a>).
 * </p>
 * <p>
 * FastMath additionally implements the following methods not found in Math/StrictMath:
 * <ul>
 * <li>{@link #asinh(double)}</li>
 * <li>{@link #acosh(double)}</li>
 * <li>{@link #atanh(double)}</li>
 * </ul>
 * The following methods are found in Math/StrictMath since 1.6 only, they are provided
 * by FastMath even in 1.5 Java virtual machines
 * <ul>
 * <li>{@link #copySign(double, double)}</li>
 * <li>{@link #getExponent(double)}</li>
 * <li>{@link #nextAfter(double,double)}</li>
 * <li>{@link #nextUp(double)}</li>
 * <li>{@link #scalb(double, int)}</li>
 * <li>{@link #copySign(float, float)}</li>
 * <li>{@link #getExponent(float)}</li>
 * <li>{@link #nextAfter(float,double)}</li>
 * <li>{@link #nextUp(float)}</li>
 * <li>{@link #scalb(float, int)}</li>
 * </ul>
 * </p>
 * @version $Id$
 * @since 2.2
 */
public class FastMath {
    /** Archimede's constant PI, ratio of circle circumference to diameter. */
    public static final double PI = 105414357.0 / 33554432.0 + 1.984187159361080883e-9;

    /** Napier's constant e, base of the natural logarithm. */
    public static final double E = 2850325.0 / 1048576.0 + 8.254840070411028747e-8;

    /** Index of exp(0) in the array of integer exponentials. */
    static final int EXP_INT_TABLE_MAX_INDEX = 750;
    /** Length of the array of integer exponentials. */
    static final int EXP_INT_TABLE_LEN = EXP_INT_TABLE_MAX_INDEX * 2;
    /** Logarithm table length. */
    static final int LN_MANT_LEN = 1024;
    /** Exponential fractions table length. */
    static final int EXP_FRAC_TABLE_LEN = 1025; // 0, 1/1024, ... 1024/1024

    /** Indicator for tables initialization.
     * <p>
     * This compile-time constant should be set to true only if one explicitly
     * wants to compute the tables at class loading time instead of using the
     * already computed ones provided as literal arrays below.
     * </p>
     */
    private static final boolean RECOMPUTE_TABLES_AT_RUNTIME = false;

    /** Tangent table, used by atan() (high bits). */
    private static final double TANGENT_TABLE_A[] =
        {
        +0.0d,
        +0.1256551444530487d,
        +0.25534194707870483d,
        +0.3936265707015991d,
        +0.5463024377822876d,
        +0.7214844226837158d,
        +0.9315965175628662d,
        +1.1974215507507324d,
        +1.5574076175689697d,
        +2.092571258544922d,
        +3.0095696449279785d,
        +5.041914939880371d,
        +14.101419448852539d,
        -18.430862426757812d,
    };

    /** Tangent table, used by atan() (low bits). */
    private static final double TANGENT_TABLE_B[] =
        {
        +0.0d,
        -7.877917738262007E-9d,
        -2.5857668567479893E-8d,
        +5.2240336371356666E-9d,
        +5.206150291559893E-8d,
        +1.8307188599677033E-8d,
        -5.7618793749770706E-8d,
        +7.848361555046424E-8d,
        +1.0708593250394448E-7d,
        +1.7827257129423813E-8d,
        +2.893485277253286E-8d,
        +3.1660099222737955E-7d,
        +4.983191803254889E-7d,
        -3.356118100840571E-7d,
    };

    /** Eighths.
     * This is used by sinQ, because its faster to do a table lookup than
     * a multiply in this time-critical routine
     */
    private static final double EIGHTHS[] = {0, 0.125, 0.25, 0.375, 0.5, 0.625, 0.75, 0.875, 1.0, 1.125, 1.25, 1.375, 1.5, 1.625};

    /*
     *  There are 52 bits in the mantissa of a double.
     *  For additional precision, the code splits double numbers into two parts,
     *  by clearing the low order 30 bits if possible, and then performs the arithmetic
     *  on each half separately.
     */

    /**
     * 0x40000000 - used to split a double into two parts, both with the low order bits cleared.
     * Equivalent to 2^30.
     */
    private static final long HEX_40000000 = 0x40000000L; // 1073741824L

    /** Mask used to clear low order 30 bits */
    private static final long MASK_30BITS = -1L - (HEX_40000000 -1); // 0xFFFFFFFFC0000000L;

    /** 2^52 - double numbers this large must be integral (no fraction) or NaN or Infinite */
    private static final double TWO_POWER_52 = 4503599627370496.0;
    /** 2^53 - double numbers this large must be even. */
    private static final double TWO_POWER_53 = 2 * TWO_POWER_52;

    /** Constant: {@value}. */
    private static final double F_1_3 = 1d / 3d;
    /** Constant: {@value}. */
    private static final double F_1_5 = 1d / 5d;
    /** Constant: {@value}. */
    private static final double F_1_7 = 1d / 7d;
    /** Constant: {@value}. */
    private static final double F_1_9 = 1d / 9d;
    /** Constant: {@value}. */
    private static final double F_1_11 = 1d / 11d;
    /** Constant: {@value}. */
    private static final double F_1_13 = 1d / 13d;
    /** Constant: {@value}. */
    private static final double F_1_15 = 1d / 15d;
    /** Constant: {@value}. */
    private static final double F_1_17 = 1d / 17d;
    /** Constant: {@value}. */
    private static final double F_3_4 = 3d / 4d;
    /** Constant: {@value}. */
    private static final double F_15_16 = 15d / 16d;
    /** Constant: {@value}. */
    private static final double F_13_14 = 13d / 14d;
    /** Constant: {@value}. */
    private static final double F_11_12 = 11d / 12d;
    /** Constant: {@value}. */
    private static final double F_9_10 = 9d / 10d;
    /** Constant: {@value}. */
    private static final double F_7_8 = 7d / 8d;
    /** Constant: {@value}. */
    private static final double F_5_6 = 5d / 6d;
    /** Constant: {@value}. */
    private static final double F_1_2 = 1d / 2d;
    /** Constant: {@value}. */
    private static final double F_1_4 = 1d / 4d;

    /**
     * Private Constructor
     */
    private FastMath() {}
    
    /**
     * Returns the first argument with the sign of the second argument.
     * A NaN {@code sign} argument is treated as positive.
     *
     * @param magnitude the value to return
     * @param sign the sign for the returned value
     * @return the magnitude with the same sign as the {@code sign} argument
     */
    public static double copySign(double magnitude, double sign){
        long m = Double.doubleToLongBits(magnitude);
        long s = Double.doubleToLongBits(sign);
        if ((m >= 0 && s >= 0) || (m < 0 && s < 0)) { // Sign is currently OK
            return magnitude;
        }
        return -magnitude; // flip sign
    }

    /**
     * Returns the first argument with the sign of the second argument.
     * A NaN {@code sign} argument is treated as positive.
     *
     * @param magnitude the value to return
     * @param sign the sign for the returned value
     * @return the magnitude with the same sign as the {@code sign} argument
     */
    public static float copySign(float magnitude, float sign){
        int m = Float.floatToIntBits(magnitude);
        int s = Float.floatToIntBits(sign);
        if ((m >= 0 && s >= 0) || (m < 0 && s < 0)) { // Sign is currently OK
            return magnitude;
        }
        return -magnitude; // flip sign
    }

    /**
     * Arctangent function
     *  @param x a number
     *  @return atan(x)
     */
    public static double atan(double x) {
        return atan(x, 0.0, false);
    }

    /** Internal helper function to compute arctangent.
     * @param xa number from which arctangent is requested
     * @param xb extra bits for x (may be 0.0)
     * @param leftPlane if true, result angle must be put in the left half plane
     * @return atan(xa + xb) (or angle shifted by {@code PI} if leftPlane is true)
     */
    private static double atan(double xa, double xb, boolean leftPlane) {
        boolean negate = false;
        int idx;

        if (xa == 0.0) { // Matches +/- 0.0; return correct sign
            return leftPlane ? copySign(Math.PI, xa) : xa;
        }

        if (xa < 0) {
            // negative
            xa = -xa;
            xb = -xb;
            negate = true;
        }

        if (xa > 1.633123935319537E16) { // Very large input
            return (negate ^ leftPlane) ? (-Math.PI * F_1_2) : (Math.PI * F_1_2);
        }

        /* Estimate the closest tabulated arctan value, compute eps = xa-tangentTable */
        if (xa < 1) {
            idx = (int) (((-1.7168146928204136 * xa * xa + 8.0) * xa) + 0.5);
        } else {
            final double oneOverXa = 1 / xa;
            idx = (int) (-((-1.7168146928204136 * oneOverXa * oneOverXa + 8.0) * oneOverXa) + 13.07);
        }
        double epsA = xa - TANGENT_TABLE_A[idx];
        double epsB = -(epsA - xa + TANGENT_TABLE_A[idx]);
        epsB += xb - TANGENT_TABLE_B[idx];

        double temp = epsA + epsB;
        epsB = -(temp - epsA - epsB);
        epsA = temp;

        /* Compute eps = eps / (1.0 + xa*tangent) */
        temp = xa * HEX_40000000;
        double ya = xa + temp - temp;
        double yb = xb + xa - ya;
        xa = ya;
        xb += yb;

        //if (idx > 8 || idx == 0)
        if (idx == 0) {
            /* If the slope of the arctan is gentle enough (< 0.45), this approximation will suffice */
            //double denom = 1.0 / (1.0 + xa*tangentTableA[idx] + xb*tangentTableA[idx] + xa*tangentTableB[idx] + xb*tangentTableB[idx]);
            final double denom = 1d / (1d + (xa + xb) * (TANGENT_TABLE_A[idx] + TANGENT_TABLE_B[idx]));
            //double denom = 1.0 / (1.0 + xa*tangentTableA[idx]);
            ya = epsA * denom;
            yb = epsB * denom;
        } else {
            double temp2 = xa * TANGENT_TABLE_A[idx];
            double za = 1d + temp2;
            double zb = -(za - 1d - temp2);
            temp2 = xb * TANGENT_TABLE_A[idx] + xa * TANGENT_TABLE_B[idx];
            temp = za + temp2;
            zb += -(temp - za - temp2);
            za = temp;

            zb += xb * TANGENT_TABLE_B[idx];
            ya = epsA / za;

            temp = ya * HEX_40000000;
            final double yaa = (ya + temp) - temp;
            final double yab = ya - yaa;

            temp = za * HEX_40000000;
            final double zaa = (za + temp) - temp;
            final double zab = za - zaa;

            /* Correct for rounding in division */
            yb = (epsA - yaa * zaa - yaa * zab - yab * zaa - yab * zab) / za;

            yb += -epsA * zb / za / za;
            yb += epsB / za;
        }


        epsA = ya;
        epsB = yb;

        /* Evaluate polynomial */
        final double epsA2 = epsA * epsA;

        /*
    yb = -0.09001346640161823;
    yb = yb * epsA2 + 0.11110718400605211;
    yb = yb * epsA2 + -0.1428571349122913;
    yb = yb * epsA2 + 0.19999999999273194;
    yb = yb * epsA2 + -0.33333333333333093;
    yb = yb * epsA2 * epsA;
         */

        yb = 0.07490822288864472;
        yb = yb * epsA2 + -0.09088450866185192;
        yb = yb * epsA2 + 0.11111095942313305;
        yb = yb * epsA2 + -0.1428571423679182;
        yb = yb * epsA2 + 0.19999999999923582;
        yb = yb * epsA2 + -0.33333333333333287;
        yb = yb * epsA2 * epsA;


        ya = epsA;

        temp = ya + yb;
        yb = -(temp - ya - yb);
        ya = temp;

        /* Add in effect of epsB.   atan'(x) = 1/(1+x^2) */
        yb += epsB / (1d + epsA * epsA);

        //result = yb + eighths[idx] + ya;
        double za = EIGHTHS[idx] + ya;
        double zb = -(za - EIGHTHS[idx] - ya);
        temp = za + yb;
        zb += -(temp - za - yb);
        za = temp;

        double result = za + zb;
        double resultb = -(result - za - zb);

        if (leftPlane) {
            // Result is in the left plane
            final double pia = 1.5707963267948966 * 2;
            final double pib = 6.123233995736766E-17 * 2;

            za = pia - result;
            zb = -(za - pia + result);
            zb += pib - resultb;

            result = za + zb;
            resultb = -(result - za - zb);
        }


        if (negate ^ leftPlane) {
            result = -result;
        }

        return result;
    }
}