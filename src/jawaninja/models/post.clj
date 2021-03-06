(ns jawaninja.models.post
  (:require [jawaninja.models.user :as users]
            [clojure.string :as string]
            [noir.validation :as vali]
            [noir.session :as session]
            [clojure.contrib.math :as math])
  (:use [jawaninja.database])
  )

(def posts-per-page 5)
(declare new-moniker? gen-moniker)
(declare url edit-url)
(declare all find-by-id find-by-moniker find-last create! add! remove! edit!)
(declare valid-edit? valid-create?)
(declare create-posts-table! drop-posts-table! create-default-post!)

;; Moniker

(defn new-moniker? [moniker]
  (nil? (find-by-moniker moniker)))

(defn gen-moniker [title]
  (-> title
    (string/lower-case)
    (string/replace #"[^a-zA-Z0-9\s]" "")
    (string/replace #" " "-")))

;; Urls

(defn url [{:keys [moniker title]}]
  (str "/blog/post/" (if (nil? moniker) (gen-moniker title) moniker)))

(defn edit-url [{:keys [id]}]
  (str "/blog/admin/post/edit/" id))

;; Queries

(defn all []
  (db-query ["SELECT * FROM posts ORDER BY created_at DESC"]))

(defn total []
  (:count (first (db-query ["SELECT COUNT(*) as count FROM posts"]))))

(defn total-pages []
  (math/ceil (/ (total) posts-per-page)))

(defn page-list []
  (map #(vector (str %) (str "/blog/page/" %))
       (range 1 (+ 1 (total-pages)))))

(defn get-page [page]
  (let [page-num (dec (Integer. page)) ;; make it 1-based indexing
        offset (* page-num posts-per-page)]
    (db-query ["SELECT * FROM posts ORDER BY created_at DESC LIMIT ? OFFSET ?" posts-per-page offset])))

(defn find-by-id [id]
  (first (db-query ["SELECT * FROM posts WHERE id = ?", id])))

(defn find-by-moniker [moniker]
  (first (db-query ["SELECT * FROM posts WHERE moniker = ?", moniker])))

(defn find-last [count]
  (db-query ["SELECT * FROM posts ORDER BY created_at DESC LIMIT ?" count]))

(defn create! [{:keys [title body] :as post}]
  (db-insert!
    :posts
    [:moniker :title :body :created_at :updated_at]
    [(gen-moniker title) title body nil nil]))

(defn add! [post]
  (if (valid-create? post) (create! post)))

(defn remove! [{:keys [id] :as post}]
  (db-delete! :posts ["id = ?" id]))

(defn edit! [{:keys [id title body] :as post}]
  (when (valid-edit? post)
    (db-update!
      :posts
      ["id = ?" id]
      {:moniker (gen-moniker title) :title title :body body})))

;; Validation

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

;; Initialization

(defn init! []
  (drop-posts-table!)
  (create-posts-table!)
  (create-default-post!)
  (all))

(defn- create-posts-table! []
  (db-create-table!
    :posts
    [:id "INT(11)" "NOT NULL" "PRIMARY KEY" "AUTO_INCREMENT"]
    [:moniker "VARCHAR(32)" "NOT NULL"]
    [:title "VARCHAR(32)" "NOT NULL"]
    [:body "TEXT" "NOT NULL"]
    [:created_at "TIMESTAMP" "DEFAULT '0000-00-00 00:00:00'"]
    [:updated_at "TIMESTAMP" "DEFAULT CURRENT_TIMESTAMP on update NOW()"])
  (db-do-command! "CREATE UNIQUE INDEX post_id_index ON posts (id) USING BTREE;")
  (db-do-command! "CREATE UNIQUE INDEX post_moniker_index ON posts (moniker) USING BTREE;")
  (db-do-command! "CREATE INDEX post_created_at_index ON posts (created_at) USING BTREE;"))

(defn- drop-posts-table! []
  (db-drop-table! :posts))

(defn- create-default-post! []
  (create! {:title "Welcome" :body "This is my __first__ post `print 'hello'`!

    class Test
      def test
        puts 'hello!'
      end
    end

"}))
