(ns jawaninja.models.user
  (:require [clojure.java.jdbc :as sql]
            [noir.util.crypt :as crypt]
            [noir.validation :as vali]
            [noir.session :as session])
  (:use [jawaninja.database]))

;; getters

(defn all
  "Return all the rows of the users table as a vector"
  []
  (sql/with-connection db
    (sql/with-query-results res
      ["SELECT * FROM users"]
      (into [] res))))

(defn find-by-id
  "Return all the rows of the users table as a vector"
  [id]
  (sql/with-connection db
    (sql/with-query-results res
      ["SELECT * FROM users WHERE id=?", id]
      (first (into [] res)))))

(defn find-by-username
  "Return all the rows of the users table as a vector"
  [username]
  (sql/with-connection db
    (sql/with-query-results res
      ["SELECT * FROM users WHERE username=?", username]
      (first (into [] res)))))

(defn admin? []
  (session/get :admin))

(defn me []
  (session/get :username))

;; creaters
(defn valid-edit? [{:keys [username password]}]
  (vali/rule (vali/min-length? username 3)
             [:username "username must be at least 3 characters."])
  (vali/rule (vali/min-length? password 5)
             [:password "Password must be at least 5 characters."])
  (not (vali/errors? :username :password)))

(defn valid-create? [{:keys [username password] :as user}]
  (valid-edit? user)
  (vali/rule (not (find-by-username username))
             [:username "That username is already taken"])
  (not (vali/errors? :username :password)))

(defn create! [{:keys [id username password created_at updated_at] :as user}]
  (sql/with-connection db
                       (sql/insert-values
                         :users
                         [:id :username :password :created_at :updated_at]
                         [id username (crypt/encrypt password) created_at updated_at])))

(defn add! [user]
  (if (valid-create? user) (create! user)))

(defn edit! [{:keys [id username password] :as user}]
  (when (valid-edit? user)
    (sql/with-connection db
                         (sql/update-values
                           :users
                           ["id = ?" id]
                           {:username username :password (crypt/encrypt password)}))))

(defn remove! [{:keys [id] :as user}]
  (sql/with-connection db
                       (sql/delete-rows :users ["id = ?" id])))

(defn- create-users-table!
  "Create the users table"
  []
  (sql/create-table
    :users
    [:id "int(11)" "NOT NULL" "PRIMARY KEY" "AUTO_INCREMENT"]
    [:username "varchar(32)" "UNIQUE" "NOT NULL"]
    [:password "varchar(255)" "NOT NULL"]
    [:created_at "TIMESTAMP" "DEFAULT '0000-00-00 00:00:00'"]
    [:updated_at "TIMESTAMP" "DEFAULT CURRENT_TIMESTAMP on update NOW()"])
  (sql/do-commands "CREATE UNIQUE INDEX user_id_index ON users (id) USING BTREE;")
  (sql/do-commands "CREATE UNIQUE INDEX user_username_index ON users (username) USING BTREE;"))

(defn- drop-users-table!
  "Drop the users table"
  []
  (try
    (sql/drop-table :users)
    (catch Exception _)))

(defn- create-default-user! []
  (create! {:username "admin" :password "admin"}))

(defn login! [{:keys [username password] :as user}]
  (let [{stored-pass :password} (find-by-username username)]
    (if (and stored-pass 
             (crypt/compare password stored-pass))
      (do
        (session/put! :admin true)
        (session/put! :username username))
      (vali/set-error :username "Invalid username or password"))))

(defn init! []
  (sql/with-connection db
    (drop-users-table!)
    (create-users-table!)
    (create-default-user!)
    (all)))

