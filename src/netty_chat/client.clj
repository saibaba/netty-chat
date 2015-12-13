(ns netty-chat.client
  (:import
    io.netty.bootstrap.Bootstrap
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
    io.netty.channel.socket.nio.NioSocketChannel))

(gen-class
  :name   netty_chat.client.ChatClientHandler
  :extends io.netty.channel.SimpleChannelInboundHandler
  :main   false
  :prefix "chat-client-handler-")
 
(defn chat-client-handler-channelRead0
  [this channelHandlerContext message]
    (println message))

(gen-class
  :name   netty_chat.client.ChatClientInitializer
  :extends io.netty.channel.ChannelInitializer
  :main   false
  :prefix "chat-client-initializer-")

(defn chat-client-initializer-initChannel
  [this socketChannel]
  (let [pipeline (.pipeline socketChannel)
        frameDetector (DelimiterBasedFrameDecoder. 8192 (Delimiters/lineDelimiter)) ]
    (.addLast pipeline "framer" frameDetector)
    (.addLast pipeline "decoder" (StringDecoder.))
    (.addLast pipeline "encoder" (StringEncoder.))
    (.addLast pipeline "handler" (netty_chat.client.ChatClientHandler. ))))

(defn new-group
  []
  (new NioEventLoopGroup))

(defn new-bootstrap
  [g]
  (doto (new Bootstrap)
    (.group g)
    (.channel NioSocketChannel)
    (.handler (netty_chat.client.ChatClientInitializer. ))))

(defn new-channel
  [bootstrap host port]
  (.channel (.sync (.connect bootstrap host port))))

(defn run
  [host port]
  (let [group (new-group)
        bootstrap (new-bootstrap group)
        channel (new-channel bootstrap host port)]
    (loop []
      (let [l (read-line) o (str l "\r\n")]
        (println (str "[DEBUG] - sending to server: " o))
        (.write channel o)
        (.flush channel)
        (recur)))))
