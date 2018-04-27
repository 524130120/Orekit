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
package org.orekit.data;


import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hipparchus.exception.DummyLocalizable;
import org.junit.Assert;
import org.junit.Test;
import org.orekit.Utils;
import org.orekit.errors.OrekitException;

public class DataProvidersManagerTest {

    @Test
    public void testDefaultConfiguration() throws OrekitException {
        System.setProperty(DataProvidersManager.OREKIT_DATA_PATH, getPath("regular-data"));
        CountingLoader crawler = new CountingLoader(false);
        DataProvidersManager.getInstance().clearProviders();
        Assert.assertFalse(DataProvidersManager.getInstance().isSupported(new DirectoryCrawler(new File(getPath("regular-data")))));
        Assert.assertTrue(DataProvidersManager.getInstance().feed(".*", crawler));
        Assert.assertEquals(22, crawler.getCount());
    }

    @Test
    public void testLoadMonitoring() throws OrekitException {
        System.setProperty(DataProvidersManager.OREKIT_DATA_PATH, getPath("regular-data"));
        DataProvidersManager manager = DataProvidersManager.getInstance();
        manager.clearProviders();
        manager.clearLoadedDataNames();
        Assert.assertFalse(manager.isSupported(new DirectoryCrawler(new File(getPath("regular-data")))));
        Assert.assertEquals(0, manager.getLoadedDataNames().size());
        CountingLoader tleCounter = new CountingLoader(false);
        Assert.assertTrue(manager.feed(".*\\.tle$", tleCounter));
        Assert.assertEquals(4, tleCounter.getCount());
        Assert.assertEquals(4, manager.getLoadedDataNames().size());
        CountingLoader de405Counter = new CountingLoader(false);
        Assert.assertTrue(manager.feed(".*\\.405$", de405Counter));
        Assert.assertEquals(4, de405Counter.getCount());
        Assert.assertEquals(8, manager.getLoadedDataNames().size());
        manager.clearLoadedDataNames();
        Assert.assertEquals(0, manager.getLoadedDataNames().size());
    }

    @Test
    public void testLoadFailure() {
        System.setProperty(DataProvidersManager.OREKIT_DATA_PATH, getPath("regular-data"));
        DataProvidersManager.getInstance().clearProviders();
        CountingLoader crawler = new CountingLoader(true);
        try {
            DataProvidersManager.getInstance().feed(".*", crawler);
            Assert.fail("an exception should have been thrown");
        } catch (OrekitException oe) {
            // expected
        }
        Assert.assertEquals(22, crawler.getCount());
    }

    @Test
    public void testEmptyProperty() throws OrekitException {
        System.setProperty(DataProvidersManager.OREKIT_DATA_PATH, "");
        CountingLoader crawler = new CountingLoader(false);
        DataProvidersManager.getInstance().clearProviders();
        DataProvidersManager.getInstance().feed(".*", crawler);
        Assert.assertEquals(0, crawler.getCount());
    }

    @Test(expected=OrekitException.class)
    public void testInexistentDirectory() throws OrekitException {
        File inexistent = new File(getPath("regular-data"), "inexistent");
        System.setProperty(DataProvidersManager.OREKIT_DATA_PATH, inexistent.getAbsolutePath());
        CountingLoader crawler = new CountingLoader(false);
        DataProvidersManager.getInstance().clearProviders();
        DataProvidersManager.getInstance().feed(".*", crawler);
    }

    @Test(expected=OrekitException.class)
    public void testInexistentZipArchive() throws OrekitException {
        File inexistent = new File(getPath("regular-data"), "inexistent.zip");
        System.setProperty(DataProvidersManager.OREKIT_DATA_PATH, inexistent.getAbsolutePath());
        CountingLoader crawler = new CountingLoader(false);
        DataProvidersManager.getInstance().clearProviders();
        DataProvidersManager.getInstance().feed(".*", crawler);
    }

    @Test(expected=OrekitException.class)
    public void testNeitherDirectoryNorZip() throws OrekitException {
        System.setProperty(DataProvidersManager.OREKIT_DATA_PATH, getPath("regular-data/UTC-TAI.history"));
        CountingLoader crawler = new CountingLoader(false);
        DataProvidersManager.getInstance().clearProviders();
        DataProvidersManager.getInstance().feed(".*", crawler);
    }

    @Test
    public void testListModification() throws OrekitException {
        System.setProperty(DataProvidersManager.OREKIT_DATA_PATH, getPath("regular-data"));
        CountingLoader crawler = new CountingLoader(false);
        DataProvidersManager manager = DataProvidersManager.getInstance();
        manager.clearProviders();
        Assert.assertFalse(manager.isSupported(new DirectoryCrawler(new File(getPath("regular-data")))));
        Assert.assertTrue(manager.feed(".*", crawler));
        Assert.assertTrue(crawler.getCount() > 0);
        List<DataProvider> providers = manager.getProviders();
        Assert.assertEquals(1, providers.size());
        for (DataProvider provider : providers) {
            Assert.assertTrue(manager.isSupported(provider));
        }
        Assert.assertNotNull(manager.removeProvider(providers.get(0)));
        Assert.assertEquals(0, manager.getProviders().size());
        DataProvider provider = new DataProvider() {
            public boolean feed(Pattern supported, DataLoader visitor) throws OrekitException {
                return true;
            }
        };
        manager.addProvider(provider);
        Assert.assertEquals(1, manager.getProviders().size());
        manager.addProvider(provider);
        Assert.assertEquals(2, manager.getProviders().size());
        Assert.assertNotNull(manager.removeProvider(provider));
        Assert.assertEquals(1, manager.getProviders().size());
        Assert.assertNull(manager.removeProvider(new DataProvider() {
            public boolean feed(Pattern supported, DataLoader visitor) throws OrekitException {
                throw new OrekitException(new DummyLocalizable("oops!"));
            }
        }));
        Assert.assertEquals(1, manager.getProviders().size());
        Assert.assertNotNull(manager.removeProvider(manager.getProviders().get(0)));
        Assert.assertEquals(0, manager.getProviders().size());
    }

