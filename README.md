Socket Server
=============

This repository contains the code necessary for a socket server.

Usage
-----
In order to connect, first use telnet like so:

```telnet sockets.me 6379```

Once connected, establish a key:

```key example```

Then, broadcast a message to the key created:

```publish example hello```

Any command that is not recognized will result in an exit from telnet.
