package org.openspaces.foreignindexes.geospatial;

import com.gigaspaces.core.geospatial.shapes.*;
import com.gigaspaces.internal.metadata.ITypeDesc;
import com.j_spaces.core.cache.foreignIndexes.*;
import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.context.jts.JtsSpatialContext;
import com.spatial4j.core.shape.SpatialRelation;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.IntField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.spatial.SpatialStrategy;
import org.apache.lucene.spatial.prefix.RecursivePrefixTreeStrategy;
import org.apache.lucene.spatial.prefix.tree.GeohashPrefixTree;
import org.apache.lucene.spatial.prefix.tree.SpatialPrefixTree;
import org.apache.lucene.spatial.query.SpatialArgs;
import org.apache.lucene.spatial.query.SpatialOperation;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.openspaces.core.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

/**
 * Created by yechielf
 *
 * @since 11.0
 */
public class LuceneGeospatialCustomRelationHandler extends CustomRelationHandler {
    private static final Logger logger = Logger.getLogger(LuceneGeospatialCustomRelationHandler.class.getName());

    public final static Map<String, SpatialOperation> spatialOperationMap = new HashMap<String, SpatialOperation>();
    private final static FieldType fieldType = new FieldType();

    static {
        spatialOperationMap.put("WITHIN", SpatialOperation.IsWithin);
        spatialOperationMap.put("CONTAINS", SpatialOperation.Contains);
        spatialOperationMap.put("DISJOINT", SpatialOperation.IsDisjointTo);
        spatialOperationMap.put("INTERSECTS", SpatialOperation.Intersects);

        fieldType.setStored(false);
        fieldType.setStoreTermVectorOffsets(false);
        fieldType.setStoreTermVectorPayloads(false);
        fieldType.setStoreTermVectorPositions(false);
        fieldType.setStoreTermVectors(false);
        fieldType.setTokenized(false);
        fieldType.setIndexOptions(IndexOptions.DOCS);

    }

    private String mainDirectory;
    static final String GSUID = "GSUID";
    static final String GSVERSION = "GSVERSION";

    static final int MAX_RESULTS = 10000;
    private final ConcurrentMap<Object, IIndexableServerEntry> _uidToEntry;

    private LuceneHolder luceneEntryHolder;


    private SpatialContext spatialContext = JtsSpatialContext.GEO;
    private int maxLevels = 11;//results in sub-meter precision for geohash
    private SpatialPrefixTree grid = new GeohashPrefixTree(spatialContext, maxLevels);

    public class LuceneHolder {
        private Directory _directory;
        private IndexWriter _indexWriter;

        public LuceneHolder(Directory _directory, IndexWriter _indexWriter) {
            this._directory = _directory;
            this._indexWriter = _indexWriter;
        }

        public IndexWriter getIndexWriter() {
            return _indexWriter;
        }

        public Directory getDirectory() {
            return _directory;
        }
    }


    public LuceneGeospatialCustomRelationHandler() {
        _uidToEntry = new ConcurrentHashMap<Object, IIndexableServerEntry>();
    }

    private LuceneHolder createLuceneHolder(String path) throws IOException {
        MMapDirectory directory = new MMapDirectory(Paths.get(path));
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig wc = new IndexWriterConfig(analyzer);
        wc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        IndexWriter iwriter = new IndexWriter(directory, wc);
        return new LuceneHolder(directory, iwriter);
    }


    @Override
    public void initialize(String className, String namespace, String spaceName, Class annotationType) throws Exception {
        super.initialize(className, namespace, spaceName, annotationType);
        mainDirectory = System.getProperty("com.gs.foreignindex.lucene.work", System.getProperty("user.home"));
        File luceneIndexdDirectory = new File(mainDirectory, spaceName);
        if (luceneIndexdDirectory.exists()) {
            FileUtils.deleteFileOrDirectory(luceneIndexdDirectory);
        }
        luceneEntryHolder = createLuceneHolder(mainDirectory + "/" + className + "/entries");
        CustomRelationHandler.addHandler("geospatial", className, this);

    }


