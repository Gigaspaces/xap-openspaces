package org.openspaces.core.util.numbers;

/**
 * @author kimchy
 */
public class DoubleHelper implements NumberHelper<Double>{
    
    public int compare(Number lhs, Number rhs) {
        return cast(lhs).compareTo(cast(rhs));
    }

    public Double add(Number lhs, Number rhs) {
        return lhs.doubleValue() + rhs.doubleValue();
    }

    public Double sub(Number lhs, Number rhs) {
        return lhs.doubleValue() - rhs.doubleValue();
    }

    public Double mult(Number lhs, Number rhs) {
        return lhs.doubleValue() * rhs.doubleValue();
    }

    public Double div(Number lhs, Number rhs) {
        return lhs.doubleValue() / rhs.doubleValue();
    }

    public Double MAX_VALUE() {
        return Double.MAX_VALUE;
    }

    public Double MIN_VALUE() {
        return Double.MIN_VALUE;
    }

    public Double ONE() {
        return 1D;
    }

    public Double ZERO() {
        return 0D;
    }

    public Double cast(Number n) {
        if (n instanceof Double) {
            return (Double) n;
        }
        return n.doubleValue();
    }
}
