/* Copyright 2013 Applied Defense Solutions, Inc.
 * Licensed to CS Communication & Systèmes (CS) under one or more
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
package org.orekit.propagation.events.handlers;

import org.orekit.errors.OrekitException;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.events.EventDetector;
import org.orekit.propagation.events.EventDetector.Action;


/**
 * An interface defining how to override event handling behavior in the standard
 * propagator eventing classes without requiring subclassing.  In cases where
 * one wishes to use anonymous classes rather than explicit subclassing this
 * allows for a more direct way to override the behavior.  Event classes have to
 * specifically support this capability.
 *
 * @author Hank Grabowski
 *
 * @param <T> object type that the handler is called from
 * @since 6.1
 */
public interface DetectorEventHandler<T extends EventDetector> {

    /**
     * eventOccurred method mirrors the same interface method as in {@link EventDetector}
     * and its subclasses, but with an additional parameter that allows the calling
     * method to pass in an object from the detector which would have potential
     * additional data to allow the implementing class to determine the correct
     * return state.
     *
     * @param s SpaceCraft state to be used in the evaluation
     * @param detector object with appropriate type that can be used in determining correct return state
     * @param increasing with the event occured in an "increasing" or "decreasing" slope direction
     * @return the Action that the calling detector should pass back to the evaluation system
     *
     * @exception OrekitException if some specific error occurs
     */
    Action eventOccurred(SpacecraftState s, T detector, boolean increasing) throws OrekitException;

    /** Reset the state prior to continue propagation.
     * <p>This method is called after the step handler has returned and
     * before the next step is started, but only when {@link
     * #eventOccurred} has itself returned the {@link EventDetector.Action#RESET_STATE}
     * indicator. It allows the user to reset the state for the next step,
     * without perturbing the step handler of the finishing step. If the
     * {@link #eventOccurred} never returns the {@link EventDetector.Action#RESET_STATE}
     * indicator, this function will never be called, and it is safe to simply return null.</p>
     * @param detector object with appropriate type that can be used in determining correct return state
     * @param oldState old state
     * @return new state
     * @exception OrekitException if the state cannot be reseted
     */
    SpacecraftState resetState(T detector, SpacecraftState oldState) throws OrekitException;

}
