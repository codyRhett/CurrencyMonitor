#!/bin/bash
mvn clean install

mvn dependency:copy-dependencies

cd target/

java -cp CurrencyMonitor-1.0-SNAPSHOT.jar:dependency/sqlite-jdbc-3.36.0.2.jar main