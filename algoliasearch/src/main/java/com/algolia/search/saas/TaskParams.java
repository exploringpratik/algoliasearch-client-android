/*
 * Copyright (c) 2015 Algolia
 * http://www.algolia.com/
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.algolia.search.saas;

import com.algolia.search.saas.Listener.Index.IndexingListener;
import com.algolia.search.saas.Listener.Index.SearchListener;

import org.json.JSONArray;
import org.json.JSONObject;

public class TaskParams {
    public static class Search {
        protected SearchListener listener;
        public Query query;

        protected JSONObject content;
        protected AlgoliaException error;

        protected Search(SearchListener listener, Query query) {
            this.listener = listener;
            this.query = query;
        }

        protected void sendResult(Index index) {
            if (content != null) {
                listener.searchResult(index, query, content);
            } else {
                listener.searchError(index, query, error);
            }
        }
    }

    public static class Indexing {
        protected IndexingListener listener;
        public TaskKind kind;
        public JSONObject object;
        public JSONArray objects;
        public String objectID;

        protected JSONObject content;
        protected AlgoliaException error;

        protected Indexing(IndexingListener listener, TaskKind kind, JSONObject object) {
            this.listener = listener;
            this.kind = kind;
            this.object = object;
        }

        protected Indexing(IndexingListener listener, TaskKind kind, JSONObject object, String objectID) {
            this.listener = listener;
            this.kind = kind;
            this.object = object;
            this.objectID = objectID;
        }

        protected Indexing(IndexingListener listener, TaskKind kind, JSONArray objects) {
            this.listener = listener;
            this.kind = kind;
            this.objects = objects;
        }

        protected void sendResult(Index index) {
            if (content != null) {
                listener.indexingResult(index, this, content);
            } else {
                listener.indexingError(index, this, error);
            }
        }
    }
}