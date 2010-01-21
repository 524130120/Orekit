/* Copyright 2002-2010 CS Communication & Systèmes
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
package org.orekit.attitudes;


import org.apache.commons.math.geometry.Rotation;
import org.apache.commons.math.geometry.Vector3D;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.orekit.Utils;
import org.orekit.errors.OrekitException;
import org.orekit.frames.FramesFactory;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.DateComponents;
import org.orekit.time.TimeComponents;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.PVCoordinates;

public class FixedRateTest {

    @Test
    public void testZeroRate() throws OrekitException {
        FixedRate law =
            new FixedRate(new Attitude(FramesFactory.getEME2000(),
                                       new Rotation(0.48, 0.64, 0.36, 0.48, false),
                                       Vector3D.ZERO),
                          new AbsoluteDate(new DateComponents(2004, 3, 2),
                                           new TimeComponents(13, 17, 7.865),
                                           TimeScalesFactory.getUTC()));
        PVCoordinates pv =
            new PVCoordinates(new Vector3D(28812595.32012577, 5948437.4640250085, 0),
                              new Vector3D(0, 0, 3680.853673522056));
        Rotation attitude0 = law.getState(law.getReferenceDate(),
                                          pv, FramesFactory.getEME2000()).getRotation();
        Assert.assertEquals(0, Rotation.distance(attitude0, law.getReferenceAttitude().getRotation()), 1.0e-10);
        Rotation attitude1 = law.getState(new AbsoluteDate(law.getReferenceDate(), 10.0),
                                          pv, FramesFactory.getEME2000()).getRotation();
        Assert.assertEquals(0, Rotation.distance(attitude1, law.getReferenceAttitude().getRotation()), 1.0e-10);
        Rotation attitude2 = law.getState(new AbsoluteDate(law.getReferenceDate(), -20.0),
                                          pv, FramesFactory.getEME2000()).getRotation();
        Assert.assertEquals(0, Rotation.distance(attitude2, law.getReferenceAttitude().getRotation()), 1.0e-10);

    }

    @Test
    public void testNonZeroRate() throws OrekitException {
        final double rate = 2 * Math.PI / (12 * 60);
        FixedRate law =
            new FixedRate(new Attitude(FramesFactory.getEME2000(),
                                       new Rotation(0.48, 0.64, 0.36, 0.48, false),
                                       new Vector3D(rate, Vector3D.PLUS_K)),
                          new AbsoluteDate(new DateComponents(2004, 3, 2),
                                           new TimeComponents(13, 17, 7.865),
                                           TimeScalesFactory.getUTC()));
        PVCoordinates pv =
            new PVCoordinates(new Vector3D(28812595.32012577, 5948437.4640250085, 0),
                              new Vector3D(0, 0, 3680.853673522056));
        Rotation attitude0 = law.getState(law.getReferenceDate(),
                                          pv, FramesFactory.getEME2000()).getRotation();
        Assert.assertEquals(0, Rotation.distance(attitude0, law.getReferenceAttitude().getRotation()), 1.0e-10);
        Rotation attitude1 = law.getState(new AbsoluteDate(law.getReferenceDate(), 10.0),
                                          pv, FramesFactory.getEME2000()).getRotation();
        Assert.assertEquals(10 * rate, Rotation.distance(attitude1, law.getReferenceAttitude().getRotation()), 1.0e-10);
        Rotation attitude2 = law.getState(new AbsoluteDate(law.getReferenceDate(), -20.0),
                                          pv, FramesFactory.getEME2000()).getRotation();
        Assert.assertEquals(20 * rate, Rotation.distance(attitude2, law.getReferenceAttitude().getRotation()), 1.0e-10);
        Assert.assertEquals(30 * rate, Rotation.distance(attitude2, attitude1), 1.0e-10);
        Rotation attitude3 = law.getState(new AbsoluteDate(law.getReferenceDate(), 720.0),
                                          pv, FramesFactory.getEME2000()).getRotation();
        Assert.assertEquals(0, Rotation.distance(attitude3, law.getReferenceAttitude().getRotation()), 1.0e-10);

    }

    @Before
    public void setUp() {
        Utils.setDataRoot("regular-data");
    }

}

