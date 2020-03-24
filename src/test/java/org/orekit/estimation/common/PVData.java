/* Copyright 2002-2020 CS Group
 * Licensed to CS Group (CS) under one or more
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

package org.orekit.estimation.common;

/** Container for Position-velocity data. */
class PVData {

    /** Position sigma. */
    private final double positionSigma;

    /** Velocity sigma. */
    private final double velocitySigma;

    /** Simple constructor.
     * @param positionSigma position sigma
     * @param velocitySigma velocity sigma
     */
    PVData(final double positionSigma, final double velocitySigma) {
        this.positionSigma = positionSigma;
        this.velocitySigma = velocitySigma;
    }

    /** Get position sigma.
     * @return position sigma
     */
    public double getPositionSigma() {
        return positionSigma;
    }

    /** Get velocity sigma.
     * @return velocity sigma
     */
    public double getVelocitySigma() {
        return velocitySigma;
    }

}