(ns jawaninja.models.user
  (:require [clojure.java.jdbc :as sql]
            [noir.util.crypt :as crypt]
            [noir.validation :as vali]
            [noir.session :as session])
  (:use [jawaninja.database]))

(defn create-user [name password]
  (sql/insert-values
    :users
    [:name :password :created_at :updated_at]
    [name password nil nil]))

(defn- create-users
  "Create the users table"
  []
  (sql/create-table
    :users
    [:id "int(11)" "NOT NULL" "PRIMARY KEY" "AUTO_INCREMENT"]
    [:name "varchar(32)" "UNIQUE" "NOT NULL"]
    [:password "varchar(32)" "NOT NULL"]
    [:created_at "TIMESTAMP" "DEFAULT '0000-00-00 00:00:00'"]
    [:updated_at "TIMESTAMP" "DEFAULT CURRENT_TIMESTAMP on update NOW()"]))

(defn- drop-users
  "Drop the users table"
  []
  (try
    (sql/drop-table :users)
    (catch Exception _)))


(defn- create-default-user []
  (create-user "admin" "admin"))

(defn all-users
  "Return all the rows of the users table as a vector"
  []
  (sql/with-connection db
    (sql/with-query-results res
      ["SELECT * FROM users"]
      (into [] res))))

(defn init! []
  (sql/with-connection db
    (drop-users)
    (create-users)
    (create-default-user)
    (all-users)))

