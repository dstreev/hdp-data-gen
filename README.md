# Data Generation with MapReduce

## License
>   Licensed under the Apache License, Version 2.0 (the "License");
>   you may not use this file except in compliance with the License.
>   You may obtain a copy of the License at
>
>       http://www.apache.org/licenses/LICENSE-2.0
>
>   Unless required by applicable law or agreed to in writing, software
>   distributed under the License is distributed on an "AS IS" BASIS,
>   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
>   See the License for the specific language governing permissions and
>   limitations under the License.

## Summary
This is a Data Generator that will run in MapReduce to produce sample datasets that are configured by a json control file.  Output can be directed to HDFS or to a Kafka Topic.

## Building
A maven pom is provided and can be used to build the project with:
<code>mvn clean install</code>

This will also create a `target/lib directory` with the jar's that need to be copied to HDFS to support the Mappers.

## Json Schema and Examples
Are in the `src/main/resources` directory and will be a part of the binary created from maven.

## Running
Load the Json configuration file into HDFS and then invoke `bin/rungentool.sh` with the following switches.

```
Check Usage
17/11/21 15:15:24 INFO mapreduce.DataGenTool: No SINK specified, using DEFAULT (HDFS)
usage: hadoop jar
              <jar-file>com.hortonworks.pso.data.generator.mapreduce.DataG
              enTool
 -count <count>           total record count
 -help                    This help
 -jsonCfg <json config>   control file
 -mappers <mappers>       parallelism
 -output <output>         Sink output information. HDFS-Output Directory
                          or Kafka-URL: kafka://<kafka host>/<topic> OR
                          kafka://kafka-server:9000,kafka-server2:9000/foo
                          bar
 -sink <sink>             Target Sink: (HDFS|KAFKA) default-HDFS
```

### Example Usage
Using HDFS relative paths in example below to find the Json file in home directory and to create an output directory there as well.

```
[maria_dev@sandbox bin]$ ./rungentool.sh -count 40000 -jsonCfg sample-record-generator.json -mappers 4 -sink HDFS -output sample-output
~/perf/hdp-data-gen-master/bin ~/perf/hdp-data-gen-master/bin
Check Usage
17/11/21 15:52:57 INFO mapreduce.DataGenTool: Using Sink:HDFS
17/11/21 15:52:58 INFO impl.TimelineClientImpl: Timeline service address: http://sandbox.hortonworks.com:8188/ws/v1/timeline/
17/11/21 15:52:58 INFO client.RMProxy: Connecting to ResourceManager at sandbox.hortonworks.com/172.17.0.2:8050
17/11/21 15:52:59 INFO client.AHSProxy: Connecting to Application History server at sandbox.hortonworks.com/172.17.0.2:10200
17/11/21 15:53:00 INFO mapreduce.DataGenInputFormat: Generating 40000 using 4
17/11/21 15:53:00 INFO mapreduce.JobSubmitter: number of splits:4
	... OUTPUT LINES REMOVED ...
	File Output Format Counters 
		Bytes Written=4155888
[maria_dev@sandbox bin]$ hdfs dfs -ls -R sample-output
-rw-r--r--   1 maria_dev hdfs          0 2017-11-21 15:53 sample-output/_SUCCESS
-rw-r--r--   1 maria_dev hdfs    1039016 2017-11-21 15:53 sample-output/part-m-00000
-rw-r--r--   1 maria_dev hdfs    1038939 2017-11-21 15:53 sample-output/part-m-00001
-rw-r--r--   1 maria_dev hdfs    1038971 2017-11-21 15:53 sample-output/part-m-00002
-rw-r--r--   1 maria_dev hdfs    1038962 2017-11-21 15:53 sample-output/part-m-00003
[maria_dev@sandbox bin]$ hdfs dfs -tail sample-output/part-m-00003
2010-11-20 02:52:59	761	10.79.184.238	ToMobile	192.168.22.24	1	8074957	386.9	true	661.4271
3AFBADFDDA	2011-04-21 11:03:24	817	10.88.252.135	FromMobile	192.168.89.251	1	8487980	721.2	false	737.1775
5F3F2EE44E	2012-01-15 05:41:04	569	10.71.158.190	FromMobile	192.168.78.49	1	7713729	724.92	false	595.1762
EAB5A4D3BA	2010-10-31 06:45:03	485	10.67.176.35	ToMobile	192.168.34.114	1	7538425	67.0	false	563.0247
4FB45ABDBF	2011-02-19 05:27:54	456	10.49.14.129	FromMobile	192.168.53.254	1	6707775	974.89	false	410.6796
F4F54FA44D	2011-06-30 07:55:07	927	10.83.230.128	ToMobile	192.168.49.218	0	8261227	166.6	true	695.59
B3F4AED1D3	2012-08-23 05:54:30	164	10.57.33.49	ToMobile	192.168.51.174	0	7067697	114.16	true	476.6909
3FB4DAAD3E	2010-09-02 16:32:46	423	10.74.250.28	ToMobile	192.168.31.107	0	7863392	102.02	true	622.6251
A5B5AD24DD	2011-05-08 11:01:19	826	10.62.122.210	ToMobile	192.168.67.228	1	7306223	948.45	false	520.4377
52EE5F23F4	2013-03-19 15:27:38	230	10.18.138.95	FromMobile	192.168.17.172	1	5347261	32.2	true	161.155
[maria_dev@sandbox bin]$ 
```

