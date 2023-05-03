#!/bin/sh

if [ "$#" -ne 1 ]; then
  echo "Error: This script requires exactly 1 argument."
  echo "Usage: ./run-artillery.sh AWS_REGION"
  exit 1
fi

AWS_REGION="$1"

echo "Running Artillery on product-api-standard"
STANDARD_STACK_NAME="product-api-standard"
STANDARD_API_GW_URL=$(aws cloudformation describe-stacks --stack-name "$STANDARD_STACK_NAME" --query "Stacks[0].Outputs[?OutputKey=='ProductApiUrl'].OutputValue" --region $AWS_REGION --output text)
artillery run -t $STANDARD_API_GW_URL -v '{ "url": "/Prod/products" }' misc/loadtest.yaml

sleep 30

echo "Running Artillery on product-api-snapstart"
SNAPSTART_STACK_NAME="product-api-snapstart"
SNAPSTART_API_GW_URL=$(aws cloudformation describe-stacks --stack-name "$SNAPSTART_STACK_NAME" --query "Stacks[0].Outputs[?OutputKey=='ProductApiUrl'].OutputValue" --region $AWS_REGION --output text)
artillery run -t $SNAPSTART_API_GW_URL -v '{ "url": "/Prod/products" }' misc/loadtest.yaml
