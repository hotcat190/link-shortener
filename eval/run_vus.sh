NPM_COMMAND=$1
VUS=$2

echo "Starting load test suite for: $NPM_COMMAND, $VUS VUs"

node src/be_eval.js $NPM_COMMAND $VUS &
npm run $NPM_COMMAND $VUS

echo ""
echo "------------------------------------------------"
echo "Load test suite finished for: $NPM_COMMAND, $VUS VUs"