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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.File;
import java.util.Date;

public class RecordGeneratorTest {

    @Test
    public void Test001() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode rootNode = mapper.readValue(new File("src/main/resources/sample-record-generator.json"), JsonNode.class);

            RecordGenerator recGen = new RecordGenerator(rootNode);

            groupGen(recGen);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    public void Test002() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode rootNode = mapper.readValue(new File("src/main/resources/sample-record-ordered-generator.json"), JsonNode.class);

            RecordGenerator recGen = new RecordGenerator(rootNode);

            groupGen(recGen);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void Test003() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode rootNode = mapper.readValue(new File("src/main/resources/sample-null-generator.json"), JsonNode.class);

            RecordGenerator recGen = new RecordGenerator(rootNode);

            groupGen(recGen);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void Test004() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode rootNode = mapper.readValue(new File("src/main/resources/sample-startstop-generator.json"), JsonNode.class);

            RecordGenerator recGen = new RecordGenerator(rootNode);

            groupGen(recGen);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void Test050() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode rootNode = mapper.readValue(new File("src/main/resources/sample-record-generator.json"), JsonNode.class);

            RecordGenerator recGen = new RecordGenerator(rootNode);
            long start = new Date().getTime();
            System.out.println("Starting.. " );
            for (int i=0;i<1000000;i++) {
                String value = recGen.next();
            }
            long end = new Date().getTime();
            System.out.println("Finished generating 1,000,000 in (ms): " + (end-start));


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void groupGen(RecordGenerator recGen) {
        System.out.println(recGen.next());
        System.out.println(recGen.next());
        System.out.println(recGen.next());
        System.out.println(recGen.next());
        System.out.println(recGen.next());
        System.out.println(recGen.next());
        System.out.println(recGen.next());
    }
}
