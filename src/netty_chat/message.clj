(ns netty-chat.message
  (:use [netty-chat.newline-protocol])
  (:import
    io.netty.channel.SimpleChannelInboundHandler))

(gen-class
  :name   netty_chat.message.ChatMessageHandler
  :extends io.netty.channel.SimpleChannelInboundHandler
  :main   false
  :init   init
  :state  state
  ;:constructors { [clojure.lang.Fn clojure.lang.Fn clojure.lang.Fn] [] }
  :methods [ [ setThem [clojure.lang.Fn clojure.lang.Fn clojure.lang.Fn] void ] ]
  :prefix "chat-message-handler-")

(defn chat-message-handler-init
  [] ; [on-connected on-disconnected on-read]
  ;[ [] (atom {:on-connected on-connected :on-disconnected on-disconnected :on-read on-read }) ])
  [ [] (atom {:on-connected nil :on-disconnected nil :on-read nil }) ])

(defn chat-message-handler-handlerAdded
  [this ctx]
  (let [f (@(.state this) :on-connected) ]
    (f ctx)))

(defn chat-message-handler-handlerRemoved
  [this ctx]
  (let [f (@(.state this) :on-disconnected) ]
    (f ctx)))

(defn chat-message-handler-channelRead
  [this ctx message]
  (let [f (@(.state this) :on-read) ]
    (f ctx message)))

(defn set-field
  [instance key value]
  (swap! (.state instance) into {key value}))


(defn chat-message-handler-setThem
  [this c d r]
  (set-field this :on-connected c)
  (set-field this :on-disconnected d)
  (set-field this :on-read r))
