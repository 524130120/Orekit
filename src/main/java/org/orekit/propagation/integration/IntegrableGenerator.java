/* Copyright 2002-2021 CS GROUP
 * Licensed to CS GROUP (CS) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * CS licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.orekit.propagation.integration;

import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.StackableGenerator;
import org.orekit.time.AbsoluteDate;

/** {@link StackableGenerator Stackable generator} relying on integration.
 * @see org.orekit.propagation.Propagator
 * @author Luc Maisonobe
 * @since 11.1
 */
public interface IntegrableGenerator extends StackableGenerator {

    /** {@inheritDoc}
     * {@link IntegrableGenerator Integrable generator} are not closed form,
     * so this method returns {@code false}.
     */
    @Override
    default boolean isClosedForm() {
        return false;
    }

    /** Get the dimension of the generated derivative.
     * @return dimension of the generated
     */
    int getDimension();

    /** Initialize the generator at the start of propagation.
     * @param initialState initial state information at the start of propagation
     * @param target       date of propagation
     */
    default void init(final SpacecraftState initialState, final AbsoluteDate target) {
        // nothing by default
    }

}
