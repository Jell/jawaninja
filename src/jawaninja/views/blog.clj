(ns jawaninja.views.blog
  (:use noir.core
        hiccup.core
        hiccup.page-helpers
        jawaninja.helpers.timestamps
        jawaninja.helpers.markdown)
  (:require [jawaninja.models.post :as posts]
            [jawaninja.models.user :as user]
            [jawaninja.views.common :as common]
            [noir.response :as resp]))

;; Page structure

(defpartial post-item [{:keys [title body created_at] :as post}]
            (when post
              [:li.post
               [:h2 title]
               [:ul.datetime
                [:li (timestamp->date created_at) ]
                [:li (timestamp->time created_at) ]]
               [:div.content (md->html body)]]))

(defpartial blog-page [items]
            (common/main-layout
              [:ul.posts
               (map post-item items)]))

;; Blog pages

(defpage "/" []
         (resp/redirect "/blog/"))

(defpage "/blog/" []
         (blog-page (posts/find-last 5)))

(defpage "/blog/post/:id" {:keys [id]}
         (blog-page [(posts/find-by-id id)]))
