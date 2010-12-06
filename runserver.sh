#!/bin/sh

CWD=`pwd`/`dirname ${0}`

KEYSTORE=${CWD}/foobar
KEYPASS=foobar

cd build
exec java -Djavax.net.ssl.keyStore=${KEYSTORE} -Djavax.net.ssl.keyStorePassword=${KEYPASS} cirrus.server.Server $@
