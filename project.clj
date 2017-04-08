(defproject netty-chat "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"] [io.netty/netty-all "4.0.33.Final"] ]
  :aliases { 
             "srvr" [ "run" "-m" "netty-chat.server" 8888 ] 
             "clnt" [ "run" "-m" "netty-chat.client" "revathi" "localhost" 8888 ] 
             "spdy-srvr" [ "run" "-m" "netty-chat.spdy-server" 8888 ] 
             "spdy-clnt" [ "run" "-m" "netty-chat.spdy-client" "revathi" "localhost" 8888 ] 
           }
  :aot :all)
