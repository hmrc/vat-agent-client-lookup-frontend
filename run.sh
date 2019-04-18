#!/usr/bin/env bash
sbt 'run 9149 -Dlogger.resource=logback-test.xml -Dapplication.router=testOnly.Routes'