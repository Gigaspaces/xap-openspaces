/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.core.util.numbers;

import java.math.BigDecimal;

/**
 * @author kimchy
 */
public class BigDecimalHelper implements NumberHelper<BigDecimal> {

    public int compare(Number lhs, Number rhs) {
        return cast(lhs).compareTo(cast(rhs));
    }

    public BigDecimal add(Number lhs, Number rhs) {
        BigDecimal lhsBigInt = cast(lhs);
        BigDecimal rhsBigInt = cast(rhs);
        return lhsBigInt.add(rhsBigInt);
    }

    public BigDecimal sub(Number lhs, Number rhs) {
        BigDecimal lhsBigInt = cast(lhs);
        BigDecimal rhsBigInt = cast(rhs);
        return lhsBigInt.subtract(rhsBigInt);
    }

    public BigDecimal mult(Number lhs, Number rhs) {
        BigDecimal lhsBigInt = cast(lhs);
        BigDecimal rhsBigInt = cast(rhs);
        return lhsBigInt.multiply(rhsBigInt);
    }

    public BigDecimal div(Number lhs, Number rhs) {
        BigDecimal lhsBigInt = cast(lhs);
        BigDecimal rhsBigInt = cast(rhs);
        return lhsBigInt.divide(rhsBigInt);
    }

    public BigDecimal MAX_VALUE() {
        return BigDecimal.valueOf(Long.MAX_VALUE);
    }

    public BigDecimal MIN_VALUE() {
        return BigDecimal.valueOf(Long.MIN_VALUE);
    }

    public BigDecimal ONE() {
        return BigDecimal.ONE;
    }

    public BigDecimal ZERO() {
        return BigDecimal.ZERO;
    }

    public BigDecimal cast(Number n) {
        if (n instanceof BigDecimal) {
            return (BigDecimal) n;
        }
        return BigDecimal.valueOf(n.doubleValue());
    }
}