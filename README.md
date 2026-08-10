> [!IMPORTANT]
> **PROJECT ARCHIVED**: This repository is archived and no longer actively maintained. The code remains available for historical reference, viewing, and forking.
>
> **Recommended Alternatives:**
> * **[WireMock Cloud](https://www.wiremock.io/):** Official hosted/SaaS WireMock platform requiring no infrastructure maintenance.
> * **WireMock Docker in AWS Lambda / ECS / App Runner:** Deploy the official `wiremock/wiremock` OCI container image directly to AWS Lambda container support or container services like AWS App Runner / ECS Fargate.
> * **AWS API Gateway Mock Integration:** Built-in API Gateway mock endpoints for lightweight static stubs.

# WireMock AWS Lambda Handler (`lambda-wiremock`)

This project provides a lightweight framework for running [WireMock](https://wiremock.org/) as an in-memory mocking engine inside Java-based AWS Lambda functions.

## Overview & Motivation

Running WireMock inside an AWS Lambda traditionally required spinning up an HTTP web server (such as Jetty or Spring Boot), which adds significant cold-start latency and overhead. 

`lambda-wiremock` bridges AWS API Gateway events directly to WireMock using WireMock's in-memory `DirectCallHttpServer`. This avoids HTTP server startup costs and delivers fast, lightweight mock responses directly within the Lambda execution environment.

## Key Features

* **Lightweight & Fast**: Uses `DirectCallHttpServer` for direct in-memory request routing, bypassing Jetty/Spring Boot startup overhead.
* **AWS Event Support**: Adapts both AWS API Gateway V2 HTTP events (`APIGatewayV2HTTPEvent`) and API Gateway V1 REST Proxy events (`APIGatewayProxyRequestEvent`).
* **Classpath Stub Loading**: Automatically loads WireMock mapping definitions from `wiremock/mappings/*.json` on the classpath.
* **Pluggable Architecture**: Uses Java `ServiceLoader` to allow customization of server builders (`WiremockServerBuilder`) and adaptors (`RequestAdaptor`, `ResponseAdaptor`).
* **Powertools Logging Integration**: Integrates with AWS Lambda Powertools for structured logging and correlation tracking.

## Building and Testing

**Prerequisites:** Java 17+ and Gradle.

1. Clone the repository:
   ```bash
   git clone https://github.com/boxheed/lambda-wiremock.git
   cd lambda-wiremock
   ```

2. Build and run tests using the Gradle wrapper:
   ```bash
   ./gradlew build
   ```

## Usage & Examples

Check the [`examples/`](./examples) directory for complete sample projects:
* **[`examples/basic`](./examples/basic)**: Demonstrates integrating `lambda-wiremock-lib` in a Gradle project packaged with ShadowJar for deployment to AWS Lambda.
* **[`examples/executable`](./examples/executable)**: Demonstrates running standalone stubs.

Handler class configuration for AWS Lambda:
`com.fizzpod.wiremock.APIGatewayV2HTTPLambdaHandler`

## License

This project is licensed under the [Apache License 2.0](LICENSE).


