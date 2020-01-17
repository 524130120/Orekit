/* Copyright 2002-2020 CS Systèmes d'Information
 * Licensed to CS Systèmes d'Information (CS) under one or more
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
package org.orekit.estimation.measurements.modifiers;

import java.util.Collections;
import java.util.List;

import org.orekit.estimation.measurements.EstimatedMeasurement;
import org.orekit.estimation.measurements.EstimationModifier;
import org.orekit.estimation.measurements.InterSatellitesRange;
import org.orekit.utils.ParameterDriver;

/** Class modifying theoretical range measurement with Shapiro time delay.
 * <p>
 * Shapiro time delay is a relativistic effect due to gravity.
 * </p>
 *
 * @author Luc Maisonobe
 * @since 10.0
 */
public class ShapiroInterSatelliteRangeModifier extends AbstractShapiroBaseModifier implements EstimationModifier<InterSatellitesRange> {

    /** Simple constructor.
     * @param gm gravitational constant for main body in signal path vicinity.
     */
    public ShapiroInterSatelliteRangeModifier(final double gm) {
        super(gm);
    }

    /** {@inheritDoc} */
    @Override
    public List<ParameterDriver> getParametersDrivers() {
        return Collections.emptyList();
    }

    /** {@inheritDoc} */
    @Override
    public void modify(final EstimatedMeasurement<InterSatellitesRange> estimated) {
        doModify(estimated);
    }

}
