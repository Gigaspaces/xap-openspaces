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
