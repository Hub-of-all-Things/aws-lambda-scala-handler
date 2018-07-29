# AWS Lambda Scala Handler

Wrappers for writing AWS Lambda functions in Scala.

This library provides convenience wrappers on top of the basic I/O stream based interface to write typesafe Lambda functions. It uses Play JSON library for straightforward JSON marshalling and provides predefined interfaces for plain Lambda functions executed raw as well as those called via Lambda Proxy (e.g. behind API Gateway).

Note: async call wrappers are also provided primarily for easily integrating with other libraries providing async interfaces, however note that async on AWS Lambda is mostly meaningless as one instance of the function strictly handles only one request in parallel. Any benefit would therefore only be gained if multiple asynchronous operations were run as part of the same function execution.

## Installation

For installation, include the following in your `build.sbt`:

libraryDependencies += "org.hatdex" %% "aws-lambda-scala-handler" % "0.0.2-SNAPSHOT",
