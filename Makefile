JAVAC := javac

SRCDIR := cirrus
BUILDDIR := build
OUTPUTDIR := output
DOWNLOADDIR := downloads

SERVER := $(SRCDIR)/server/Server.java
CLIENT := $(SRCDIR)/client/Client.java

.PHONY: all clean server client

all: server client

clean:
	-rm -rf $(BUILDDIR)
	-rm -rf $(OUTPUTDIR)
	-rm -rf $(DOWNLOADDIR)

server: $(SERVER) | $(BUILDDIR)
	javac -d $(BUILDDIR) $^

client: $(CLIENT) | $(BUILDDIR)
	javac -d $(BUILDDIR) $^

$(BUILDDIR):
	mkdir $(BUILDDIR)
	mkdir $(OUTPUTDIR)
	mkdir $(DOWNLOADDIR)
