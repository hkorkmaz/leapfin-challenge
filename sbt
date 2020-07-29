#!/bin/sh

rm ./RUNNING_PID 2> /dev/null

java \
  -Xms1G \
  -Xmx2G \
  $SBT_OPTS \
  -jar `dirname $0`/sbt-launch.jar \
  "$@"