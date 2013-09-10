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

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataGenTool extends Configured implements Tool {

    static int printUsage() {
    System.out.println("\nUsage: hadoop jar <this.jar> " +
                        DataGenTool.class.getCanonicalName() +
                        " -mappers <stations-file> " +
                        "-output <output> " +
                        "-json.cfg <config> " +
                        "-count <count per mapper>\n ");
        ToolRunner.printGenericCommandUsage(System.out);
        return 2;
    }


    @Override
    public int run(String[] args) throws Exception {

        Job job = Job.getInstance(getConf()); // new Job(conf, this.getClass().getCanonicalName());

//        Configuration conf = getConf();

        int mappers = 2;
        String output = null;
        String config = null;
        long count = 100;

        List<String> otherArgs = new ArrayList<String>();
        for(int i=0; i < args.length; ++i) {
          try {
            if ("-mappers".equals(args[i])) {
              mappers = Integer.parseInt(args[++i]);
              otherArgs.add("-Dmapreduce.job.maps="+Integer.toString(mappers));
            } else if ("-output".equals(args[i])) {
              output = args[++i];
            } else if ("-json.cfg".equals(args[i])) {
              config = args[++i];
            } else if ("-count".equals(args[i])) {
              count = Long.parseLong(args[++i]);
            } else {
                  otherArgs.add(args[i]);
            }

          } catch (NumberFormatException except) {
            System.out.println("ERROR: Integer expected instead of " + args[i]);
            return printUsage();
          } catch (ArrayIndexOutOfBoundsException except) {
            System.out.println("ERROR: Required parameter missing from " +
                args[i-1]);
            return printUsage(); // exits
          }
        }

        job.getConfiguration().set("json.cfg", config);

        String[] altArgs = new String[otherArgs.size()];
        otherArgs.toArray(altArgs);

        GenericOptionsParser gop = new GenericOptionsParser(job.getConfiguration(), altArgs);

        DataGenInputFormat.setNumberOfRows(job,count);

        job.setJarByClass(DataGenTool.class);

        Path output_path = new Path(output);

        if (output_path.getFileSystem(getConf()).exists(output_path)) {
            throw new IOException("Output directory " + output_path +
                    " already exists.");
        }

        FileOutputFormat.setOutputPath(job, output_path);

        job.setMapperClass(DataGenMapper.class);
        // Map Only Job
        job.setNumReduceTasks(0);
//        job.setReducerClass(RerateReducer.class);

        job.setInputFormatClass(DataGenInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        job.setMapOutputKeyClass(NullWritable.class);
        job.setMapOutputValueClass(Text.class);
//        job.setOutputKeyClass(Text.class);
//        job.setOutputValueClass(Text.class);

        return job.waitForCompletion(true) ? 0 : 1;

    }


    public static void main(String[] args) throws Exception {
        int result;
        result = ToolRunner.run(new Configuration(), new DataGenTool(), args);
        System.exit(result);
    }
}
