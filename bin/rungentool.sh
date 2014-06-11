#!/bin/bash

function printUsage {
echo "NOTE: All paths and files are on HDFS"
echo "$0 <json.config.file> <count(records to create)> <mappers> <output-path>"
exit -1
}

if [ $# != 4 ]; then
    printUsage
fi

APP_DIR=`dirname $0`

pushd $APP_DIR

# Build Classpath for MapReduce Jobs
#. ./genjars.sh

# Build Classpath for Tool
#. ./hadoop_classpath.sh

APP_JAR=../target/data.gen-1.0-SNAPSHOT-shaded.jar
MAIN=com.hortonworks.pso.data.generator.mapreduce.DataGenTool

#hadoop jar $APP_JAR $MAIN --libjars $GENJARS -json.cfg $1 -count $2 -mappers $3 -output $4
hadoop jar $APP_JAR $MAIN -jsonCfg $1 -count $2 -mappers $3 -output $4

# Return to original directory
popd
