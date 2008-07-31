package org.openspaces.core.util.numbers;

/**
 * @author kimchy
 */
public class ShortHelper implements NumberHelper<Short> {
    
    public int compare(Number lhs, Number rhs) {
        return cast(lhs).compareTo(cast(rhs));
    }

    public Short add(Number lhs, Number rhs) {
        return (short)(lhs.shortValue() + rhs.shortValue());
    }

    public Short sub(Number lhs, Number rhs) {
        return (short)(lhs.shortValue() - rhs.shortValue());
    }

    public Short mult(Number lhs, Number rhs) {
        return (short)(lhs.shortValue() * rhs.shortValue());
    }

    public Short div(Number lhs, Number rhs) {
        return (short)(lhs.shortValue() / rhs.shortValue());
    }

    public Short MAX_VALUE() {
        return Short.MAX_VALUE;
    }

    public Short MIN_VALUE() {
        return Short.MIN_VALUE;
    }

    public Short ONE() {
        return 1;
    }

    public Short ZERO() {
        return 0;
    }

    public Short cast(Number n) {
        if (n instanceof Short) {
            return (Short) n;
        }
        return n.shortValue();
    }
}
