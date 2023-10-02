# Wiremock Lambda Handler

This project provides a framework for creating Java based Lambda's using Wiremock as the mocking engine. 

**Motivation**

This library looks to provide an alternative and pluggable way to run wiremock inside a Java based AWS Lambda that other solutions do not provide.  

**Features**

The key features are:

* Lightweight - other solutions use Spring Boot to bridge between AWS Lambdas and Wiremock, Spring is notoriously heavy weight solution and doesn't (in my opinion) play nicely within a Lambda
* Pluggable - using the Java Service Loader it is possible to plugin in key parts of the implementation 


**Building the library**

To install and build the project, follow these steps:

1. Clone the repository: `git clone https://github.com/my-username/my-project.git`
2. Run the gradle wrapper `./gradlew build`

**Contributing**

If you would like to contribute to my project, please follow these guidelines:

* Create a pull request with your changes.
* Include a description of your changes in the pull request message.
* Make sure your changes pass all of the tests.
* Make the pull request against the `develop` branch

**License**

This project is released under the Apache 2 license.

