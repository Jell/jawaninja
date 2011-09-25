(ns jawaninja.database
  (:require [clojure.java.jdbc :as sql]
            [clj-yaml.core :as yaml]))

(def db-config (yaml/parse-string (slurp "config/database.yml")))

(def db {:classname "com.mysql.jdbc.Driver"
         :subprotocol "mysql"
         :subname (str "//" (:host db-config) ":" (:port db-config) "/" (:database db-config))
         :user (:user db-config)
         :password (:password db-config)})

(defn all-tables
  "Return all tables as a vector"
  []
  (sql/with-connection db
    (sql/with-query-results res
      ["SHOW TABLES"]
      (map (keyword (str "tables_in_" (:database db-config)))
           (into [] res)))))

