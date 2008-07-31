package org.openspaces.core.util.numbers;

/**
 * @author kimchy
 */
public class LongHelper implements NumberHelper<Long>{

    public int compare(Number lhs, Number rhs) {
        return cast(lhs).compareTo(cast(rhs));
    }

    public Long add(Number lhs, Number rhs) {
        return lhs.longValue() + rhs.longValue();
    }

    public Long sub(Number lhs, Number rhs) {
        return lhs.longValue() - rhs.longValue();
    }

    public Long mult(Number lhs, Number rhs) {
        return lhs.longValue() * rhs.longValue();
    }

    public Long div(Number lhs, Number rhs) {
        return lhs.longValue() / rhs.longValue();
    }

    public Long MAX_VALUE() {
        return Long.MAX_VALUE;
    }

    public Long MIN_VALUE() {
        return Long.MIN_VALUE;
    }

    public Long ONE() {
        return 1L;
    }

    public Long ZERO() {
        return 0L;
    }

    public Long cast(Number n) {
        if (n instanceof Long) {
            return (Long) n;
        }
        return n.longValue();
    }
}
