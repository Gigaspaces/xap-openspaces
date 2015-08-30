package org.openspaces.foreignindexes.geospatial;

import com.j_spaces.core.cache.foreignIndexes.ForeignQueryEntriesResultIterator;
import com.j_spaces.core.cache.foreignIndexes.IIndexableServerEntry;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;

import java.util.concurrent.ConcurrentMap;

/**
 * Created by yechielf
 * @since 11.0
 */
public class LuceneIterator extends ForeignQueryEntriesResultIterator {
    private final ScoreDoc[] _scores;
    private final IndexSearcher _is;
    private int _pos;
    private final ConcurrentMap<Object,IIndexableServerEntry> _uidToEntry;
    private final DirectoryReader directoryReader;

    public LuceneIterator(ScoreDoc[] scores, IndexSearcher is, ConcurrentMap<Object, IIndexableServerEntry> uidToEntry, DirectoryReader directoryReader)
    {
        _scores = scores;
        _is =is;
        _uidToEntry=uidToEntry;
        this.directoryReader = directoryReader;
    }

    public boolean hasNext() throws Exception
    {
        return _pos <  _scores.length;
    }

    public IIndexableServerEntry next() throws Exception
    {
        Document d = _is.doc(_scores[_pos++].doc);
        return _uidToEntry.get(d.get(LuceneGeospatialCustomRelationHandler.GSUID));
    }

    public void close() throws Exception {
        directoryReader.close();
    }
}
