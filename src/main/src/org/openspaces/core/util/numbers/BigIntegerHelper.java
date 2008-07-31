package org.openspaces.core.util.numbers;

import java.math.BigInteger;

/**
 * @author kimchy
 */
public class BigIntegerHelper implements NumberHelper<BigInteger> {

    public int compare(Number lhs, Number rhs) {
        return cast(lhs).compareTo(cast(rhs));
    }

    public boolean isGreater(BigInteger lhs, BigInteger rhs) {
        return lhs.compareTo(rhs) > 0;
    }

    public BigInteger add(Number lhs, Number rhs) {
        BigInteger lhsBigInt = cast(lhs);
        BigInteger rhsBigInt = cast(rhs);
        return lhsBigInt.add(rhsBigInt);
    }

    public BigInteger sub(Number lhs, Number rhs) {
        BigInteger lhsBigInt = cast(lhs);
        BigInteger rhsBigInt = cast(rhs);
        return lhsBigInt.subtract(rhsBigInt);
    }

    public BigInteger mult(Number lhs, Number rhs) {
        BigInteger lhsBigInt = cast(lhs);
        BigInteger rhsBigInt = cast(rhs);
        return lhsBigInt.multiply(rhsBigInt);
    }

    public BigInteger div(Number lhs, Number rhs) {
        BigInteger lhsBigInt = cast(lhs);
        BigInteger rhsBigInt = cast(rhs);
        return lhsBigInt.divide(rhsBigInt);
    }

    public BigInteger MAX_VALUE() {
        return BigInteger.valueOf(Long.MAX_VALUE);
    }

    public BigInteger MIN_VALUE() {
        return BigInteger.valueOf(Long.MIN_VALUE);
    }

    public BigInteger ONE() {
        return BigInteger.ONE;
    }

    public BigInteger ZERO() {
        return BigInteger.ZERO;
    }

    public BigInteger cast(Number n) {
        if (n instanceof BigInteger) {
            return (BigInteger) n;
        }
        return BigInteger.valueOf(n.longValue());
    }
}
