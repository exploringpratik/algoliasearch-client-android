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

import android.support.annotation.NonNull;

import org.apache.http.HttpException;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Abstract Entry point in the Java API.
 */
abstract class BaseAPIClient {
    private int httpSocketTimeoutMS = 30000;
    private int httpConnectTimeoutMS = 2000;
    private int httpSearchTimeoutMS = 5000;

    private final static String version = "2.7.0";

    private final String applicationID;
    private final String apiKey;
    private List<String> readHosts;
    private List<String> writeHosts;

    /**
     * HTTP headers that will be sent with every request.
     */
    private HashMap<String, String> headers;

    /**
     * Algolia Search initialization
     *
     * @param applicationID the application ID you have in your admin interface
     * @param apiKey        a valid API key for the service
     * @param hosts         the list of hosts that you have received for the service
     */
    protected BaseAPIClient(String applicationID, String apiKey, String[] hosts) {
        if (applicationID == null || applicationID.length() == 0) {
            throw new RuntimeException("AlgoliaSearch requires an applicationID.");
        }
        this.applicationID = applicationID;
        if (apiKey == null || apiKey.length() == 0) {
            throw new RuntimeException("AlgoliaSearch requires an apiKey.");
        }
        this.apiKey = apiKey;
        if (hosts != null) {
            setReadHosts(hosts);
            setWriteHosts(hosts);
        } else {
            setReadHosts(
                applicationID + "-dsn.algolia.net",
                applicationID + "-1.algolianet.com",
                applicationID + "-2.algolianet.com",
                applicationID + "-3.algolianet.com"
            );
            setWriteHosts(
                applicationID + ".algolia.net",
                applicationID + "-1.algolianet.com",
                applicationID + "-2.algolianet.com",
                applicationID + "-3.algolianet.com"
            );
        }
        headers = new HashMap<String, String>();
    }

    public String getApplicationID() {
        return applicationID;
    }

    /**
     * Set an HTTP header that will be sent with every request.
     *
     * @param name  Header name.
     * @param value Value for the header. If null, the header will be removed.
     */
    public void setHeader(@NonNull String name, String value) {
        if (value == null) {
            headers.remove(name);
        } else {
            headers.put(name, value);
        }
    }

    /**
     * Get an HTTP header.
     *
     * @param name Header name.
     */
    public String getHeader(@NonNull String name) {
        return headers.get(name);
    }

    public String[] getReadHosts() {
        return readHosts.toArray(new String[readHosts.size()]);
    }

    public void setReadHosts(@NonNull String... hosts) {
        if (hosts.length == 0) {
            throw new IllegalArgumentException("Hosts array cannot be empty");
        }
        readHosts = Arrays.asList(hosts);
    }

    public String[] getWriteHosts() {
        return writeHosts.toArray(new String[writeHosts.size()]);
    }

    public void setWriteHosts(@NonNull String... hosts) {
        if (hosts.length == 0) {
            throw new IllegalArgumentException("Hosts array cannot be empty");
        }
        writeHosts = Arrays.asList(hosts);
    }

    /**
     * Set read and write hosts to the same value (convenience method).
     *
     * @param hosts New hosts. Must not be empty.
     */
    public void setHosts(@NonNull String... hosts) {
        setReadHosts(hosts);
        setWriteHosts(hosts);
    }

    /**
     * Allow to set timeout
     *
     * @param connectTimeout connection timeout in MS
     * @param readTimeout    socket timeout in MS
     */
    public void setTimeout(int connectTimeout, int readTimeout) {
        httpSocketTimeoutMS = readTimeout;
        httpConnectTimeoutMS = httpSearchTimeoutMS = connectTimeout;
    }

    /**
     * Allow to set timeout
     *
     * @param connectTimeout connection timeout in MS
     * @param readTimeout    socket timeout in MS
     * @param searchTimeout  socket timeout in MS
     */
    public void setTimeout(int connectTimeout, int readTimeout, int searchTimeout) {
        httpSocketTimeoutMS = readTimeout;
        httpConnectTimeoutMS = connectTimeout;
        httpSearchTimeoutMS = searchTimeout;
    }

