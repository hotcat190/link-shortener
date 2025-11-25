#!/bin/bash

# --- This script runs a sweep of load tests from 100 to 500 VUs ---
#
# It takes one argument: a label (e.g., SYNC or ASYNC) to tag the
# log files, allowing for easy A/B comparison.
#
# Usage:
# ./run_load_tests.sh SYNC
# ./run_load_tests.sh ASYNC

# 1. Check if the log label argument (SYNC/ASYNC) is provided
if [ -z "$1" ]; then
    echo "Error: Missing log label."
    echo "Usage: ./run_load_tests.sh [SYNC|ASYNC]"
    exit 1
fi

LOG_LABEL=$1
NPM_COMMAND=$2
CACHE_ENABLED=$3

echo "Starting load test suite for: $LOG_LABEL"
echo "Press Ctrl+C to stop the entire suite."
echo "------------------------------------------------"

# 2. Loop from 100 to 1000, with a step of 100
for (( VUS=1000; VUS<=1000; VUS+=100 )); do
    echo ""
    echo "Starting test: $VUS VUs ($LOG_LABEL)"
    
    # 3. Run the npm script with the VUS count and LOG_LABEL
    # This will execute the "eval_n" script from your package.json
    node src/be_eval.js $VUS $LOG_LABEL $NPM_COMMAND $CACHE_ENABLED &
    npm run $NPM_COMMAND $VUS $LOG_LABEL $CACHE_ENABLED
    
    echo "Completed test: $VUS VUs ($LOG_LABEL). Log saved."
   
    sleep 10
done

echo ""
echo "------------------------------------------------"
echo "Load test suite finished for: $LOG_LABEL"
