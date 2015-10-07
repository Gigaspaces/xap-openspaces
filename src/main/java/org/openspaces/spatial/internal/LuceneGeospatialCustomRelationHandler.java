/*******************************************************************************
 *
 * Copyright (c) 2015 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/
package org.openspaces.spatial.internal;

import com.gigaspaces.internal.metadata.ITypeDesc;
import com.gigaspaces.spatial.shapes.*;
import com.j_spaces.core.cache.foreignIndexes.*;
import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.context.jts.JtsSpatialContext;
import com.spatial4j.core.shape.SpatialRelation;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.spatial.SpatialStrategy;
import org.apache.lucene.spatial.prefix.RecursivePrefixTreeStrategy;
import org.apache.lucene.spatial.prefix.tree.GeohashPrefixTree;
import org.apache.lucene.spatial.prefix.tree.SpatialPrefixTree;
import org.apache.lucene.spatial.query.SpatialArgs;
import org.apache.lucene.spatial.query.SpatialOperation;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.openspaces.core.util.FileUtils;
import org.openspaces.spatial.SpaceSpatialIndex;

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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * Created by yechielf
 * @since 11.0
 */
public class LuceneGeospatialCustomRelationHandler extends CustomRelationHandler {
    private static final Logger logger = Logger.getLogger(LuceneGeospatialCustomRelationHandler.class.getName());

    public final static Map<String, SpatialOperation> spatialOperationMap = new HashMap<String, SpatialOperation>();
    static {
        spatialOperationMap.put("WITHIN", SpatialOperation.IsWithin);
        spatialOperationMap.put("CONTAINS", SpatialOperation.Contains);
        spatialOperationMap.put("DISJOINT", SpatialOperation.IsDisjointTo);
        spatialOperationMap.put("INTERSECTS", SpatialOperation.Intersects);

    }

    static final String GSUID = "GSUID";
    static final String GSVERSION = "GSVERSION";
    private static final String GSUIDANDVERSION = GSUID + "_" + GSVERSION;

    static final int MAX_RESULTS = Integer.MAX_VALUE;
    private final ConcurrentMap<Object, IIndexableServerEntry> _uidToEntry;

    @SuppressWarnings("FieldCanBeLocal")
    private double _distErrPct = 0.025;//SpatialArgs.DEFAULT_DISTERRPCT;

    private SpatialContext spatialContext = JtsSpatialContext.GEO;
    private int maxLevels = 11;//results in sub-meter precision for geohash
    private SpatialPrefixTree grid = new GeohashPrefixTree(spatialContext, maxLevels);
    private File luceneIndexdDirectory;

    private AtomicInteger uncommittedChanges = new AtomicInteger(0);


    private static Map<String, LuceneHolder> _luceneHolderMap = new ConcurrentHashMap<String, LuceneHolder>();



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
    public void initialize(String namespace, String spaceName) throws Exception {
        super.initialize(namespace, spaceName);
        String mainDirectory = System.getProperty("com.gs.foreignindex.lucene.work", System.getProperty("user.home"));
        luceneIndexdDirectory = new File(mainDirectory, spaceName);
        if (luceneIndexdDirectory.exists()) {
            FileUtils.deleteFileOrDirectory(luceneIndexdDirectory);
        }
    }

    private void commit(String className) throws IOException {
        if (uncommittedChanges.incrementAndGet() == 1000) {
            uncommittedChanges.set(0);
            getLuceneHolder(className).getIndexWriter().commit();
        }
    }


    @Override
    public void insertEntry(IIndexableServerEntry entry, Map<String, CustomRelationAnnotationHolder> customRelationAnnotationsHolders) throws Exception {
        boolean docHasShape = false;
        //construct a document and add all fixed  properties
        Document doc = new Document();
        for (CustomRelationAnnotationHolder customRelationAnnotationHolder : customRelationAnnotationsHolders.values()) {
            if (customRelationAnnotationHolder != null && customRelationAnnotationHolder.getAnnotation().annotationType().equals(SpaceSpatialIndex.class)) {
                Object val = entry.getPropertyValue(customRelationAnnotationHolder.getFieldName());
                if (val instanceof Shape) {
                    Shape gigaShape = (Shape) val;
                    com.spatial4j.core.shape.Shape shape = toSpatial4j(gigaShape);
                    Field[] fields = createStrategyByFieldName(customRelationAnnotationHolder.getFieldName()).createIndexableFields(shape);
                    for (Field field : fields) {
                        doc.add(field);
                    }
                    docHasShape = true;
                }
            }
            if (docHasShape) {
                //cater for uid & version
                //noinspection deprecation
                doc.add(new Field(GSUID, (String) entry.getUid(), Field.Store.YES,
                        Field.Index.NO));

                //noinspection deprecation
                doc.add(new Field(GSUIDANDVERSION, entry.getUid() + String.valueOf(entry.getVersion()), Field.Store.YES,
                        Field.Index.NOT_ANALYZED));

                String className = entry.getEntryCacheInfo().getClassName();
                getLuceneHolder(className).getIndexWriter().addDocument(doc);

                commit(className);
                _uidToEntry.put(entry.getUid(), entry);
            }
        }
    }

    @Override
    public void introduceType(String className) throws IOException {
        LuceneHolder luceneEntryHolder = createLuceneHolder(luceneIndexdDirectory.getAbsolutePath() + "/" + className + "/entries");
        _luceneHolderMap.put(className, luceneEntryHolder);
    }

