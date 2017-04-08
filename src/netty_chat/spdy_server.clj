(ns netty-chat.spdy-server
  (:use [netty-chat.spdy-protocol])
  (:import
    io.netty.channel.group.DefaultChannelGroup
    io.netty.util.concurrent.GlobalEventExecutor
    io.netty.bootstrap.ServerBootstrap
    io.netty.channel.nio.NioEventLoopGroup
    io.netty.channel.SimpleChannelInboundHandler
    io.netty.channel.socket.nio.NioServerSocketChannel))

(gen-class
  :name   netty_chat.server.SpdyChatServerHandler
  :extends io.netty.channel.SimpleChannelInboundHandler
  :main   false
  :methods [ [getChannels [] io.netty.channel.group.DefaultChannelGroup]]
  :prefix "spdy-chat-server-handler-")

(def client-channels-for-chat-server (DefaultChannelGroup. (GlobalEventExecutor/INSTANCE)))

(defn create-write-to-channel
  [ctx incoming msg]
  (fn [channel]
    (let [message (:message msg) user (:user msg)]
      (println (str "[DEBUG] " user " - will write to connected channels" message))
      (if (= incoming channel)
        (.writeAndFlush channel {:message message :user (str user "(me)") } )
        (.writeAndFlush channel {:message message :user user}))
      channel)))

(defn spdy-chat-server-handler-handlerAdded
  [this ctx]
  (let [incoming (.channel ctx)
        remoteAddress (.remoteAddress incoming)]
    (.add client-channels-for-chat-server incoming)
    ; calling doall to realize the lazy sequence created by map
    (doall (map (create-write-to-channel ctx incoming {:message (str "[" remoteAddress "] has joined the conversation!") :user remoteAddress} ) client-channels-for-chat-server))))

(defn spdy-chat-server-handler-handlerRemoved
  [this ctx]
  (let [incoming (.channel ctx)
        remoteAddress (.remoteAddress incoming)]
    ; calling doall to realize the lazy sequence created by map
    (doall (map (create-write-to-channel ctx incoming {:message (str "[" remoteAddress "] has left the conversation!") :user remoteAddress} ) client-channels-for-chat-server))
    (.remove client-channels-for-chat-server incoming) ))

(defn spdy-chat-server-handler-channelRead
  [this ctx message]
  (let [incoming (.channel ctx)
        remoteAddress (.remoteAddress incoming)]
    (println (str "[DEBUG] - channelRead rcvd " message))
    ; calling doall to realize the lazy sequence created by map
    (doall (map (create-write-to-channel ctx incoming message) client-channels-for-chat-server))))

(defn new-boss-group
  []
  (new NioEventLoopGroup))

(defn new-worker-group
  []
  (new NioEventLoopGroup))

(defn new-bootstrap
  [boss worker]
  (doto (new ServerBootstrap)
    (.group boss worker)
    (.channel NioServerSocketChannel)
    (.childHandler (create-spdy-chat-channel-handler netty_chat.server.SpdyChatServerHandler true))))

(defn new-channel
  [bootstrap port]
  (.sync (.closeFuture (.channel (.sync (.bind bootstrap port))))))

(defn run
  [port]
  (let [boss-group (new-boss-group)
        worker-group (new-worker-group)
        bootstrap (new-bootstrap boss-group worker-group)
        channel (new-channel bootstrap port)]))

(defn -main
  [port]
  (netty-chat.spdy-server/run port))