    @Override
    public void insertEntry(IIndexableServerEntry entry, Map<String, CustomRelationAnnotationHolder> customRelationAnnotationsHolders) throws Exception {
        boolean docHasShape = false;
        _uidToEntry.put(entry.getUid(), entry);
        //construct a document and add all fixed  properties
        Document doc = new Document();
        for (String fieldName : customRelationAnnotationsHolders.keySet()) {
            CustomRelationAnnotationHolder holder = customRelationAnnotationsHolders.get(fieldName + ":" + getNamespace());
            if(holder != null && holder.getAnnotation().annotationType().equals(Geospatial.class)){
                Geospatial geospatial = (Geospatial) holder.getAnnotation();
                if (geospatial.indexed()) {
                    Object val = entry.getPropertyValue(fieldName);
                    if (val instanceof Shape) {
                        Shape gigaShape = (Shape) val;
                        com.spatial4j.core.shape.Shape shape = toSpatial4j(gigaShape);
                        Field[] fields = createStrategyByFieldName(fieldName).createIndexableFields(shape);
                        for (Field field : fields) {
                            doc.add(field);
                        }
                        docHasShape = true;
                    }
                }
            }
        }
        if (docHasShape) {
//            Field classField = new Field("class", getClassName(), fieldType);
//            doc.add(classField);

            //cater for uid & version
            //noinspection deprecation
            doc.add(new Field(GSUID, (String) entry.getUid(), Field.Store.YES,
                    Field.Index.NOT_ANALYZED));

            FieldType gsVersionFieldType = new FieldType();
            gsVersionFieldType.setStored(true);
            gsVersionFieldType.setIndexOptions(IndexOptions.NONE);
            gsVersionFieldType.setNumericType(FieldType.NumericType.INT);
            doc.add(new IntField(GSVERSION, entry.getVersion(), gsVersionFieldType));

            luceneEntryHolder.getIndexWriter().addDocument(doc);
            luceneEntryHolder.getIndexWriter().commit();
        }
    }

