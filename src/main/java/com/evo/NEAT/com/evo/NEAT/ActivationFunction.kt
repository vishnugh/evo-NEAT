package com.evo.NEAT.com.evo.NEAT

import kotlin.math.exp
import kotlin.math.pow

/*
 * Copyright (C) 2004  Derek James and Philip Tucker
 *
 * This file is part of ANJI (Another NEAT Java Implementation).
 *
 * ANJI is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Created on Feb 26, 2004 by Philip Tucker
 */
/**
 * Abstracts activation function for neurons.
 *
 * @author Philip Tucker
 */
enum class ActivationFunction {

    /**
     * Absolute activation function.
     *
     * @author Oliver Coleman
     */
    AbsoluteActivationFunction /*implements ActivationFunction, DifferentiableFunction*/ {
        /**
         * identifying string
         */


        /**
         * Return absolute value of `input`, clamped to range [0, 1].
         *
         * @see com.anji.nn.activationfunction.ActivationFunction.apply
         */
        override fun apply(input: Double): Double = Math.abs(input)
        override fun applyDiff(x: Double) =
            /* If x == 0 this is not correct but is probably really rare*/
            if (x < 0.0) -1.0 else 1.0

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.getMaxValue
         */
        override fun getMaxValue(): Double = Double.MAX_VALUE.toDouble()

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.getMinValue
         */
        override fun getMinValue(): Double = 0.0

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.cost
         */
        override fun cost(): Long = 42
    },

