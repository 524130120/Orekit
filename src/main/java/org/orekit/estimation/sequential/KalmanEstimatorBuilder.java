/* Copyright 2002-2018 CS Systèmes d'Information
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
package org.orekit.estimation.sequential;

import java.util.ArrayList;
import java.util.List;

import org.hipparchus.linear.MatrixDecomposer;
import org.hipparchus.linear.QRDecomposer;
import org.hipparchus.linear.RealMatrix;
import org.orekit.errors.OrekitException;
import org.orekit.propagation.conversion.NumericalPropagatorBuilder;
import org.orekit.utils.ParameterDriversList;

/** Builder for a Kalman filter estimator.
 * @author Romain Gerbaud
 * @author Maxime Journot
 * @since 9.2
 */
public class KalmanEstimatorBuilder {

    /** Decomposer to use for the correction phase. */
    private MatrixDecomposer decomposer;

    /** Builders for propagators. */
    private List<NumericalPropagatorBuilder> propagatorBuilders;

    /** Estimated measurements parameters. */
    private ParameterDriversList estimatedMeasurementsParameters;

    /** Initial covariance matrix. */
    private RealMatrix initialCovarianceMatrix;

    /** Process noise matrices providers. */
    private List<ProcessNoiseMatrixProvider> processNoiseMatricesProviders;

    /** Default constructor.
     *  Set an extended Kalman filter, with linearized covariance prediction.
     */
    public KalmanEstimatorBuilder() {
        this.decomposer                      = new QRDecomposer(1.0e-15);
        this.propagatorBuilders              = new ArrayList<>();
        this.estimatedMeasurementsParameters = null;
        this.initialCovarianceMatrix         = null;
        this.processNoiseMatricesProviders   = new ArrayList<>();
    }

    /** Construct a {@link KalmanEstimatorReal} from the data in this builder.
     * @return a new {@link KalmanEstimatorReal}.
     * @throws OrekitException if building the filter failed
     */
    public KalmanEstimator build()
        throws OrekitException {
        // FIXME: Add checks on the existence of the different arguments
        return new KalmanEstimator(decomposer,
                                   propagatorBuilders.toArray(new NumericalPropagatorBuilder[propagatorBuilders.size()]),
                                   estimatedMeasurementsParameters == null ?
                                                                      new ParameterDriversList() :
                                                                      estimatedMeasurementsParameters,
                                   initialCovarianceMatrix,
                                   processNoiseMatricesProviders.toArray(new ProcessNoiseMatrixProvider[propagatorBuilders.size()]));
    }

    /** Configure the matrix decomposer.
     * @param matrixDecomposer decomposer to use for the correction phase
     * @return this object.
     */
    public KalmanEstimatorBuilder decomposer(final MatrixDecomposer matrixDecomposer) {
        decomposer = matrixDecomposer;
        return this;
    }

    /** Add a propagation configuration.
     * <p>
     * This method must be called once for each propagator to managed with the
     * {@link KalmanEstimator Kalman estimator}. The propagators order in the
     * Kalman filter will be the call order.
     * </p>
     * @param builder The propagator builder to use in the Kalman filter.
     * @param provider The process noise matrices provider to use.
     * @return this object.
     */
    public KalmanEstimatorBuilder addPropagationConfiguration(final NumericalPropagatorBuilder builder,
                                                              final ProcessNoiseMatrixProvider provider) {
        propagatorBuilders.add(builder);
        processNoiseMatricesProviders.add(provider);
        return this;
    }

    /** Configure the estimated measurement parameters.
     * @param estimatedMeasurementsParams The estimated measurements' parameters list.
     * @return this object.
     *
     */
    public KalmanEstimatorBuilder estimatedMeasurementsParameters(final ParameterDriversList estimatedMeasurementsParams) {
        estimatedMeasurementsParameters = estimatedMeasurementsParams;
        return this;
    }

    /** Configure the initial covariance matrix.
     * @param initialP The initial covariance matrix to use.
     * @return this object.
     */
    public KalmanEstimatorBuilder initialCovarianceMatrix(final RealMatrix initialP) {
        initialCovarianceMatrix = initialP;
        return this;
    }

}