    private LuceneHolder getLuceneHolder(String className) throws IOException {
        return _luceneHolderMap.get(className);
    }

    @Override
    public boolean isIndexed(ITypeDesc typeDesc, Annotation annotation) {
        return annotation.annotationType().equals(SpaceSpatialIndex.class);

    }

    @Override
    public void removeEntry(IIndexableServerEntry entry) throws Exception {
        if (_uidToEntry.containsKey(entry.getUid())) {
            String className = entry.getEntryCacheInfo().getClassName();
            getLuceneHolder(className).getIndexWriter().deleteDocuments(new TermQuery(
                    new Term(GSUIDANDVERSION, entry.getUid() + String.valueOf(entry.getVersion()))));
            commit(className);
            _uidToEntry.remove(entry.getUid());
        }
    }

    @Override
    public void replaceEntry(IIndexableServerEntry entry, Map<String, CustomRelationAnnotationHolder> customRelationAnnotationsHolders) {
        try {
            boolean docHasShape = false;
            //construct a document and add all fixed  properties
            Document doc = new Document();
            for (CustomRelationAnnotationHolder holder : customRelationAnnotationsHolders.values()) {
                if (holder != null && holder.getAnnotation().annotationType().equals(SpaceSpatialIndex.class)) {
                    Object val = entry.getPropertyValue(holder.getFieldName());
                    if (val instanceof Shape) {
                        Shape gigaShape = (Shape) val;
                        com.spatial4j.core.shape.Shape shape = toSpatial4j(gigaShape);
                        Field[] fields = createStrategyByFieldName(holder.getFieldName()).createIndexableFields(shape);
                        for (Field field : fields) {
                            doc.add(field);
                        }
                        docHasShape = true;
                    }
                }
            }
            if (docHasShape) {
                //cater for uid & version
                //noinspection deprecation
                doc.add(new Field(GSUID, (String) entry.getUid(), Field.Store.YES,
                        Field.Index.NO));

                //noinspection deprecation
                doc.add(new Field(GSUIDANDVERSION, entry.getUid() + String.valueOf(entry.getVersion()), Field.Store.YES,
                        Field.Index.NOT_ANALYZED));

                //Add new

                String className = entry.getEntryCacheInfo().getClassName();
                LuceneHolder luceneEntryHolder = getLuceneHolder(className);

                luceneEntryHolder.getIndexWriter().addDocument(doc);


                //Delete old
                luceneEntryHolder.getIndexWriter().deleteDocuments(new TermQuery(
                        new Term(GSUIDANDVERSION, entry.getUid() + String.valueOf(entry.getVersion() - 1))));

                commit(className);

                _uidToEntry.put(entry.getUid(), entry);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to update entry with id ["+String.valueOf(entry.getUid())+"]");
        }
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
        for (LuceneHolder luceneHolder : _luceneHolderMap.values()) {
            luceneHolder.getIndexWriter().close();
        }
        _luceneHolderMap.clear();
        FileUtils.deleteFileOrDirectory(luceneIndexdDirectory);
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
            for (int i = 0; i < polygon.getNumOfPoints(); i++) {
                coordinates += (int) polygon.getPoint(i).getX() + " " + (int) polygon.getPoint(i).getY() + ",";
            }
            coordinates += (int) polygon.getPoint(0).getX() + " " + (int) polygon.getPoint(0).getY();
            return spatialContext.readShapeFromWkt("POLYGON ((" + coordinates + "))");
            //return poly;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }


    private SpatialStrategy createStrategyByFieldName(String fieldName) {
        RecursivePrefixTreeStrategy recursivePrefixTreeStrategy = new RecursivePrefixTreeStrategy(grid, fieldName);
        recursivePrefixTreeStrategy.setDistErrPct(_distErrPct);
        return recursivePrefixTreeStrategy;
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

    private boolean rematchAlreadyMatchedIndexPath(@SuppressWarnings("UnusedParameters") String path) {
        //TODO change per field/path
        return _distErrPct != 0;
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

        LuceneHolder luceneHolder = getLuceneHolder(typeName);
        luceneHolder.getIndexWriter().commit();
        uncommittedChanges.set(0);

        com.spatial4j.core.shape.Shape subjectShape = toSpatial4j((Shape) subject);
        SpatialArgs args = new SpatialArgs(spatialOperationMap.get(spatialRelation.name()), subjectShape);

        DirectoryReader dr = DirectoryReader.open(luceneHolder.getDirectory());
        IndexSearcher is = new IndexSearcher(dr);

        Query q = createStrategyByFieldName(path).makeQuery(args);
//        BooleanQuery booleanQuery = new BooleanQuery();
//        booleanQuery.add(q, BooleanClause.Occur.MUST);
//        booleanQuery.add(new TermQuery(new Term("class", getClassName())), BooleanClause.Occur.MUST);
//        ScoreDoc[] scores = is.search(booleanQuery, MAX_RESULTS).scoreDocs;

        ScoreDoc[] scores = is.search(q, MAX_RESULTS).scoreDocs;
        return new LuceneIterator(scores, is, _uidToEntry, dr, (rematchAlreadyMatchedIndexPath(path) ? null : path));
    }

}
