#!/usr/bin/env bash

#
# Script to build (and possibly push) the Ontop Docker image from sources, integrating with Maven build
# (Maven cache and pre-built artifacts are reused) and supporting generating a multiplaform image
# (linux/amd64 + linux/arm64).
#
# Loosely inspired by https://github.com/apache/spark/blob/master/bin/docker-image-tool.sh
#

# Halt at first error (i.e., non 0 command return code)
set -e

# Move to root folder of Ontop source tree
cd "$( dirname "${BASH_SOURCE[0]}" )"/../..

# Extract the Ontop version from pom.xml (to be used in Docker image tag, if not overridden)
VERSION=$( grep '<artifactId>ontop</artifactId>' pom.xml -C3 | grep -oP '(?<=<version>)[^>]+(?=</version>)' )

# Build variables customizable via command line options
TARGET="image-from-binaries"
CLEANARG=
NOCACHEARG=
QUIETARG=
NAME="ontop/ontop-endpoint"
TAG="${VERSION}"
CROSS_BUILD="false"
PUSH="false"

# Build variables non-customizable (edit in this script, and ensure BINDIR is relative to Ontop root)
BINDIR="build/distribution/target/ontop"
BUILDARGS="--build-arg ONTOP_CLI_BINDIR=${BINDIR}"
PLATFORMS="linux/amd64,linux/arm64"

# Handle -h or --help command line options
if [[ "$@" = *--help ]] || [[ "$@" = *-h ]]; then cat <<EOF
Usage: $0 [options]

Builds and/or pushes the Ontop Docker image, using Docker buildx.
(see https://docs.docker.com/buildx/working-with-buildx/ for buildx setup).

Options:
    -c            compile from scratch outside Docker ('mvn clean package -Pcli')
    -C            compile from scratch inside Docker (as -c, but use dockerized Maven)
                  this option does not require a JDK and provide maximum reproducibility
                  at the expenses of longer build time (mostly to download dependencies)
    -n            build Docker image with --no-cache
    -t name:tag   tagged image name ('name:tag' format) to apply to the generated image
                  (default: ${NAME}:${TAG})
    -T tag        image tag (the part after ':') to apply to the generated image
                  (default: ${TAG})
    -N name       image name (the part before ':') to apply to to the generated image
                  (default: ${NAME})
    -p            also pushes the created Docker image
    -x            cross build for linux/amd64 and linux/arm64; automatically pushes
                  (if not used, only the image for the local OS/arch platform is built)
    -b key=value  supply additional --build-arg to Docker (can be used multiple times)
    -q            suppress output
    -h, --help    display command line usage

Unless option -C is used, code is compiled outside Docker using a local JDK and Maven,
with substantial speedups due to reuse of local Maven cache and already built artifacts.

Examples:
    ./build-image.sh                     Build local platform image using local JDK/Maven
    ./build-image.sh -C                  Build local platform image entirely within Docker,
                                         i.e. not requiring a JDK being installed
    ./build-image.sh -N myrepo/ontop -x  Build multi-platform image pushing it to a custom
                                         repository (e.g., for pre-release testing)
    ./build-image.sh -C -n -x            Build multi-platform image entirely within Docker,
                                         without cache for further reproducibility, pushing
                                         the image to the official Ontop Docker repository

EOF
exit 0
fi

# Parse script options, updating corresponding build variables
while getopts cCnt:T:N:pxb:q option
do
    case "${option}" in
        c) CLEANARG="clean";;
        C) TARGET="image-from-sources";;
        n) NOCACHEARG="--no-cache";;
        t) NAMETAG=${OPTARG};;
        T) TAG=${OPTARG};;
        N) NAME=${OPTARG};;
        p) PUSH=1;;
        x) CROSS_BUILD=1; PUSH=1;;
        b) BUILDARGS=${BUILDARGS}" --build-arg "${OPTARG};;
        q) QUIETARG="-q";;
    esac
done

# Assemble image name:tag from its components (-T, -N options), if not explicitly set (-t option)
NAMETAG="${NAMETAG:-${NAME}:${TAG}}"

# Helper function to log timestamped message (output suppressed if option -q is supplied)
function log {
    [ -z "$QUIETARG" ] && echo -e "[$(date "+%Y-%m-%d %H:%M:%S")] $1" || true
}

# Compile outside Docker (if -C not used), integrating with local Maven build workflow
if [ "${TARGET}" = "image-from-binaries" ]; then
    # Compile via Maven ('clean' triggered by -c option; ontop-cli assembly not zipped)
    log "Compiling Ontop ${VERSION}"
    ./mvnw ${QUIETARG} ${CLEANARG} package -Pcli -Dassembly.cli.format=dir

    # Rearrange generated ontop-cli files, dropping unused files and adding entrypoint.sh script
    log "Assembling content of image ${NAMETAG}"
    rm -rf ${BINDIR}
    mv build/distribution/target/ontop-cli-* ${BINDIR}
    rm -r ${BINDIR}/{ontop.bat,ontop,ontop-completion.sh,jdbc}
    cp client/docker/entrypoint.sh ${BINDIR}
fi

# Build via Docker 'buildx', differentiating "simple" (local platform only) vs "cross" (linux/amd64 + linux/arm64) build
if [ "${CROSS_BUILD}" != "false" ]; then
    # When cross-building, the generated multi-platform image cannot be stored locally but need to be pushed to a Docker repository
    log "Building & pushing multi-platform image ${NAMETAG}"
    docker buildx build -f client/docker/Dockerfile --target ${TARGET} -t "${NAMETAG}" ${NOCACHEARG} ${QUIETARG} ${BUILDARGS} --push --platform "${PLATFORMS}" .
else
    # When not cross-building, pushing the image is optional and is triggered by supplying option '-p'
    log "Building image ${NAMETAG}"
    docker buildx build -f client/docker/Dockerfile --target ${TARGET} -t "${NAMETAG}" ${NOCACHEARG} ${QUIETARG} ${BUILDARGS} --load .
    if [ "${PUSH}" != "false" ]; then
        log "Pushing image ${NAMETAG}"
        docker push ${QUIETARG} "${NAMETAG}"
    fi
fi

# Log completion time and message
log "Done"