    /**
     * Cosine activation function.
     *
     * @author Philip Tucker
     */
    CosineActivationFunction /*implements ActivationFunction, DifferentiableFunction*/ {

        /**
         * Returns cosine(input).
         */
        override fun apply(input: Double): Double = Math.cos(input)
        override fun applyDiff(x: Double): Double = -Math.sin(x)

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.getMaxValue
         */
        override fun getMaxValue(): Double = 1.0

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.getMinValue
         */
        override fun getMinValue(): Double {
            return -1.0
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.cost
         */
        override fun cost(): Long {
            return 42
        }
    },

    /**
     * Divide activation function (divides first input by second input).
     *
     * @author Oliver Coleman
     */
    DivideActivationFunction /*implements ActivationFunction, ActivationFunctionNonIntegrating*/ {
        /**
         * identifying string
         */


        /**
         * Not used, returns 0.
         */
        override fun apply(input: Double): Double {
            return 0.0
        }

        /**
         * Return first input divided by second input (or just first input if no
         * second input).Output is capped to +/- Double.MAX_VALUE
         * @param input
         * @param bias
         * @return
         */
        override fun apply(input: DoubleArray, bias: Double): Double {
            if (input.size > 0) {
                if (input.size < 2) {
                    return input[0]
                }
                val v = input[0] / input[1]
                if (java.lang.Double.isNaN(v) || java.lang.Double.isInfinite(v)) {
                    val pos = Math.signum(input[0]) == Math.signum(input[1])
                    return (if (pos) Double.MAX_VALUE.toDouble() else -Double.MAX_VALUE.toDouble())
                }
                return Math.max(-Double.MAX_VALUE.toDouble(), Math.min(Double.MAX_VALUE.toDouble(), v))
            }
            return 0.0
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.getMaxValue
         */
        override fun getMaxValue(): Double {
            return Double.MAX_VALUE.toDouble()
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.getMinValue
         */
        override fun getMinValue(): Double {
            return 0.0
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.cost
         */
        override fun cost(): Long {
            return 42
        }
    },

    /**
     * Modified classic sigmoid. Submitted to NEAT group by zenguyuno@yahoo.com from
     * EvSail ANN package.
     *
     * @author Philip Tucker
     */
    EvSailSigmoidActivationFunction /*implements ActivationFunction, DifferentiableFunction*/ {
        private val SEP = 0.3
        private val DENOMINATOR = 2 * SEP * SEP

        /**
         * identifying string
         */


        /**
         * This class should only be accessd via ActivationFunctionFactory.
         */
        //		EvSailSigmoidActivationFunction() {
        //		// no-op
        //		}
        /**
         * Approximation of classic sigmoid.
         *
         * @see com.anji.nn.activationfunction.ActivationFunction.apply
         */
        override fun apply(input: Double): Double {
            return if (input <= -SEP) {
                0.0
            } else if (input <= 0) {
                val tmp = input + SEP
                tmp * tmp / DENOMINATOR
            } else if (input < SEP) {
                val tmp = input - SEP
                1 - tmp * tmp / DENOMINATOR
            } else {
                1.0
            }
        }

        override fun applyDiff(x: Double): Double {
            return if (x <= -SEP) {
                0.0
            } else if (x <= 0) {
                2 * (SEP + x) / DENOMINATOR
            } else if (x < SEP) {
                -(2 * (-SEP + x)) / DENOMINATOR
            } else {
                0.0
            }
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.getMaxValue
         */
        override fun getMaxValue(): Double {
            return 1.0
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.getMinValue
         */
        override fun getMinValue(): Double {
            return 0.0
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.cost
         */
        override fun cost(): Long {
            return 166
        }
    },

    /**
     * @author cLins
     */
    ExponentialLinearFunction /*implements ActivationFunction, DifferentiableFunction
             */ {
        /**
         * identifying string
         */

        private val alpha = 1.0


        override fun apply(input: Double): Double {
            return if (input < 0) {
                alpha * (Math.exp(input) - 1)
            } else {
                input
            }
        }

        override fun applyDiff(x: Double): Double {
            return if (x < 0) {
                alpha * Math.exp(x)
            } else {
                1.0
            }
        }

        override fun getMaxValue(): Double {
            return Double.MAX_VALUE
        }

        override fun getMinValue(): Double {
            return -alpha
        }

        override fun cost(): Long {
            return 42
        }
    },

    /**
     * Gaussian activation function.
     *
     * @author Oliver Coleman
     */
    GaussianActivationFunction /*implements ActivationFunction, DifferentiableFunction*/ {
        private val SLOPE = 1.0

        /**
         * identifying string
         */


        /**
         * This class should only be accessd via ActivationFunctionFactory.
         */
        //        GaussianActivationFunction() {
        //            // no-op
        //        }
        /**
         * Return `input` with Gaussian function transformation.
         *
         * @see com.anji.nn.activationfunction.ActivationFunction.apply
         */
        override fun apply(input: Double): Double {
            return Math.exp(-(input * input * SLOPE))
        }

        override fun applyDiff(x: Double): Double {
            return -2 * Math.exp(-SLOPE * x * x) * SLOPE * x
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.getMaxValue
         */
        override fun getMaxValue(): Double {
            return 1.0
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.getMinValue
         */
        override fun getMinValue(): Double {
            return 0.0
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.cost
         */
        override fun cost(): Long {
            return 42
        }
    },

    /**
     * Inverse absolute value.
     *
     * @author Philip Tucker
     */
    InverseAbsActivationFunction /*implements ActivationFunction, DifferentiableFunction */ {
        private val SLOPE = 0.3

        /**
         * identifying string
         */


        /**
         * This class should only be accessd via ActivationFunctionFactory.
         */
        //        InverseAbsActivationFunction() {
        //            // no-op
        //        }
        /**
         * Inverse absolute value.
         *
         * @see com.anji.nn.activationfunction.ActivationFunction.apply
         */
        override fun apply(input: Double): Double {
            return 1 / (SLOPE * Math.abs(input) + 1)
        }

        override fun applyDiff(x: Double): Double {
            // As given by Wolfram Alpha
            return -(SLOPE * x) / (Math.abs(x) * Math.pow(1 + SLOPE * Math.abs(x), 2.0))
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.getMaxValue
         */
        override fun getMaxValue(): Double {
            return 1.0
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.getMinValue
         */
        override fun getMinValue(): Double {
            return 0.0
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.cost
         */
        override fun cost(): Long {
            return 75
        }
    },

    /**
     * Linear activation function.
     *
     * @author Philip Tucker
     */
    LinearActivationFunction /*implements ActivationFunction, DifferentiableFunction */ {
        /**
         * identifying string
         */


        /**
         * This class should only be accessd via ActivationFunctionFactory.
         */
        //        LinearActivationFunction() {
        //            // no-op
        //        }
        /**
         * Return `input` with no transformation.
         *
         * @see com.anji.nn.activationfunction.ActivationFunction.apply
         */
        override fun apply(input: Double): Double {
            return input
        }

        override fun applyDiff(input: Double): Double {
            return 1.0
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.getMaxValue
         */
        override fun getMaxValue(): Double {
            return Double.MAX_VALUE.toDouble()
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.getMinValue
         */
        override fun getMinValue(): Double {
            return (-Double.MAX_VALUE).toDouble()
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.cost
         */
        override fun cost(): Long {
            return 42
        }
    },
//
//    /**
//     * Logic AND activation function.
//     *
//     * @author Oliver Coleman
//     */
//    LogicAndActivationFunction /* extends;
//
//	LogicActivationFunction */ {
//        /**
//         * identifying string
//         */
//
//
//
//
//        /**
//         * Returns the result of a logical AND over all inputs, where an input value
//         * greater than or equal to 0.5 is considered logical true, and less than
//         * 0.5 false.
//         *
//         * @param input
//         * @param bias
//         * @return 1 or 0 depending on result of logic operation.
//         */
//        override fun apply(input: DoubleArray, bias: Double): Double {
//            var result = false
//            for (i in input.indices) {
//                result = result and (input[i] >= 0.5)
//            }
//            return  (if (result) 1.0 else 0.toDouble())
//        }
//
//        override fun getMaxValue(): Double {
//            return 1.0
//        }
//
//        override fun getMinValue(): Double {
//            return 0.0
//        }
//
//        override fun cost(): Long {
//            return 42
//        }
//    },

//    /**
//     * Logic OR activation function.
//     *
//     * @author Oliver Coleman
//     */
//    LogicOrActivationFunction /*extends LogicActivationFunction */ {
//        /**
//         * identifying string
//         */
//
//
//
//        /**
//         * This class should only be accessed via ActivationFunctionFactory.
//         */
//        //		LogicOrActivationFunction() {
//        //		}
//        /**
//         * Returns the result of a logical OR over all inputs, where an input value
//         * greater than or equal to 0.5 is considered logical true, and less than
//         * 0.5 false.
//         *
//         * @return 1 or 0 depending on result of logic operation.
//         */
//        override fun apply(input: DoubleArray, bias: Double): Double {
//            var result = false
//            for (i in input.indices) {
//                result = result or (input[i] >= 0.5)
//            }
//            return  (if (result) 1.0 else 0.toDouble())
//        }
//
//        override fun getMaxValue(): Double {
//            return 1.0
//        }
//
//        override fun getMinValue(): Double {
//            return 0.0
//        }
//
//        override fun cost(): Long {
//            return 42
//        }
//    },

//    /**
//     * Logic XOR activation function.
//     *
//     * @author Oliver Coleman
//     */
//    LogicXORActivationFunction /*extends LogicActivationFunction */ {
//        /**
//         * identifying string
//         */
//
//
//
//
//        override fun getMaxValue(): Double {
//            return 1.0
//        }
//
//        override fun getMinValue(): Double {
//            return 0.0
//        }
//
//        override fun cost(): Long {
//            return 42
//        }
//        /**
//         * This class should only be accessed via ActivationFunctionFactory.
//         */
//        //        LogicXORActivationFunction() {
//        //        }
//        /**
//         * Returns the result of a logical XOR over all inputs, where an input value
//         * greater than or equal to 0.5 is considered logical true, and less than
//         * 0.5 false.
//         *
//         * @return 1 or 0 depending on result of logic operation.
//         */
//        override fun apply(input: DoubleArray, bias: Double): Double {
//            var result = false
//            for (i in input.indices) {
//                result = result xor (input[i] >= 0.5)
//            }
//            return (if (result) 1.0 else 0.toDouble())
//        }
//    },

    /**
     * Multiply activation function.
     *
     * @author Oliver Coleman
     */
    /*   MultiplyActivationFunction *//*implements ActivationFunction, ActivationFunctionNonIntegrating*//* {
        */
    /**
     * identifying string
     *//*
        

        
        */
    /**
     * This class should only be accessd via ActivationFunctionFactory.
     *//*
        //		MultiplyActivationFunction() {
        //		 no-op
        //		}
        */
    /**
     * Not used, use [.apply] as this is a
     * non-integrating function.
     *//*
        override fun apply(input: Double): Double {
            return 0.0
        }

        */
    /**
     * Return result of inputs multiplied together.
     *//*
        override fun apply(input: DoubleArray, bias: Double): Double {
            if (input.size == 0) {
                return 0.0
            }
            var result = input[0]
            for (i in 1 until input.size) {
                result *= input[i]
            }
            return result
        }

        */
    /**
     * @see com.anji.nn.activationfunction.ActivationFunction.getMaxValue
     *//*
        override fun getMaxValue(): Double {
            return Double.MAX_VALUE
        }

        */
    /**
     * @see com.anji.nn.activationfunction.ActivationFunction.getMinValue
     *//*
        override fun getMinValue(): Double {
            return -Double.MAX_VALUE
        }

        */
    /**
     * @see com.anji.nn.activationfunction.ActivationFunction.cost
     *//*
        override fun cost(): Long {
            return 42
        }
    },
*/
    /**
     * Negative linear activation function.
     */
    NegatedLinearActivationFunction /*implements ActivationFunction, DifferentiableFunction*/ {
        /**
         * identifying string
         */


        /**
         * This class should only be accessd via ActivationFunctionFactory.
         */
        //		NegatedLinearActivationFunction() {
        //		// no-op
        //		}
        /**
         * Return `input` with opposite sign.
         *
         * @see com.anji.nn.activationfunction.ActivationFunction.apply
         */
        override fun apply(input: Double): Double {
            return -input
        }

        override fun applyDiff(x: Double): Double {
            return -1.0
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.getMaxValue
         */
        override fun getMaxValue(): Double {
            return Double.MAX_VALUE.toDouble()
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.getMinValue
         */
        override fun getMinValue(): Double {
            return (-Double.MAX_VALUE).toDouble()
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.cost
         */
        override fun cost(): Long {
            return 42
        }
    },

    /**
     * Square-root function.
     *
     * @author Oliver Coleman
     */
    PowerActivationFunction /*implements ActivationFunction, ActivationFunctionNonIntegrating*/ {
        /**
         * identifying string
         */


        /**
         * This class should only be accessd via ActivationFunctionFactory.
         */
        //		PowerActivationFunction() {
        //		// no-op
        //		}
        /**
         * Not used, returns 0.
         */
        override fun apply(input: Double): Double {
            return input*input
        }

        /**
         * Return first input raised to the power of the absolute value of the
         * second input (or just first input if no second input).
         */
        override fun apply(input: DoubleArray, bias: Double): Double {
            if (input.size < 2) {
                return input[0]
            }
            val v = Math.pow(input[0], Math.abs(input[1]))
            if (java.lang.Double.isNaN(v)) {
                return 0.0
            }
            return if (java.lang.Double.isInfinite(v)) {
                if (v < 0) -Double.MAX_VALUE / 2 else Double.MAX_VALUE / 2
            } else v
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.getMaxValue
         */
        override fun getMaxValue(): Double {
            return Double.MAX_VALUE
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.getMinValue
         */
        override fun getMinValue(): Double {
            return -Double.MAX_VALUE
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.cost
         */
        override fun cost(): Long {
            return 75
        }
    },

    /**
     * Reciprocal function (inverse).
     *
     * @author Oliver Coleman
     */
    RecipriocalActivationFunction /*implements ActivationFunction, DifferentiableFunction */ {
        /**
         * identifying string
         */


        /**
         * This class should only be accessd via ActivationFunctionFactory.
         */
        //		RecipriocalActivationFunction() {
        //		// no-op
        //		}
        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.apply
         */
        override fun apply(input: Double): Double {
            var `val` = 1 / input
            if (java.lang.Double.isNaN(`val`)) {
                return if (input < 0) getMinValue() else getMaxValue()
            }
            if (`val` < getMinValue()) {
                `val` = getMinValue()
            } else if (`val` > getMaxValue()) {
                `val` = getMaxValue()
            }
            return `val`
        }

        override fun applyDiff(x: Double): Double {
            val `val` = -1 / (x * x)
            return if (java.lang.Double.isNaN(`val`)) {
                getMinValue()
            } else `val`
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.getMaxValue
         */
        override fun getMaxValue(): Double {
            return Double.MAX_VALUE * 0.1
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.getMinValue
         */
        override fun getMinValue(): Double {
            return -getMaxValue()
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.cost
         */
        override fun cost(): Long {
            return 75
        }
    },  /*
     *   YAHNI Yet Another HyperNEAT Implementation
     *   Copyright (C) 2020  Christian Lins <christian@lins.me>
     *
     *   This program is free software: you can redistribute it and/or modify
     *   it under the terms of the GNU General Public License as published by
     *   the Free Software Foundation, either version 3 of the License, or
     *   (at your option) any later version.
     *
     *   This program is distributed in the hope that it will be useful,
     *   but WITHOUT ANY WARRANTY; without even the implied warranty of
     *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     *   GNU General Public License for more details.
     *
     *   You should have received a copy of the GNU General Public License
     *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
     */

    /**
     * Ramp activation function.
     *
     * @author Oliver Coleman
     * Edited by Christian Lins, 2020
     */
    RectifierActivationFunction /*implements ActivationFunction, DifferentiableFunction*/ {
        /**
         * identifying string
         */


        /**
         * This class should only be accessd via ActivationFunctionFactory.
         */
        //        RectifierActivationFunction() {
        //            // no-op
        //        }
        /**
         * Returns 0 if the input <= 0, otherwise the input value.
         *
         * @see com.anji.nn.activationfunction.ActivationFunction.apply
         */
        override fun apply(x: Double): Double {
            return Math.max(0.0, x)
        }

        override fun applyDiff(x: Double): Double {
            return if (x < 0) {
                0.0
            } else {
                1.0
            }
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.getMaxValue
         */
        override fun getMaxValue(): Double {
            return Double.MAX_VALUE.toDouble()
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.getMinValue
         */
        override fun getMinValue(): Double {
            return 0.0
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.cost
         */
        override fun cost(): Long {
            return 42
        }
    },
    LeakyRectifierActivationFunction /*implements ActivationFunction, DifferentiableFunction*/ {
        /**
         * identifying string
         */


        /**
         * This class should only be accessd via ActivationFunctionFactory.
         */
        //        RectifierActivationFunction() {
        //            // no-op
        //        }
        /**
         * Returns 0 if the input <= 0, otherwise the input value.
         *
         * @see com.anji.nn.activationfunction.ActivationFunction.apply
         */
        override fun apply(x: Double): Double {
            return Math.max(-0.01, x)
        }

        override fun applyDiff(x: Double): Double {
            return if (x < 0) {
                0.0
            } else {
                1.0
            }
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.getMaxValue
         */
        override fun getMaxValue(): Double {
            return Double.MAX_VALUE.toDouble()
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.getMinValue
         */
        override fun getMinValue(): Double {
            return 0.0
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.cost
         */
        override fun cost(): Long {
            return 42
        }
    },

    /**
     * Classic Sigmoid.
     *
     * @author Philip Tucker
     */
    SigmoidActivationFunction /*implements ActivationFunction, DifferentiableFunction*/ {
        /**
         * identifying string
         */


        /**
         * This class should only be accessed via ActivationFunctionFactory.
         */
        //        SigmoidActivationFunction() {
        //        }
        override fun apply(input: Double): Double = 1.0 / (1.0 + exp(-input))

        override fun applyDiff(input: Double): Double {
            val fn = apply(input)
            return fn * (1 - fn)
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.getMaxValue
         */
        override fun getMaxValue(): Double {
            return 1.0
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.getMinValue
         */
        override fun getMinValue(): Double {
            return 0.0
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.cost
         */
        override fun cost(): Long {
            return 497
        }
    },  /*
     * Copyright (C) 2004 Derek James and Philip Tucker
     *
     * This file is part of ANJI (Another NEAT Java Implementation).
     *
     * ANJI is free software; you can redistribute it and/or modify it under the terms of the GNU
     * General Public License as published by the Free Software Foundation; either version 2 of the
     * License, or (at your option) any later version.
     *
     * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
     * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
     * the GNU General Public License for more details.
     *
     * You should have received a copy of the GNU General Public License along with this program; if
     * not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
     * 02111-1307 USA
     *
     * Created on Aug 14, 2004 by Philip Tucker
     */

    /**
     * @author Philip Tucker
     */
    SignedClampedLinearActivationFunction /*implements ActivationFunction, DifferentiableFunction*/ {
        /**
         * id string
         */


        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.apply
         */
        override fun apply(input: Double): Double {
            return if (input <= -1.0) {
                -1.0
            } else if (input >= 1.0) {
                1.0
            } else {
                input
            }
        }

        override fun applyDiff(x: Double): Double {
            return if (x <= 1.0 || x >= 1.0) {
                0.0
            } else {
                1.0
            }
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.getMaxValue
         */
        override fun getMaxValue(): Double {
            return 1.0
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.getMinValue
         */
        override fun getMinValue(): Double {
            return -1.0
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.cost
         */
        override fun cost(): Long {
            return 42
        }
    },

    /**
     * Step activation function.
     *
     * @author Philip Tucker
     */
    SignedStepActivationFunction /*implements ActivationFunction, DifferentiableFunction*/ {
        /**
         * identifying string
         */


        /**
         * This class should only be accessd via ActivationFunctionFactory.
         */
        //        SignedStepActivationFunction() {
        //            // no-op
        //        }
        /**
         * @return -1 if `input`< 0, 1 otherwise @see com.an
         * ji.nn.activationfunction.ActivationFunction#apply(double)
         */
        override fun apply(input: Double): Double {
            return (if (input <= 0) -1.0 else 1.0)
        }

        override fun applyDiff(x: Double): Double {
            return 0.0
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.getMaxValue
         */
        override fun getMaxValue(): Double {
            return 1.0
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.getMinValue
         */
        override fun getMinValue(): Double {
            return -1.0
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.cost
         */
        override fun cost(): Long {
            return 40
        }
    },

    /**
     * Sine activation function.
     *
     * @author Philip Tucker
     */
    SineActivationFunction /*implements ActivationFunction, DifferentiableFunction*/ {
        /**
         * identifying string
         */


        /**
         * This class should only be accessd via ActivationFunctionFactory.
         */
        //        SineActivationFunction() {
        //            // no-op
        //        }
        /**
         * Returns sine(input).
         */
        override fun apply(input: Double): Double {
            return Math.sin(input)
        }

        override fun applyDiff(x: Double): Double {
            return Math.cos(x)
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.getMaxValue
         */
        override fun getMaxValue(): Double {
            return 1.0
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.getMinValue
         */
        override fun getMinValue(): Double {
            return -1.0
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.cost
         */
        override fun cost(): Long {
            return 42
        }
    },

    /**
     * Square-root function.
     *
     * @author Oliver Coleman
     */
    SqrtActivationFunction /*implements ActivationFunction, DifferentiableFunction*/ {
        /**
         * identifying string
         */


        /**
         * This class should only be accessd via ActivationFunctionFactory.
         */
        //        SqrtActivationFunction() {
        //            // no-op
        //        }
        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.apply
         */
        override fun apply(input: Double): Double {
            if (input > 0) {
                return Math.sqrt(input)
            }
            return if (input < 0) {
                -Math.sqrt(-input)
            } else 0.0
        }

        override fun applyDiff(x: Double): Double {
            return if (x > 0) {
                1 / 2 * Math.sqrt(x)
            } else if (x < 0) {
                1 / 2 * Math.sqrt(-x)
            } else 0.0
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.getMaxValue
         */
        override fun getMaxValue(): Double {
            return Double.MAX_VALUE
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.getMinValue
         */
        override fun getMinValue(): Double {
            return -Double.MAX_VALUE
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.cost
         */
        override fun cost(): Long {
            return 75
        }
    },

    /**
     * Square-root function for values with magnitude > 1, otherwise linear.
     *
     * @author Oliver Coleman
     */
    SqrtAndLinearActivationFunction /*implements ActivationFunction, DifferentiableFunction*/ {
        /**
         * identifying string
         */


        /**
         * This class should only be accessd via ActivationFunctionFactory.
         */
        //        SqrtAndLinearActivationFunction() {
        //            // no-op
        //        }
        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.apply
         */
        override fun apply(input: Double): Double {
            if (input >= -1 && input <= 1) {
                return input
            }
            return if (input > 0) {
                Math.sqrt(input)
            } else {
                -Math.sqrt(-input)
            }
        }

        override fun applyDiff(x: Double): Double {
            if (x >= -1 && x <= 1) {
                return 1.0
            }
            return if (x > 0) {
                1 / 2 * Math.sqrt(x)
            } else {
                1 / 2 * Math.sqrt(-x)
            }
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.getMaxValue
         */
        override fun getMaxValue(): Double {
            return Double.MAX_VALUE
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.getMinValue
         */
        override fun getMinValue(): Double {
            return -Double.MAX_VALUE
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.cost
         */
        override fun cost(): Long {
            return 75
        }
    },

    /**
     * Steepened Sigmoid with slope of 4.9.
     *
     * @author Philip Tucker
     */
    SteepSigmoidActivationFunction /*implements ActivationFunction, DifferentiableFunction*/ {
        private val SLOPE = 4.9

        /**
         * identifying string
         */


        /**
         * This class should only be accessed via ActivationFunctionFactory.
         */
        //        SteepSigmoidActivationFunction() {
        //        }
        override fun apply(input: Double): Double {
            return 1.0 / (1.0 + Math.exp(-(input * SLOPE)))
        }

        override fun applyDiff(x: Double): Double {
            return SLOPE * Math.exp(-SLOPE * x) / Math.pow(1 + Math.exp(-SLOPE * x), 2.0)
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.getMaxValue
         */
        override fun getMaxValue(): Double {
            return 1.0
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.getMinValue
         */
        override fun getMinValue(): Double {
            return 0.0
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.cost
         */
        override fun cost(): Long {
            return 497
        }
    },

    /**
     * Step activation function.
     *
     * @author Philip Tucker
     */
    StepActivationFunction /*implements ActivationFunction, DifferentiableFunction*/ {
        /**
         * identifying string
         */


        /**
         * This class should only be accessd via ActivationFunctionFactory.
         */
        //        StepActivationFunction() {
        //            // no-op
        //        }
        /**
         * @return 0. if `input`< 0, 1 otherwise @see com.an
         * ji.nn.activationfunction.ActivationFunction#apply(double)
         */
        override fun apply(input: Double): Double {
            return (if (input <= 0) 0.0 else 1.toDouble())
        }

        override fun applyDiff(x: Double): Double {
            return 0.0
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.getMaxValue
         */
        override fun getMaxValue(): Double {
            return 1.0
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.getMinValue
         */
        override fun getMinValue(): Double {
            return 0.0
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.cost
         */
        override fun cost(): Long {
            return 40
        }
    },

    /**
     * Hyperbolic tangent.
     *
     * @author Philip Tucker
     */
    TanhActivationFunction /*implements ActivationFunction, DifferentiableFunction*/ {
        /**
         * identifying string
         */


        /**
         * This class should only be accessd via ActivationFunctionFactory.
         */
        //        TanhActivationFunction() {
        //            // no-op
        //        }
        /**
         * Hyperbolic tangent.
         *
         * @param x
         * @see com.anji.nn.activationfunction.ActivationFunction.apply
         */
        override fun apply(x: Double): Double {
            return -1 + 2 / (1 + Math.exp(-2 * x))
        }

        override fun applyDiff(x: Double): Double {
            return 0.5 * Math.log((1 + x) / (1 - x))
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.getMaxValue
         */
        override fun getMaxValue(): Double {
            return 1.0
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.getMinValue
         */
        override fun getMinValue(): Double {
            return -1.0
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.cost
         */
        override fun cost(): Long {
            return 385
        }
    },

    /**
     * Hyperbolic tangent modified to have a "well" around 0. This can be used for
     * control neurons for which we would ilke the neural netowkr to be able easily
     * to rest at 0.
     *
     * @author Philip Tucker
     */
    TanhCubicActivationFunction /*implements ActivationFunction, DifferentiableFunction*/ {
        /**
         * identifying string
         */


        /**
         * This class should only be accessd via ActivationFunctionFactory.
         */
        //        TanhCubicActivationFunction() {
        //            // no-op
        //        }
        /**
         * Hyperbolic tangent of cubic.
         *
         * @param x
         * @see com.anji.nn.activationfunction.ActivationFunction.apply
         */
        override fun apply(x: Double): Double {
            return -1 + 2 / (1 + Math.exp(Math.pow(-x, 3.0)))
        }

        override fun applyDiff(x: Double): Double {
            // As given by Wolfram Alpha
            return 6 * Math.exp(-Math.pow(x, 3.0)) * Math.pow(x, 2.0) /
                    Math.pow(1 + Math.exp(Math.pow(-x, 3.0)), 2.0)
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.getMaxValue
         */
        override fun getMaxValue(): Double {
            return 1.0
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.getMinValue
         */
        override fun getMinValue(): Double {
            return -1.0
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.cost
         */
        override fun cost(): Long {
            return 1231
        }
    },

    /**
     * Modified classic sigmoid. Copied from
     * [JOONE](http://www.jooneworld.com/) `SigmoidLayer`.
     *
     * @author Philip Tucker
     */
    BipolarSigmoidActivationFunction /*implements ActivationFunction */ {
        private val SLOPE = 2.0

        /**
         * identifying string
         */


        /**
         * This class should only be accessd via ActivationFunctionFactory.
         */
        //        BipolarSigmoidActivationFunction() {
        //            // no-op
        //        }
        /**
         * Modified classic sigmoid.
         *
         * @see com.anji.nn.activationfunction.ActivationFunction.apply
         */
        override fun apply(input: Double): Double {
            return 2.0 / (1.0 + Math.exp(-(input * SLOPE))) - 1.0
        }

        override fun applyDiff(x: Double): Double {
            // As given by Wolfram Alpha
            return 2.0 * Math.exp(-SLOPE * x) * SLOPE / Math.pow(1 + Math.exp(-SLOPE * x), 2.0)
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.getMaxValue
         */
        override fun getMaxValue(): Double {
            return 1.0
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.getMinValue
         */
        override fun getMinValue(): Double {
            return -1.0
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.cost
         */
        override fun cost(): Long {
            return 497
        }
    },

    /**
     * Absolute activation function.
     *
     * @author Oliver Coleman
     */
    ClampedAbsoluteActivationFunction /*implements ActivationFunction, DifferentiableFunction*/ {
        /**
         * identifying string
         */


        /**
         * This class should only be accessd via ActivationFunctionFactory.
         */
        //        ClampedAbsoluteActivationFunction() {
        //            // no-op
        //        }
        /**
         * Return absolute value of `input`, clamped to range [0, 1].
         *
         * @see com.anji.nn.activationfunction.ActivationFunction.apply
         */
        override fun apply(input: Double): Double {
            return Math.min(Math.abs(input), 1.0)
        }

        override fun applyDiff(x: Double): Double {
            return if (x < 0) {
                if (x >= -1) {
                    -1.0
                } else {
                    0.0
                }
            } else {
                // we ignore x == 0 here
                if (x >= 1) {
                    1.0
                } else {
                    0.0
                }
            }
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.getMaxValue
         */
        override fun getMaxValue(): Double {
            return 1.0
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.getMinValue
         */
        override fun getMinValue(): Double {
            return 0.0
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.cost
         */
        override fun cost(): Long {
            return 42
        }
    },  /*
     * Copyright (C) 2004 Derek James and Philip Tucker
     *
     * This file is part of ANJI (Another NEAT Java Implementation).
     *
     * ANJI is free software; you can redistribute it and/or modify it under the terms of the GNU
     * General Public License as published by the Free Software Foundation; either version 2 of the
     * License, or (at your option) any later version.
     *
     * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
     * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
     * the GNU General Public License for more details.
     *
     * You should have received a copy of the GNU General Public License along with this program; if
     * not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
     * 02111-1307 USA
     *
     * Created on Aug 14, 2004 by Philip Tucker
     */

    /**
     * @author Philip Tucker
     */
    ClampedLinearActivationFunction /*implements ActivationFunction, DifferentiableFunction */ {
        /**
         * unique ID string
         */


        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.apply
         */
        override fun apply(input: Double): Double {
            return if (input <= 0) {
                0.0
            } else if (input >= 1) {
                1.0
            } else {
                input
            }
        }

        override fun applyDiff(x: Double): Double {
            return if (x <= 0 || x >= 1) {
                0.0
            } else {
                1.0
            }
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.getMaxValue
         */
        override fun getMaxValue(): Double {
            return 1.0
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.getMinValue
         */
        override fun getMinValue(): Double {
            return 0.0
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.cost
         */
        override fun cost(): Long {
            return 42
        }
    },

    /**
     * @author Oliver Coleman
     */
    ConvertToSignedActivationFunction /*implements ActivationFunction, DifferentiableFunction*/ {
        /**
         * unique ID string
         */


        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.apply
         */
        override fun apply(input: Double): Double {
            var input = input
            if (input <= 0) {
                input = 0.0
            } else if (input >= 1) {
                input = 1.0
            }
            return input * 2 - 1
        }

        override fun applyDiff(x: Double): Double {
            return if (x <= 0 || x >= 1) {
                0.0
            } else {
                2.0
            }
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.getMaxValue
         */
        override fun getMaxValue(): Double {
            return 1.0
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.getMinValue
         */
        override fun getMinValue(): Double {
            return -1.0
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.cost
         */
        override fun cost(): Long {
            return 42
        }
    },
    LogicActivationFunction /*extends ActivationFunction implements ActivationFunctionNonIntegrating */ {
        /**
         * Not used as this is a non-integrating function, returns 0.
         *
         * @see .apply
         */
        override fun apply(input: Double): Double {
            return 0.0
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.getMaxValue
         */
        override fun getMaxValue(): Double {
            return 1.0
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.getMinValue
         */
        override fun getMinValue(): Double {
            return 0.0
        }

        /**
         * @see com.anji.nn.activationfunction.ActivationFunction.cost
         */
        override fun cost(): Long {
            return 42
        }
    };

    /**
     * Apply activation function to input.
     *
     * @param input
     * @return double result of applying activation function to `input`
     */
    abstract fun apply(input: Double): Double

    open fun apply(input: DoubleArray, bias: Double): Double {
        TODO()
    }

    open fun applyDiff(x: Double): Double {
        TODO()
    }

    /**
     * @return ceiling value for this function
     */
    abstract fun getMaxValue(): Double

    /**
     * @return floor value for this function
     */
    abstract fun getMinValue(): Double

    /**
     * @return number corresponding to cost of activation in resources
     */
    abstract fun cost(): Long

}