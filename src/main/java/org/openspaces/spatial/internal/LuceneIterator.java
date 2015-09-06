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
