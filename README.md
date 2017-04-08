# netty-chat

A Clojure project to play with netty and build netty-chat application.

## Usage

### Compile
$ lein compile (generates aot classes)

### Run
In one window:
$ lein repl
* (require '[netty-chat.server])
* (netty-chat.server/run 8888)

In some multiple windows:
$ lein repl
* (require '[netty-chat.client])
* (netty-chat.client/run "user" "localhost" 8888)
# type some messages in each window and observe as they show up in other windows

## TODO
consider adding javassist as dependency ... why?

## References

* https://www.youtube.com/watch?v=tsz-assb1X8
* https://github.com/gongshow20/NettyChatServer/blob/master/NettyPractice/src/com/BarDownSystems/NettyChatServer/ChatServerHandler.java

