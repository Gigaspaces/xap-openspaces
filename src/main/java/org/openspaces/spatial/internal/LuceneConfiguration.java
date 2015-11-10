package org.openspaces.spatial.internal;

import com.gigaspaces.config.ConfigurationException;
import com.gigaspaces.internal.server.space.SpaceConfigReader;
import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.context.SpatialContextFactory;
import com.spatial4j.core.context.jts.JtsSpatialContext;
import com.spatial4j.core.context.jts.JtsSpatialContextFactory;
import org.apache.lucene.spatial.SpatialStrategy;
import org.apache.lucene.spatial.bbox.BBoxStrategy;
import org.apache.lucene.spatial.prefix.RecursivePrefixTreeStrategy;
import org.apache.lucene.spatial.prefix.tree.GeohashPrefixTree;
import org.apache.lucene.spatial.prefix.tree.QuadPrefixTree;
import org.apache.lucene.spatial.prefix.tree.SpatialPrefixTree;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.store.RAMDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * @author Yohana Khoury
 * @since 11.0
 */
public class LuceneConfiguration {
    public static final String GEOSPATIAL_PREFIX = "geospatial.lucene";

    //space-config.geospatial.lucene.strategy
    public static final String STRATEGY = GEOSPATIAL_PREFIX + ".strategy";
    public static final String STRATEGY_DEFAULT = SupportedSpatialStrategy.RecursivePrefixTree.name();

    //space-config.geospatial.lucene.strategy.spatial-prefix-tree
    public static final String SPATIAL_PREFIX_TREE = GEOSPATIAL_PREFIX + ".strategy.spatial-prefix-tree";
    public static final String SPATIAL_PREFIX_TREE_DEFAULT = SupportedSpatialPrefixTree.GeohashPrefixTree.name();
    //space-config.geospatial.lucene.strategy.spatial-prefix-tree.max-levels
    public static final String SPATIAL_PREFIX_TREE_MAX_LEVELS = GEOSPATIAL_PREFIX + ".strategy.spatial-prefix-tree.max-levels";
    public static final String SPATIAL_PREFIX_TREE_MAX_LEVELS_DEFAULT = "11";
    //space-config.geospatial.lucene.strategy.dist-err-pct
    public static final String DIST_ERR_PCT = GEOSPATIAL_PREFIX + ".strategy.distance-error-pct";
    public static final String DIST_ERR_PCT_DEFAULT = "0.025";

    //space-config.geospatial.lucene.storage.directory-type
    public static final String STORAGE_DIRECTORYTYPE = GEOSPATIAL_PREFIX + ".storage.directory-type";
    public static final String STORAGE_DIRECTORYTYPE_DEFAULT = SupportedDirectory.MMapDirectory.name();
    //space-config.geospatial.lucene.storage.mmapdirectory.location
    public static final String STORAGE_LOCATION = GEOSPATIAL_PREFIX + ".storage.location";
    public static final String STORAGE_LOCATION_DEFAULT = System.getProperty("com.gs.foreignindex.lucene.work", System.getProperty("user.home"));

    //space-config.geospatial.lucene.spatial-context
    public static final String SPATIAL_CONTEXT = GEOSPATIAL_PREFIX + ".spatial-context";
    public static final String SPATIAL_CONTEXT_DEFAULT = SupportedSpatialContext.JTS_GEO.name();

    private SpatialContext _spatialContext;
    private StrategyFactory _strategyFactory;
    private DirectoryFactory _directoryFactory;
    private String _location;

    private enum SupportedSpatialStrategy {
        RecursivePrefixTree, BBox;
        public static SupportedSpatialStrategy byName (String key) {
            for (SupportedSpatialStrategy spatialStrategy : SupportedSpatialStrategy.values()) {
                if (spatialStrategy.name().equalsIgnoreCase(key)) {
                    return spatialStrategy;
                }
            }
            return null;
        }
    }

