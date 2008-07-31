package org.openspaces.core.util.numbers;

/**
 * @author kimchy
 */
public class FloatHelper implements NumberHelper<Float>{

    public int compare(Number lhs, Number rhs) {
        return cast(lhs).compareTo(cast(rhs));
    }

    public Float add(Number lhs, Number rhs) {
        return lhs.floatValue() + rhs.floatValue();
    }

    public Float sub(Number lhs, Number rhs) {
        return lhs.floatValue() - rhs.floatValue();
    }

    public Float mult(Number lhs, Number rhs) {
        return lhs.floatValue() * rhs.floatValue();
    }

    public Float div(Number lhs, Number rhs) {
        return lhs.floatValue() / rhs.floatValue();
    }

    public Float MAX_VALUE() {
        return Float.MAX_VALUE;
    }

    public Float MIN_VALUE() {
        return Float.MIN_VALUE;
    }

    public Float ONE() {
        return 1F;
    }

    public Float ZERO() {
        return 0F;
    }

    public Float cast(Number n) {
        if (n instanceof Float) {
            return (Float) n;
        }
        return n.floatValue();
    }
}
