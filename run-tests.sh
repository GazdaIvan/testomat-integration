#!/bin/bash
mvn clean test
npx report-xml "target/surefire-reports/*.xml"
