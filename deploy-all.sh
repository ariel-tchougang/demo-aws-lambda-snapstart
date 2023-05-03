#!/bin/sh

echo "Clearing screen"
clear

if [ "$#" -ne 2 ]; then
  echo "Error: This script requires exactly 2 arguments."
  echo "Usage: ./deploy-all.sh UPLOAD_BUCKET AWS_REGION"
  exit 1
fi

echo "Removing old build files"
rm -rf .aws-sam/

echo "Building application"
sam build --template-file template-standard.yaml

UPLOAD_BUCKET="$1"
AWS_REGION="$2"

echo "Deploying standard version"
sam deploy --no-confirm-changeset --config-file config-standard.toml --s3-bucket $UPLOAD_BUCKET --region $AWS_REGION

echo "Copying files template-snapstart.yaml into .aws-sam/build/template.yaml"
rm -f .aws-sam/build/template.yaml
cp template-snapstart.yaml .aws-sam/build/
mv .aws-sam/build/template-snapstart.yaml .aws-sam/build/template.yaml

echo "Deploying snapstart version"
sam deploy --no-confirm-changeset --config-file config-snapstart.toml --s3-bucket $UPLOAD_BUCKET --region $AWS_REGION
