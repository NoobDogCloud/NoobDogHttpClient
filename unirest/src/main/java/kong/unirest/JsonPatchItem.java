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

import org.json.gsc.JSONObject;

import java.util.Objects;

public class JsonPatchItem {
    private final JsonPatchOperation op;
    private final String path;
    private final Object value;

    public JsonPatchItem(JsonPatchOperation op, String path, Object value){
        this.op = op;
        this.path = path;
        this.value = value;
    }

    public JsonPatchItem(JsonPatchOperation op, String path) {
        this(op, path, null);
    }

    public JsonPatchItem(JSONObject row) {
        this.op = JsonPatchOperation.valueOf(row.getString("op"));
        this.path = row.getString("path");
        if(row.has(op.getOperationtype())) {
            this.value = row.get(op.getOperationtype());
        } else {
            this.value = null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {return true;}
        if (o == null || getClass() != o.getClass()) {return false;}
        JsonPatchItem that = (JsonPatchItem) o;
        return op == that.op &&
                Objects.equals(path, that.path) &&
                Objects.equals(toString(), that.toString());
    }

    @Override
    public int hashCode() {
        return Objects.hash(op, path, value);
    }

    @Override
    public String toString() {
        JSONObject json = new JSONObject()
                .put("op", op)
                .put("path", path);

        if(Objects.nonNull(value)){
            json.put(op.getOperationtype(), value);
        }

        return json.toString();
    }

    public JsonPatchOperation getOp() {
        return op;
    }

    public String getPath() {
        return path;
    }

    public Object getValue() {
        return value;
    }
}