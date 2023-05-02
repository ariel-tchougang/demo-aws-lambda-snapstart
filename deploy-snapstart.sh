#!/bin/sh

clear
sam build --template-file template-snapstart.yaml
sam deploy --no-confirm-changeset --template-file template-snapstart.yaml --config-file config-snapstart.toml --s3-bucket speedup-lambda-demo-bucket-atchougs --region eu-west-1