## Supported Datatypes

### StringField
Supports both "Random" strings of a desired length range and "Sets".

#### Random String Configuration Example
<pre><code>
       {
            "string": {
                "random": {
                    "min": 10,
                    "max": 10,
                    "chars": "ABDEF12345",
                    "pool": {
                        "size": "100"
                    }
                }
            }
        }
</code></pre>

Random support a pool of string values when defined.  If present ("pool"), a random set of strings will be generated during construction based on the other settings.  These values are placed in the pool and these values are randomly given out during record creation.  This is helpful if you are trying to establish a field with a constrained bucket of values.  If the list is short, consider using "set" for more control.

#### Set String Configuration Example
<pre><code>
        {
            "string": {
                "set": ["ToMobile", "FromMobile"]
            }
        }
</code></pre>


### BooleanField

Produces a boolean output.  The "format" identifies the boolean values.  This can be anything. IE: true:false or F:T or YES:NO.  The values must be delimited by a ":" and will be randomly used during record creation.

<pre><code>
        {
            "boolean": {
                "format": "0:1"
            }
        }
</pre></code>

### DateField
Produces a random date between the two dates supplied in the range.  The range values must match the "format" element.  The "format" also controls the output format used during record creation.  The java "SimpleDateFormat" pattern values are used to define "format".

<pre><code>
        {
            "date": {
                "format": "yyyy-MM-dd HH:mm:ss",
                "range": {
                    "min": "2010-01-01 00:00:00",
                    "max": "2013-04-01 23:59:59"
                }
            }
        }
</pre></code>

### Start.Stop
Produces a start and stop date field that are consistent with each other and vary by a range determined in by the "spread" configuration.

The order, when supplied, is an array containing the precedence in the output.  If it isn't supplied the fields will be added (start then stop) to the record based on the order in the configuration file.

<pre><code>
        {
            "start.stop": {
                "order": [1,2],
                "format": "yyyy-MM-dd HH:mm:ss",
                "range": {
                    "min": "2010-01-01 00:00:00",
                    "max": "2013-04-01 23:59:59"
                },
                "spread": {
                    "min": 100,
                    "max": 100000
                }
            }
        }
</pre></code>

### IPAddressField
Produces a random Ip address between the values defined in "minIp" and "maxIp".  Pooling is also supported and follows the same pattern used for "pool" in the StringField.

<pre><code>
        {
            "ip": {
                "minIp": "192.168.14.0",
                "maxIp": "192.168.100.254",
                "pool": {
                    "size": 100
                }
            }
        }
</pre></code>

### ListField (not yet implemented)
The goal is to use a defined dictionary file of values for record creation.  This is helpful for generating data in different datasets that may share a common attribute used to later join them, like an account number.

### NumberField
Support the random creation of "int,long,float,double" values.  When non-real numbers are used, a "decimal" element controls the precision of the values used in record creation.

The produced numbers will be between the "min" and "max" values.

<pre><code>
       {
            "number": {
                "type": "long",
                "min": 4523100,
                "max": 9921000
            }
        },
        {
            "number": {
                "type": "float",
                "decimals": 2,
                "min": 10,
                "max": 1000
            }
        }
</pre></code>

### NullField
Create an empty field, useless the "nullvalue" element is set, in which case, that value will be used.

<pre><code>
        {
            "null": {
                "nullvalue": "NULL"
            }
        }
</pre></code>


## Field Positions
Field positions are determined by the order of the fields as defined in the json configuration file.  If you would like to override this, an "order" element can be added to the field definition.

If one field has an "order" element defined, then ALL fields must have an "order" element, otherwise the parser will complain.

The "order" doesn't need to be consecutive, just weighted.  Skipping values will NOT leave empty values, use the NullField for that.
<pre><code>
        {
            "string": {
                "order": 1,
                "set": ["ToMobile", "FromMobile"]
            }
        }
</pre></code>