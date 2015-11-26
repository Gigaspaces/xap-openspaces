package org.openspaces.spatial.internal;

import com.gigaspaces.config.ConfigurationException;
import com.gigaspaces.internal.server.space.SpaceConfigReader;
import com.j_spaces.kernel.log.JProperties;
import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.context.jts.JtsSpatialContext;
import com.spatial4j.core.shape.impl.RectangleImpl;
import org.apache.lucene.spatial.SpatialStrategy;
import org.apache.lucene.spatial.bbox.BBoxStrategy;
import org.apache.lucene.spatial.prefix.RecursivePrefixTreeStrategy;
import org.apache.lucene.spatial.prefix.tree.GeohashPrefixTree;
import org.apache.lucene.spatial.prefix.tree.QuadPrefixTree;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * @author Yohana Khoury
 * @since 11.0
 */
public class LuceneConfigurationTest {
    private final static String SPACENAME = "dummyspace";
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        JProperties.setSpaceProperties(SPACENAME, new Properties());
    }

    @After
    public void afterTest() {
        //clear directories
        File directoryA = new File(System.getProperty("user.home")+"/A");
        if (directoryA.exists()) {
            directoryA.delete();
        }
    }

    @Test
    public void testDefaults()  {
        LuceneConfiguration luceneConfiguration = new LuceneConfiguration();
        SpaceConfigReader spaceConfigReader = new SpaceConfigReader(SPACENAME);
        try {
            luceneConfiguration.initialize(spaceConfigReader);
        } catch (Exception e) {
            Assert.fail("Should not throw exception: " + e.toString());
        }

        //test directory
        Directory directory = null;
        try {
            directory = luceneConfiguration.getDirectory("A");
        } catch (IOException e) {
            Assert.fail("Should not throw exception: " + e.toString());
        }
        Assert.assertEquals("MMapDirectory should be the default directory", MMapDirectory.class, directory.getClass());
        Assert.assertEquals("user.home should be the default location", System.getProperty("user.home"), luceneConfiguration.getLocation());
        try {
            Assert.assertEquals("MMapDirectory location should be user.home/A", new MMapDirectory(Paths.get(System.getProperty("user.home") + "/A")).getDirectory(), ((MMapDirectory) directory).getDirectory());
        } catch (IOException e) {
            Assert.fail("Should not throw exception: " + e.toString());
        }

        //test strategy
        SpatialStrategy strategy = luceneConfiguration.getStrategy("myfield");
        Assert.assertEquals("Default strategy should be RecursivePrefixTree", RecursivePrefixTreeStrategy.class, strategy.getClass());
        Assert.assertEquals("Strategy's spatial context should be GEO", JtsSpatialContext.GEO, strategy.getSpatialContext());
        Assert.assertTrue("DistErrPct default for strategy should be 0.025", 0.025 == ((RecursivePrefixTreeStrategy) strategy).getDistErrPct());

        //test spatialprefixtree
        Assert.assertEquals("GeohashPrefixTree should be the default spatial prefix tree for strategy", GeohashPrefixTree.class, ((RecursivePrefixTreeStrategy) strategy).getGrid().getClass());
        Assert.assertEquals("MaxLevels should be 11 as default", 11, ((RecursivePrefixTreeStrategy) strategy).getGrid().getMaxLevels());

        //test spatialcontext
        Assert.assertEquals("Default spatialcontext should be JTS_GEO", JtsSpatialContext.GEO, luceneConfiguration.getSpatialContext());
    }

    @Test
    public void testInvalidDirectoryType() {
        LuceneConfiguration luceneConfiguration = new LuceneConfiguration();
        SpaceConfigReader spaceConfigReader = new SpaceConfigReader(SPACENAME);
        spaceConfigReader.setSpaceProperty("geospatial.lucene.storage.directory-type", "A");
        try {
            luceneConfiguration.initialize(spaceConfigReader);
            Assert.fail("A ConfigurationException should be thrown");
        } catch (ConfigurationException e) {
            //OK
            Assert.assertEquals("Unsupported directory type A. Options are: [MMapDirectory, RAMDirectory]", e.getMessage());
        } catch (Exception e) {
            Assert.fail("Expecting ConfigurationException but got " + e.getClass().getTypeName());
        }
    }

    @Test
    public void testRAMDirectoryTypeCaseInsensitive() {
        LuceneConfiguration luceneConfiguration = new LuceneConfiguration();
        SpaceConfigReader spaceConfigReader = new SpaceConfigReader(SPACENAME);
        spaceConfigReader.setSpaceProperty("geospatial.lucene.storage.directory-type", "Ramdirectory");
        try {
            luceneConfiguration.initialize(spaceConfigReader);
        } catch (Exception e) {
            Assert.fail("Should not throw exception: " + e.toString());
        }

        Directory directory = null;
        try {
            directory = luceneConfiguration.getDirectory("unused");
        } catch (IOException e) {
            Assert.fail("Should not throw exception: " + e.toString());
        }
        Assert.assertEquals("Unexpected Directory type", RAMDirectory.class, directory.getClass());
    }

    @Test
    public void testMMapDirectoryTypeAndLocation() throws IOException {
        LuceneConfiguration luceneConfiguration = new LuceneConfiguration();
        SpaceConfigReader spaceConfigReader = new SpaceConfigReader(SPACENAME);
        spaceConfigReader.setSpaceProperty("geospatial.lucene.storage.directory-type", "MMapDirectory");
        spaceConfigReader.setSpaceProperty("geospatial.lucene.storage.location", temporaryFolder.getRoot().getAbsolutePath()+"/tempdir");
        try {
            luceneConfiguration.initialize(spaceConfigReader);
        } catch (Exception e) {
            Assert.fail("Should not throw exception: " + e.toString());
        }

        Directory directory = luceneConfiguration.getDirectory("subfolder");

        Assert.assertEquals("Unexpected Directory type", MMapDirectory.class, directory.getClass());
        Assert.assertEquals(temporaryFolder.getRoot().getAbsolutePath()+"/tempdir/subfolder", ((MMapDirectory) directory).getDirectory().toFile().getAbsolutePath());
    }

    @Test
    public void testInvalidSpatialContextTree() {
        LuceneConfiguration luceneConfiguration = new LuceneConfiguration();
        SpaceConfigReader spaceConfigReader = new SpaceConfigReader(SPACENAME);
        spaceConfigReader.setSpaceProperty("geospatial.lucene.strategy.spatial-prefix-tree", "invalidValue");
        try {
            luceneConfiguration.initialize(spaceConfigReader);
            Assert.fail("A ConfigurationException should be thrown");
        } catch (ConfigurationException e) {
            //OK
            Assert.assertEquals("Unsupported spatial prefix tree type: invalidValue. Options are: [GeohashPrefixTree, QuadPrefixTree]", e.getMessage());
        } catch (Exception e) {
            Assert.fail("Expecting ConfigurationException but got " + e.getClass().getTypeName());
        }
    }

    @Test
    public void testSpatialContextTreeQuadPrefixTreeAndMaxLevels() {
        LuceneConfiguration luceneConfiguration = new LuceneConfiguration();
        SpaceConfigReader spaceConfigReader = new SpaceConfigReader(SPACENAME);
        spaceConfigReader.setSpaceProperty("geospatial.lucene.strategy.spatial-prefix-tree", "QuadPrefixTree");
        spaceConfigReader.setSpaceProperty("geospatial.lucene.strategy.spatial-prefix-tree.max-levels", "20");

        try {
            luceneConfiguration.initialize(spaceConfigReader);
        } catch (Exception e) {
            Assert.fail("Should not throw exception: " + e.toString());
        }

        SpatialStrategy strategy = luceneConfiguration.getStrategy("myField");
        Assert.assertEquals("Unexpected spatial prefix tree", QuadPrefixTree.class, ((RecursivePrefixTreeStrategy) strategy).getGrid().getClass());
        Assert.assertEquals("MaxLevels should be 20 as default", 20, ((RecursivePrefixTreeStrategy) strategy).getGrid().getMaxLevels());
    }

    @Test
    public void testInvalidSpatialContext() {
        LuceneConfiguration luceneConfiguration = new LuceneConfiguration();
        SpaceConfigReader spaceConfigReader = new SpaceConfigReader(SPACENAME);
        spaceConfigReader.setSpaceProperty("geospatial.lucene.spatial-context", "dummy");

        try {
            luceneConfiguration.initialize(spaceConfigReader);
            Assert.fail("A ConfigurationException should be thrown");
        } catch (ConfigurationException e) {
            //OK
            Assert.assertEquals("Unsupported spatial context type dummy. Options are: [GEO, NON_GEO, JTS_GEO, JTS_NON_GEO]", e.getMessage());
        } catch (Exception e) {
            Assert.fail("Expecting ConfigurationException but got " + e.getClass().getTypeName());
        }
    }

    @Test
    public void testSpatialContextGEO() {
        LuceneConfiguration luceneConfiguration = new LuceneConfiguration();
        SpaceConfigReader spaceConfigReader = new SpaceConfigReader(SPACENAME);
        spaceConfigReader.setSpaceProperty("geospatial.lucene.spatial-context", "GEO");

        try {
            luceneConfiguration.initialize(spaceConfigReader);
        } catch (Exception e) {
            Assert.fail("Should not throw exception: " + e.toString());
        }

        Assert.assertEquals("Unexpected spatial context", SpatialContext.GEO, luceneConfiguration.getSpatialContext());
        Assert.assertEquals("Expecting geo spatial context", true, luceneConfiguration.getSpatialContext().isGeo());
    }

    @Test
    public void testSpatialContextNonGEO() {
        LuceneConfiguration luceneConfiguration = new LuceneConfiguration();
        SpaceConfigReader spaceConfigReader = new SpaceConfigReader(SPACENAME);
        spaceConfigReader.setSpaceProperty("geospatial.lucene.strategy.spatial-prefix-tree", "QuadPrefixTree");
        spaceConfigReader.setSpaceProperty("geospatial.lucene.spatial-context", "NON_GEO");

        try {
            luceneConfiguration.initialize(spaceConfigReader);
        } catch (Exception e) {
            Assert.fail("Should not throw exception: " + e.toString());
        }

        Assert.assertEquals("Unexpected spatial context", SpatialContext.GEO.getClass(), luceneConfiguration.getSpatialContext().getClass());
        Assert.assertEquals("Expecting geo spatial context", false, luceneConfiguration.getSpatialContext().isGeo());
    }

    @Test
    public void testSpatialContextJTSNonGEO() {
        LuceneConfiguration luceneConfiguration = new LuceneConfiguration();
        SpaceConfigReader spaceConfigReader = new SpaceConfigReader(SPACENAME);
        spaceConfigReader.setSpaceProperty("geospatial.lucene.strategy.spatial-prefix-tree", "QuadPrefixTree");
        spaceConfigReader.setSpaceProperty("geospatial.lucene.spatial-context", "JTS_NON_GEO");

        try {
            luceneConfiguration.initialize(spaceConfigReader);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Should not throw exception: " + e.toString());
        }

        Assert.assertEquals("Unexpected spatial context", JtsSpatialContext.GEO.getClass(), luceneConfiguration.getSpatialContext().getClass());
        Assert.assertEquals("Expecting geo spatial context", false, luceneConfiguration.getSpatialContext().isGeo());
    }

    @Test
    public void testSpatialContextGEOInvalidWorldBoundsPropertyValue() {
        LuceneConfiguration luceneConfiguration = new LuceneConfiguration();
        SpaceConfigReader spaceConfigReader = new SpaceConfigReader(SPACENAME);
        spaceConfigReader.setSpaceProperty("geospatial.lucene.spatial-context", "GEO");
        spaceConfigReader.setSpaceProperty("geospatial.lucene.spatial-context.world-bounds", "invalidvaluehere");

        try {
            luceneConfiguration.initialize(spaceConfigReader);
            Assert.fail("A ConfigurationException should be thrown");
        } catch (ConfigurationException e) {
            //OK
            Assert.assertEquals("World bounds [invalidvaluehere] must be of format: minX, maxX, minY, maxY", e.getMessage());
        } catch (Exception e) {
            Assert.fail("Expecting ConfigurationException but got " + e.getClass().getTypeName());
        }
    }


    @Test
    public void testSpatialContextNONGEOInvalidWorldBoundsValues() {
        LuceneConfiguration luceneConfiguration = new LuceneConfiguration();
        SpaceConfigReader spaceConfigReader = new SpaceConfigReader(SPACENAME);
        spaceConfigReader.setSpaceProperty("geospatial.lucene.spatial-context", "NON_GEO");
        spaceConfigReader.setSpaceProperty("geospatial.lucene.spatial-context.world-bounds", "1,7,9,1");

        try {
            luceneConfiguration.initialize(spaceConfigReader);
            Assert.fail("A ConfigurationException should be thrown");
        } catch (ConfigurationException e) {
            //OK
            Assert.assertEquals("Values of world bounds [minX, maxX, minY, maxY]=[1,7,9,1] must meet: minX<=maxX, minY<=maxY", e.getMessage());
        } catch (Exception e) {
            Assert.fail("Expecting ConfigurationException but got " + e.getClass().getTypeName());
        }
    }


    @Test
    public void testSpatialContextJTSGEOInvalidWorldBoundsStringValue() {
        LuceneConfiguration luceneConfiguration = new LuceneConfiguration();
        SpaceConfigReader spaceConfigReader = new SpaceConfigReader(SPACENAME);
        spaceConfigReader.setSpaceProperty("geospatial.lucene.spatial-context", "JTS_GEO");
        spaceConfigReader.setSpaceProperty("geospatial.lucene.spatial-context.world-bounds", "1,7,1,4a");

        try {
            luceneConfiguration.initialize(spaceConfigReader);
            Assert.fail("A ConfigurationException should be thrown");
        } catch (ConfigurationException e) {
            //OK
            Assert.assertEquals("World bounds parameters must numbers", e.getMessage());
        } catch (Exception e) {
            Assert.fail("Expecting ConfigurationException but got " + e.getClass().getTypeName());
        }
    }

    @Test
    public void testSpatialContextJTSNONGEO() {
        LuceneConfiguration luceneConfiguration = new LuceneConfiguration();
        SpaceConfigReader spaceConfigReader = new SpaceConfigReader(SPACENAME);
        spaceConfigReader.setSpaceProperty("geospatial.lucene.strategy.spatial-prefix-tree", "QuadPrefixTree");
        spaceConfigReader.setSpaceProperty("geospatial.lucene.spatial-context", "JTS_NON_GEO");
        spaceConfigReader.setSpaceProperty("geospatial.lucene.spatial-context.world-bounds", "1,10,-100,100");

        try {
            luceneConfiguration.initialize(spaceConfigReader);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Should not throw exception: " + e.toString());
        }

        Assert.assertEquals("Unexpected spatial context", JtsSpatialContext.GEO.getClass(), luceneConfiguration.getSpatialContext().getClass());
        Assert.assertEquals("Expecting geo spatial context", false, luceneConfiguration.getSpatialContext().isGeo());
        Assert.assertEquals("Unexpected spatial context world bound", new RectangleImpl(1, 10, -100, 100, null), luceneConfiguration.getSpatialContext().getWorldBounds());
    }

    @Test
    public void testInvalidStrategy() {
        LuceneConfiguration luceneConfiguration = new LuceneConfiguration();
        SpaceConfigReader spaceConfigReader = new SpaceConfigReader(SPACENAME);
        spaceConfigReader.setSpaceProperty("geospatial.lucene.strategy", "mystrategy");

        try {
            luceneConfiguration.initialize(spaceConfigReader);
            Assert.fail("A ConfigurationException should be thrown");
        } catch (ConfigurationException e) {
            //OK
            Assert.assertEquals("Unsupported strategy mystrategy. Options are: [RecursivePrefixTree, BBox]", e.getMessage());
        } catch (Exception e) {
            Assert.fail("Expecting ConfigurationException but got " + e.getClass().getTypeName());
        }
    }

    @Test
    public void testStrategyRecursivePrefixTreeAndDistErrPct() {
        LuceneConfiguration luceneConfiguration = new LuceneConfiguration();
        SpaceConfigReader spaceConfigReader = new SpaceConfigReader(SPACENAME);
        spaceConfigReader.setSpaceProperty("geospatial.lucene.strategy", "RecursivePrefixTree");
        spaceConfigReader.setSpaceProperty("geospatial.lucene.strategy.spatial-prefix-tree", "GeohashPrefixTree");
        spaceConfigReader.setSpaceProperty("geospatial.lucene.strategy.spatial-prefix-tree.max-levels", "10");
        spaceConfigReader.setSpaceProperty("geospatial.lucene.strategy.distance-error-pct", "0.5");

        try {
            luceneConfiguration.initialize(spaceConfigReader);
        } catch (Exception e) {
            Assert.fail("Should not throw exception: " + e.toString());
        }

        Assert.assertEquals("Unexpected strategy type", RecursivePrefixTreeStrategy.class, luceneConfiguration.getStrategy("myField").getClass());
        RecursivePrefixTreeStrategy strategy = (RecursivePrefixTreeStrategy) luceneConfiguration.getStrategy("myField");
        Assert.assertEquals("Unexpected spatial prefix tree", GeohashPrefixTree.class, strategy.getGrid().getClass());
        Assert.assertEquals("MaxLevels should be 10 as default", 10, strategy.getGrid().getMaxLevels());
        Assert.assertTrue("Expecting distance-error-pct to be 0.5", 0.5 == strategy.getDistErrPct());
    }

    @Test
    public void testStrategyBBox() {
        LuceneConfiguration luceneConfiguration = new LuceneConfiguration();
        SpaceConfigReader spaceConfigReader = new SpaceConfigReader(SPACENAME);
        spaceConfigReader.setSpaceProperty("geospatial.lucene.strategy", "BBox");
        spaceConfigReader.setSpaceProperty("geospatial.lucene.spatial-context", "NON_GEO");


        try {
            luceneConfiguration.initialize(spaceConfigReader);
        } catch (Exception e) {
            Assert.fail("Should not throw exception: " + e.toString());
        }

        Assert.assertEquals("Unexpected strategy type", BBoxStrategy.class, luceneConfiguration.getStrategy("myField").getClass());
        Assert.assertEquals("Unexpected spatial context", SpatialContext.GEO.getClass(), luceneConfiguration.getSpatialContext().getClass());
        Assert.assertEquals("Expecting geo spatial context", false, luceneConfiguration.getSpatialContext().isGeo());
    }

}