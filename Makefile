JAVAC := javac

SRCDIR := cirrus
BUILDDIR := build

.PHONY: all

all: server client

clean:
	-rm -rf $(BUILDDIR)

server: $(BUILDDIR)/ServerFile.class

client: $(BUILDDIR)/ClientFile.class

$(BUILDDIR)/%.class: $(SRCDIR)/%.java | $(BUILDDIR)
	javac -d $(BUILDDIR) $^

runClient:
	java -Djavax.net.ssl.trustStore=foobar -Djavax.net.ssl.trustStorePassword=foobar ClientFile

runClientDebug:
	java -Djavax.net.ssl.trustStore=foobar -Djavax.net.ssl.trustStorePassword=foobar -Djava.protocol.handler.pkgs=com.sun.net.ssl.internal.www.protocol -Djavax.net.debug=ssl ClientFile

runServer:
	java -Djavax.net.ssl.keyStore=foobar -Djavax.net.ssl.keyStorePassword=foobar ServerFile

runServerDebug:
	java -Djavax.net.ssl.keyStore=foobar -Djavax.net.ssl.keyStorePassword=foobar -Djava.protocol.handler.pkgs=com.sun.net.ssl.internal.www.protocol -Djavax.net.debug=ssl ServerFile

$(BUILDDIR):
	mkdir $(BUILDDIR)
