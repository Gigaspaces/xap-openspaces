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
import com.gigaspaces.internal.server.space.SpaceConfigReader;
import com.gigaspaces.spatial.shapes.*;
import com.j_spaces.core.cache.foreignIndexes.*;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.spatial.SpatialStrategy;
import org.apache.lucene.spatial.query.SpatialArgs;
import org.apache.lucene.spatial.query.SpatialOperation;
import org.apache.lucene.store.Directory;
import org.openspaces.core.util.FileUtils;
import org.openspaces.spatial.SpaceSpatialIndex;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by yechielf
 * @since 11.0
 */
public class LuceneGeospatialCustomRelationHandler extends CustomRelationHandler {
    private static final Logger _logger = Logger.getLogger(LuceneGeospatialCustomRelationHandler.class.getName());

    static final String GSUID = "GSUID";
    static final String GSVERSION = "GSVERSION";
    private static final String GSUIDANDVERSION = GSUID + "_" + GSVERSION;

    static final int MAX_RESULTS = Integer.MAX_VALUE;
    private final ConcurrentMap<Object, ForeignIndexableServerEntry> _uidToEntry;

    private AtomicInteger uncommittedChanges = new AtomicInteger(0);


    private Map<String, LuceneHolder> _luceneHolderMap = new ConcurrentHashMap<String, LuceneHolder>();
    private LuceneConfiguration _luceneConfiguration;
    private File _luceneIndexdDirectory;


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
        _uidToEntry = new ConcurrentHashMap<Object, ForeignIndexableServerEntry>();
    }

    private LuceneHolder createLuceneHolder(String path) throws IOException {
        Directory directory = _luceneConfiguration.getDirectory(path);
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig wc = new IndexWriterConfig(analyzer);
        wc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        IndexWriter iwriter = new IndexWriter(directory, wc);
        return new LuceneHolder(directory, iwriter);
    }


    @Override
    public void initialize(String namespace, String spaceName, SpaceConfigReader reader) throws Exception {
        super.initialize(namespace, spaceName, reader);
        _luceneConfiguration = new LuceneConfiguration();
        _luceneConfiguration.initialize(reader);

        _luceneIndexdDirectory = new File(_luceneConfiguration.getLocation(), spaceName);
        if (_luceneIndexdDirectory.exists()) {
            FileUtils.deleteFileOrDirectory(_luceneIndexdDirectory);
        }
    }

    private void commit(String className) throws IOException {
        if (uncommittedChanges.incrementAndGet() == 1000) {
            uncommittedChanges.set(0);
            getLuceneHolder(className).getIndexWriter().commit();
        }
    }


    @Override
    public void insertEntry(IIndexableServerEntry entry, Map<String, CustomRelationAnnotationHolder> customRelationAnnotationsHolders, boolean fromTransactionalUpdate) throws Exception {
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
            if (!fromTransactionalUpdate) {
                ForeignIndexableServerEntry exist; // one existing in the map
                ForeignIndexableServerEntry mine = (ForeignIndexableServerEntry) entry; // current entry casted.

                if ((exist = _uidToEntry.putIfAbsent(entry.getUid(), mine)) != null) {//a lingering remove- wait for it
                    exist.waitForRemovalFromForeignIndex();
                }
                _uidToEntry.put(entry.getUid(), mine);
            }
        }
    }

    @Override
    public void introduceType(String className) throws IOException {
        if (!_luceneHolderMap.containsKey(className)) {
            LuceneHolder luceneEntryHolder = createLuceneHolder(_luceneIndexdDirectory.getAbsolutePath() + "/" + className + "/entries");
            _luceneHolderMap.put(className, luceneEntryHolder);
        } else {
            _logger.log(Level.WARNING, "Type [" + className + "] is already introduced to geospatial handler");
        }
    }

    private LuceneHolder getLuceneHolder(String className) throws IOException {
        return _luceneHolderMap.get(className);
    }

    @Override
    public boolean isIndexed(ITypeDesc typeDesc, Annotation annotation) {
        return annotation.annotationType().equals(SpaceSpatialIndex.class);

    }

    @Override
    public void removeEntry(IIndexableServerEntry entry, ForeignIndexRemoveMode foreignIndexRemoveMode) throws Exception {
        ForeignIndexableServerEntry exist = _uidToEntry.get(entry.getUid());
        if (exist != null) {
            if (foreignIndexRemoveMode == ForeignIndexRemoveMode.NO_XTN && !exist.equals(entry))
                throw new RuntimeException("invalid ForeignIndexableServerEntry in remove uid=" + entry.getUid());
            String className = entry.getEntryCacheInfo().getClassName();
            int version = entry.getVersion();
            if (foreignIndexRemoveMode == ForeignIndexRemoveMode.ON_XTN_UPDATED_COMMIT)
                version--;
            if (foreignIndexRemoveMode == ForeignIndexRemoveMode.ON_XTN_UPDATED_ROLLBACK)
                version++;

            getLuceneHolder(className).getIndexWriter().deleteDocuments(new TermQuery(
                    new Term(GSUIDANDVERSION, entry.getUid() + String.valueOf(version))));
            commit(className);

            if (foreignIndexRemoveMode == ForeignIndexRemoveMode.NO_XTN) {
                _uidToEntry.remove(entry.getUid(), entry);
                exist.setRemovedFromForeignIndex();
            }
            else
            {
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//TBD replate entry data in record !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            }
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

                _uidToEntry.put(entry.getUid(), (ForeignIndexableServerEntry)entry);
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
        FileUtils.deleteFileOrDirectory(_luceneIndexdDirectory);
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
        return _luceneConfiguration.getSpatialContext().makeCircle(circle.getPoint().getX(), circle.getPoint().getY(), circle.getRadius());
    }

    private com.spatial4j.core.shape.Shape convertRectangle(Rectangle rectangle) {
        return _luceneConfiguration.getSpatialContext().makeRectangle(rectangle.getMinX(), rectangle.getMaxX(),
                rectangle.getMinY(), rectangle.getMaxY());
    }

    private com.spatial4j.core.shape.Shape convertPoint(Point point) {
        return _luceneConfiguration.getSpatialContext().makePoint(point.getX(), point.getY());
    }

    com.spatial4j.core.shape.Shape convertPolygon(Polygon polygon) {
        try {
            String coordinates = "";
            for (int i = 0; i < polygon.getNumOfPoints(); i++) {
                coordinates += (i == 0 ? "" : ",") + polygon.getPoint(i).getX() + " " + polygon.getPoint(i).getY();
            }
            if (!polygon.getPoint(polygon.getNumOfPoints()-1).equals(polygon.getPoint(0))) {
                coordinates += "," + polygon.getPoint(0).getX() + " " + polygon.getPoint(0).getY();
            }
            return _luceneConfiguration.getSpatialContext().readShapeFromWkt("POLYGON ((" + coordinates + "))");
            //return poly;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }


    private SpatialStrategy createStrategyByFieldName(String fieldName) {
        return _luceneConfiguration.getStrategy(fieldName);
    }

    public boolean applyOperationFilter(String relation, Object actual, Object matchedAgainst) {
        if (!(actual instanceof Shape) || !(matchedAgainst instanceof Shape)) {
            throw new IllegalArgumentException("Relation " + relation + " can be applied only for geometrical shapes, instead given: " + actual + " and " + matchedAgainst);
        } else {
            SpatialOp spatialOperation;
            try {
                spatialOperation = SpatialOp.valueOf(relation.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Relation " + relation + " not found, known relations are: " + Arrays.asList(SpatialOp.values()));

            }
            com.spatial4j.core.shape.Shape actualShape = toSpatial4j((Shape) actual);
            com.spatial4j.core.shape.Shape matchedAgainstShape = toSpatial4j((Shape) matchedAgainst);
            return spatialOperation.evaluate(actualShape, matchedAgainstShape);
        }
    }

    private boolean rematchAlreadyMatchedIndexPath(@SuppressWarnings("UnusedParameters") String path) {
        //TODO change per field/path
        return _luceneConfiguration.getDistErrPct() != 0;
    }

    @Override
    public ForeignQueryEntriesResultIterator scanIndex(String typeName, String path, String namespace, String relation, Object subject) throws Exception {
        SpatialOp spatialOp;
        try {
            spatialOp = SpatialOp.valueOf(relation.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Relation " + relation + " not found, known relations for " + namespace + " are: " + Arrays.asList(SpatialOp.values()));
        }
        if (!(subject instanceof Shape)) {
            throw new IllegalArgumentException("Relation " + relation + " can be applied only for geometrical shapes, instead given: " + subject);
        }

        LuceneHolder luceneHolder = getLuceneHolder(typeName);
        luceneHolder.getIndexWriter().commit();
        uncommittedChanges.set(0);

        com.spatial4j.core.shape.Shape subjectShape = toSpatial4j((Shape) subject);
        DirectoryReader dr = DirectoryReader.open(luceneHolder.getDirectory());
        IndexSearcher is = new IndexSearcher(dr);

        Query query = createQuery(path, subjectShape, spatialOp);
//        BooleanQuery booleanQuery = new BooleanQuery();
//        booleanQuery.add(q, BooleanClause.Occur.MUST);
//        booleanQuery.add(new TermQuery(new Term("class", getClassName())), BooleanClause.Occur.MUST);
//        ScoreDoc[] scores = is.search(booleanQuery, MAX_RESULTS).scoreDocs;

        //Filter instead of query
        //query = new BooleanQuery.Builder().add(query, BooleanClause.Occur.FILTER).build();
        ScoreDoc[] scores = is.search(query, MAX_RESULTS).scoreDocs;
        return new LuceneIterator(scores, is, _uidToEntry, dr, (rematchAlreadyMatchedIndexPath(path) ? null : path));
    }

    private enum SpatialOp {
        WITHIN(SpatialOperation.IsWithin),
        CONTAINS(SpatialOperation.Contains),
        INTERSECTS(SpatialOperation.Intersects),
        DISJOINT(SpatialOperation.IsDisjointTo) {
            @Override
            public Query makeQuery(SpatialStrategy spatialStrategy, com.spatial4j.core.shape.Shape subjectShape) {
                SpatialArgs intersectsArgs = new SpatialArgs(SpatialOperation.Intersects, subjectShape);
                Query intersectsQuery = spatialStrategy.makeQuery(intersectsArgs);

                return new BooleanQuery.Builder()
                        .add(new MatchAllDocsQuery(), BooleanClause.Occur.SHOULD)
                        .add(intersectsQuery, BooleanClause.Occur.MUST_NOT)
                        .build();
            }
        };

        private final SpatialOperation _spatialOperation;

        SpatialOp(SpatialOperation spatialOperation) {
            this._spatialOperation = spatialOperation;
        }

        public Query makeQuery(SpatialStrategy spatialStrategy, com.spatial4j.core.shape.Shape subjectShape) {
            SpatialArgs args = new SpatialArgs(_spatialOperation, subjectShape);
            return spatialStrategy.makeQuery(args);
        }

        public boolean evaluate(com.spatial4j.core.shape.Shape indexedShape, com.spatial4j.core.shape.Shape queryShape) {
            return _spatialOperation.evaluate(indexedShape, queryShape);
        }
    }

    private Query createQuery(String path, com.spatial4j.core.shape.Shape subjectShape, SpatialOp op) {
        return op.makeQuery(createStrategyByFieldName(path), subjectShape);
    }

}
