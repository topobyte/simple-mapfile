#!/bin/bash

set -e

DIR=$(dirname $0)

pushd "$DIR" > /dev/null
./gradlew clean installDist postInstallScript
popd

TARGET="$HOME/share/topobyte/simple-mapfile/simple-mapfile-snapshot"

mkdir -p "$TARGET"
rsync -av --delete "$DIR/tools/build/install/simple-mapfile/" "$TARGET"

"$DIR"/tools/build/setup/post-install.sh
