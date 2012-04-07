(ns jawaninja.database
  (:require [clojure.java.jdbc :as sql]
            [clj-yaml.core :as yaml]))

(def db-config (yaml/parse-string (slurp "config/database.yml")))

(def db {:classname "com.mysql.jdbc.Driver"
         :subprotocol "mysql"
         :subname (str "//" (:host db-config) ":" (:port db-config) "/" (:database db-config))
         :user (:user db-config)
         :password (:password db-config)})

(defn db-query [query]
  (sql/with-connection db
                       (sql/with-query-results res query (into [] res))))

(defn db-insert! [& params]
  (sql/with-connection db
                       (apply sql/insert-values params)))

(defn db-create-table! [& params]
  (sql/with-connection db
                       (apply sql/create-table params)))

(defn db-drop-table! [table]
  (sql/with-connection db
                       (try
                         (sql/drop-table table)
                         (catch Exception _))))

(defn db-do-command! [& params]
  (sql/with-connection db
                       (apply sql/do-commands params)))

(defn db-update! [& params]
  (sql/with-connection db
                       (sql/transaction (apply sql/update-values params))))

(defn db-delete! [& params]
  (sql/with-connection db
                       (apply sql/delete-rows params)))


(defn all-tables []
  (map (keyword (str "tables_in_" (:database db-config)))
       (db-query ["SHOW TABLES"])))
