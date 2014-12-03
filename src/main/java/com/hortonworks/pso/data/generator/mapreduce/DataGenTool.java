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

import kafka.bridge.hadoop2.KafkaOutputFormat;
import org.apache.commons.cli.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.MRJobConfig;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

import java.io.IOException;

public class DataGenTool extends Configured implements Tool {

    static private Logger LOG = Logger.getLogger(DataGenTool.class.getName());

    private Options options;
    private Path outputPath;
    private Sink sink;
    public static final int DEFAULT_MAPPERS = 2;
    public static final long DEFAULT_COUNT = 100;

    public enum Sink { HDFS, KAFKA};

    public DataGenTool() {
        buildOptions();
    }

    private void buildOptions() {
        options = new Options();
        Option help = OptionBuilder
                .withDescription("This help")
                .create("help");
        Option mappers = OptionBuilder.withArgName("mappers")
                .hasArg()
                .withDescription("parallelism")
                .create("mappers");
        Option sink = OptionBuilder.withArgName("sink")
                .hasArg()
                .withDescription("Target Sink: (HDFS|KAFKA) default-HDFS")
                .create("sink");
        Option outputDir = OptionBuilder.withArgName("output")
                .hasArg()
                .withDescription("Sink output information. HDFS-Output Directory or Kafka-URL: kafka://<kafka host>/<topic> OR kafka://kafka-server:9000,kafka-server2:9000/foobar")
                .create("output");
        Option config = OptionBuilder.withArgName("json config")
                .hasArg()
                .withDescription("control file")
                .create("jsonCfg");
        Option count = OptionBuilder.withArgName("count")
                .hasArg()
                .withDescription("total record count")
                .create("count");
        options.addOption(help);
        options.addOption(mappers);
        options.addOption(sink);
        options.addOption(outputDir);
        options.addOption(config);
        options.addOption(count);
    }

    private void printUsage() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("hadoop jar <jar-file>" + this.getClass().getCanonicalName(), options);
    }

    private boolean checkUsage(String[] args, Job job) {
        boolean rtn = true;
        Configuration configuration = job.getConfiguration();

        CommandLineParser clParser = new GnuParser();
        CommandLine line = null;
        try {
            line = clParser.parse(options, args);
        } catch (ParseException pe) {
            printUsage();
        }

        if (line.hasOption("help")) {
            return false;
        }

        job.setInputFormatClass(DataGenInputFormat.class);

        if (line.hasOption("count")) {
            DataGenInputFormat.setNumberOfRows(job, Long.parseLong(line.getOptionValue("count")));
        } else {
            DataGenInputFormat.setNumberOfRows(job, DEFAULT_COUNT);
        }

        if (line.hasOption("mappers")) {
            configuration.set(MRJobConfig.NUM_MAPS, line.getOptionValue("mappers"));
        } else {
            // Default
            configuration.setInt(MRJobConfig.NUM_MAPS, DEFAULT_MAPPERS);
        }

        if (line.hasOption("sink")) {
            String sinkOption = line.getOptionValue("sink");
            try {
                sink = Sink.valueOf(sinkOption.toUpperCase());

                job.setInputFormatClass(DataGenInputFormat.class);

                LOG.info("Using Sink:" + sink.toString());

                switch (sink) {
                    case HDFS:
                        job.setOutputFormatClass(TextOutputFormat.class);
                        if (line.hasOption("output")) {
                            outputPath = new Path(line.getOptionValue("output"));
                            FileOutputFormat.setOutputPath(job, outputPath);
                            job.setMapperClass(DataGenMapper.class);
                            job.setMapOutputKeyClass(NullWritable.class);
                            job.setMapOutputValueClass(Text.class);
                        } else {
                            return false;
                        }
                        break;
                    case KAFKA:
                        job.setOutputFormatClass(KafkaOutputFormat.class);
                        if (line.hasOption("output")) {
                            outputPath = new Path(line.getOptionValue("output"));
                            // The Topic should be included in the URL as well.
                            job.setMapperClass(KafkaDataGenMapper.class);
//                            job.setMapOutputKeyClass(NullWritable.class);
//                            job.setMapOutputValueClass(byte[].class);
                            KafkaOutputFormat.setOutputPath(job, outputPath);
                        } else {
                            return false;
                        }
                        break;
                }

            } catch (IllegalArgumentException iae) {
                return false;
            }
        } else {
            // Default HDFS.
            LOG.info("No SINK specified, using DEFAULT (HDFS)");
            job.setInputFormatClass(DataGenInputFormat.class);
            job.setOutputFormatClass(TextOutputFormat.class);
            job.setMapperClass(DataGenMapper.class);
            job.setMapOutputKeyClass(NullWritable.class);
            job.setMapOutputValueClass(Text.class);
            if (line.hasOption("output")) {
                outputPath = new Path(line.getOptionValue("output"));
                FileOutputFormat.setOutputPath(job, outputPath);
            } else {
                return false;
            }
        }

        if (line.hasOption("jsonCfg")) {
            configuration.set(DataGenMapper.CONFIG_FILE, line.getOptionValue("jsonCfg"));
        } else {
            configuration.set(DataGenMapper.CONFIG_FILE, "DEFAULT");
        }

        return rtn;
    }


    @Override
    public int run(String[] args) throws Exception {

        Job job = Job.getInstance(getConf()); // new Job(conf, this.getClass().getCanonicalName());

        System.out.println("Check Usage");
        if (!checkUsage(args, job)) {
            printUsage();
            return -1;
        }

        job.setJarByClass(DataGenTool.class);

        if (sink == Sink.HDFS && (outputPath == null || outputPath.getFileSystem(job.getConfiguration()).exists(outputPath))) {
            throw new IOException("Output directory " + outputPath +
                    " already exists OR is missing from parameters list.");
        }

        // Map Only Job
        job.setNumReduceTasks(0);

        return job.waitForCompletion(true) ? 0 : 1;

    }


    public static void main(String[] args) throws Exception {
        int result;
        result = ToolRunner.run(new Configuration(), new DataGenTool(), args);
        System.exit(result);
    }
}
