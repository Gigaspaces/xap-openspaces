package org.openspaces.spatial.internal;

import com.gigaspaces.internal.server.space.SpaceConfigReader;
import com.j_spaces.kernel.log.JProperties;
import com.spatial4j.core.context.jts.JtsSpatialContext;
import org.apache.lucene.spatial.SpatialStrategy;
import org.apache.lucene.spatial.prefix.RecursivePrefixTreeStrategy;
import org.apache.lucene.spatial.prefix.tree.GeohashPrefixTree;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.Properties;

/**
 * @author Yohana Khoury
 * @since 11.0
 */
public class LuceneConfigurationTest {
    private final static String SPACENAME = "dummyspace";

    @Before
    public void setUp() throws Exception {
        JProperties.setSpaceProperties(SPACENAME, new Properties());
    }

    @Test
    public void testDefaults() throws Exception {
        LuceneConfiguration luceneConfiguration = new LuceneConfiguration();
        SpaceConfigReader spaceConfigReader = new SpaceConfigReader(SPACENAME);
        luceneConfiguration.initialize(spaceConfigReader);

        //test directory
        Directory directory = luceneConfiguration.getDirectory("A");
        Assert.assertEquals("MMapDirectory should be the default directory", MMapDirectory.class, directory.getClass());
        Assert.assertEquals("user.home should be the default location", System.getProperty("user.home"), luceneConfiguration.getLocation());
        Assert.assertEquals("MMapDirectory location should be user.home/A", new MMapDirectory(Paths.get(System.getProperty("user.home") + "/A")).getDirectory(), ((MMapDirectory) directory).getDirectory());

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
}