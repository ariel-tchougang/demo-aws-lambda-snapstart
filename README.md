# demo-aws-lambda-snapstart

This project contains source code and supporting files for a serverless application that you can deploy with the SAM CLI. It includes the following files and folders.

- ProductApiFunction/src/main - Code for the application's Lambda function.
- events - Invocation events that you can use to invoke the function.
- ProductApiFunction/src/test - Unit tests for the application code. 
- template-standard.yaml - A template that defines the application's AWS resources without AWS Lambda Snapstart.
- config-standard.toml - Deployment configuration file for standard version.
- template-standard.yaml - A template that defines the application's AWS resources without AWS Lambda Snapstart.
- config-snapstart.toml - Deployment configuration file for Snapstart version.
- deploy-all.sh - Builds and deploys standard and snapstart cloudformation stacks.
- cleanup-all.sh - Deletes standard and snapstart cloudformation stacks.
- misc/loadtest.yaml - load test parameters
- run-artillery.sh - Runs Artillery load testing on both standard and snapstart api gateways.
- run-logs-insights.sh - Queries standard and snapstart logs for cold start and restore duration data.


The application uses several AWS resources, including Lambda functions and an API Gateway API. These resources are defined in the `template-standard.yaml` or `template-snapstart.yaml` files in this project. You can update the template to add AWS resources through the same deployment process that updates your application code.

If you prefer to use an integrated development environment (IDE) to build and test your application, you can use the AWS Toolkit.  
The AWS Toolkit is an open source plug-in for popular IDEs that uses the SAM CLI to build and deploy serverless applications on AWS. The AWS Toolkit also adds a simplified step-through debugging experience for Lambda function code. See the following links to get started.

