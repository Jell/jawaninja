(ns jawaninja.models.user
  (:require [noir.util.crypt :as crypt]
            [noir.validation :as vali]
            [noir.session :as session])
  (:use [jawaninja.database]))

;; Declarations
(declare admin? me login!)
(declare all find-by-id find-by-username create! add! remove! edit!)
(declare valid-edit? valid-create?)
(declare create-users-table! drop-users-table! create-default-user!)

;; Session

(defn admin? []
  (session/get :admin))

(defn me []
  (session/get :username))

(defn login! [{:keys [username password] :as user}]
  (let [{stored-pass :password} (find-by-username username)]
    (if (and stored-pass (crypt/compare password stored-pass))
      (do
        (session/put! :admin true)
        (session/put! :username username))
      (vali/set-error :username "Invalid username or password"))))

;; Queries

(defn all []
  (db-query ["SELECT * FROM users"]))

(defn find-by-id [id]
  (first (db-query ["SELECT * FROM users WHERE id = ?", id])))

(defn find-by-username [username]
  (first (db-query ["SELECT * FROM users WHERE username = ?", username])))

(defn create! [{:keys [username password] :as user}]
  (db-insert!
    :users
    [:username :password :created_at :updated_at]
    [username (crypt/encrypt password) nil nil]))

(defn add! [user]
  (if (valid-create? user) (create! user)))

(defn remove! [{:keys [id] :as user}]
  (db-delete! :users ["id = ?" id]))

(defn edit! [{:keys [id username password] :as user}]
  (when (valid-edit? user)
    (db-update!
      :users
      ["id = ?" id]
      {:username username :password (crypt/encrypt password)})))

;; Validation

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

;; Initialization

(defn init! []
  (drop-users-table!)
  (create-users-table!)
  (create-default-user!)
  (all))

(defn- create-users-table! []
  (db-create-table!
    :users
    [:id "int(11)" "NOT NULL" "PRIMARY KEY" "AUTO_INCREMENT"]
    [:username "varchar(32)" "UNIQUE" "NOT NULL"]
    [:password "varchar(255)" "NOT NULL"]
    [:created_at "TIMESTAMP" "DEFAULT '0000-00-00 00:00:00'"]
    [:updated_at "TIMESTAMP" "DEFAULT CURRENT_TIMESTAMP on update NOW()"])
  (db-do-command! "CREATE UNIQUE INDEX user_id_index ON users (id) USING BTREE;")
  (db-do-command! "CREATE UNIQUE INDEX user_username_index ON users (username) USING BTREE;"))

(defn- drop-users-table! []
  (db-drop-table! :users))

(defn- create-default-user! []
  (create! {:username "admin" :password "admin"}))


