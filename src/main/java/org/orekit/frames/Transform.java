/* Copyright 2002-2014 CS Systèmes d'Information
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
package org.orekit.frames;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.math3.RealFieldElement;
import org.apache.commons.math3.geometry.euclidean.threed.FieldRotation;
import org.apache.commons.math3.geometry.euclidean.threed.FieldVector3D;
import org.apache.commons.math3.geometry.euclidean.threed.Line;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.Pair;
import org.orekit.errors.OrekitException;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeInterpolable;
import org.orekit.time.TimeShiftable;
import org.orekit.time.TimeStamped;
import org.orekit.utils.AngularCoordinates;
import org.orekit.utils.FieldPVCoordinates;
import org.orekit.utils.PVCoordinates;


/** Transformation class in three dimensional space.
 *
 * <p>This class represents the transformation engine between {@link Frame frames}.
 * It is used both to define the relationship between each frame and its
 * parent frame and to gather all individual transforms into one
 * operation when converting between frames far away from each other.</p>
 * <p> The convention used in OREKIT is vectorial transformation. It means
 * that a transformation is defined as a transform to apply to the
 * coordinates of a vector expressed in the old frame to obtain the
 * same vector expressed in the new frame.<p>
 *
 * <p>Instances of this class are guaranteed to be immutable.</p>
 *
 *  <h5> Example </h5>
 *
 * <pre>
 *
 * 1 ) Example of translation from R<sub>A</sub> to R<sub>B</sub>:
 * We want to transform the {@link PVCoordinates} PV<sub>A</sub> to PV<sub>B</sub>.
 *
 * With :  PV<sub>A</sub> = ({1, 0, 0} , {1, 0, 0});
 * and  :  PV<sub>B</sub> = ({0, 0, 0} , {0, 0, 0});
 *
 * The transform to apply then is defined as follows :
 *
 * Vector3D translation = new Vector3D(-1, 0, 0);
 * Vector3D velocity = new Vector3D(-1, 0, 0);
 *
 * Transform R1toR2 = new Transform(translation, velocity);
 *
 * PV<sub>B</sub> = R1toR2.transformPVCoordinates(PV<sub>A</sub>);
 *
 *
 * 2 ) Example of rotation from R<sub>A</sub> to R<sub>B</sub>:
 * We want to transform the {@link PVCoordinates} PV<sub>A</sub> to PV<sub>B</sub>.
 *
 * With :  PV<sub>A</sub> = ({1, 0, 0}, {1, 0, 0});
 * and  :  PV<sub>B</sub> = ({0, 1, 0}, {-2, 1, 0});
 *
 * The transform to apply then is defined as follows :
 *
 * Rotation rotation = new Rotation(Vector3D.PLUS_K, FastMath.PI / 2);
 * Vector3D rotationRate = new Vector3D(0, 0, -2);
 *
 * Transform R1toR2 = new Transform(rotation, rotationRate);
 *
 * PV<sub>B</sub> = R1toR2.transformPVCoordinates(PV<sub>A</sub>);
 *
 * </pre>
 *
 * @author Luc Maisonobe
 * @author Fabien Maussion
 */
