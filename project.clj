(defproject jawaninja "0.1.0-SNAPSHOT"
            :description "FIXME: write this!"
            :dependencies [[org.clojure/clojure "1.3.0"]
                           [clj-time "0.3.0"]
                           [noir "1.2.0"]
                           [org.markdownj/markdownj "0.3.0-1.0.2b4"]
                           [org.clojure/java.jdbc "0.0.7"]
                           [mysql/mysql-connector-java "5.1.17"]
                           [clj-yaml "0.3.0-SNAPSHOT"]]
            :dev-dependencies [[lein-ring "0.4.6"]]
            :ring {:handler jawaninja.server/handler}
            :main jawaninja.server)

