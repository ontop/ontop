#
# ONTOP DOCKERFILE
#
# Multi-platform (linux, amd64/arm64), multi-stage dockerfile based on Eclipse Temurin JDK 11
# - use with 'docker buildx build client/docker/Dockerfile .' (from root folder)
# - use with script 'build-image.sh' (call './build-image.sh --help' for instructions)
#

# JRE BUILD STAGE using 'jlink' to build a compact JRE with the only modules required by Ontop
# - based on Eclipse Temurin JRE 11 (on Ubuntu 22.04 LTS 'jammy', for both amd64 and arm64)
# - run './build-image.sh -d' to detect required Java modules to be used with '--add-modules'
#   then manually add the following modules (for JMX and SSL/TLS to work properly)
#   jdk.management.agent,jdk.crypto.cryptoki,jdk.crypto.ec
# - pass --build-arg ONTOP_JRE_MODULES=<comma-separated-list> to override the set of modules,
#   or use "ALL-MODULE-PATH" as value to include *all* JRE modules (34M image size increase)
FROM eclipse-temurin:11-jammy AS jre-builder
ARG ONTOP_JRE_MODULES=\
"java.base,java.compiler,java.desktop,java.instrument,java.management.rmi,"\
"java.naming,java.prefs,java.scripting,java.security.jgss,java.sql,"\
"jdk.httpserver,jdk.jfr,jdk.management,jdk.management.agent,jdk.unsupported,"\
"jdk.crypto.cryptoki,jdk.crypto.ec"
RUN ${JAVA_HOME}/bin/jlink \
    --add-modules "${ONTOP_JRE_MODULES}" \
    --strip-debug \
    --no-man-pages \
    --no-header-files \
    --compress=2 \
    --verbose \
    --output /jre

# ONTOP BASE IMAGE STAGE setting up JRE, dependencies, user, ports, volume, labels & healthcheck
# - Ontop binaries added by final stage 'ontop-image-from-binaries' or 'ontop-image-from-sources'
# - Ubuntu 22.04 LTS 'jammy' as base image for both amd64 and arm64, extended with netcat (nc) for
#   'entrypoint.sh', and jattach (small tool to facilitate thread/heap dumps for diagnostics)
# - may switch to Alpine Linux once supported: https://github.com/adoptium/containers/issues/158
FROM ubuntu:jammy AS ontop-base-image
ENV JAVA_HOME="/opt/java/openjdk"
ENV PATH="${JAVA_HOME}/bin:/opt/ontop:${PATH}"
COPY --from=jre-builder /jre ${JAVA_HOME}
WORKDIR /opt/ontop
RUN apt-get update && \
    apt-get install -y --no-install-recommends wget netcat jattach && \
    rm -rf /var/cache/apt/archives /var/lib/apt/lists /var/log/dpkg.log /var/log/lastlog /var/log/faillog && \
    groupadd -r ontop && \
    useradd -r -g ontop -d /opt/ontop -s /sbin/nologin -c "Ontop unprivileged user" ontop
LABEL \
    org.opencontainers.image.url="https://github.com/ontop/ontop/tree/version5/client/docker" \
    org.opencontainers.image.documentation="https://ontop-vkg.org/guide/cli.html" \
    org.opencontainers.image.source="https://github.com/ontop/ontop/" \
    org.opencontainers.image.title="Ontop" \
    org.opencontainers.image.description="Ontop is a platform to query relational databases as Virtual RDF Knowledge Graphs using SPARQL" \
    org.opencontainers.image.vendor="Ontop developers: https://github.com/ontop" \
    org.opencontainers.image.authors="Email: https://groups.google.com/g/ontop4obda, Issues: https://github.com/ontop/ontop/issues" \
    org.opencontainers.image.licenses="Apache-2.0"
USER ontop
VOLUME /tmp
EXPOSE 8080 8686
ENTRYPOINT [ "./entrypoint.sh" ]
HEALTHCHECK --interval=5s --timeout=5s --start-period=60s --retries=3 CMD [ "/bin/sh", "healthcheck.sh" ]

# ONTOP BUILD STAGE triggered by 'ontop-image-from-sources' to compile with a Maven Docker image
# - except for generated binaries, content here will not end up in the built Ontop image
# - maven:3-eclipse-temurin-11 is based on the same Ubuntu 22.04 "jammy" used as base image
# - this stage runs only on the current platform as Java compiling is platform-independent
FROM --platform=${BUILDPLATFORM} maven:3-eclipse-temurin-11 AS ontop-builder
WORKDIR /build
COPY . ./
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -Passet-cli -Dmaven.test.skip -Dassembly.cli.format=dir && \
    mv build/distribution/target/ontop-cli-*/ ontop-docker && \
    cp client/docker/entrypoint.sh client/docker/healthcheck.sh ontop-docker && \
    cd ontop-docker && \
    rm -rf ontop.bat ontop-completion.sh jdbc

# ALTERNATIVE FINAL STAGE that uses pre-built binaries from ${ONTOP_CLI_BINDIR}
# - stage used by build-image.sh and more convenient for developers, as it integrates with regular
#   Maven builds by reusing the Maven cache as well as previously generated binary artifacts.
# - ${ONTOP_CLI_BINDIR} defaults to build/distribution/target/ontop-docker (as in build-image.sh)
#   and can be set by calling 'docker buildx build' with --build-arg ONTOP_CLI_BINDIR=<path>
#   (in that case, be sure to use a path that is not ignored by Dockerfile.dockerignore)
FROM ontop-base-image AS ontop-image-from-binaries
ARG ONTOP_CLI_BINDIR="build/distribution/target/ontop-docker"
COPY ${ONTOP_CLI_BINDIR} /opt/ontop/

# DEFAULT FINAL STAGE based on compiling Ontop within Docker (so only sw requirement is Docker)
FROM ontop-base-image AS ontop-image-from-sources
COPY --from=ontop-builder /build/ontop-docker /opt/ontop/
