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

import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hortonworks.pso.data.generator.fields.StringField;
import org.junit.Test;

import java.io.File;

public class StringFieldTest {

    @Test
    public void TestOne() {
        assertTrue(true);

        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode rootNode = mapper.readValue(new File("src/main/resources/sample-record-generator.json"), JsonNode.class);
            System.out.println(rootNode.size());
            JsonNode array = rootNode.get("fields");
            System.out.println(array.size());
            JsonNode field1 = array.get(0);
            System.out.println(field1.size());
            JsonNode f1type = field1.get("string");
            StringField sf = new StringField(f1type);

            System.out.println(sf.getValue());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
