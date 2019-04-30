#! /bin/bash

set -eux

export MAVEN_OPTS="-Xms2G -Xmx2G -DskipTests=true -Dmaven.javadoc.skip=true"

mvn -B -V clean install

mkdir -pv artifacts/
mv -v target/*.jar artifacts/
