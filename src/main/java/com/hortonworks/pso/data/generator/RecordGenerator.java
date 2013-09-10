/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hortonworks.pso.data.generator;

import com.fasterxml.jackson.databind.JsonNode;
import com.hortonworks.pso.data.generator.fields.*;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class RecordGenerator {
    private Map<Integer,FieldType> fields = new TreeMap<Integer,FieldType>();
    private String delimiter = "\t";
    private String terminator = "\n";
    private boolean orderForced = false;

    public RecordGenerator(JsonNode node) {
        // This node should be either the "root" node OR the "fields" node.
        delimiter = node.get("delimiter").asText();

        JsonNode fieldsNode = node.get("fields");

        //fields = new FieldType[fieldsNode.size()];

        for (int i=0;i < fieldsNode.size();i++) {
            JsonNode fieldNode = fieldsNode.get(i);
            if (fieldNode.has("string")) {
                FieldType field = new StringField(fieldNode.get("string"));
                addFields(field);
            } else if (fieldNode.has("number")) {
                FieldType field = new NumberField(fieldNode.get("number"));
                addFields(field);
            } else if (fieldNode.has("ip")) {
                FieldType field = new IPAddressField(fieldNode.get("ip"));
                addFields(field);
            } else if (fieldNode.has("boolean")) {
                FieldType field = new BooleanField(fieldNode.get("boolean"));
                addFields(field);
            } else if (fieldNode.has("date")) {
                FieldType field = new DateField(fieldNode.get("date"));
                addFields(field);
            } else if (fieldNode.has("null")) {
                FieldType field = new NullField(fieldNode.get("null"));
                addFields(field);
            }
        }
    }

    private void addFields(FieldType field) {
        boolean fieldHasOrder = field.getJsonNode().has("order");
        if (orderForced | fieldHasOrder) {
            orderForced = true;
            if (!fieldHasOrder) {
                throw new RuntimeException("Once order is used to control field positions, it must be used in every field definition.");
            } else {
                fields.put(field.getJsonNode().get("order").asInt(), field);
            }
        } else {
           fields.put(fields.size(),field);
        }
    }

    /*
    Generate New Record.
     */
    public String next() {
        StringBuilder sb = new StringBuilder();
        Iterator<Map.Entry<Integer,FieldType>> fieldsIterator = fields.entrySet().iterator();

        while (fieldsIterator.hasNext()) {
            Map.Entry<Integer, FieldType> fieldMapRec = fieldsIterator.next();
            sb.append(fieldMapRec.getValue().getValue());
            if (fieldsIterator.hasNext())
                sb.append(delimiter);
        }

        return sb.toString();
    }

}
