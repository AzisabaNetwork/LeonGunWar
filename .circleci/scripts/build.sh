#! /bin/bash

set -eux

export MAVEN_OPTS="-Xms2G -Xmx2G -DskipTests=true -Dmaven.javadoc.skip=true"

mkdir -pv artifacts/
mvn -B -V clean install
mv -v target/*.jar artifacts/
