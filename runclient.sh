#!/bin/sh

CWD=`dirname ${0}`

KEYSTORE=${CWD}/foobar
KEYPASS=foobar

exec java -cp ${CWD}/build -Djavax.net.ssl.trustStore=${KEYSTORE} -Djavax.net.ssl.trustStorePassword=${KEYPASS} cirrus.client.Client $@
