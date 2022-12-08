#!/bin/bash

./gradlew --parallel e2eTestSpringExampleJar e2eTestSpringExampleJarCustomE2e :test :spotbugsMain e2e-api:test \
  e2e-api:spotbugsMain
exit $?
