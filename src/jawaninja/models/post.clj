(ns jawaninja.models.post
  (:require [clojure.java.jdbc :as sql]
            [jawaninja.models.user :as users]
            [noir.validation :as vali]
            [noir.session :as session])
  (:use [jawaninja.database])
  )


;; instance methods

(defn url [{:keys [id]}]
  (str "/blog/post/" id))

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

(defn find-last [count]
  (sql/with-connection db
    (sql/with-query-results res
      ["SELECT * FROM posts ORDER BY created_at DESC LIMIT ?" count]
      (into [] res))))

(defn create-post [title body]
  (sql/insert-values
    :posts
    [:title :body :created_at :updated_at]
    [title body nil nil]))

(defn- create-posts
  "Create the posts table"
  []
  (sql/create-table
    :posts
    [:id "INT(11)" "NOT NULL" "PRIMARY KEY" "AUTO_INCREMENT"]
    [:title "VARCHAR(32)" "NOT NULL"]
    [:body "TEXT" "NOT NULL"]
    [:created_at "TIMESTAMP" "DEFAULT '0000-00-00 00:00:00'"]
    [:updated_at "TIMESTAMP" "DEFAULT CURRENT_TIMESTAMP on update NOW()"]))

(defn- drop-posts
  "Drop the posts table"
  []
  (try
    (sql/drop-table :posts)
    (catch Exception _)))

(defn- create-default-post []
  (create-post "Welcome" "This is my __first__ post `print 'hello'`!

    class Test
      def test
        puts 'hello!'
      end
    end

"))

(defn init! []
  (sql/with-connection db
    (drop-posts)
    (create-posts)
    (create-default-post)
    (all)))