* [CLion](https://docs.aws.amazon.com/toolkit-for-jetbrains/latest/userguide/welcome.html)
* [GoLand](https://docs.aws.amazon.com/toolkit-for-jetbrains/latest/userguide/welcome.html)
* [IntelliJ](https://docs.aws.amazon.com/toolkit-for-jetbrains/latest/userguide/welcome.html)
* [WebStorm](https://docs.aws.amazon.com/toolkit-for-jetbrains/latest/userguide/welcome.html)
* [Rider](https://docs.aws.amazon.com/toolkit-for-jetbrains/latest/userguide/welcome.html)
* [PhpStorm](https://docs.aws.amazon.com/toolkit-for-jetbrains/latest/userguide/welcome.html)
* [PyCharm](https://docs.aws.amazon.com/toolkit-for-jetbrains/latest/userguide/welcome.html)
* [RubyMine](https://docs.aws.amazon.com/toolkit-for-jetbrains/latest/userguide/welcome.html)
* [DataGrip](https://docs.aws.amazon.com/toolkit-for-jetbrains/latest/userguide/welcome.html)
* [VS Code](https://docs.aws.amazon.com/toolkit-for-vscode/latest/userguide/welcome.html)
* [Visual Studio](https://docs.aws.amazon.com/toolkit-for-visual-studio/latest/user-guide/welcome.html)

## Make all provided shell scripts executable (for Linux based environments)

```bash
chmod 755 *.sh
```

## Unit tests

Tests are defined in the `ProductApiFunction/src/test` folder in this project.

```bash
mvn test -f ProductApiFunction/pom.xml
```

## Use the SAM CLI to build, test & deploy

### About SAM CLI

The Serverless Application Model Command Line Interface (SAM CLI) is an extension of the AWS CLI that adds functionality for building and testing Lambda applications. It uses Docker to run your functions in an Amazon Linux environment that matches Lambda. It can also emulate your application's build environment and API.

To use the SAM CLI, you need the following tools.

* SAM CLI - [Install the SAM CLI](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/serverless-sam-cli-install.html)
* Java11 - [Install the Java 11](https://docs.aws.amazon.com/corretto/latest/corretto-11-ug/downloads-list.html)
* Maven - [Install Maven](https://maven.apache.org/install.html)
* Docker - [Install Docker community edition](https://hub.docker.com/search/?type=edition&offering=community)

### Build and deploy both stacks automatically

```bash
./deploy-all.sh REPLACE_WITH_UPLOAD_BUCKET REPLACE_WITH_AWS_REGION
```

### Manual steps to build, test & deploy 
* Build your application with the `sam build` command.

#### Standard stack build

```bash
sam build --template-file template-standard.yaml
```

#### Snapstart stack build

```bash
sam build --template-file template-snapstart.yaml
```

The SAM CLI installs dependencies defined in `ProductApiFunction/pom.xml`, creates a deployment package, and saves it in the `.aws-sam/build` folder.

#### Use the SAM CLI to test locally

* Test a single function by invoking it directly with a test event. An event is a JSON document that represents the input that the function receives from the event source. Test events are included in the `events` folder in this project.

* Run functions locally and invoke them with the `sam local invoke` command.

```bash
sam local invoke ProductApiFunction --event events/event.json
```

* The SAM CLI can also emulate your application's API. Use the `sam local start-api` to run the API locally on port 3000.

```bash
sam local start-api
curl http://localhost:3000/
```

* The SAM CLI reads the application template to determine the API's routes and the functions that they invoke. The `Events` property on each function's definition includes the route and method for each path.

```yaml
          Events:
            GetAll:
              Type: Api
              Properties:
                Path: /products
                Method: GET
            GetOne:
              Type: Api
              Properties:
                Path: /products/{id}
                Method: GET
            Create:
              Type: Api
              Properties:
                Path: /products
                Method: POST
            Delete:
              Type: Api
              Properties:
                Path: /products/{id}
                Method: DELETE
```

#### Manually Deploy the sample application

To build and deploy your application for the first time, run the following in your shell:

##### Standard stack deployment (without Snapstart)
```bash
sam deploy --no-confirm-changeset --config-file config-standard.toml --s3-bucket REPLACE_WITH_BUCKET_NAME --region REPLACE_WITH_AWS_REGION
```

##### Snapstart stack deployment
```bash
sam deploy --no-confirm-changeset --config-file config-snapstart.toml --s3-bucket REPLACE_WITH_BUCKET_NAME --region REPLACE_WITH_AWS_REGION
```

The first command will build the source of your application. The second command will package and deploy your application to AWS. Be mindful to provide the following parameters:

* **S3 Bucket Name**: The name of the S3 bucket where the files will be uploaded
* **AWS Region**: The AWS region you want to deploy your app to.

You can find your API Gateway Endpoint URL in the output values displayed after deployment.

## Add a resource to your application
The application template uses AWS Serverless Application Model (AWS SAM) to define application resources. AWS SAM is an extension of AWS CloudFormation with a simpler syntax for configuring common serverless application resources such as functions, triggers, and APIs. For resources not included in [the SAM specification](https://github.com/awslabs/serverless-application-model/blob/master/versions/2016-10-31.md), you can use standard [AWS CloudFormation](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-template-resource-type-ref.html) resource types.

## Fetch, tail, and filter Lambda function logs

To simplify troubleshooting, SAM CLI has a command called `sam logs`. `sam logs` lets you fetch logs generated by your deployed Lambda function from the command line. In addition to printing the logs on the terminal, this command has several nifty features to help you quickly find the bug.

`NOTE`: This command works for all AWS Lambda functions; not just the ones you deploy using SAM.

### Standard
```bash
sam logs -n ProductApiFunction --stack-name product-api-standard --tail
```

### Snapstart
```bash
sam logs -n ProductApiFunction --stack-name product-api-snapstart --tail
```

You can find more information and examples about filtering Lambda function logs in the [SAM CLI Documentation](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/serverless-sam-cli-logging.html).

## Load testing with Artillery and Faker

Setup your load tests parameters to your convenience (folder misc/):

```yaml
config:
  phases:
    - duration: 60
      arrivalRate: 100
      name: Loadtest with 100 concurrent requests for 60 seconds
  http:
    timeout: 30
  processor: "./randomName.js"
scenarios:
  - flow:
      - function: "generateRandomName"
      - post:
          url: "/Prod/products"
          json:
            name: "{{ randomName }}"
          capture:
            - json: "$.id"
              as: "productId"
      - get:
          url: "/Prod/products/{{ productId }}"
      - get:
          url: "/Prod/products"
      - delete:
          url: "/Prod/products/{{ productId }}"

```

```javascript
const { faker } = require('@faker-js/faker');

module.exports = {
  generateRandomName: function (userContext, events, done) {
    userContext.vars.randomName = faker.commerce.productName();
    return done();
  },
};

```


### Install Artillery
```bash
npm install -g artillery@latest
artillery dino
cd misc
npm init -y && npm install @faker-js/faker
cd ..
```

### Run Artillery load testing on both stacks
```bash
./run-artillery.sh REPLACE_WITH_AWS_REGION
```

#### Standard stack only
* Run Artillery load test
```bash
artillery run -t REPLACE_WITH_STANDARD_API_GW_URL misc/loadtest.yaml
```

#### Snapstart stack only
* Run Artillery load test
```bash
artillery run -t REPLACE_WITH_SNAPSTART_API_GW_URL misc/loadtest.yaml
```

### Observe results

* After both load test are done running, navigate to CloudWatch-Log Insights
* Select the log groups /aws/lambda/product-api-standard & /aws/lambda/product-api-snapstart
* Ensure you have set the proper time-frame to cover the executions that you just executed (e.g. last 15 minutes)
* Copy and execute the following query:

```SQL
  filter @type = "REPORT"
  | parse @log /\d+:\/aws\/lambda\/(?<function>.*)/
  | parse @message /Restore Duration: (?<restoreDuration>.*?) ms/
  | stats
count(*) as invocations,
pct(@duration+coalesce(@initDuration,0)+coalesce(restoreDuration,0), 0) as p0,
pct(@duration+coalesce(@initDuration,0)+coalesce(restoreDuration,0), 25) as p25,
pct(@duration+coalesce(@initDuration,0)+coalesce(restoreDuration,0), 50) as p50,
pct(@duration+coalesce(@initDuration,0)+coalesce(restoreDuration,0), 90) as p90,
pct(@duration+coalesce(@initDuration,0)+coalesce(restoreDuration,0), 95) as p95,
pct(@duration+coalesce(@initDuration,0)+coalesce(restoreDuration,0), 99) as p99,
pct(@duration+coalesce(@initDuration,0)+coalesce(restoreDuration,0), 100) as p100
group by function, (ispresent(@initDuration) or ispresent(restoreDuration)) as coldstart
  | sort by coldstart desc


```
* Observe the results with cold start for both lambda functions (at p90 & p95)
* Evaluate the difference in cold start / restore duration between the 2 lambda functions (compare values at p90 & p95)
* Do you observe any gain? It should be around 40% to 45% faster with Snapstart.


## Cleanup

To delete the sample application that you created, use the AWS CLI. Assuming you used your project name for the stack name, you can run the following:

```bash
./cleanup-all.sh REPLACE_WITH_REGION
```

### Delete Standard stack only
```bash
aws cloudformation delete-stack --stack-name product-api-standard --region REPLACE_WITH_REGION
```

### Delete Snapstart stack only
```bash
aws cloudformation delete-stack --stack-name product-api-snapstart --region REPLACE_WITH_REGION
```

## Resources

See the [AWS SAM developer guide](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/what-is-sam.html) for an introduction to SAM specification, the SAM CLI, and serverless application concepts.

Next, you can use AWS Serverless Application Repository to deploy ready to use Apps that go beyond hello world samples and learn how authors developed their applications: [AWS Serverless Application Repository main page](https://aws.amazon.com/serverless/serverlessrepo/).

Official AWS workshop on [Java on AWS Lambda](https://catalog.workshops.aws/java-on-aws-lambda/en-US).
