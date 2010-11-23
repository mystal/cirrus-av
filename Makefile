JAVAC := javac

SRCDIR := cirrus
BUILDDIR := build

SERVER := $(SRCDIR)/server/Server.java
CLIENT := $(SRCDIR)/client/Client.java

.PHONY: all clean server client

all: server client

clean:
	-rm -rf $(BUILDDIR)

server: $(SERVER) | $(BUILDDIR)
	javac -d $(BUILDDIR) $^

client: $(CLIENT) | $(BUILDDIR)
	javac -d $(BUILDDIR) $^

$(BUILDDIR):
	mkdir $(BUILDDIR)
