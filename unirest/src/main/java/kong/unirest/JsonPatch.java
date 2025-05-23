/**
 * The MIT License
 *
 * Copyright for portions of unirest-java are held by Kong Inc (c) 2013.
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

package kong.unirest;


import org.json.gsc.JSONArray;
import org.json.gsc.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class JsonPatch {

    private final List<JsonPatchItem> items = new ArrayList<>();

    public JsonPatch(){}

    public JsonPatch(String fromString){
        for (Object row : JSONArray.build(fromString)) {
            if (row instanceof JSONObject) {
                items.add(new JsonPatchItem((JSONObject) row));
            }
        }
    }

    public void add(String path, Object value) {
        items.add(new JsonPatchItem(JsonPatchOperation.add, path, value));
    }

    public void remove(String path) {
        items.add(new JsonPatchItem(JsonPatchOperation.remove, path));
    }

    public void replace(String path, Object value) {
        items.add(new JsonPatchItem(JsonPatchOperation.replace, path, value));
    }

    public void test(String path, Object value) {
        items.add(new JsonPatchItem(JsonPatchOperation.test, path, value));
    }

    public void move(String from, String path) {
        items.add(new JsonPatchItem(JsonPatchOperation.move, path, from));
    }

    public void copy(String from, String path) {
        items.add(new JsonPatchItem(JsonPatchOperation.copy, path, from));
    }

    @Override
    public String toString() {
        JSONArray a = new JSONArray();
        items.forEach(i -> a.put(JSONObject.build(i.toString())));
        return a.toString();
    }

    public Collection<JsonPatchItem> getOperations(){
        return Collections.unmodifiableList(items);
    }
}