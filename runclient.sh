#!/bin/sh

CWD=`pwd`/`dirname ${0}`

KEYSTORE=${CWD}/foobar
KEYPASS=foobar

cd build
exec java -Djavax.net.ssl.trustStore=${KEYSTORE} -Djavax.net.ssl.trustStorePassword=${KEYPASS} cirrus.client.Client $@
