package org.openspaces.core.util.numbers;

import java.math.BigInteger;

/**
 * @author kimchy
 */
public class NumberHelperFactory {

    @SuppressWarnings("unchecked")
    public static <N extends Number> NumberHelper<N> getNumberHelper(Class<N> type) throws IllegalArgumentException {
        if (type.equals(Integer.class)) {
            return (NumberHelper<N>) new IntegerHelper();
        }

        if (type.equals(Long.class)) {
            return (NumberHelper<N>) new LongHelper();
        }

        if (type.equals(Float.class)) {
            return (NumberHelper<N>) new FloatHelper();
        }

        if (type.equals(Double.class)) {
            return (NumberHelper<N>) new DoubleHelper();
        }

        if (type.equals(Short.class)) {
            return (NumberHelper<N>) new ShortHelper();
        }

        if (type.equals(BigInteger.class)) {
            return (NumberHelper<N>) new BigIntegerHelper();
        }

        throw new IllegalArgumentException("No number helper support for type [" + type.getName() + "]");
    }
}
