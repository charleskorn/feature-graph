#!/usr/bin/env bash

set -e

SOURCE_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

function main {
  case "$1" in

  build)
    build
    ;;

  test)
    test
    ;;

  run)
    run "${@:2}"
    ;;

  *)
    help
    exit 1
    ;;

  esac
}

function help {
  echo "Usage:"
  echo " build        builds the application"
  echo " test         runs the test suite"
  echo " run <args>   builds and runs the application (<args> are passed through to the application)"
}

function build {
  runInDockerContainer sbt stage
}

function test {
  runInDockerContainer sbt test
}

function run {
  echo "Building..."
  build

  echo "Running..."
  runInDockerContainer ./target/universal/stage/bin/feature-graph "$@"
}

function runInDockerContainer {
  IVY2_CACHE_DIR=$SOURCE_DIR/build/cache/ivy2
  SBT_CACHE_DIR=$SOURCE_DIR/build/cache/sbt

  mkdir -p $IVY2_CACHE_DIR
  mkdir -p $SBT_CACHE_DIR

  docker run \
    -it --rm \
    -v $SOURCE_DIR:/work \
    -v $IVY2_CACHE_DIR:/root/.ivy2/cache \
    -v $IVY2_CACHE_DIR:/root/.sbt/boot \
    -w /work \
    1science/sbt:0.13.8-oracle-jre-8 \
    "$@"
}

main "$@"
