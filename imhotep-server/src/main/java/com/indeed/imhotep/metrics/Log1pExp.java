/*
 * Copyright (C) 2014 Indeed Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
 package com.indeed.imhotep.metrics;

import com.indeed.flamdex.api.IntValueLookup;

/**
 * log(1 + e^x)
 * @author jwolfe
 */
public class Log1pExp implements IntValueLookup {
    private final IntValueLookup operand;
    private final int scaleFactor;

    public Log1pExp(IntValueLookup operand, int scaleFactor) {
        this.operand = operand;
        this.scaleFactor = scaleFactor;
    }

    @Override
    public long getMin() {
        return Long.MIN_VALUE;
    }

    @Override
    public long getMax() {
        return Long.MAX_VALUE;
    }

    @Override
    public void lookup(int[] docIds, long[] values, int n) {
        operand.lookup(docIds, values, n);
        for (int i = 0; i < n; i++) {
            final double x = values[i] / (double) scaleFactor;
            final double result;
            if (x > 0) {
                // This is mathematically the same as log(1 + e^x):
                // log(1+e^x) = log(e^x * (e^(-x) + 1)) = log(e^x) + log(1 + e^-x) = x + log1p(e^-x)
                // Except it won't overflow the 64-bit floating point numbers with large values of x.
                result = x + Math.log1p(Math.exp(-x));
            } else {
                result = Math.log1p(Math.exp(x));
            }
            // the output is clamped to [Integer.MIN_VALUE, Integer.MAX_VALUE] (JLS §5.1.3)
            values[i] = (long) (result * scaleFactor);
        }
    }

    @Override
    public long memoryUsed() {
        return operand.memoryUsed();
    }

    @Override
    public void close() {
        operand.close();
    }
}
