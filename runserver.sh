#!/bin/sh

CWD=`dirname ${0}`

KEYSTORE=${CWD}/foobar
KEYPASS=foobar

exec java -cp ${CWD}/build -Djavax.net.ssl.keyStore=${KEYSTORE} -Djavax.net.ssl.keyStorePassword=${KEYPASS} cirrus.server.Server $@
