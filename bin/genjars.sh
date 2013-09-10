#!/bin/bash

# When writing a MapReduce job that interacts with third-party libraries
# that aren't a part of hadoop-core, these libraries need to reside in HDFS
# to support cluster jobs.
#
# Using hadoop fs -put ..  , place the libraries on HDFS with permissions
#    the will allow other to read them.  This script will query the directory
#    in HDFS and build the classpath to be used for --libjars 
#
# This technique uses existing HDFS files for distributed cache, avoiding
#    the need to push the libraries with every request.
#
# Source this script on the client that will be used to launch the Map Reduce job.
#  Then add $GENJARS as the value for --libjars when launching the job.
#
# Construct a libjars path from the jars on HDFS in your HDFS home directory "gen.libs"

GEN_LIBS_DIR=gen.libs

for i in `hadoop fs -ls $GEN_LIBS_DIR`; do
	if [[ $i == *.jar ]]; then
		if [ "${GENJARS}" == "" ]; then
			GENJARS=hdfs://$i
		else
			GENJARS=hdfs://$i,${GENJARS}
		fi
	fi
done

export GENJARS
echo $GENJARS
