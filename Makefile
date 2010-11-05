client:
	javac ClientFile.java
	
server:
	javac ServerFile.java
	
runClient:
	java -Djavax.net.ssl.keyStore=javakey -Djavax.net.ssl.trustStorePassword=1qaz2wsx ClientFile

runServer:
	java -Djavax.net.ssl.keyStore=javakey -Djavax.net.ssl.keyStorePassword=1qaz2wsx -Djava.protocol.handler.pkgs=com.sun.net.ssl.internal.www.protocol -Djavax.net.debug=ssl ServerFile
	
all: server client

clean:
	rm -f *.class