    /**
     * List all existing indexes
     *
     * @return a JSON Object in the form:
     * { "items": [ {"name": "contacts", "createdAt": "2013-01-18T15:33:13.556Z"},
     *              {"name": "notes", "createdAt": "2013-01-18T15:33:13.556Z"}]}
     */
    protected JSONObject listIndexes() throws AlgoliaException {
        return getRequest("/1/indexes/", false);
    }

    /**
     * Delete an index
     *
     * @param indexName the name of index to delete
     * @return an object containing a "deletedAt" attribute
     */
    protected JSONObject deleteIndex(String indexName) throws AlgoliaException {
        try {
            return deleteRequest("/1/indexes/" + URLEncoder.encode(indexName, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Move an existing index.
     *
     * @param srcIndexName the name of index to copy.
     * @param dstIndexName the new index name that will contains a copy of srcIndexName (destination will be overriten if it already exist).
     */
    protected JSONObject moveIndex(String srcIndexName, String dstIndexName) throws AlgoliaException {
        try {
            JSONObject content = new JSONObject();
            content.put("operation", "move");
            content.put("destination", dstIndexName);
            return postRequest("/1/indexes/" + URLEncoder.encode(srcIndexName, "UTF-8") + "/operation", content.toString(), false);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new AlgoliaException(e.getMessage());
        }
    }

    /**
     * Copy an existing index.
     *
     * @param srcIndexName the name of index to copy.
     * @param dstIndexName the new index name that will contains a copy of srcIndexName (destination will be overriten if it already exist).
     */
    protected JSONObject copyIndex(String srcIndexName, String dstIndexName) throws AlgoliaException {
        try {
            JSONObject content = new JSONObject();
            content.put("operation", "copy");
            content.put("destination", dstIndexName);
            return postRequest("/1/indexes/" + URLEncoder.encode(srcIndexName, "UTF-8") + "/operation", content.toString(), false);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new AlgoliaException(e.getMessage());
        }
    }

    protected JSONObject multipleQueries(List<IndexQuery> queries, String strategy) throws AlgoliaException {
        try {
            JSONArray requests = new JSONArray();
            for (IndexQuery indexQuery : queries) {
                requests.put(new JSONObject()
                    .put("indexName", indexQuery.getIndexName())
                    .put("params", indexQuery.getQuery().build())
                );
            }
            JSONObject body = new JSONObject().put("requests", requests);
            String path = "/1/indexes/*/queries";
            if (strategy != null) {
                path += "?strategy=" + strategy;
            }
            return postRequest(path, body.toString(), true);
        } catch (JSONException e) {
            throw new AlgoliaException(e.getMessage());
        }
    }

    /**
     * Custom batch
     *
     * @param actions the array of actions
     * @throws AlgoliaException if the response is not valid json
     */
    protected JSONObject batch(JSONArray actions) throws AlgoliaException {
        try {
            JSONObject content = new JSONObject();
            content.put("requests", actions);
            return postRequest("/1/indexes/*/batch", content.toString(), false);
        } catch (JSONException e) {
            throw new AlgoliaException(e.getMessage());
        }
    }

    private enum Method {
        GET, POST, PUT, DELETE
    }

    protected byte[] getRequestRaw(String url, boolean search) throws AlgoliaException {
        return _requestRaw(Method.GET, url, null, readHosts, httpConnectTimeoutMS, search ? httpSearchTimeoutMS : httpSocketTimeoutMS);
    }

    protected JSONObject getRequest(String url, boolean search) throws AlgoliaException {
        return _request(Method.GET, url, null, readHosts, httpConnectTimeoutMS, search ? httpSearchTimeoutMS : httpSocketTimeoutMS);
    }

    protected JSONObject deleteRequest(String url) throws AlgoliaException {
        return _request(Method.DELETE, url, null, writeHosts, httpConnectTimeoutMS, httpSocketTimeoutMS);
    }

    protected JSONObject postRequest(String url, String obj, boolean readOperation) throws AlgoliaException {
        return _request(Method.POST, url, obj, (readOperation ? readHosts : writeHosts), httpConnectTimeoutMS, (readOperation ? httpSearchTimeoutMS : httpSocketTimeoutMS));
    }

    protected byte[] postRequestRaw(String url, String obj, boolean readOperation) throws AlgoliaException {
        return _requestRaw(Method.POST, url, obj, (readOperation ? readHosts : writeHosts), httpConnectTimeoutMS, (readOperation ? httpSearchTimeoutMS : httpSocketTimeoutMS));
    }

    protected JSONObject putRequest(String url, String obj) throws AlgoliaException {
        return _request(Method.PUT, url, obj, writeHosts, httpConnectTimeoutMS, httpSocketTimeoutMS);
    }

    /**
     * Reads the InputStream as UTF-8
     *
     * @param stream the InputStream to read
     * @return the stream's content as a String
     * @throws IOException if the stream can't be read, decoded as UTF-8 or closed
     */
    private static String _toCharArray(InputStream stream) throws IOException {
        InputStreamReader is = new InputStreamReader(stream, "UTF-8");
        StringBuilder builder = new StringBuilder();
        char[] buf = new char[1000];
        int l = 0;
        while (l >= 0) {
            builder.append(buf, 0, l);
            l = is.read(buf);
        }
        is.close();
        return builder.toString();
    }

    /**
     * Reads the InputStream into a byte array
     * @param stream the InputStream to read
     * @return the stream's content as a byte[]
     * @throws AlgoliaException if the stream can't be read or flushed
     */
    private byte[] _toByteArray(InputStream stream) throws AlgoliaException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int read;
        byte[] buffer = new byte[1024];

        try {
            while ((read = stream.read(buffer, 0, buffer.length)) != -1) {
                out.write(buffer, 0, read);
            }

            out.flush();
            return out.toByteArray();
        } catch (IOException e) {
            throw new AlgoliaException("Error while reading stream: " + e.getMessage());
        }
    }


    protected static JSONObject _getJSONObject(String input) throws JSONException {
        return new JSONObject(new JSONTokener(input));
    }

    protected static JSONObject _getJSONObject(byte[] array) throws JSONException, UnsupportedEncodingException {
        return new JSONObject(new String(array, "UTF-8"));
    }

    private static JSONObject _getAnswerJSONObject(InputStream istream) throws IOException, JSONException {
        return _getJSONObject(_toCharArray(istream));
    }

    /**
     * Send the query according to parameters and returns its result as a JSONObject
     *
     * @param m              HTTP Method to use
     * @param url            endpoint URL
     * @param json           optional JSON Object to send
     * @param hostsArray     array of hosts to try successively
     * @param connectTimeout maximum wait time to open connection
     * @param readTimeout    maximum time to read data on socket
     * @return a JSONObject containing the resulting data or error
     * @throws AlgoliaException if the request data is not valid json
     */
    private synchronized JSONObject _request(Method m, String url, String json, List<String> hostsArray, int connectTimeout, int readTimeout) throws AlgoliaException {
        try {
            return _getJSONObject(_requestRaw(m, url, json, hostsArray, connectTimeout, readTimeout));
        } catch (JSONException e) {
            throw new AlgoliaException("JSON decode error:" + e.getMessage());
        } catch (UnsupportedEncodingException e) {
            throw new AlgoliaException("UTF-8 decode error:" + e.getMessage());
        }
    }

    /**
     * Send the query according to parameters and returns its result as a JSONObject
     *
     * @param m              HTTP Method to use
     * @param url            endpoint URL
     * @param json           optional JSON Object to send
     * @param hostsArray     array of hosts to try successively
     * @param connectTimeout maximum wait time to open connection
     * @param readTimeout    maximum time to read data on socket
     * @return a JSONObject containing the resulting data or error
     * @throws AlgoliaException in case of connection or data handling error
     */
    private synchronized byte[] _requestRaw(Method m, String url, String json, List<String> hostsArray, int connectTimeout, int readTimeout) throws AlgoliaException {
        String requestMethod;
        HashMap<String, String> errors = new HashMap<String, String>();
        // for each host
        for (String host : hostsArray) {
            switch (m) {
                case DELETE:
                    requestMethod = "DELETE";
                    break;
                case GET:
                    requestMethod = "GET";
                    break;
                case POST:
                    requestMethod = "POST";
                    break;
                case PUT:
                    requestMethod = "PUT";
                    break;
                default:
                    throw new IllegalArgumentException("Method " + m + " is not supported");
            }

            // set URL
            try {
                URL hostURL = new URL("https://" + host + url);
                HttpURLConnection hostConnection = (HttpURLConnection) hostURL.openConnection();

                //set timeouts
                hostConnection.setRequestMethod(requestMethod);
                hostConnection.setConnectTimeout(connectTimeout);
                hostConnection.setReadTimeout(readTimeout);

                // set auth headers
                hostConnection.setRequestProperty("X-Algolia-Application-Id", this.applicationID);
                hostConnection.setRequestProperty("X-Algolia-API-Key", this.apiKey);
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    hostConnection.setRequestProperty(entry.getKey(), entry.getValue());
                }

                // set user agent
                hostConnection.setRequestProperty("User-Agent", "Algolia for Android " + version);

                // write JSON entity
                if (json != null) {
                    if (!(requestMethod.equals("PUT") || requestMethod.equals("POST"))) {
                        throw new IllegalArgumentException("Method " + m + " cannot enclose entity");
                    }
                    hostConnection.setRequestProperty("Content-type", "application/json");
                    hostConnection.setDoOutput(true);
                    StringEntity se = new StringEntity(json, "UTF-8");
                    se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                    se.writeTo(hostConnection.getOutputStream());
                }

                // read response
                int code = hostConnection.getResponseCode();
                final boolean codeIsError = code / 100 != 2;
                InputStream stream = codeIsError ?
                        hostConnection.getErrorStream() : hostConnection.getInputStream();

                final byte[] rawResponse;
                String encoding = hostConnection.getContentEncoding();
                if (encoding != null && encoding.contains("gzip")) {
                    rawResponse = _toByteArray(new GZIPInputStream(stream));
                } else {
                    rawResponse = _toByteArray(stream);
                }

                // handle http errors
                if (codeIsError) {
                    if (code / 100 == 4) {
                        String message = _getJSONObject(rawResponse).getString("message");
                        consumeQuietly(hostConnection);
                        throw new AlgoliaException(message, code);
                    } else {
                        final String errorMessage = _toCharArray(stream);
                        consumeQuietly(hostConnection);
                        addError(errors, host, new HttpException(errorMessage));
                        continue;
                    }
                }
                return rawResponse;

            } catch (JSONException e) { // fatal
                throw new AlgoliaException("JSON decode error:" + e.getMessage());
            } catch (UnsupportedEncodingException e) { // fatal
                throw new AlgoliaException("Invalid JSON Object: " + json);
            } catch (IOException e) { // host error, continue on the next host
                addError(errors, host, e);
            }
        }

        StringBuilder builder = new StringBuilder("Hosts unreachable: ");
        Boolean first = true;
        for (Map.Entry<String, String> entry : errors.entrySet()) {
            if (!first) {
                builder.append(", ");
            }
            builder.append(entry.toString());
            first = false;
        }
        throw new AlgoliaException(builder.toString());
    }

    private void addError(HashMap<String, String> errors, String host, Exception e) {
        errors.put(host, String.format("%s=%s", e.getClass().getName(), e.getMessage()));
    }

    /**
     * Ensures that the entity content is fully consumed and the content stream, if exists,
     * is closed.
     */
    private void consumeQuietly(final HttpURLConnection connection) {
        try {
            int read = 0;
            while (read != -1) {
                read = connection.getInputStream().read();
            }
            connection.getInputStream().close();
            read = 0;
            while (read != -1) {
                read = connection.getErrorStream().read();
            }
            connection.getErrorStream().close();
            connection.disconnect();
        } catch (IOException e) {
            // no inputStream to close
        }
    }
}