    private enum SupportedSpatialPrefixTree {
        GeohashPrefixTree, QuadPrefixTree;
        public static SupportedSpatialPrefixTree byName (String key) {
            for (SupportedSpatialPrefixTree spatialPrefixTree : SupportedSpatialPrefixTree.values()) {
                if (spatialPrefixTree.name().equalsIgnoreCase(key)) {
                    return spatialPrefixTree;
                }
            }
            return null;
        }
    }

    private enum SupportedSpatialContext {
        GEO, NON_GEO, JTS_GEO, JTS_NON_GEO;
        public static SupportedSpatialContext byName (String key) {
            for (SupportedSpatialContext spatialContext : SupportedSpatialContext.values()) {
                if (spatialContext.name().equalsIgnoreCase(key)) {
                    return spatialContext;
                }
            }
            return null;
        }
    }

    private enum SupportedDirectory {
        MMapDirectory, RAMDirectory;
        public static SupportedDirectory byName (String key) {
            for (SupportedDirectory directory : SupportedDirectory.values()) {
                if (directory.name().equalsIgnoreCase(key)) {
                    return directory;
                }
            }
            return null;
        }
    }


    public LuceneConfiguration() {
    }

    public void initialize(SpaceConfigReader reader) throws Exception {
        this._spatialContext = createSpatialContext(reader);
        this._strategyFactory = createStrategyFactory(reader);
        this._directoryFactory = createDirectoryFactory(reader);

    }

    private SpatialContext createSpatialContext(SpaceConfigReader reader) throws Exception {
        String spatialContextString = reader.getSpaceProperty(SPATIAL_CONTEXT, SPATIAL_CONTEXT_DEFAULT);
        SupportedSpatialContext spatialContext = SupportedSpatialContext.byName(spatialContextString);
        if (spatialContext == null)
            throw new ConfigurationException("Unsupported spatial context type " + spatialContextString+". Options are: " + Arrays.asList(SupportedSpatialContext.values()));

        switch (spatialContext) {
            case JTS_GEO:
                return JtsSpatialContext.GEO;
            case JTS_NON_GEO: {
                JtsSpatialContextFactory factory = new JtsSpatialContextFactory();
                factory.geo = false;
                return new JtsSpatialContext(factory);
            }
            case GEO:
                return SpatialContext.GEO;
            case NON_GEO: {
                SpatialContextFactory factory = new SpatialContextFactory();
                factory.geo = false;
                return new SpatialContext(factory);
            }
            default:
                throw new RuntimeException("Unhandled spatial context type " + spatialContext);
        }
    }

    protected StrategyFactory createStrategyFactory(SpaceConfigReader reader) throws Exception {
        String strategyString = reader.getSpaceProperty(STRATEGY, STRATEGY_DEFAULT);
        SupportedSpatialStrategy spatialStrategy = SupportedSpatialStrategy.byName(strategyString);
        if (spatialStrategy == null)
            throw new ConfigurationException("Unsupported strategy " + strategyString+". Options are: " + Arrays.asList(SupportedSpatialStrategy.values()));

        switch (spatialStrategy) {
            case RecursivePrefixTree: {
                String maxLevelsStr = reader.getSpaceProperty(SPATIAL_PREFIX_TREE_MAX_LEVELS, SPATIAL_PREFIX_TREE_MAX_LEVELS_DEFAULT);
                String spatialPrefixTreeType = reader.getSpaceProperty(SPATIAL_PREFIX_TREE, SPATIAL_PREFIX_TREE_DEFAULT);
                final SpatialPrefixTree geohashPrefixTree = createSpatialPrefixTree(spatialPrefixTreeType, _spatialContext, Integer.valueOf(maxLevelsStr));
                String distErrPctValue = reader.getSpaceProperty(DIST_ERR_PCT, DIST_ERR_PCT_DEFAULT);
                final double distErrPct = Double.valueOf(distErrPctValue);

                return new StrategyFactory("RecursivePrefixTree") {
                    @Override
                    public SpatialStrategy createStrategy(String fieldName) {
                        RecursivePrefixTreeStrategy strategy = new RecursivePrefixTreeStrategy(geohashPrefixTree, fieldName);
                        strategy.setDistErrPct(distErrPct);
                        return strategy;
                    }

                    @Override
                    public double getDistErrPct() {
                        return distErrPct;
                    }
                };
            }
            case BBox: {
                return new StrategyFactory("BBox") {
                    @Override
                    public SpatialStrategy createStrategy(String fieldName) {
                        return new BBoxStrategy(_spatialContext, fieldName);
                    }
                };
            }
            default:
                throw new RuntimeException("Unhandled strategy " + spatialStrategy);
        }
    }

