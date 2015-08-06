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

package com.algolia.search.saas.Listener;

import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Index;
import com.algolia.search.saas.Query;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * Asynchronously receive result of Index asynchronous methods
 */
public interface IndexListener {

    /**
     * Asynchronously receive result of Index.deleteObjectASync method.
     */
    void deleteObjectResult(Index index, String objectID, JSONObject result);

    /**
     * Asynchronously receive error of Index.deleteObjectASync method.
     */
    void deleteObjectError(Index index, String objectID, AlgoliaException e);

    /**
     * Asynchronously receive result of Index.deleteObjectsASync method.
     */
    void deleteObjectsResult(Index index, JSONArray objects, JSONObject result);

    /**
     * Asynchronously receive error of Index.deleteByQueryASync method.
     */
    void deleteByQueryError(Index index, Query query, AlgoliaException e);

    /**
     * Asynchronously receive result of Index.deleteByQueryASync method.
     */
    void deleteByQueryResult(Index index);

    /**
     * Asynchronously receive error of Index.deleteObjectsASync method.
     */
    void deleteObjectsError(Index index, List<JSONObject> objects, AlgoliaException e);

    /**
     * Asynchronously receive result of Index.partialUpdateObjectASync method.
     */
    void partialUpdateResult(Index index, JSONObject object, String objectID, JSONObject result);

    /**
     * Asynchronously receive error of Index.partialUpdateObjectASync method.
     */
    void partialUpdateError(Index index, JSONObject object, String objectID, AlgoliaException e);

    /**
     * Asynchronously receive result of Index.partialUpdateObjectsASync method.
     */
    void partialUpdateObjectsResult(Index index, List<JSONObject> objects, JSONObject result);

    /**
     * Asynchronously receive error of Index.partialUpdateObjectsASync method.
     */
    void partialUpdateObjectsError(Index index, List<JSONObject> objects, AlgoliaException e);

    /**
     * Asynchronously receive result of Index.partialUpdateObjectsASync method.
     */
    void partialUpdateObjectsResult(Index index, JSONArray objects, JSONObject result);

    /**
     * Asynchronously receive error of Index.partialUpdateObjectsASync method.
     */
    void partialUpdateObjectsError(Index index, JSONArray objects, AlgoliaException e);

    /**
     * Asynchronously receive result of Index.getObjectASync method.
     */
    void getObjectResult(Index index, String objectID, JSONObject result);

    /**
     * Asynchronously receive error of Index.getObjectASync method.
     */
    void getObjectError(Index index, String objectID, AlgoliaException e);

    /**
     * Asynchronously receive result of Index.getObjectsASync method.
     */
    void getObjectsResult(Index index, List<String> objectIDs, JSONObject result);

    /**
     * Asynchronously receive error of Index.getObjectsASync method.
     */
    void getObjectsError(Index index, List<String> objectIDs, AlgoliaException e);

    /**
     * Asynchronously receive result of Index.waitTaskASync method.
     */
    void waitTaskResult(Index index, String taskID);

    /**
     * Asynchronously receive error of Index.waitTaskASync method.
     */
    void waitTaskError(Index index, String taskID, AlgoliaException e);

    /**
     * Asynchronously receive result of Index.getSettingsASync method.
     */
    void getSettingsResult(Index index, JSONObject result);

    /**
     * Asynchronously receive error of Index.getSettingsASync method.
     */
    void getSettingsError(Index index, AlgoliaException e);

    /**
     * Asynchronously receive result of Index.setSettingsASync method.
     */
    void setSettingsResult(Index index, JSONObject settings, JSONObject result);

    /**
     * Asynchronously receive error of Index.setSettingsASync method.
     */
    void setSettingsError(Index index, JSONObject settings, AlgoliaException e);
}