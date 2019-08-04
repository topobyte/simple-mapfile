#!/bin/bash

set -e

DIR=$(dirname $0)

pushd "$DIR" > /dev/null
./gradlew clean installDist setupScripts
popd

"$DIR"/tools/build/setup/install.sh
"$DIR"/tools/build/setup/post-install.sh
