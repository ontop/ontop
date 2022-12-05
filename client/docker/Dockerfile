# COMMON STAGE setting up the Ontop image, extended by 'image-from-binaries' or 'image-from-sources'
# We use Eclipse Temurin JRE 11 on Ubuntu 20.04 LTS "focal" (default for Temurin JRE 11) supporting
# amd64 and arm64, extended with nc (netcat) support used by wait-for-it.sh and our entrypoint.sh
# scripts (note: Temurin JRE 11 on Alpine supports only amd64 likely due use of non-glibc library).
FROM eclipse-temurin:11-jre-focal AS common
RUN apt-get update && \
    apt-get install -y netcat && \
    rm -rf /var/cache/apt/archives /var/lib/apt/lists
VOLUME /tmp
EXPOSE 8080
WORKDIR /opt/ontop
ENTRYPOINT [ "./entrypoint.sh" ]

# COMPILE STAGE triggered by 'image-from-sources' and using a Docker image of Maven for compiling
# (note that except for generated binaries, content here will not end up in the built Ontop image)
FROM --platform=${BUILDPLATFORM} maven:3-eclipse-temurin-11-focal AS compile
WORKDIR /build
COPY . ./
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -Pcli -Dassembly.cli.format=dir && \
    mv build/distribution/target/ontop-cli-* ontop && \
    cp client/docker/entrypoint.sh ontop && \
    cd ontop && \
    rm -r ontop.bat ontop ontop-completion.sh jdbc

# ALTERNATIVE FINAL STAGE that uses pre-built binaries from ${ONTOP_CLI_BINDIR}, which defaults to
# 'build/distribution/target/ontop' and can be set by passing --build-arg ONTOP_CLI_BINDIR=<path>
# (if not using the default, be sure to use a path that is not ignored by Dockerfile.dockerignore!)
# This stage is used by build-image.sh and is more convenient for developers, as it integrates with
# regular Maven builds by reusing the Maven cache as well as previously generated binary artifacts.
FROM common AS image-from-binaries
ARG ONTOP_CLI_BINDIR=build/distribution/target/ontop
COPY ${ONTOP_CLI_BINDIR} /opt/ontop/

# DEFAULT FINAL STAGE based on compiling Ontop within Docker (so only sw requirement is Docker)
FROM common AS image-from-sources
COPY --from=compile /build/ontop /opt/ontop/
