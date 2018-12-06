# VAT Agent Client Lookup Frontend

[![Apache-2.0 license](http://img.shields.io/badge/license-Apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
[![Build Status](https://travis-ci.org/hmrc/vat-agent-client-lookup-frontend.svg)](https://travis-ci.org/hmrc/vat-agent-client-lookup-frontend)
[![Download](https://api.bintray.com/packages/hmrc/releases/vat-agent-client-lookup-frontend/images/download.svg)](https://bintray.com/hmrc/releases/vat-agent-client-lookup-frontend/_latestVersion)

## Summary

This is the repository for VAT Agent Client Lookup Frontend.

This service provides the functionality to verify that an agent can act on behalf of a given client to manage their VAT account details.

## Requirements

This service is written in [Scala](http://www.scala-lang.org/) and [Play](http://playframework.com/), so needs at least a [JRE](https://www.java.com/en/download/) to run.

## Running the application

In order to run this microservice, you must have SBT installed. You should then be able to start the application using:

`sbt "run 9149"`

## Testing

Use the following command to run unit and integration tests and to get a coverage report:

`sbt clean coverage test it:test coverageReport`

## License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")

