#!/bin/sh

CWD=`pwd`

KEYSTORE=${CWD}/foobar
KEYPASS=foobar

cd build
java -Djavax.net.ssl.trustStore=${KEYSTORE} -Djavax.net.ssl.trustStorePassword=${KEYPASS} ClientFile $@
