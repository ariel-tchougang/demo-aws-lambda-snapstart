#!/bin/sh

echo "Clearing screen"
clear

if [ "$#" -ne 1 ]; then
  echo "Error: This script requires exactly 1 argument."
  echo "Usage: ./cleanup-all.sh AWS_REGION"
  exit 1
fi

echo "Removing old build files"
rm -rf .aws-sam/

echo "Cleaning mvn build"
mvn clean -f ProductApiFunction/pom.xml

AWS_REGION="$1"

echo "Deleting stack product-api-standard"
aws cloudformation delete-stack --stack-name product-api-standard --region $AWS_REGION

echo "Deleting stack product-api-snapstart"
aws cloudformation delete-stack --stack-name product-api-snapstart --region $AWS_REGION
