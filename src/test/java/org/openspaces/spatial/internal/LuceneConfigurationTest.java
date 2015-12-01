package org.openspaces.spatial.internal;

import com.gigaspaces.config.ConfigurationException;
import com.gigaspaces.internal.server.space.SpaceConfigReader;
import com.j_spaces.kernel.log.JProperties;
import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.context.jts.JtsSpatialContext;
import com.spatial4j.core.shape.impl.RectangleImpl;
import org.apache.lucene.spatial.SpatialStrategy;
import org.apache.lucene.spatial.bbox.BBoxStrategy;
import org.apache.lucene.spatial.composite.CompositeSpatialStrategy;
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

    private String getWorkingDir() {
        return temporaryFolder.getRoot().getAbsolutePath();
    }

    @Test
    public void testDefaults()  {
        LuceneConfiguration luceneConfiguration = new LuceneConfiguration();
        SpaceConfigReader spaceConfigReader = new SpaceConfigReader(SPACENAME);
        try {
            luceneConfiguration.initialize(spaceConfigReader, getWorkingDir());
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
        String expectedLocation = getWorkingDir()+File.separator+"luceneIndex";
        Assert.assertEquals("WorkingDir/luceneIndex ("+expectedLocation+") should be the default location", expectedLocation, luceneConfiguration.getLocation());
        try {
            Assert.assertEquals("MMapDirectory location should be workingDir/luceneIndex/A "+expectedLocation+"/A", new MMapDirectory(Paths.get(expectedLocation + "/A")).getDirectory(), ((MMapDirectory) directory).getDirectory());
        } catch (IOException e) {
            Assert.fail("Should not throw exception: " + e.toString());
        }

        //test strategy
        SpatialStrategy strategy = luceneConfiguration.getStrategy("myfield");
        Assert.assertEquals("Default strategy should be RecursivePrefixTree", RecursivePrefixTreeStrategy.class, strategy.getClass());
        Assert.assertEquals("Unexpected spatial context", JtsSpatialContext.class, luceneConfiguration.getSpatialContext().getClass());
        Assert.assertTrue("DistErrPct default for strategy should be 0.025", 0.025 == ((RecursivePrefixTreeStrategy) strategy).getDistErrPct());

        //test spatialprefixtree
        Assert.assertEquals("GeohashPrefixTree should be the default spatial prefix tree for strategy", GeohashPrefixTree.class, ((RecursivePrefixTreeStrategy) strategy).getGrid().getClass());
        Assert.assertEquals("MaxLevels should be 11 as default", 11, ((RecursivePrefixTreeStrategy) strategy).getGrid().getMaxLevels());

        //test spatialcontext
        Assert.assertEquals("Default spatialcontext should be JTS", JtsSpatialContext.class, luceneConfiguration.getSpatialContext().getClass());
        Assert.assertEquals("Default spatialcontext.geo should be true", true, luceneConfiguration.getSpatialContext().isGeo());

    }

    @Test
    public void testInvalidDirectoryType() {
        LuceneConfiguration luceneConfiguration = new LuceneConfiguration();
        SpaceConfigReader spaceConfigReader = new SpaceConfigReader(SPACENAME);
        spaceConfigReader.setSpaceProperty("spatial.lucene.storage.directory-type", "A");
        try {
            luceneConfiguration.initialize(spaceConfigReader, getWorkingDir());
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
        spaceConfigReader.setSpaceProperty("spatial.lucene.storage.directory-type", "Ramdirectory");
        try {
            luceneConfiguration.initialize(spaceConfigReader, getWorkingDir());
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
        spaceConfigReader.setSpaceProperty("spatial.lucene.storage.directory-type", "MMapDirectory");
        spaceConfigReader.setSpaceProperty("spatial.lucene.storage.location", temporaryFolder.getRoot().getAbsolutePath()+"/tempdir");
        try {
            luceneConfiguration.initialize(spaceConfigReader, getWorkingDir());
        } catch (Exception e) {
            Assert.fail("Should not throw exception: " + e.toString());
        }

        Directory directory = luceneConfiguration.getDirectory("subfolder");

        Assert.assertEquals("Unexpected Directory type", MMapDirectory.class, directory.getClass());
        Assert.assertEquals(temporaryFolder.getRoot().getAbsolutePath()+"/tempdir/subfolder", ((MMapDirectory) directory).getDirectory().toFile().getAbsolutePath());
    }

    @Test
    public void testLocationNoWorkingDir() throws IOException {
        LuceneConfiguration luceneConfiguration = new LuceneConfiguration();
        SpaceConfigReader spaceConfigReader = new SpaceConfigReader(SPACENAME);
        spaceConfigReader.setSpaceProperty("spatial.lucene.storage.directory-type", "MMapDirectory");
        try {
            //null as second parameter simulates there is no working dir (not pu)
            luceneConfiguration.initialize(spaceConfigReader, null);
        } catch (Exception e) {
            Assert.fail("Should not throw exception: " + e.toString());
        }

        Directory directory = luceneConfiguration.getDirectory("subfolder");

        Assert.assertEquals("Unexpected Directory type", MMapDirectory.class, directory.getClass());
        Assert.assertEquals(System.getProperty("user.dir")+"/luceneIndex/subfolder", ((MMapDirectory) directory).getDirectory().toFile().getAbsolutePath());
    }

    @Test
    public void testInvalidSpatialContextTree() {
        LuceneConfiguration luceneConfiguration = new LuceneConfiguration();
        SpaceConfigReader spaceConfigReader = new SpaceConfigReader(SPACENAME);
        spaceConfigReader.setSpaceProperty("spatial.lucene.strategy.spatial-prefix-tree", "invalidValue");
        try {
            luceneConfiguration.initialize(spaceConfigReader, getWorkingDir());
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
        spaceConfigReader.setSpaceProperty("spatial.lucene.strategy.spatial-prefix-tree", "QuadPrefixTree");
        spaceConfigReader.setSpaceProperty("spatial.lucene.strategy.spatial-prefix-tree.max-levels", "20");

        try {
            luceneConfiguration.initialize(spaceConfigReader, getWorkingDir());
        } catch (Exception e) {
            Assert.fail("Should not throw exception: " + e.toString());
        }

        SpatialStrategy strategy = luceneConfiguration.getStrategy("myField");
        Assert.assertEquals("Unexpected spatial prefix tree", QuadPrefixTree.class, ((RecursivePrefixTreeStrategy) strategy).getGrid().getClass());
        Assert.assertEquals("MaxLevels should be 20", 20, ((RecursivePrefixTreeStrategy) strategy).getGrid().getMaxLevels());
    }

    @Test
    public void testInvalidSpatialContext() {
        LuceneConfiguration luceneConfiguration = new LuceneConfiguration();
        SpaceConfigReader spaceConfigReader = new SpaceConfigReader(SPACENAME);
        spaceConfigReader.setSpaceProperty("spatial.context", "dummy");

        try {
            luceneConfiguration.initialize(spaceConfigReader, getWorkingDir());
            Assert.fail("A ConfigurationException should be thrown");
        } catch (ConfigurationException e) {
            //OK
            Assert.assertEquals("Unsupported spatial context type dummy. Options are: [Spatial4J, JTS]", e.getMessage());
        } catch (Exception e) {
            Assert.fail("Expecting ConfigurationException but got " + e.getClass().getTypeName());
        }
    }

    @Test
    public void testSpatialContextGEO() {
        LuceneConfiguration luceneConfiguration = new LuceneConfiguration();
        SpaceConfigReader spaceConfigReader = new SpaceConfigReader(SPACENAME);
        spaceConfigReader.setSpaceProperty("spatial.context", "spatial4j");
        spaceConfigReader.setSpaceProperty("spatial.context.geo", "true");

        try {
            luceneConfiguration.initialize(spaceConfigReader, getWorkingDir());
        } catch (Exception e) {
            Assert.fail("Should not throw exception: " + e.toString());
        }

        Assert.assertEquals("Unexpected spatial context", SpatialContext.class, luceneConfiguration.getSpatialContext().getClass());
        Assert.assertEquals("Expecting geo spatial context", true, luceneConfiguration.getSpatialContext().isGeo());
    }

    @Test
    public void testSpatialContextNonGEO() {
        LuceneConfiguration luceneConfiguration = new LuceneConfiguration();
        SpaceConfigReader spaceConfigReader = new SpaceConfigReader(SPACENAME);
        spaceConfigReader.setSpaceProperty("spatial.lucene.strategy.spatial-prefix-tree", "QuadPrefixTree");
        spaceConfigReader.setSpaceProperty("spatial.context", "spatial4j");
        spaceConfigReader.setSpaceProperty("spatial.context.geo", "false");


        try {
            luceneConfiguration.initialize(spaceConfigReader, getWorkingDir());
        } catch (Exception e) {
            Assert.fail("Should not throw exception: " + e.toString());
        }

        Assert.assertEquals("Unexpected spatial context", SpatialContext.class, luceneConfiguration.getSpatialContext().getClass());
        Assert.assertEquals("Expecting geo spatial context", false, luceneConfiguration.getSpatialContext().isGeo());
    }

    @Test
    public void testSpatialContextJTSNonGEO() {
        LuceneConfiguration luceneConfiguration = new LuceneConfiguration();
        SpaceConfigReader spaceConfigReader = new SpaceConfigReader(SPACENAME);
        spaceConfigReader.setSpaceProperty("spatial.lucene.strategy.spatial-prefix-tree", "QuadPrefixTree");
        spaceConfigReader.setSpaceProperty("spatial.context", "jts");
        spaceConfigReader.setSpaceProperty("spatial.context.geo", "false");


        try {
            luceneConfiguration.initialize(spaceConfigReader, getWorkingDir());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Should not throw exception: " + e.toString());
        }

        Assert.assertEquals("Unexpected spatial context", JtsSpatialContext.class, luceneConfiguration.getSpatialContext().getClass());
        Assert.assertEquals("Expecting geo spatial context", false, luceneConfiguration.getSpatialContext().isGeo());
    }

    @Test
    public void testSpatialContextGEOInvalidWorldBoundsPropertyValue() {
        LuceneConfiguration luceneConfiguration = new LuceneConfiguration();
        SpaceConfigReader spaceConfigReader = new SpaceConfigReader(SPACENAME);
        spaceConfigReader.setSpaceProperty("spatial.context", "jts");
        spaceConfigReader.setSpaceProperty("spatial.context.geo", "true");
        spaceConfigReader.setSpaceProperty("spatial.context.world-bounds", "invalidvaluehere");

        try {
            luceneConfiguration.initialize(spaceConfigReader, getWorkingDir());
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
        spaceConfigReader.setSpaceProperty("spatial.context", "spatial4J");
        spaceConfigReader.setSpaceProperty("spatial.context.geo", "false");
        spaceConfigReader.setSpaceProperty("spatial.context.world-bounds", "1,7,9,1");

        try {
            luceneConfiguration.initialize(spaceConfigReader, getWorkingDir());
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
        spaceConfigReader.setSpaceProperty("spatial.context", "jts");
        spaceConfigReader.setSpaceProperty("spatial.context.geo", "true");
        spaceConfigReader.setSpaceProperty("spatial.context.world-bounds", "1,7,1,4a");

        try {
            luceneConfiguration.initialize(spaceConfigReader, getWorkingDir());
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
        spaceConfigReader.setSpaceProperty("spatial.lucene.strategy.spatial-prefix-tree", "QuadPrefixTree");
        spaceConfigReader.setSpaceProperty("spatial.context", "jts");
        spaceConfigReader.setSpaceProperty("spatial.context.geo", "false");
        spaceConfigReader.setSpaceProperty("spatial.context.world-bounds", "1,10,-100,100");

        try {
            luceneConfiguration.initialize(spaceConfigReader, getWorkingDir());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Should not throw exception: " + e.toString());
        }

        Assert.assertEquals("Unexpected spatial context", JtsSpatialContext.class, luceneConfiguration.getSpatialContext().getClass());
        Assert.assertEquals("Expecting geo spatial context", false, luceneConfiguration.getSpatialContext().isGeo());
        Assert.assertEquals("Unexpected spatial context world bound", new RectangleImpl(1, 10, -100, 100, null), luceneConfiguration.getSpatialContext().getWorldBounds());
    }

    @Test
    public void testInvalidStrategy() {
        LuceneConfiguration luceneConfiguration = new LuceneConfiguration();
        SpaceConfigReader spaceConfigReader = new SpaceConfigReader(SPACENAME);
        spaceConfigReader.setSpaceProperty("spatial.lucene.strategy", "mystrategy");

        try {
            luceneConfiguration.initialize(spaceConfigReader, getWorkingDir());
            Assert.fail("A ConfigurationException should be thrown");
        } catch (ConfigurationException e) {
            //OK
            Assert.assertEquals("Unsupported strategy mystrategy. Options are: [RecursivePrefixTree, BBox, Composite]", e.getMessage());
        } catch (Exception e) {
            Assert.fail("Expecting ConfigurationException but got " + e.getClass().getTypeName());
        }
    }

    @Test
    public void testStrategyRecursivePrefixTreeAndDistErrPct() {
        LuceneConfiguration luceneConfiguration = new LuceneConfiguration();
        SpaceConfigReader spaceConfigReader = new SpaceConfigReader(SPACENAME);
        spaceConfigReader.setSpaceProperty("spatial.lucene.strategy", "RecursivePrefixTree");
        spaceConfigReader.setSpaceProperty("spatial.lucene.strategy.spatial-prefix-tree", "GeohashPrefixTree");
        spaceConfigReader.setSpaceProperty("spatial.lucene.strategy.spatial-prefix-tree.max-levels", "10");
        spaceConfigReader.setSpaceProperty("spatial.lucene.strategy.distance-error-pct", "0.5");

        try {
            luceneConfiguration.initialize(spaceConfigReader, getWorkingDir());
        } catch (Exception e) {
            Assert.fail("Should not throw exception: " + e.toString());
        }

        Assert.assertEquals("Unexpected strategy type", RecursivePrefixTreeStrategy.class, luceneConfiguration.getStrategy("myField").getClass());
        RecursivePrefixTreeStrategy strategy = (RecursivePrefixTreeStrategy) luceneConfiguration.getStrategy("myField");
        Assert.assertEquals("Unexpected spatial prefix tree", GeohashPrefixTree.class, strategy.getGrid().getClass());
        Assert.assertEquals("MaxLevels should be 10", 10, strategy.getGrid().getMaxLevels());
        Assert.assertTrue("Expecting distance-error-pct to be 0.5", 0.5 == strategy.getDistErrPct());
    }

    @Test
    public void testStrategyBBox() {
        LuceneConfiguration luceneConfiguration = new LuceneConfiguration();
        SpaceConfigReader spaceConfigReader = new SpaceConfigReader(SPACENAME);
        spaceConfigReader.setSpaceProperty("spatial.lucene.strategy", "BBox");
        spaceConfigReader.setSpaceProperty("spatial.context", "spatial4J");
        spaceConfigReader.setSpaceProperty("spatial.context.geo", "false");


        try {
            luceneConfiguration.initialize(spaceConfigReader, getWorkingDir());
        } catch (Exception e) {
            Assert.fail("Should not throw exception: " + e.toString());
        }

        Assert.assertEquals("Unexpected strategy type", BBoxStrategy.class, luceneConfiguration.getStrategy("myField").getClass());
        Assert.assertEquals("Unexpected spatial context", SpatialContext.class, luceneConfiguration.getSpatialContext().getClass());
        Assert.assertEquals("Expecting geo spatial context", false, luceneConfiguration.getSpatialContext().isGeo());
    }

    @Test
    public void testStrategyComposite() {
        LuceneConfiguration luceneConfiguration = new LuceneConfiguration();
        SpaceConfigReader spaceConfigReader = new SpaceConfigReader(SPACENAME);
        spaceConfigReader.setSpaceProperty("spatial.lucene.strategy", "composite");
        spaceConfigReader.setSpaceProperty("spatial.context", "spatial4J");
        spaceConfigReader.setSpaceProperty("spatial.context.geo", "true");


        try {
            luceneConfiguration.initialize(spaceConfigReader, getWorkingDir());
        } catch (Exception e) {
            Assert.fail("Should not throw exception: " + e.toString());
        }

        Assert.assertEquals("Unexpected strategy type", CompositeSpatialStrategy.class, luceneConfiguration.getStrategy("myField").getClass());
        Assert.assertEquals("Unexpected spatial context", SpatialContext.class, luceneConfiguration.getSpatialContext().getClass());
        Assert.assertEquals("Expecting geo spatial context", true, luceneConfiguration.getSpatialContext().isGeo());
    }

}