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

package com.hortonworks.pso.data.generator.mapreduce;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hortonworks.pso.data.generator.RecordGenerator;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.log4j.Logger;

import java.io.*;

public class DataGenMapper extends Mapper<LongWritable, NullWritable, NullWritable, Text> {
    static private Logger LOG = Logger.getLogger(DataGenTool.class.getName());

    public static final String CONFIG_FILE = "app.config.file";
    public static final String DEFAULT_CONFIG_RESOURCE_FILE = "/validation-generator.json";
    
    private RecordGenerator recordGenerator;

    protected void setup(Context context) {
        // Get the conf location from the job conf.
        String config = context.getConfiguration().get(CONFIG_FILE);
        if (config.equals("DEFAULT")) {
            // Use the default validation file.
            LOG.info("Using DEFAULT Config File: " + DEFAULT_CONFIG_RESOURCE_FILE + " from package resources.");
            try {
                InputStream stream = getClass().getResourceAsStream(DEFAULT_CONFIG_RESOURCE_FILE);
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readValue(stream, JsonNode.class);

                recordGenerator = new RecordGenerator(root);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        } else {
            LOG.info("Config File: " + config);

            // Read the Config from the path.
            FileSystem FS1 = null;
            FSDataInputStream fsdis = null;
            try {
                FS1 = FileSystem.get(context.getConfiguration());

                Path path = new Path(config);

                fsdis = FS1.open(path);

                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readValue(fsdis, JsonNode.class);

                recordGenerator = new RecordGenerator(root);

            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            } finally {
                IOUtils.closeStream(fsdis);
            }
        }

    }

    public void map(LongWritable key, NullWritable value, Context context) throws IOException, InterruptedException {
        Text record = new Text();
        record.set(recordGenerator.next());
        context.write(NullWritable.get(), record);
    }
}
