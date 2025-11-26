#!/bin/bash

# --- This script runs a sweep of load tests from 100 to 500 VUs ---
#
# It takes one argument: a label (e.g., SYNC or ASYNC) to tag the
# log files, allowing for easy A/B comparison.
#
# Usage:
# ./run_load_tests.sh SYNC
# ./run_load_tests.sh ASYNC

NPM_COMMAND=$1

echo "Starting load test suite for ($NPM_COMMAND)"
echo "Press Ctrl+C to stop the entire suite."
echo "------------------------------------------------"

# 2. Loop from 100 to 1000, with a step of 100
for (( VUS=100; VUS<=1000; VUS+=100 )); do
    echo ""
    echo "Starting test: $VUS VUs"
    
    # 3. Run the npm script with the VUS count and LOG_LABEL
    # This will execute the "eval_n" script from your package.json
    node src/be_eval.js $NPM_COMMAND $VUS &
    npm run $NPM_COMMAND $VUS
    
    echo "Completed test: $VUS VUs. Log saved."
   
    sleep 60
done

echo ""
echo "------------------------------------------------"
echo "Load test suite finished for ($NPM_COMMAND)."
