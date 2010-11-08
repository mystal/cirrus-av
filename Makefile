client:
	javac ClientFile.java
	
server:
	javac ServerFile.java

runClient:
	java -Djavax.net.ssl.trustStore=foobar -Djavax.net.ssl.trustStorePassword=foobar ClientFile

runClientDebug:
	java -Djavax.net.ssl.trustStore=foobar -Djavax.net.ssl.trustStorePassword=foobar -Djava.protocol.handler.pkgs=com.sun.net.ssl.internal.www.protocol -Djavax.net.debug=ssl ClientFile

runServer:
	java -Djavax.net.ssl.keyStore=foobar -Djavax.net.ssl.keyStorePassword=foobar ServerFile

runServerDebug:
	java -Djavax.net.ssl.keyStore=foobar -Djavax.net.ssl.keyStorePassword=foobar -Djava.protocol.handler.pkgs=com.sun.net.ssl.internal.www.protocol -Djavax.net.debug=ssl ServerFile
	
all: server client

clean:
	rm -f *.class
