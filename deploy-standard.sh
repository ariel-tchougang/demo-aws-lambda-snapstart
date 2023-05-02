#!/bin/sh

clear
sam build --template-file template-standard.yaml
sam deploy --no-confirm-changeset --template-file template-standard.yaml --config-file config-standard.toml --s3-bucket speedup-lambda-demo-bucket-atchougs --region eu-west-1
