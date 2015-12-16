(ns netty-chat.client
  (:use [netty-chat.newline-protocol])
  (:import
    io.netty.bootstrap.Bootstrap
    io.netty.channel.nio.NioEventLoopGroup
    io.netty.channel.SimpleChannelInboundHandler
    io.netty.channel.socket.nio.NioSocketChannel))

(gen-class
  :name   netty_chat.client.ChatClientHandler
  :extends io.netty.channel.SimpleChannelInboundHandler
  :main   false
  :prefix "chat-client-handler-")
 
(defn chat-client-handler-channelRead0
  [this channelHandlerContext message]
    (println message))

(defn new-group
  []
  (new NioEventLoopGroup))

(defn new-bootstrap
  [g]
  (doto (new Bootstrap)
    (.group g)
    (.channel NioSocketChannel)
    (.handler (create-chat-channel-handler netty_chat.client.ChatClientHandler))))

(defn new-channel
  [bootstrap host port]
  (.channel (.sync (.connect bootstrap host port))))

(defn run
  [host port]
  (let [group (new-group)
        bootstrap (new-bootstrap group)
        channel (new-channel bootstrap host port)]
    (loop []
      (let [l (read-line) o (str l)]
        (println (str "[DEBUG] - sending to server: " o))
        (.write channel o)
        (recur)))))
