#!/bin/bash

# Build up classpath for client-side MR job creation/launch that
# requires access to third-party libs.

# You will still need --libjars for the task trackers on the cluster.
#  See the genjars.sh scripts for that.

for jar in `ls ../target/lib/*.jar`; do
GEN_TOOL_JARS=$GEN_TOOL_JARS:$jar
done

export HADOOP_CLASSPATH=$GEN_TOOL_JARS

echo "HADOOP_CLASSPATH: $HADOOP_CLASSPATH"