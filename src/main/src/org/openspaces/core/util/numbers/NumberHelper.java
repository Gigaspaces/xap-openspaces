package org.openspaces.core.util.numbers;

import java.util.Comparator;

/**
 * @author kimchy
 */
public interface NumberHelper<N extends Number> extends Comparator<Number> {

    N cast(Number n);

    N MAX_VALUE();

    N MIN_VALUE();

    N ONE();

    N ZERO();

    N add(Number lhs, Number rhs);

    N sub(Number lhs, Number rhs);

    N mult(Number lhs, Number rhs);

    N div(Number lhs, Number rhs);
}
