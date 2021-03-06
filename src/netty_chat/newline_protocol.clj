(ns netty-chat.newline-protocol
  (:require [clojure.string ])
  (:import
    io.netty.channel.ChannelDuplexHandler
    io.netty.handler.logging.LoggingHandler
    io.netty.handler.logging.LogLevel
    io.netty.handler.codec.DelimiterBasedFrameDecoder
    io.netty.handler.codec.Delimiters
    io.netty.handler.codec.string.StringDecoder
    io.netty.handler.codec.string.StringEncoder))

(gen-class
  :name   netty_chat.protocol.NewLineAddingChannelHandler
  :main   false
  :extends io.netty.channel.ChannelOutboundHandlerAdapter
  :prefix "newline-adding-channel-handler-")

(defn newline-adding-channel-handler-write
  [this ctx msg promise]
  (println (str "[DEBUG] - newline adder handler -  will add new line to " msg))
  (.writeAndFlush ctx (str msg "\r\n") promise))

(gen-class
  :name   netty_chat.protocol.ChatMessageConverterChannelHandler
  :main   false
  :extends io.netty.channel.ChannelDuplexHandler
  :prefix "message-converter-channel-handler-")

(defn message-converter-channel-handler-write
  [this ctx msg promise]
  (println (str "[DEBUG] - message converter handler -  will add user to message " msg))
  (.write ctx (str (:user msg) "!" (:message msg) ) promise))

(defn message-converter-channel-handler-channelRead
  [this ctx msg]
  (println (str "[DEBUG] - message converter handler -  will extract user from message " msg))
  (let [ [user message]  (clojure.string/split msg #"!")]
    (.fireChannelRead ctx {:message message :user user} )))

(defn assign-protocol-handlers
  [channel app-handler]
  (println (str "pipelines will be initialized for " channel))
  (let [pipeline (.pipeline channel)
        frameDetector (DelimiterBasedFrameDecoder. 8192 (Delimiters/lineDelimiter)) ]
    (doto pipeline
      (.addLast "logger" (LoggingHandler. LogLevel/DEBUG))
      (.addLast "framer" frameDetector)
      (.addLast "decoder" (StringDecoder.))
      (.addLast "encoder" (StringEncoder.))
      (.addLast "addnewlinehandler" (netty_chat.protocol.NewLineAddingChannelHandler. ))
      (.addLast "msgconverter" (netty_chat.protocol.ChatMessageConverterChannelHandler. ))
      (.addLast "handler" app-handler))))

(gen-class
  :name   netty_chat.protocol.ChatChannelInitializer
  :extends io.netty.channel.ChannelInitializer
  :main   false
  :init   init
  :state  state
  :constructors { [Class] [] }
  :prefix "chat-channel-initializer-")

(defn chat-channel-initializer-init
  [app-handler-class]
  [ [] (atom {:app-handler-class app-handler-class })])
 
(defn chat-channel-initializer-initChannel
  [this channel]
  (println (str "Channel initialized for " channel))
  (let [app-handler-class (@(.state this) :app-handler-class) app-handler (.newInstance app-handler-class)]
    (assign-protocol-handlers channel app-handler)))

(defn create-chat-channel-handler
  [app-handler-class]
  (netty_chat.protocol.ChatChannelInitializer. app-handler-class))
