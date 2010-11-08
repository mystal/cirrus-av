#!/bin/sh

CWD=`pwd`

KEYSTORE=${CWD}/foobar
KEYPASS=foobar

cd build
java -Djavax.net.ssl.keyStore=${KEYSTORE} -Djavax.net.ssl.keyStorePassword=${KEYPASS} ServerFile