    @Test
    public void testComplexPropertySetting() throws OrekitException {
        String sep = System.getProperty("path.separator");
        File top = new File(getPath("regular-data"));
        File dir1 = new File(top, "de405-ephemerides");
        File dir2 = new File(new File(top, "Earth-orientation-parameters"), "monthly");
        System.setProperty(DataProvidersManager.OREKIT_DATA_PATH,
                           dir1 + sep + sep + sep + sep + dir2);
        DataProvidersManager.getInstance().clearProviders();

        CountingLoader crawler = new CountingLoader(false);
        Assert.assertTrue(DataProvidersManager.getInstance().feed(".*\\.405$", crawler));
        Assert.assertEquals(4, crawler.getCount());

        crawler = new CountingLoader(false);
        Assert.assertTrue(DataProvidersManager.getInstance().feed(".*\\.txt$", crawler));
        Assert.assertEquals(1, crawler.getCount());

        crawler = new CountingLoader(false);
        Assert.assertTrue(DataProvidersManager.getInstance().feed("bulletinb_.*\\.txt$", crawler));
        Assert.assertEquals(2, crawler.getCount());

    }

    @Test
    public void testMultiZip() throws OrekitException {
        System.setProperty(DataProvidersManager.OREKIT_DATA_PATH, getPath("zipped-data/multizip.zip"));
        CountingLoader crawler = new CountingLoader(false);
        DataProvidersManager.getInstance().clearProviders();
        Assert.assertTrue(DataProvidersManager.getInstance().feed(".*\\.txt$", crawler));
        Assert.assertEquals(6, crawler.getCount());
    }

    @Test
    public void testSimpleFilter() throws OrekitException {
        Utils.setDataRoot("regular-data");
        CountingFilter filter = new CountingFilter();
        DataProvidersManager.getInstance().addFilter(filter);
        CountingLoader crawler = new CountingLoader(false);
        Assert.assertTrue(DataProvidersManager.getInstance().feed(".*", crawler));
        Assert.assertEquals(22, crawler.getCount());
        Assert.assertEquals(22, filter.getFilteredCount());
        Assert.assertEquals(22, filter.getOpenedCount());
    }

    @Test
    public void testMultiLayerFilter() throws OrekitException {
        Utils.setDataRoot("regular-data");
        final int layers = 10;
        MultiLayerFilter filter = new MultiLayerFilter(layers);
        DataProvidersManager.getInstance().addFilter(filter);
        CountingLoader crawler = new CountingLoader(false);
        Assert.assertTrue(DataProvidersManager.getInstance().feed(".*", crawler));
        Assert.assertEquals(22, crawler.getCount());
        Assert.assertEquals(22 * layers, filter.getOpenedCount());
    }

    private static class CountingLoader implements DataLoader {
        private boolean shouldFail;
        private int count;
        public CountingLoader(boolean shouldFail) {
            this.shouldFail = shouldFail;
            count = 0;
        }
        public boolean stillAcceptsData() {
            return true;
        }
        public void loadData(InputStream input, String name)
            throws OrekitException {
            ++count;
            if (shouldFail) {
                throw new OrekitException(new DummyLocalizable("intentional failure"));
            }
        }
        public int getCount() {
            return count;
        }
    }

    private static class CountingFilter implements DataFilter {
        private Map<NamedData, NamedData> filtered;
        private int opened;
        public CountingFilter() {
            filtered = new IdentityHashMap<>();
            opened   = 0;
        }
        public NamedData filter(NamedData original) {
            if (filtered.containsKey(original)) {
                return original;
            } else {
                NamedData f = new NamedData(original.getName(),
                                            () -> {
                                                ++opened;
                                                return original.getStreamOpener().openStream();
                                            });
                filtered.put(f, f);
                return f;
            }
        }
        public int getFilteredCount() {
            return filtered.size();
        }
        public int getOpenedCount() {
            return opened;
        }
    }

    private static class MultiLayerFilter implements DataFilter {
        private static final String  PREFIX  = "multilayer-";
        private static final Pattern PATTERN = Pattern.compile(PREFIX + "(\\d+)-(.*)");
        private final int layers;
        private int opened;
        public MultiLayerFilter(final int layers) {
            this.layers = layers;
            this.opened = 0;
        }
        public NamedData filter(final NamedData original) {
            Matcher matcher = PATTERN.matcher(original.getName());
            int level = 0;
            String baseName = original.getName();
            if (matcher.matches()) {
                level = Integer.parseInt(matcher.group(1));
                baseName = matcher.group(2);
            }
            if (level++ < layers) {
                // add one filtering layer
                return new NamedData(PREFIX + level + "-" + baseName,
                                     () -> {
                                         ++opened;
                                         return original.getStreamOpener().openStream();
                                     });
            } else {
                // final layer, don't filter anymore
                return original;
            }
        }
        public int getOpenedCount() {
            return opened;
        }
    }

    private String getPath(String resourceName) {
        try {
            ClassLoader loader = DirectoryCrawlerTest.class.getClassLoader();
            return loader.getResource(resourceName).toURI().getPath();
        } catch (URISyntaxException e) {
            Assert.fail(e.getLocalizedMessage());
            return null;
        }
    }

}
