#!/bin/bash

if [ "$#" -ne 1 ]; then
  echo "Error: This script requires exactly 1 argument."
  echo "Usage: ./run-logs-insights.sh AWS_REGION"
  exit 1
fi

AWS_REGION="$1"  
LOG_GROUP_NAME_1="/aws/lambda/product-api-standard"
LOG_GROUP_NAME_2="/aws/lambda/product-api-snapstart"
QUERY='filter @type = "REPORT" | parse @log /\d+:\/aws\/lambda\/(?<function>.*)/ | parse @message /Restore Duration: (?<restoreDuration>.*?) ms/ | stats count(*) as invocations, pct(@duration+coalesce(@initDuration,0)+coalesce(restoreDuration,0), 0) as p0, pct(@duration+coalesce(@initDuration,0)+coalesce(restoreDuration,0), 25) as p25, pct(@duration+coalesce(@initDuration,0)+coalesce(restoreDuration,0), 50) as p50, pct(@duration+coalesce(@initDuration,0)+coalesce(restoreDuration,0), 90) as p90, pct(@duration+coalesce(@initDuration,0)+coalesce(restoreDuration,0), 95) as p95, pct(@duration+coalesce(@initDuration,0)+coalesce(restoreDuration,0), 99) as p99, pct(@duration+coalesce(@initDuration,0)+coalesce(restoreDuration,0), 100) as p100 group by function, (ispresent(@initDuration) or ispresent(restoreDuration)) as coldstart | sort by coldstart desc'
END=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
START=$(date -u -d "-15 minutes" +"%Y-%m-%dT%H:%M:%SZ")

# Start the first query
QUERY_ID_1=$(aws logs start-query --log-group-name "${LOG_GROUP_NAME_1}" --start-time "$(date +%s --date="${START}")" --end-time "$(date +%s --date="${END}")" --query-string "${QUERY}" --query 'queryId' --region $AWS_REGION --output text)

# Start the second query
QUERY_ID_2=$(aws logs start-query --log-group-name "${LOG_GROUP_NAME_2}" --start-time "$(date +%s --date="${START}")" --end-time "$(date +%s --date="${END}")" --query-string "${QUERY}" --query 'queryId' --region $AWS_REGION --output text)

# Wait for the queries to complete
sleep 10

# Retrieve the results for the first query
echo "Results for log group $LOG_GROUP_NAME_1:"
aws logs get-query-results --query-id "${QUERY_ID_1}" --region $AWS_REGION

# Retrieve the results for the second query
echo "Results for log group $LOG_GROUP_NAME_2:"
aws logs get-query-results --query-id "${QUERY_ID_2}" --region $AWS_REGION
