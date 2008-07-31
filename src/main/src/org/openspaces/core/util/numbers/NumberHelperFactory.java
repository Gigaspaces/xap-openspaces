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
