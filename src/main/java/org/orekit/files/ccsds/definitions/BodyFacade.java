/* Copyright 2002-2022 CS GROUP
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
package org.orekit.files.ccsds.definitions;

import org.orekit.bodies.CelestialBody;

/** Facade in front of several center bodies in CCSDS messages.
 * @author Luc Maisonobe
 * @since 11.0
 */
public class BodyFacade {

    /** Name of the center. */
    private final String name;

    /** Celestial body (may be null). */
    private final CelestialBody body;

    /** Simple constructor.
     * @param name name of the frame
     * @param body celestial body (may be null)
     */
    public BodyFacade(final String name, final CelestialBody body) {
        this.name = name;
        this.body = body;
    }

    /** Get the CCSDS name for the body.
     * @return CCSDS name
     */
    public String getName() {
        return name;
    }

    /** Get the celestial body.
     * @return celestial body (may be null)
     */
    public CelestialBody getBody() {
        return body;
    }

}
