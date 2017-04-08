(ns netty-chat.spdy-client
  (:use [netty-chat.spdy-protocol])
  (:import
    io.netty.bootstrap.Bootstrap
    io.netty.channel.nio.NioEventLoopGroup
    io.netty.channel.SimpleChannelInboundHandler
    io.netty.channel.socket.nio.NioSocketChannel))

(gen-class
  :name   netty_chat.client.SpdyChatClientHandler
  :extends io.netty.channel.SimpleChannelInboundHandler
  :main   false
  :prefix "spdy-chat-client-handler-")
 
(defn spdy-chat-client-handler-channelRead
  [this channelHandlerContext msg]
    (println (str "[" (:user msg) "] " (:message msg))))

(defn new-group
  []
  (new NioEventLoopGroup))

(defn new-bootstrap
  [g]
  (doto (new Bootstrap)
    (.group g)
    (.channel NioSocketChannel)
    (.handler (create-spdy-chat-channel-handler netty_chat.client.SpdyChatClientHandler false))))

(defn new-channel
  [bootstrap host port]
  (.channel (.sync (.connect bootstrap host port))))

(defn run
  [user host port]
  (let [group (new-group)
        bootstrap (new-bootstrap group)
        channel (new-channel bootstrap host port)]
    (loop []
      (let [l (read-line) o (str l)]
        (println (str "[DEBUG] - sending to server: " o))
        (.write channel { :user user :message o})
        (recur)))))

(defn -main
  [user host port]
  (netty-chat.spdy-client/run user host port))
