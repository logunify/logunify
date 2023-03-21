FROM eclipse-temurin:11-jdk-jammy AS base

RUN export DEBIAN_FRONTEND=noninteractive DEBCONF_NONINTERACTIVE_SEEN=true && apt-get -q update && \
    apt-get install -y protobuf-compiler \
        binutils \
        git \
        unzip \
        gnupg2 \
        libc6-dev \
        libcurl4-openssl-dev \
        libedit2 \
        libgcc-9-dev \
        libpython3.8 \
        libsqlite3-0 \
        libstdc++-9-dev \
        libxml2-dev \
        libz3-dev \
        pkg-config \
        tzdata \
        curl \
        zlib1g-dev \
        && rm -r /var/lib/apt/lists/*

ARG SWIFT_PLATFORM=ubuntu22.04
ARG SWIFT_BRANCH=swift-5.7.3-release
ARG SWIFT_VERSION=swift-5.7.3-RELEASE
ARG SWIFT_WEBROOT=https://download.swift.org
ARG SWIFT_PLUGIN_VERSION=1.21.0

ARG TS_PROTOBUF_VERSION=0.4.3
ARG NODE_VERSION=18

ENV SWIFT_PLATFORM=$SWIFT_PLATFORM \
    SWIFT_BRANCH=$SWIFT_BRANCH \
    SWIFT_VERSION=$SWIFT_VERSION \
    SWIFT_WEBROOT=$SWIFT_WEBROOT \
    SWIFT_PLUGIN_VERSION=$SWIFT_PLUGIN_VERSION \
    TS_PROTOBUF_VERSION=$TS_PROTOBUF_VERSION \
    NODE_VERSION=$NODE_VERSION

# Setup Swift
RUN set -e; \
    ARCH_NAME="$(dpkg --print-architecture)"; \
    url=; \
    case "${ARCH_NAME##*-}" in \
        'amd64') \
            OS_ARCH_SUFFIX=''; \
            ;; \
        'arm64') \
            OS_ARCH_SUFFIX='-aarch64'; \
            ;; \
        *) echo >&2 "error: unsupported architecture: '$ARCH_NAME'"; exit 1 ;; \
    esac; \
    SWIFT_WEBDIR="$SWIFT_WEBROOT/$SWIFT_BRANCH/$(echo $SWIFT_PLATFORM | tr -d .)$OS_ARCH_SUFFIX" \
    && SWIFT_BIN_URL="$SWIFT_WEBDIR/$SWIFT_VERSION/$SWIFT_VERSION-$SWIFT_PLATFORM$OS_ARCH_SUFFIX.tar.gz" \
    && SWIFT_SIG_URL="$SWIFT_BIN_URL.sig" \
    # - Grab curl here so we cache better up above \
    && export DEBIAN_FRONTEND=noninteractive \
    && curl -fSL "$SWIFT_BIN_URL" -o swift.tar.gz \
    # - Unpack the toolchain, set libs permissions, and clean up.
    && tar -xzf swift.tar.gz --directory / --strip-components=1 \
    && chmod -R o+r /usr/lib/swift \
    && rm -rf swift.tar.gz

# Setup Swift Plugin
RUN  set -e; \
     ARCH_NAME="$(dpkg --print-architecture)"; \
     case "${ARCH_NAME##*-}" in \
         'amd64') \
             BUILD_PREFIX='x86_64'; \
             ;; \
         'arm64') \
             BUILD_PREFIX='aarch64'; \
             ;; \
         *) echo >&2 "error: unsupported architecture: '$ARCH_NAME'"; exit 1 ;; \
     esac; \
    curl -fSL https://github.com/logunify/swift-protobuf/archive/refs/tags/$SWIFT_PLUGIN_VERSION.tar.gz -o swift-protobuf.tar.gz \
    && tar -xzf swift-protobuf.tar.gz \
    && cd swift-protobuf-$SWIFT_PLUGIN_VERSION \
    && swift build -c release \
    && cp .build/$BUILD_PREFIX-unknown-linux-gnu/release/protoc-gen-swift /usr/bin/ \
    && rm -rf swift-protobuf.tar.gz swift-protobuf-$SWIFT_PLUGIN_VERSION

#Setup Node Plugin
RUN set -e; \
    curl -sL https://deb.nodesource.com/setup_$NODE_VERSION.x | bash - \
    && apt-get install -y nodejs \
    &&  npm install -g @logunify/protoc-gen-ts@$TS_PROTOBUF_VERSION

## Config user
RUN groupadd -r app && useradd -r -g app app
RUN mkdir /app && chown app:app /app
RUN usermod -d /app app

FROM base AS logunify
# Setup the service
COPY web_service/build/libs/web_service-1.0.0-SNAPSHOT.jar /app/web_service.jar
COPY config/config.properties /app/config.properties
COPY codegen/build/install/codegen /app/codegen

ENTRYPOINT java -jar /app/web_service.jar \
                --package-builder.protoc-plugin-path=/app/codegen/bin/protoc_plugin \
                --package-builder.gradle-path=gradle \
                --package-builder.swift-plugin-path= \
                --package-builder.typescript-plugin-path= \
                --spring.config.location=/app/config.properties,classpath:/applications.properties \
                --schema.location=/app/schemas

FROM base AS codegen
COPY codegen/build/install/codegen /app/codegen

ENTRYPOINT /app/codegen/bin/codegen "$@"