(ns jawaninja.models.post
  (:require [clojure.java.jdbc :as sql]
            [jawaninja.models.user :as users]
            [clojure.string :as string]
            [noir.validation :as vali]
            [noir.session :as session])
  (:use [jawaninja.database])
  )

;; Getters
(defn all
  "Return all the rows of the posts table as a vector"
  []
  (sql/with-connection db
    (sql/with-query-results res
      ["SELECT * FROM posts"]
      (into [] res))))

(defn find-by-id
  "Return all the rows of the users table as a vector"
  [id]
  (sql/with-connection db
    (sql/with-query-results res
      ["SELECT * FROM posts WHERE id=?", id]
      (first (into [] res)))))

(defn find-by-moniker
  "Return all the rows of the users table as a vector"
  [moniker]
  (sql/with-connection db
    (sql/with-query-results res
      ["SELECT * FROM posts WHERE moniker=?", moniker]
      (first (into [] res)))))

(defn find-last [count]
  (sql/with-connection db
    (sql/with-query-results res
      ["SELECT * FROM posts ORDER BY created_at DESC LIMIT ?" count]
      (into [] res))))


;; instance methods


(defn new-moniker? [moniker]
  (nil? (find-by-moniker moniker)))

(defn gen-moniker [title]
  (-> title
    (string/lower-case)
    (string/replace #"[^a-zA-Z0-9\s]" "")
    (string/replace #" " "-")))

(defn url [{:keys [moniker title]}]
  (str "/blog/post/" (if (nil? moniker) (gen-moniker title) moniker )))

(defn edit-url [{:keys [id]}]
  (str "/blog/admin/post/edit/" id))

(defn valid-edit? [{:keys [title body] :as post}]
  (vali/rule (vali/has-value? title)
             [:title "There must be a title"])
  (vali/rule (vali/has-value? body)
             [:body "There's no post content."])
  (not (vali/errors? :title :body)))

(defn valid-create? [{:keys [title body] :as post}]
  (valid-edit? post)
  (vali/rule (new-moniker? (gen-moniker title))
             [:title "That title is already taken."])
  (not (vali/errors? :title :body)))

;; Stateful

(defn create! [{:keys [title body] :as post}]
  (sql/with-connection db
                       (sql/insert-values
                         :posts
                         [:moniker :title :body :created_at :updated_at]
                         [(gen-moniker title) title body nil nil])))

(defn add! [{:keys [title body] :as post}]
  (if (valid-create? post) (create! post)))

(defn- create-posts-table!
  "Create the posts table"
  []
  (sql/create-table
    :posts
    [:id "INT(11)" "NOT NULL" "PRIMARY KEY" "AUTO_INCREMENT"]
    [:moniker "VARCHAR(32)" "NOT NULL"]
    [:title "VARCHAR(32)" "NOT NULL"]
    [:body "TEXT" "NOT NULL"]
    [:created_at "TIMESTAMP" "DEFAULT '0000-00-00 00:00:00'"]
    [:updated_at "TIMESTAMP" "DEFAULT CURRENT_TIMESTAMP on update NOW()"])
  (sql/do-commands "CREATE UNIQUE INDEX post_id_index ON posts (id) USING BTREE;")
  (sql/do-commands "CREATE UNIQUE INDEX post_moniker_index ON posts (moniker) USING BTREE;")
  (sql/do-commands "CREATE INDEX post_created_at_index ON posts (created_at) USING BTREE;"))

(defn edit! [{:keys [id title body] :as post}]
  (when (valid-edit? post)
    (sql/with-connection db
                         (sql/update-values
                           :posts
                           ["id = ?" id]
                           {:moniker (gen-moniker title) :title title :body body}))))

(defn remove! [{:keys [id] :as post}]
  (sql/with-connection db
                       (sql/delete-rows :posts ["id = ?" id])))

(defn- drop-posts-table!
  "Drop the posts table"
  []
  (try
    (sql/drop-table :posts)
    (catch Exception _)))

(defn- create-default-post! []
  (create! {:title "Welcome" :body "This is my __first__ post `print 'hello'`!

    class Test
      def test
        puts 'hello!'
      end
    end

"}))

(defn init! []
  (sql/with-connection db
    (drop-posts-table!)
    (create-posts-table!)
    (create-default-post!)
    (all)))

