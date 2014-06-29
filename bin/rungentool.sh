#!/bin/bash

APP_DIR=`dirname $0`

pushd $APP_DIR

# Build Classpath for MapReduce Jobs
#. ./genjars.sh

# Build Classpath for Tool
#. ./hadoop_classpath.sh

APP_JAR=../target/data.gen-1.0-SNAPSHOT-shaded.jar
MAIN=com.hortonworks.pso.data.generator.mapreduce.DataGenTool

#hadoop jar $APP_JAR $MAIN --libjars $GENJARS -json.cfg $1 -count $2 -mappers $3 -output $4
#hadoop jar $APP_JAR $MAIN -jsonCfg $1 -count $2 -mappers $3 -output $4
hadoop jar $APP_JAR $MAIN $@

# Return to original directory
popd
