#!/usr/bin/env bash

source ./export-default-env-vars.sh

export JAVA_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,address=8002,server=y,suspend=n"
export PORT="${PORT:-8082}"

# Restolino configuration
export RESTOLINO_CLASSES="zebedee-reader/target/classes"
export PACKAGE_PREFIX=com.github.onsdigital.zebedee.reader.api
export CONTENT_DIR="content"
export FORMAT_LOGGING=true

# CMD config (dev local values)
export ENABLE_DATASET_IMPORT=true
export DATASET_API_URL="http://localhost:22000"
export DATASET_API_AUTH_TOKEN="FD0108EA-825D-411C-9B1D-41EF7727F465"
export SERVICE_AUTH_TOKEN="fc4089e2e12937861377629b0cd96cf79298a4c5d329a2ebb96664c88df77b67"

export FORMAT_LOGGING=true

export FORMAT_LOGGING=true

# CMD config (dev local values)
export ENABLE_DATASET_IMPORT=true
export DATASET_API_URL="http://localhost:22000"
export DATASET_API_AUTH_TOKEN="FD0108EA-825D-411C-9B1D-41EF7727F465"
export SERVICE_AUTH_TOKEN="fc4089e2e12937861377629b0cd96cf79298a4c5d329a2ebb96664c88df77b67"

# Development: reloadable
mvn clean package dependency:copy-dependencies -Dmaven.test.skip=true && \
java $JAVA_OPTS \
 -DFORMAT_LOGGING=$FORMAT_LOGGING \
 -Dlogback.configurationFile=zebedee-reader/target/classes/logback.xml \
 -Drestolino.classes=$RESTOLINO_CLASSES \
 -Dcontent_dir=$CONTENT_DIR \
 -DSTART_EMBEDDED_SERVER=N \
 -DFORMAT_LOGGING=$FORMAT_LOGGING \
 -Drestolino.packageprefix=$PACKAGE_PREFIX \
 -DFORMAT_LOGGING=$FORMAT_LOGGING \
 -cp "zebedee-reader/target/classes/:zebedee-reader/target/dependency/*" \
 com.github.davidcarboni.restolino.Main

