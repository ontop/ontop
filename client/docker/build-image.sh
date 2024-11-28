#!/usr/bin/env bash

#
# ONTOP DOCKER IMAGE BUILD SCRIPT
#
# Builds (and possibly pushes) the Ontop Docker image from sources, integrating with Maven build
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
VERSION=$( grep '<artifactId>ontop</artifactId>' pom.xml -C3 | grep '<version>' | sed -E 's/[^>]*>([^<]*)(\s*|<.*)$/\1/' )

# Extract the Git revision, in case this script is running within a git working copy
# This allows browsing the exact sources that contributed to the image:
# https://github.com/ontop/ontop/tree/${REVISION}
REVISION="$( git rev-parse HEAD 2> /dev/null )"

# Build variables customizable via command line options
JDEPS=
TARGET="ontop-image-from-binaries"
CLEANARG=
NOCACHEARG=
QUIETARG=
NAME="ontop/ontop"
TAG="${VERSION}"
CROSS_BUILD="false"
PUSH="false"

# Build variables non-customizable (edit in this script, and ensure BINDIR is relative to Ontop root)
BINDIR="build/distribution/target/ontop-docker"
PLATFORMS="linux/amd64,linux/arm64"

# Initialize TAGARGS, BUILDARGS and LABELARGS (revision-specific labels added to the latter, if info is available)
TAGARGS=
BUILDARGS="--build-arg ONTOP_CLI_BINDIR=${BINDIR}"
LABELARGS="--label org.opencontainers.image.version=${VERSION} --label org.opencontainers.image.created=$( date -u +'%Y-%m-%dT%H:%M:%SZ' )"
if [ "${REVISION:+x}" ]; then
    LABELARGS=${LABELARGS}" --label org.opencontainers.image.revision=${REVISION}"
    LABELARGS=${LABELARGS}" --label org.opencontainers.image.source=https://github.com/ontop/ontop/tree/${REVISION}"
fi

# Handle -h or --help command line options
if [[ "$*" = *--help ]] || [[ "$*" = *-h ]]; then cat <<EOF
Usage: $0 [options]

Builds and/or pushes the Ontop Docker image, using Docker buildx.
(see https://docs.docker.com/buildx/working-with-buildx/ for buildx setup).

Options:
    -c            compile from scratch outside Docker ('mvn clean package -Passet-cli')
    -C            compile from scratch inside Docker (as -c, but use dockerized Maven)
                  this option does not require a JDK and provide maximum reproducibility
                  at the expenses of longer build time (mostly to download dependencies)
    -n            build Docker image with --no-cache
    -t name:tag   tagged image name to apply to the image (can be used multiple times),
                  using 'name:tag' format with optional ':tag' defaulting to ${TAG}
                  (default: -t ${NAME}:${TAG} -t ${NAME}:latest)
    -p            also pushes the created Docker image
    -x            cross build for linux/amd64 and linux/arm64; automatically pushes
                  (if not used, only the image for the local OS/arch platform is built)
    -b key=value  supply additional --build-arg to Docker (can be used multiple times)
    -l key=value  supply additional --label to Docker (can be used multiple times)
    -d            compile Ontop and run jdep to list required Java modules for jlink
                  (does not build and/or push a Docker image)
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
while getopts cCnt:pxb:l:dq option
do
    # shellcheck disable=SC2220
    case "${option}" in
        c) CLEANARG="clean";;
        C) TARGET="ontop-image-from-sources";;
        n) NOCACHEARG="--no-cache";;
        t) [[ "${OPTARG}" =~ ^[^:]+[:][^:]+$ ]] || OPTARG=${OPTARG}:${TAG}
           TAGARGS="${TAGARGS} -t ${OPTARG}";;
        p) PUSH=1;;
        x) CROSS_BUILD=1; PUSH=1;;
        b) BUILDARGS="${BUILDARGS} --build-arg ${OPTARG}";;
        l) LABELARGS="${LABELARGS} --label ${OPTARG}";;
        d) JDEPS=1;;
        q) QUIETARG="-q";;
    esac
done

# Apply default tags (if option '-t' never supplied) and extract space-separated list of tags
if [ -z "${TAGARGS}" ]; then
    TAGARGS="-t ${NAME}:${TAG} -t ${NAME}:latest"
fi
NAMETAGS=$(echo "${TAGARGS}" | sed -E 's/ -t / /g' | sed -E 's/^ //')

# Helper function to log timestamped message (output suppressed if option -q is supplied)
function log {
    if [ -z "${QUIETARG}" ]; then echo -e "[$(date "+%Y-%m-%d %H:%M:%S")] $1"; fi
}

# Compile outside Docker (if -C not used or -d used), integrating with local Maven build workflow
if [ "${TARGET}" = "ontop-image-from-binaries" ] || [ "${JDEPS}" ]; then
    # Compile via Maven ('clean' triggered by -c option; ontop-cli assembly not zipped)
    log "Compiling Ontop ${VERSION}"
    rm -rf build/distribution/target/ontop-cli-*/
    ./mvnw ${QUIETARG} ${CLEANARG} package -Passet-cli -Dmaven.test.skip -Dassembly.cli.format=dir

    # Rearrange generated ontop-cli files, dropping unused files and adding entrypoint.sh script
    log "Assembling content of image ${NAMETAGS}"
    rm -rf ${BINDIR}
    mv build/distribution/target/ontop-cli-*/ ${BINDIR}
    rm -rf ${BINDIR}/{ontop.bat,ontop-completion.sh,jdbc}
    cp client/docker/{entrypoint.sh,healthcheck.sh} ${BINDIR}
fi

# Run 'jdeps' on generated binaries, to list required Java modules for 'jlink --add-modules'
if [ "${JDEPS}" ]; then
    log "Detecting required Java modules via jdep (for jlink --add-modules)"
    jdeps \
        --print-module-deps \
        --ignore-missing-deps \
        --recursive \
        --multi-release 11 \
        --class-path="${BINDIR}/lib/*" \
        --module-path="${BINDIR}/lib/*" \
        ${BINDIR}/lib/ontop-cli-5.0.0-SNAPSHOT.jar
    exit 0
fi

# Build via Docker 'buildx', differentiating "simple" (local platform only) vs "cross" (linux/amd64 + linux/arm64) build
if [ "${CROSS_BUILD}" != "false" ]; then
    # When cross-building, the generated multi-platform image cannot be stored locally but need to be pushed to a Docker repository
    log "Building & pushing multi-platform image ${NAMETAGS}"
    # shellcheck disable=SC2086
    docker buildx build -f client/docker/Dockerfile --target ${TARGET} ${TAGARGS} ${NOCACHEARG} ${QUIETARG} ${BUILDARGS} ${LABELARGS} --push --platform "${PLATFORMS}" .
else
    # When not cross-building, pushing the image is optional and is triggered by supplying option '-p'
    log "Building image ${NAMETAGS}"
    # shellcheck disable=SC2086
    docker buildx build -f client/docker/Dockerfile --target ${TARGET} ${TAGARGS} ${NOCACHEARG} ${QUIETARG} ${BUILDARGS} ${LABELARGS} --load .
    if [ "${PUSH}" != "false" ]; then
        for NAMETAG in ${NAMETAGS}; do # need to iterate as --all-tags risks pushing also other local images with same name
            log "Pushing image ${NAMETAG}"
            docker push ${QUIETARG} "${NAMETAG}"
        done
    fi
fi

# Log completion time and message
log "Done"
