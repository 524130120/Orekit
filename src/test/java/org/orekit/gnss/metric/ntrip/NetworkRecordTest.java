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
package org.orekit.gnss.metric.ntrip;

import org.junit.Assert;
import org.junit.Test;

public class NetworkRecordTest {

    private static String IGS        = "NET;IGS;IGS;B;N;https://igs.bkg.bund.de/root_ftp/NTRIP/neteams/neteamlist_igs-ip.htm;https://igs.bkg.bund.de:443/root_ftp/IGS/station/rnxskl/;http://register.rtcm-ntrip.org;none";
    private static String MISC       = "NET;MISC;BKG;B;N;http://igs.bkg.bund.de/root_ftp/NTRIP/neteams/neteamlist_igs-ip.htm;https://igs.bkg.bund.de:443/root_ftp/MISC/station/rnxskl/;http://register.rtcm-ntrip.org;none";

    @Test
    public void testIGS() {
        final NetworkRecord net = new NetworkRecord(IGS);
        Assert.assertEquals(RecordType.NET,                                                         net.getRecordType());
        Assert.assertEquals("IGS",                                                                  net.getNetworkIdentifier());
        Assert.assertEquals("IGS",                                                                  net.getOperator());
        Assert.assertEquals(Authentication.BASIC,                                                   net.getAuthentication());
        Assert.assertEquals(false,                                                                  net.areFeesRequired());
        Assert.assertEquals("https://igs.bkg.bund.de/root_ftp/NTRIP/neteams/neteamlist_igs-ip.htm", net.getNetworkInfoAddress());
        Assert.assertEquals("https://igs.bkg.bund.de:443/root_ftp/IGS/station/rnxskl/",             net.getStreamInfoAddress());
        Assert.assertEquals("http://register.rtcm-ntrip.org",                                       net.getRegistrationAddress());
        Assert.assertEquals("none",                                                                 net.getMisc());
    }

    @Test
    public void testMISC() {
        final NetworkRecord net = new NetworkRecord(MISC);
        Assert.assertEquals(RecordType.NET,                                                         net.getRecordType());
        Assert.assertEquals("MISC",                                                                 net.getNetworkIdentifier());
        Assert.assertEquals("BKG",                                                                  net.getOperator());
        Assert.assertEquals(Authentication.BASIC,                                                   net.getAuthentication());
        Assert.assertEquals(false,                                                                  net.areFeesRequired());
        Assert.assertEquals("http://igs.bkg.bund.de/root_ftp/NTRIP/neteams/neteamlist_igs-ip.htm",  net.getNetworkInfoAddress());
        Assert.assertEquals("https://igs.bkg.bund.de:443/root_ftp/MISC/station/rnxskl/",            net.getStreamInfoAddress());
        Assert.assertEquals("http://register.rtcm-ntrip.org",                                       net.getRegistrationAddress());
        Assert.assertEquals("none",                                                                 net.getMisc());
    }

    @Test
    public void testDigestAuthentication() {
        final NetworkRecord net = new NetworkRecord(IGS.replace(";B;", ";D;"));
        Assert.assertEquals(Authentication.DIGEST, net.getAuthentication());
    }

    @Test
    public void testNoAuthentication() {
        final NetworkRecord net = new NetworkRecord(IGS.replace(";B;", ";N;"));
        Assert.assertEquals(Authentication.NONE, net.getAuthentication());
    }

    @Test
    public void testRequiresFees() {
        final NetworkRecord net = new NetworkRecord(IGS.replace(";B;N;", ";B;Y;"));
        Assert.assertTrue(net.areFeesRequired());
    }

}
