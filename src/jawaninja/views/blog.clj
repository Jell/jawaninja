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
(defpartial facebook-like [post]
            [:div.fb-like  {:data-send "true"
                            :data-href (str "www.jawaninja.com" (posts/url post))
                            :data-layout "box_count"
                            :data-width "50"
                            :data-show-faces "true"
                            :data-colorscheme="dark"}])

(defpartial facebook-comments [post]
            [:div.fb-comments {:data-href (str "www.jawaninja.com" (posts/url post))
                               :data-num-posts "2"
                               :data-width "500"
                               :data-colorscheme "dark"}])

(defpartial date-and-actions [{:keys [created_at] :as post}]
            [:ul.datetime
             (when (user/admin?)
               [:li (link-to (posts/edit-url post) "edit")])
             [:li (timestamp->date created_at) ]
             [:li (timestamp->time created_at) ]])

(defpartial post-item [{:keys [moniker title body created_at] :as post} & opts]
            (when post
              [:li.post
               [:h2 (link-to (posts/url post) title)]
               (facebook-like post)
               (date-and-actions post)
               [:div.content (md->html body)]
               (if (some #{:with-comments} opts)
                 (facebook-comments post))
               ]))

(defpartial page-link [page url]
            [:li
             [:a.button {:href url} page]])

(defpartial page-links []
            [:ul.actions
             (map #(apply page-link %) (posts/page-list))])
;; Blog pages

(defpage "/" []
         (resp/redirect "/blog/"))

(defpage "/blog" []
         (resp/redirect "/blog/"))

(defpage "/blog/" []
         (common/main-layout
           [:ul.posts
            (map post-item (posts/get-page 1))]
           (page-links)))

(defpage "/blog/page" []
         (resp/redirect "/blog/page/1"))

(defpage "/blog/page/" []
         (resp/redirect "/blog/page/1"))

(defpage "/blog/page/:page-num" {:keys [page-num]}
         (common/main-layout
           [:ul.posts
            (map post-item (posts/get-page page-num))]
           (page-links)))

(defpage "/blog/post/:moniker" {:keys [moniker]}
            (common/main-layout
              [:ul.posts
               (post-item (posts/find-by-moniker moniker) :with-comments)]))