    private SpatialPrefixTree createSpatialPrefixTree(String spatialPrefixTreeType, SpatialContext spatialContext, Integer maxLevels) throws Exception {
        SupportedSpatialPrefixTree spatialPrefixTree = SupportedSpatialPrefixTree.byName(spatialPrefixTreeType);
        if (spatialPrefixTree == null)
            throw new ConfigurationException("Unsupported spatial prefix tree type: " + spatialPrefixTreeType+". Options are: " + Arrays.asList(SupportedSpatialPrefixTree.values()));

        switch (spatialPrefixTree) {
            case GeohashPrefixTree:
                return new GeohashPrefixTree(spatialContext, maxLevels);
            case QuadPrefixTree:
                return new QuadPrefixTree(spatialContext, maxLevels);
            default:
                throw new RuntimeException("Unhandled spatial prefix tree type: " + spatialPrefixTree);
        }
    }

    protected DirectoryFactory createDirectoryFactory(SpaceConfigReader reader) throws Exception {
        String directoryType = reader.getSpaceProperty(STORAGE_DIRECTORYTYPE, STORAGE_DIRECTORYTYPE_DEFAULT);
        SupportedDirectory directory = SupportedDirectory.byName(directoryType);
        if (directory == null)
            throw new ConfigurationException("Unsupported directory type " + directoryType+". Options are: " + Arrays.asList(SupportedDirectory.values()));

        switch (directory) {
            case MMapDirectory: {
                _location = reader.getSpaceProperty(STORAGE_LOCATION, STORAGE_LOCATION_DEFAULT);
                return new DirectoryFactory() {
                    @Override
                    public Directory getDirectory(String relativePath) throws IOException {
                        return new MMapDirectory(Paths.get(_location+"/"+relativePath));
                    }
                };
            }
            case RAMDirectory: {
                return new DirectoryFactory() {
                    @Override
                    public Directory getDirectory(String path) throws IOException {
                        return new RAMDirectory();
                    }
                };
            }
            default:
                throw new RuntimeException("Unhandled directory type " + directory);
        }
    }


    public SpatialStrategy getStrategy(String fieldName) {
        return this._strategyFactory.createStrategy(fieldName);
    }

    public Directory getDirectory(String relativePath) throws IOException {
        return _directoryFactory.getDirectory(relativePath);
    }

    public SpatialContext getSpatialContext() {
        return _spatialContext;
    }


    public String getLocation() {
        return _location;
    }

    public boolean rematchAlreadyMatchedIndexPath(String path) {
        try {
            return _strategyFactory.getDistErrPct() != 0;
        } catch (UnsupportedOperationException e) {
            return true;
        }
    }

    public abstract class StrategyFactory {
        private String _strategyName;

        public StrategyFactory(String strategyName) {
            this._strategyName = strategyName;
        }

        public abstract SpatialStrategy createStrategy(String fieldName);
        public double getDistErrPct() {
            throw new UnsupportedOperationException(_strategyName + " strategy does not support getting distErrPct value");
        }
    }

    public abstract class DirectoryFactory {
        public abstract Directory getDirectory(String relativePath) throws IOException;
    }

}