    @Override
    public boolean isIndexed(ITypeDesc typeDesc, Annotation annotation) {
        if (annotation.annotationType().equals(Geospatial.class)) {
            Geospatial geospatial = (Geospatial) annotation;
            if (geospatial.indexed()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void removeEntry(IIndexableServerEntry entry) throws Exception {
        luceneEntryHolder.getIndexWriter().deleteDocuments(new Term(GSUID, (String) entry.getUid()));
        luceneEntryHolder.getIndexWriter().commit();
        _uidToEntry.remove(entry.getUid());
    }

    @Override
    public void replaceEntry(IIndexableServerEntry newEentry, IIndexableServerEntry originalEentry) {
        //TBD
    }


    @Override
    public void insertTemplate(IIndexableServerTemplate template) throws Exception {
    }

    @Override
    public void removeTemplate(IIndexableServerTemplate template) throws Exception {
    }

    @Override
    public ForeignQueryTemplatesResultIterator queryTemplates(String className, IIndexableServerEntry entry) throws Exception {
        return null;
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public void close() throws Exception {
        luceneEntryHolder.getIndexWriter().close();
        File mainFolder = new File(mainDirectory);
        FileUtils.deleteFileOrDirectory(mainFolder);
    }

    public com.spatial4j.core.shape.Shape toSpatial4j(Shape gigaShape) {
        if (gigaShape instanceof Rectangle) {
            return convertRectangle((Rectangle) gigaShape);
        } else if (gigaShape instanceof Circle) {
            return convertCircle((Circle) gigaShape);
        } else if (gigaShape instanceof Point) {
            return convertPoint((Point) gigaShape);
        } else if (gigaShape instanceof Polygon) {
            return convertPolygon((Polygon) gigaShape);
        } else {
            throw new RuntimeException("Unknown shape [" + gigaShape.getClass().getName() + "]");
        }
    }

    private com.spatial4j.core.shape.Shape convertCircle(Circle circle) {
        return spatialContext.makeCircle(circle.getPoint().getX(), circle.getPoint().getY(), circle.getRadius());
    }

    private com.spatial4j.core.shape.Shape convertRectangle(Rectangle rectangle) {
        return spatialContext.makeRectangle(rectangle.getMinX(), rectangle.getMaxX(),
                rectangle.getMinY(), rectangle.getMaxY());
    }

    private com.spatial4j.core.shape.Shape convertPoint(Point point) {
        return spatialContext.makePoint(point.getX(), point.getY());
    }

    private com.spatial4j.core.shape.Shape convertPolygon(Polygon polygon) {
        try {
            String coordinates = "";
            for (int i = 0; i < polygon.getCoordinates().size(); i++) {
                coordinates += (int) polygon.getCoordinates().get(i).getX() + " " + (int) polygon.getCoordinates().get(i).getY() + ",";
            }
            coordinates += (int) polygon.getCoordinates().get(0).getX() + " " + (int) polygon.getCoordinates().get(0).getY();
            return spatialContext.readShapeFromWkt("POLYGON ((" + coordinates + "))");
            //return poly;
        } catch (ParseException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    private SpatialStrategy createStrategyByFieldName(String fieldName) {
        return new RecursivePrefixTreeStrategy(grid, fieldName);
    }

    public boolean applyOperationFilter(String relation, Object actual, Object matchedAgainst) {
        if (!(actual instanceof Shape) || !(matchedAgainst instanceof Shape)) {
            logger.warning("Relation " + relation + " can be applied only for geometrical shapes, instead given: " + actual + " and " + matchedAgainst);
            return false;
        } else {
            SpatialRelation spatialRelation = SpatialRelation.valueOf(relation.toUpperCase());
            if (spatialRelation == null) {
                logger.warning("Relation " + relation + " not found, known relations are: " + Arrays.asList(SpatialRelation.values()));
                return false;
            }
            com.spatial4j.core.shape.Shape actualShape = toSpatial4j((Shape) actual);
            com.spatial4j.core.shape.Shape matchedAgainstShape = toSpatial4j((Shape) matchedAgainst);
            return actualShape.relate(matchedAgainstShape) == spatialRelation;
        }
    }

    @Override
    public ForeignQueryEntriesResultIterator scanIndex(String typeName, String path, String namespace, String relation, Object subject) throws Exception {
        SpatialRelation spatialRelation = SpatialRelation.valueOf(relation.toUpperCase());
        if (spatialRelation == null) {
            logger.warning("Relation " + relation + " not found, known relations for " + namespace + " are: " + Arrays.asList(SpatialRelation.values()));
            return null;
        }
        if (!(subject instanceof Shape)) {
            logger.warning("Relation " + relation + " can be applied only for geometrical shapes, instead given: " + subject);
            return null;
        }
        com.spatial4j.core.shape.Shape subjectShape = toSpatial4j((Shape) subject);
        SpatialArgs args = new SpatialArgs(spatialOperationMap.get(spatialRelation.name()), subjectShape);
        DirectoryReader dr = DirectoryReader.open(luceneEntryHolder.getDirectory());
        IndexSearcher is = new IndexSearcher(dr);

        Query q = createStrategyByFieldName(path).makeQuery(args);
//        BooleanQuery booleanQuery = new BooleanQuery();
//        booleanQuery.add(q, BooleanClause.Occur.MUST);
//        booleanQuery.add(new TermQuery(new Term("class", getClassName())), BooleanClause.Occur.MUST);
//        ScoreDoc[] scores = is.search(booleanQuery, MAX_RESULTS).scoreDocs;

        ScoreDoc[] scores = is.search(q, MAX_RESULTS).scoreDocs;
        return new LuceneIterator(scores, is, _uidToEntry, dr);
    }

}
