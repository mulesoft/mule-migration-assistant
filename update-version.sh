#!/bin/bash

set -e

current=$(cat pom.xml | grep '<version>.*' | head -1 | sed 's/.*<version>\([^<]*\).*/\1/')

if [[ -z $current ]]
  then
    echo "Current version not found!"
    exit 1
fi

read -p "Enter version to update to (current is ${current}): " -r
echo
if [[ $REPLY =~ ^[1-9]+\.[0-9]+\.[0-9]+(-SNAPSHOT)?$ ]]
then
    mvn versions:set -DnewVersion=${REPLY}
    pushd target-modules
    mvn versions:set -DnewVersion=${REPLY}
    popd target-modules
else
    echo "Invalid version: $REPLY"
    exit 2
fi