public class Transform
    implements TimeStamped, TimeShiftable<Transform>, TimeInterpolable<Transform>, Serializable {

    /** Identity transform. */
    public static final Transform IDENTITY = new IdentityTransform();

    /** Serializable UID. */
    private static final long serialVersionUID = -8809893979516295102L;

    /** Date of the transform. */
    private final AbsoluteDate date;

    /** Cartesian coordinates of the target frame with respect to the original frame. */
    private final PVCoordinates cartesian;

    /** Angular coordinates of the target frame with respect to the original frame. */
    private final AngularCoordinates angular;

    /** Build a transform from its primitive operations.
     * @param date date of the transform
     * @param cartesian Cartesian coordinates of the target frame with respect to the original frame
     * @param angular angular coordinates of the target frame with respect to the original frame
     */
    private Transform(final AbsoluteDate date,
                      final PVCoordinates cartesian, final AngularCoordinates angular) {
        this.date      = date;
        this.cartesian = cartesian;
        this.angular   = angular;
    }

    /** Build a translation transform.
     * @param date date of the transform
     * @param translation translation to apply (i.e. coordinates of
     * the transformed origin, or coordinates of the origin of the
     * old frame in the new frame)
     */
    public Transform(final AbsoluteDate date, final Vector3D translation) {
        this(date, new PVCoordinates(translation, Vector3D.ZERO, Vector3D.ZERO), AngularCoordinates.IDENTITY);
    }

    /** Build a rotation transform.
     * @param date date of the transform
     * @param rotation rotation to apply ( i.e. rotation to apply to the
     * coordinates of a vector expressed in the old frame to obtain the
     * same vector expressed in the new frame )
     */
    public Transform(final AbsoluteDate date, final Rotation rotation) {
        this(date, PVCoordinates.ZERO, new AngularCoordinates(rotation, Vector3D.ZERO));
    }

    /** Build a translation transform, with its first time derivative.
     * @param date date of the transform
     * @param translation translation to apply (i.e. coordinates of
     * the transformed origin, or coordinates of the origin of the
     * old frame in the new frame)
     * @param velocity the velocity of the translation (i.e. origin
     * of the old frame velocity in the new frame)
     */
    public Transform(final AbsoluteDate date, final Vector3D translation, final Vector3D velocity) {
        this(date, new PVCoordinates(translation, velocity, Vector3D.ZERO), AngularCoordinates.IDENTITY);
    }

    /** Build a translation transform, with its first and second time derivatives.
     * @param date date of the transform
     * @param translation translation to apply (i.e. coordinates of
     * the transformed origin, or coordinates of the origin of the
     * old frame in the new frame)
     * @param velocity the velocity of the translation (i.e. origin
     * of the old frame velocity in the new frame)
     * @param acceleration the acceleration of the translation (i.e. origin
     * of the old frame acceleration in the new frame)
     */
    public Transform(final AbsoluteDate date, final Vector3D translation, final Vector3D velocity, final Vector3D acceleration) {
        this(date, new PVCoordinates(translation, velocity, acceleration), AngularCoordinates.IDENTITY);
    }

    /** Build a translation transform, with its first time derivative.
     * @param date date of the transform
     * @param cartesian cartesian part of the transformation to apply (i.e. coordinates of
     * the transformed origin, or coordinates of the origin of the
     * old frame in the new frame, with their derivatives)
     */
    public Transform(final AbsoluteDate date, final PVCoordinates cartesian) {
        this(date, cartesian, AngularCoordinates.IDENTITY);
    }

    /** Build a rotation transform.
     * @param date date of the transform
     * @param rotation rotation to apply ( i.e. rotation to apply to the
     * coordinates of a vector expressed in the old frame to obtain the
     * same vector expressed in the new frame )
     * @param rotationRate the axis of the instant rotation
     * expressed in the new frame. (norm representing angular rate)
     */
    public Transform(final AbsoluteDate date, final Rotation rotation, final Vector3D rotationRate) {
        this(date, PVCoordinates.ZERO, new AngularCoordinates(rotation, rotationRate));
    }

    /** Build a rotation transform.
     * @param date date of the transform
     * @param angular angular part of the transformation to apply (i.e. rotation to
     * apply to the coordinates of a vector expressed in the old frame to obtain the
     * same vector expressed in the new frame, with its rotation rate)
     */
    public Transform(final AbsoluteDate date, final AngularCoordinates angular) {
        this(date, PVCoordinates.ZERO, angular);
    }

    /** Build a transform by combining two existing ones.
     * <p>
     * Note that the dates of the two existing transformed are <em>ignored</em>,
     * and the combined transform date is set to the date supplied in this constructor
     * without any attempt to shift the raw transforms. This is a design choice allowing
     * user full control of the combination.
     * </p>
     * @param date date of the transform
     * @param first first transform applied
     * @param second second transform applied
     */
    public Transform(final AbsoluteDate date, final Transform first, final Transform second) {
        this(date,
             new PVCoordinates(compositeTranslation(first, second),
                                compositeVelocity(first, second),
                                compositeAcceleration(first, second)),
             new AngularCoordinates(compositeRotation(first, second),
                                    compositeRotationRate(first, second)));
    }

    /** Compute a composite translation.
     * @param first first applied transform
     * @param second second applied transform
     * @return translation part of the composite transform
     */
    private static Vector3D compositeTranslation(final Transform first, final Transform second) {

        final Vector3D p1 = first.cartesian.getPosition();
        final Rotation r1 = first.angular.getRotation();
        final Vector3D p2 = second.cartesian.getPosition();

        return p1.add(r1.applyInverseTo(p2));

    }

    /** Compute a composite velocity.
     * @param first first applied transform
     * @param second second applied transform
     * @return velocity part of the composite transform
     */
    private static Vector3D compositeVelocity(final Transform first, final Transform second) {

        final Vector3D v1 = first.cartesian.getVelocity();
        final Rotation r1 = first.angular.getRotation();
        final Vector3D o1 = first.angular.getRotationRate();
        final Vector3D p2 = second.cartesian.getPosition();
        final Vector3D v2 = second.cartesian.getVelocity();

        return v1.add(r1.applyInverseTo(v2.add(Vector3D.crossProduct(o1, p2))));

    }

    /** Compute a composite acceleration.
     * @param first first applied transform
     * @param second second applied transform
     * @return acceleration part of the composite transform
     */
    private static Vector3D compositeAcceleration(final Transform first, final Transform second) {

        final Vector3D a1 = first.cartesian.getAcceleration();
        final Rotation r1 = first.angular.getRotation();
        final Vector3D o1 = first.angular.getRotationRate();
        final Vector3D v2 = second.cartesian.getVelocity();
        final Vector3D a2 = second.cartesian.getAcceleration();

        return a1.add(r1.applyInverseTo(a2.add(new Vector3D(2.0, Vector3D.crossProduct(o1, v2)))));

    }

    /** Compute a composite rotation.
     * @param first first applied transform
     * @param second second applied transform
     * @return rotation part of the composite transform
     */
    private static Rotation compositeRotation(final Transform first, final Transform second) {

        final Rotation r1 = first.angular.getRotation();
        final Rotation r2 = second.angular.getRotation();

        return r2.applyTo(r1);

    }

    /** Compute a composite rotation rate.
     * @param first first applied transform
     * @param second second applied transform
     * @return rotation rate part of the composite transform
     */
    private static Vector3D compositeRotationRate(final Transform first, final Transform second) {

        final Vector3D o1 = first.angular.getRotationRate();
        final Rotation r2 = second.angular.getRotation();
        final Vector3D o2 = second.angular.getRotationRate();

        return o2.add(r2.applyTo(o1));

    }

    /** {@inheritDoc} */
    public AbsoluteDate getDate() {
        return date;
    }

    /** {@inheritDoc} */
    public Transform shiftedBy(final double dt) {
        return new Transform(date.shiftedBy(dt), cartesian.shiftedBy(dt), angular.shiftedBy(dt));
    };

    /** {@inheritDoc}
     * <p>
     * Calling this method is equivalent to call {@link #interpolate(AbsoluteDate,
     * PVCoordinates.SampleFilter, boolean, Collection)} with
     * {@code pvaMode} set to {@link PVCoordinates.SampleFilter#SAMPLE_PVA}
     * and {@code useRotationRates} set to true.
     * </p>
     * @exception OrekitException if the number of point is too small for interpolating
     */
    public Transform interpolate(final AbsoluteDate interpolationDate,
                                 final Collection<Transform> sample)
        throws OrekitException {
        return interpolate(interpolationDate, PVCoordinates.SampleFilter.SAMPLE_PVA, true, sample);
    }

    /** Interpolate a transform from a sample set of existing transforms.
     * <p>
     * Note that even if first time derivatives (velocities and rotation rates)
     * from sample can be ignored, the interpolated instance always includes
     * interpolated derivatives. This feature can be used explicitly to
     * compute these derivatives when it would be too complex to compute them
     * from an analytical formula: just compute a few sample points from the
     * explicit formula and set the derivatives to zero in these sample points,
     * then use interpolation to add derivatives consistent with the positions
     * and rotations.
     * </p>
     * <p>
     * As this implementation of interpolation is polynomial, it should be used only
     * with small samples (about 10-20 points) in order to avoid <a
     * href="http://en.wikipedia.org/wiki/Runge%27s_phenomenon">Runge's phenomenon</a>
     * and numerical problems (including NaN appearing).
     * </p>
     * @param date interpolation date
     * @param pvaFilter filter for translation derivatives to extract from sample
     * @param useRotationRates if true, use sample points rotation rates,
     * otherwise ignore them and use only rotations
     * @param sample sample points on which interpolation should be done
     * @return a new instance, interpolated at specified date
     * @exception OrekitException if the number of point is too small for interpolating
     */
    public static Transform interpolate(final AbsoluteDate date,
                                        final PVCoordinates.SampleFilter pvaFilter, final boolean useRotationRates,
                                        final Collection<Transform> sample)
        throws OrekitException {
        final List<Pair<AbsoluteDate, PVCoordinates>> datedPV =
                new ArrayList<Pair<AbsoluteDate, PVCoordinates>>(sample.size());
        final List<Pair<AbsoluteDate, AngularCoordinates>> datedAC =
                new ArrayList<Pair<AbsoluteDate, AngularCoordinates>>(sample.size());
        for (final Transform transform : sample) {
            datedPV.add(new Pair<AbsoluteDate, PVCoordinates>(transform.getDate(),
                    transform.getCartesian()));
            datedAC.add(new Pair<AbsoluteDate, AngularCoordinates>(transform.getDate(),
                    transform.getAngular()));
        }
        final PVCoordinates      interpolatedPV = PVCoordinates.interpolate(date, pvaFilter, datedPV);
        final AngularCoordinates interpolatedAC = AngularCoordinates.interpolate(date, useRotationRates, datedAC);
        return new Transform(date, interpolatedPV, interpolatedAC);
    }

    /** Get the inverse transform of the instance.
     * @return inverse transform of the instance
     */
    public Transform getInverse() {

        final Vector3D p = cartesian.getPosition();
        final Vector3D v = cartesian.getVelocity();
        final Rotation r = angular.getRotation();
        final Vector3D o = angular.getRotationRate();

        final Vector3D rT = r.applyTo(p);
        return new Transform(date,
                             new PVCoordinates(rT.negate(),
                                                Vector3D.crossProduct(o, rT).subtract(r.applyTo(v)),
                                                // TODO: compute acceleration
                                                Vector3D.ZERO),
                             angular.revert());

    }

    /** Get a freezed transform.
     * <p>
     * This method creates a copy of the instance but frozen in time,
     * i.e. with velocity and rotation rate forced to zero.
     * </p>
     * @return a new transform, without any time-dependent parts
     */
    public Transform freeze() {
        return new Transform(date,
                             new PVCoordinates(cartesian.getPosition(), Vector3D.ZERO, Vector3D.ZERO),
                             new AngularCoordinates(angular.getRotation(), Vector3D.ZERO));
    }

    /** Transform a position vector (including translation effects).
     * @param position vector to transform
     * @return transformed position
     */
    public Vector3D transformPosition(final Vector3D position) {
        return angular.getRotation().applyTo(cartesian.getPosition().add(position));
    }

    /** Transform a position vector (including translation effects).
     * @param position vector to transform
     * @param <T> the type of the field elements
     * @return transformed position
     */
    public <T extends RealFieldElement<T>> FieldVector3D<T> transformPosition(final FieldVector3D<T> position) {
        return FieldRotation.applyTo(angular.getRotation(), position.add(cartesian.getPosition()));
    }

    /** Transform a vector (ignoring translation effects).
     * @param vector vector to transform
     * @return transformed vector
     */
    public Vector3D transformVector(final Vector3D vector) {
        return angular.getRotation().applyTo(vector);
    }

    /** Transform a vector (ignoring translation effects).
     * @param vector vector to transform
     * @param <T> the type of the field elements
     * @return transformed vector
     */
    public <T extends RealFieldElement<T>> FieldVector3D<T> transformVector(final FieldVector3D<T> vector) {
        return FieldRotation.applyTo(angular.getRotation(), vector);
    }

    /** Transform a line.
     * @param line to transform
     * @return transformed line
     */
    public Line transformLine(final Line line) {
        final Vector3D transformedP0 = transformPosition(line.getOrigin());
        final Vector3D transformedP1 = transformPosition(line.pointAt(1.0e6));
        return new Line(transformedP0, transformedP1);
    }

    /** Transform {@link PVCoordinates} including kinematic effects.
     * @param pva the position-velocity-acceleration triplet to transform.
     * @return transformed position-velocity-acceleration
     */
    public PVCoordinates transformPVCoordinates(final PVCoordinates pva) {
        final Vector3D p = pva.getPosition();
        final Vector3D v = pva.getVelocity();
        final Vector3D a = pva.getVelocity();
        final Vector3D transformedP = angular.getRotation().applyTo(p.add(cartesian.getPosition()));
        final Vector3D crossP       = Vector3D.crossProduct(angular.getRotationRate(), transformedP);
        final Vector3D transformedV = angular.getRotation().applyTo(v.add(cartesian.getVelocity())).subtract(crossP);
        final Vector3D crossV       = Vector3D.crossProduct(angular.getRotationRate(), transformedV);
        final Vector3D transformedA = angular.getRotation().applyTo(a.add(cartesian.getAcceleration())).subtract(crossV.add(crossV));
        return new PVCoordinates(transformedP, transformedV, transformedA);
    }

    /** Transform {@link FieldPVCoordinates} including kinematic effects.
     * @param pv the couple position-velocity to transform.
     * @param <T> the type of the field elements
     * @return transformed position/velocity
     */
    public <T extends RealFieldElement<T>> FieldPVCoordinates<T> transformPVCoordinates(final FieldPVCoordinates<T> pv) {
        // TODO: add acceleration
        final FieldVector3D<T> p = pv.getPosition();
        final FieldVector3D<T> v = pv.getVelocity();
        final FieldVector3D<T> transformedP = FieldRotation.applyTo(angular.getRotation(),
                                                                    p.add(cartesian.getPosition()));
        final FieldVector3D<T> cross = FieldVector3D.crossProduct(angular.getRotationRate(), transformedP);
        return new FieldPVCoordinates<T>(transformedP,
                                         FieldRotation.applyTo(angular.getRotation(),
                                                               v.add(cartesian.getVelocity())).subtract(cross));
    }

    /** Compute the Jacobian of the {@link #transformPVCoordinates(PVCoordinates)}
     * method of the transform.
     * <p>
     * Element {@code jacobian[i][j]} is the derivative of Cartesian coordinate i
     * of the transformed {@link PVCoordinates} with respect to Cartesian coordinate j
     * of the input {@link PVCoordinates} in method {@link #transformPVCoordinates(PVCoordinates)}.
     * </p>
     * <p>
     * This definition implies that if we define position-velocity coordinates
     * <pre>
     * PV<sub>1</sub> = transform.transformPVCoordinates(PV<sub>0</sub>), then
     * </pre>
     * their differentials dPV<sub>1</sub> and dPV<sub>0</sub> will obey the following relation
     * where J is the matrix computed by this method:<br/>
     * <pre>
     * dPV<sub>1</sub> = J &times; dPV<sub>0</sub>
     * </pre>
     * </p>
     * @param jacobian placeholder 6x6 (or larger) matrix to be filled with the Jacobian, if matrix
     * is larger than 6x6, only the 6x6 upper left corner will be modified
     */
    public void getJacobian(final double[][] jacobian) {

        // elementary matrix for rotation
        final double[][] mData = angular.getRotation().getMatrix();

        // dP1/dP0
        System.arraycopy(mData[0], 0, jacobian[0], 0, 3);
        System.arraycopy(mData[1], 0, jacobian[1], 0, 3);
        System.arraycopy(mData[2], 0, jacobian[2], 0, 3);

        // dP1/dV0
        Arrays.fill(jacobian[0], 3, 6, 0.0);
        Arrays.fill(jacobian[1], 3, 6, 0.0);
        Arrays.fill(jacobian[2], 3, 6, 0.0);

        // dV1/dP0
        final Vector3D o = angular.getRotationRate();
        final double mOx = -o.getX();
        final double mOy = -o.getY();
        final double mOz = -o.getZ();
        for (int i = 0; i < 3; ++i) {
            jacobian[3][i] = mOy * mData[2][i] - mOz * mData[1][i];
            jacobian[4][i] = mOz * mData[0][i] - mOx * mData[2][i];
            jacobian[5][i] = mOx * mData[1][i] - mOy * mData[0][i];
        }

        // dV1/dV0
        System.arraycopy(mData[0], 0, jacobian[3], 3, 3);
        System.arraycopy(mData[1], 0, jacobian[4], 3, 3);
        System.arraycopy(mData[2], 0, jacobian[5], 3, 3);

    }

    /** Get the underlying elementary cartesian part.
     * <p>A transform can be uniquely represented as an elementary
     * translation followed by an elementary rotation. This method
     * returns this unique elementary translation with its derivative.</p>
     * @return underlying elementary cartesian part
     * @see #getTranslation()
     * @see #getVelocity()
     */
    public PVCoordinates getCartesian() {
        return cartesian;
    }

    /** Get the underlying elementary translation.
     * <p>A transform can be uniquely represented as an elementary
     * translation followed by an elementary rotation. This method
     * returns this unique elementary translation.</p>
     * @return underlying elementary translation
     * @see #getCartesian()
     * @see #getVelocity()
     * @see #getAcceleration()
     */
    public Vector3D getTranslation() {
        return cartesian.getPosition();
    }

    /** Get the first time derivative of the translation.
     * @return first time derivative of the translation
     * @see #getCartesian()
     * @see #getTranslation()
     * @see #getAcceleration()
     */
    public Vector3D getVelocity() {
        return cartesian.getVelocity();
    }

    /** Get the second time derivative of the translation.
     * @return second time derivative of the translation
     * @see #getCartesian()
     * @see #getTranslation()
     * @see #getVelocity()
     */
    public Vector3D getAcceleration() {
        return cartesian.getAcceleration();
    }

    /** Get the underlying elementary angular part.
     * <p>A transform can be uniquely represented as an elementary
     * translation followed by an elementary rotation. This method
     * returns this unique elementary rotation with its derivative.</p>
     * @return underlying elementary angular part
     * @see #getRotation()
     * @see #getRotationRate()
     */
    public AngularCoordinates getAngular() {
        return angular;
    }

    /** Get the underlying elementary rotation.
     * <p>A transform can be uniquely represented as an elementary
     * translation followed by an elementary rotation. This method
     * returns this unique elementary rotation.</p>
     * @return underlying elementary rotation
     * @see #getAngular()
     * @see #getRotationRate()
     */
    public Rotation getRotation() {
        return angular.getRotation();
    }

    /** Get the first time derivative of the rotation.
     * <p>The norm represents the angular rate.</p>
     * @return First time derivative of the rotation
     * @see #getAngular()
     * @see #getRotation()
     */
    public Vector3D getRotationRate() {
        return angular.getRotationRate();
    }

    /** Specialized class for identity transform. */
    private static class IdentityTransform extends Transform {

        /** Serializable UID. */
        private static final long serialVersionUID = -9042082036141830517L;

        /** Simple constructor. */
        public IdentityTransform() {
            super(AbsoluteDate.J2000_EPOCH, PVCoordinates.ZERO, AngularCoordinates.IDENTITY);
        }

        /** {@inheritDoc} */
        @Override
        public Transform shiftedBy(final double dt) {
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public Transform getInverse() {
            return this;
        };

        /** {@inheritDoc} */
        @Override
        public Vector3D transformPosition(final Vector3D position) {
            return position;
        }

        /** {@inheritDoc} */
        @Override
        public Vector3D transformVector(final Vector3D vector) {
            return vector;
        }

        /** {@inheritDoc} */
        @Override
        public Line transformLine(final Line line) {
            return line;
        }

        /** {@inheritDoc} */
        @Override
        public PVCoordinates transformPVCoordinates(final PVCoordinates pv) {
            return pv;
        }

        /** {@inheritDoc} */
        @Override
        public void getJacobian(final double[][] jacobian) {
            for (int i = 0; i < 6; ++i) {
                Arrays.fill(jacobian[i], 0, 6, 0.0);
                jacobian[i][i] = 1.0;
            }
        }

    }

}
