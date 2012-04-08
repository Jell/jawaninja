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
                               :data-colorscheme "light"}])

(defpartial date-and-actions [{:keys [created_at] :as post}]
            [:ul {:class "unstyled pull-right"}
             [:li.label (timestamp->date created_at) ]
             (when (user/admin?)
               [:li.btn (link-to (posts/edit-url post) "edit")])])

(defpartial post-item [{:keys [moniker title body created_at] :as post} & opts]
  (when post
    [:div.row-fluid
     [:div.span2
      (facebook-like post)
      [:p]]
     [:div.span10
      (date-and-actions post)
      [:h2 (link-to (posts/url post) title)]
      (md->html body)
      (if (some #{:with-comments} opts)
        [:div.row-fluid (facebook-comments post)])]]))

(defpartial page-link [index page url]
            [:li {:class (if (= page index) "active" nil)}
             [:a {:href url} page]])

(defpartial page-links [page-num]
    [:div.row-fluid
     [:div.span2 [:p]]
     [:div {:class "pagination span8"}
      [:ul
       (map (fn [[page url]] (page-link page-num page url))
            (posts/page-list))]]])

(defpartial facebook-meta [title moniker]
  [:meta {:property "og:title"     :content title}]
  [:meta {:property "og:type"      :content "website"}]
  [:meta {:property "og:url"       :content (str "http://www.jawaninja.com/blog/post/" moniker)}]
  [:meta {:property "og:image"     :content "http://www.jawaninja.com/img/jawaninja-pixel.png"}]
  [:meta {:property "og:site_name" :content "Jawaninja"}]
  [:meta {:property "fb:admins"    :content "565303681"}])

;; Blog pages

(defpage "/" []
         (resp/redirect "/blog/"))

(defpage "/blog" []
         (resp/redirect "/blog/"))

(defpage "/blog/" []
  (render "/blog/page/:page-num" {:page-num "1"}))

(defpage "/blog/page" []
         (resp/redirect "/blog/page/1"))

(defpage "/blog/page/:page-num" {:keys [page-num]}
  (common/main-layout {:main [(map post-item (posts/get-page page-num))
                              (page-links page-num)]}))

(defpage "/blog/post/:moniker" {:keys [moniker]}
  (let [{:keys [title] :as post} (posts/find-by-moniker moniker)]
    (common/main-layout {:head (facebook-meta title moniker)
                         :main [(post-item post :with-comments)]})))
