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