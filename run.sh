#!/bin/bash

NUMBER_OF_WORKERS=10
TIMEOUT=60
LOG_LEVEL=INFO

CLEAR='\033[0m'
RED='\033[0;31m'

function usage() {
  if [[ -n "$1" ]]; then
    echo -e "${RED}!!!! $1 !!!!${CLEAR}";
  fi
  echo "Usage: $0 [-n number-of-workers] [-t worker-timeout] [-l log-level]"
  echo "  -n   The number of workers, default=10"
  echo "  -t,  Timeout in seconds for workers, default=60"
  echo "  -l,  Log level for the application, default=OFF, options : [OFF, ERROR, WARNING, INFO, DEBUG]"
  echo ""
  echo "Example: $0 -n 2 -t 10 -l INFO"
  exit 1
}

# parse params
while [[ "$#" > 0 ]]; do case $1 in
  -n) NUMBER_OF_WORKERS="$2"; shift;shift;;
  -t) TIMEOUT="$2";shift;shift;;
  -l) LOG_LEVEL="$2";shift;shift;;
  -h) usage ;shift;shift;;
   *) usage "Unknown parameter passed: $1"; shift; shift;;
esac; done

# verify params
if [[ -z "$NUMBER_OF_WORKERS" ]]; then usage "Number of workers is not set"; fi;
if [[ -z "$TIMEOUT" ]]; then usage "Timeout is not set."; fi;
if [[ -z "$LOG_LEVEL" ]]; then usage "Log level is not set."; fi;

java -Dworker.count=${NUMBER_OF_WORKERS} -Dworker.timeout=${TIMEOUT} -Dlog.level=${LOG_LEVEL} -jar dist/finleap.jar