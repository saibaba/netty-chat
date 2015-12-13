(ns netty-chat.server
  (:import
    io.netty.channel.group.DefaultChannelGroup
    io.netty.util.concurrent.GlobalEventExecutor
    io.netty.bootstrap.ServerBootstrap
    io.netty.channel.Channel
    io.netty.channel.EventLoopGroup
    io.netty.channel.nio.NioEventLoopGroup
    io.netty.channel.SimpleChannelInboundHandler
    io.netty.channel.ChannelInitializer
    io.netty.channel.ChannelPipeline
    io.netty.channel.socket.SocketChannel
    io.netty.handler.codec.DelimiterBasedFrameDecoder
    io.netty.handler.codec.Delimiters
    io.netty.handler.codec.string.StringDecoder
    io.netty.handler.codec.string.StringEncoder
    io.netty.channel.socket.nio.NioServerSocketChannel))

(gen-class
  :name   netty_chat.server.ChatServerHandler
  :extends io.netty.channel.SimpleChannelInboundHandler
  :main   false
  :methods [ [getChannels [] io.netty.channel.group.DefaultChannelGroup]]
  :prefix "chat-server-handler-")

(def client-channels-for-chat-server (DefaultChannelGroup. (GlobalEventExecutor/INSTANCE)))

(defn create-write-to-channel
  [incoming message]
  (fn [channel]
    (let [msg (str message "\r\n")]
      (println (str "[DEBUG] - will write to connected channels" msg))
      (if (= incoming channel)
        (.write channel (str "YOU SAID *** " msg))
        (.write channel msg))
      (.flush channel)
      channel)))

(defn chat-server-handler-handlerAdded
  [this ctx]
  (let [incoming (.channel ctx)
        remoteAddress (.remoteAddress incoming)]
    (.add client-channels-for-chat-server incoming)
    ; calling doall to realize the lazy sequence created by map
    (doall (map (create-write-to-channel incoming (str "[" remoteAddress "] has joined!")) client-channels-for-chat-server))))

(defn chat-server-handler-handlerRemoved
  [this ctx]
  (let [incoming (.channel ctx)
        remoteAddress (.remoteAddress incoming)]
    ; calling doall to realize the lazy sequence created by map
    (doall (map (create-write-to-channel incoming (str "[" remoteAddress "] has left!") ) client-channels-for-chat-server))
    (.remove client-channels-for-chat-server incoming) ))

(defn chat-server-handler-channelRead0
  [this ctx message]
  (let [incoming (.channel ctx)
        remoteAddress (.remoteAddress incoming)]
    (println (str "[DEBUG] - channelRead0 rcvd " message))
    ; calling doall to realize the lazy sequence created by map
    (doall (map (create-write-to-channel incoming (str "[" remoteAddress "]" message)) client-channels-for-chat-server))))
    
(gen-class
  :name   netty_chat.server.ChatServerInitializer
  :extends io.netty.channel.ChannelInitializer
  :main   false
  :prefix "chat-server-initializer-")

(defn chat-server-initializer-initChannel
  [this socketChannel]
  (println (str "Channel initialized for " socketChannel))
  (let [pipeline (.pipeline socketChannel)
        frameDetector (DelimiterBasedFrameDecoder. 8192 (Delimiters/lineDelimiter)) ]
    (.addLast pipeline "framer" frameDetector)
    (.addLast pipeline "decoder" (StringDecoder.))
    (.addLast pipeline "encoder" (StringEncoder.))
    (.addLast pipeline "handler" (netty_chat.server.ChatServerHandler. ))))

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
    (.childHandler (netty_chat.server.ChatServerInitializer. ))))

(defn new-channel
  [bootstrap port]
  (.sync (.closeFuture (.channel (.sync (.bind bootstrap port))))))

(defn run
  [port]
  (let [boss-group (new-boss-group)
        worker-group (new-worker-group)
        bootstrap (new-bootstrap boss-group worker-group)
        channel (new-channel bootstrap port)]))