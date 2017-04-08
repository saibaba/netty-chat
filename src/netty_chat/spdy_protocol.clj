(ns netty-chat.spdy-protocol
  (:require [clojure.string ])
  (:import
    io.netty.handler.codec.spdy.SpdyFrameCodec
    io.netty.handler.codec.spdy.SpdySessionHandler
    io.netty.handler.codec.spdy.DefaultSpdyDataFrame
    io.netty.handler.codec.spdy.SpdyVersion
    io.netty.buffer.ByteBuf
    io.netty.channel.ChannelDuplexHandler
    io.netty.handler.logging.LoggingHandler
    io.netty.handler.logging.LogLevel))

(gen-class
  :name   netty_chat.protocol.SpdyChatMessageConverterChannelHandler
  :main   false
  :extends io.netty.channel.ChannelDuplexHandler
  :prefix "spdy-message-converter-channel-handler-")

(defn spdy-message-converter-channel-handler-write
  [this ctx msg promise]
  (println (str "[DEBUG] - message converter handler -  will add user to message " msg))
  (.write ctx (str (:user msg) "!" (:message msg) ) promise))

(defn spdy-message-converter-channel-handler-channelRead
  [this ctx msg]
  (println (str "[DEBUG] - message converter handler -  will extract user from message " msg))
  (let [ [user message]  (clojure.string/split msg #"!")]
    (.fireChannelRead ctx {:message message :user user} )))

(defn assign-protocol-handlers
  [channel app-handler server-mode]
  (println (str "pipelines will be initialized for " channel))
  (let [pipeline (.pipeline channel)]
    (doto pipeline
      (.addLast "decoder" (SpdyFrameCodec. (SpdyVersion/SPDY_3_1)))
      (.addLast "spdy_Session_handler" (SpdySessionHandler. (SpdyVersion/SPDY_3_1) server-mode))
      (.addLast "msgconverter" (netty_chat.protocol.SpdyChatMessageConverterChannelHandler. ))
      (.addLast "handler" app-handler))))

(gen-class
  :name   netty_chat.protocol.SpdyChatChannelInitializer
  :extends io.netty.channel.ChannelInitializer
  :main   false
  :init   init
  :state  state
  :constructors { [Class Boolean] [] }
  :prefix "spdy-chat-channel-initializer-")

(defn spdy-chat-channel-initializer-init
  [app-handler-class server-mode]
  [ [] (atom {:app-handler-class app-handler-class :server-mode server-mode })])
 
(defn spdy-chat-channel-initializer-initChannel
  [this channel]
  (println (str "Channel initialized for " channel))
  (let [app-handler-class (@(.state this) :app-handler-class) app-handler (.newInstance app-handler-class) server-mode (@(.state this) :server-mode) ]
    (assign-protocol-handlers channel app-handler server-mode)))

(defn create-spdy-chat-channel-handler
  [app-handler-class server-mode]
  (netty_chat.protocol.SpdyChatChannelInitializer. app-handler-class server-mode))

