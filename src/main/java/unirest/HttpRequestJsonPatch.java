/**
 * The MIT License
 *
 * Copyright for portions of OpenUnirest/uniresr-java are held by Mashape (c) 2013 as part of Kong/unirest-java.
 * All other copyright for OpenUnirest/unirest-java are held by OpenUnirest (c) 2018.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package unirest;

import org.apache.http.HttpEntity;
import org.apache.http.entity.BasicHttpEntity;

import java.io.ByteArrayInputStream;

class HttpRequestJsonPatch extends BaseRequest<JsonPatchRequest> implements JsonPatchRequest {

    private JsonPatch items = new JsonPatch();

    HttpRequestJsonPatch(Config config, String url) {
        super(config, HttpMethod.PATCH, url);
        header("Content-Type", CONTENT_TYPE);
    }

    @Override
    public JsonPatchRequest add(String path, Object value) {
        items.add(path, value);
        return this;
    }

    @Override
    public JsonPatchRequest remove(String path) {
        items.remove(path);
        return this;
    }

    @Override
    public JsonPatchRequest replace(String path, Object value) {
        items.replace(path, value);
        return this;
    }

    @Override
    public JsonPatchRequest test(String path, Object value) {
        items.test(path, value);
        return this;
    }

    @Override
    public JsonPatchRequest move(String from, String path) {
        items.move(from, path);
        return this;
    }

    @Override
    public JsonPatchRequest copy(String from, String path) {
        items.copy(from, path);
        return this;
    }

    @Override
    public Body getBody() {
        return this;
    }

    @Override
    public HttpEntity getEntity() {
        BasicHttpEntity e = new BasicHttpEntity();
        e.setContent(new ByteArrayInputStream(items.toString().getBytes()));
        return e;
    }
}