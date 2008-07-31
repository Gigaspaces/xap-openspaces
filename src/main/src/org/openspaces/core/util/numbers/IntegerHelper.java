package org.openspaces.core.util.numbers;

/**
 * @author kimchy
 */
public class IntegerHelper implements NumberHelper<Integer> {

    public int compare(Number lhs, Number rhs) {
        return cast(lhs).compareTo(cast(rhs));
    }

    public Integer add(Number lhs, Number rhs) {
        return lhs.intValue() + rhs.intValue();
    }

    public Integer sub(Number lhs, Number rhs) {
        return lhs.intValue() - rhs.intValue();
    }

    public Integer mult(Number lhs, Number rhs) {
        return lhs.intValue() * rhs.intValue();
    }

    public Integer div(Number lhs, Number rhs) {
        return lhs.intValue() / rhs.intValue();
    }

    public Integer MAX_VALUE() {
        return Integer.MAX_VALUE;
    }

    public Integer MIN_VALUE() {
        return Integer.MIN_VALUE;
    }

    public Integer ONE() {
        return 1;
    }

    public Integer ZERO() {
        return 0;
    }

    public Integer cast(Number n) {
        if (n instanceof Integer) {
            return (Integer) n;
        }
        return n.intValue();
    }
}
