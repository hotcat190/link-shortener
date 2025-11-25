NPM_COMMAND=$1
VUS=$2
LOG_LABEL=$3
CACHE_ENABLED=$4

echo "Starting load test suite for: $NPM_COMMAND, $VUS VUs, $LOG_LABEL, $CACHE_ENABLED"

node src/be_eval.js $VUS $LOG_LABEL $NPM_COMMAND $CACHE_ENABLED &
npm run $NPM_COMMAND $VUS $LOG_LABEL $CACHE_ENABLED

echo ""
echo "------------------------------------------------"
echo "Load test suite finished for: $LOG_LABEL"