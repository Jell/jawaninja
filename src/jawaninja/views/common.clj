(ns jawaninja.views.common
  (:use noir.core
        hiccup.core
        hiccup.page-helpers)
  (:require [jawaninja.models.user :as users]))

;; Links

(defn welcome-user []
  (if (users/me) [:div#welcome [:p (str "Welcome " (users/me) "!")]]))

(def main-links [{:url "/blog/" :text "Blog"}
                 {:url "/blog/login" :text "Login"}])

(def admin-links [{:url "/blog/" :text "Blog"}
                  {:url "/blog/admin" :text "Posts"}
                  {:url "/blog/admin/users" :text "Users"}
                  {:url "/blog/logout" :text "Logout"}])

(def includes {:jquery (include-js "http://ajax.googleapis.com/ajax/libs/jquery/1.6.1/jquery.min.js")
               :bootstrap.css (include-css "/css/bootstrap.css")
               :bootstrap.js (include-js "/js/bootstrap.js")
               :default (include-css "/css/default.css")
               :iphone [:meta {:name "viewport" :content "width=device-width,minimum-scale=1.0, maximum-scale=1.0" }]
               :blog.js (include-js "/js/blog.js")})

;; Helpers

(defpartial build-head [incls & metas]
            [:head
             [:title "Jawaninja"]
             (map #(get includes %) incls)
             metas])

(defpartial link-item [{:keys [url text]}]
  (link-to {:class "btn"} url text))

;; Partials

(defpartial github-banner []
  [:a {:id "githubbanner" :href "https://github.com/Jell" :target "_blank"}
    [:img {:src "/img/githubbanner.png" :alt "Fork me on GitHub"}]
  ])

(defpartial header []
  (github-banner)
  [:div.page-header
   [:div.row-fluid
    [:div.span2
     [:img.pull-right {:src "/img/jawaninja-pixel.png"}]
     [:p]]
    [:div.span8
     [:h1 (link-to "/blog/" "Jawaninja")]
     [:p "Elucubrations of a Jedi wannabe"]
     [:div {:class "btn-group pull-right"}
      (map link-item (if (users/admin?) admin-links main-links))]
     ]
    ]
   ]
  )

(defpartial about []
    [:img {:id "about-img" :src "/img/profile.jpg" :alt "profile picture"}]
    [:h4 "About me"]
    [:p "I'm a passionate developer that loves programming, learning stuff, playing guitar, programming and home-brewing beer."])

(defpartial social []
    [:h4 "Social"]
    [:a {:href "http://www.facebook.com/jeanlouis.giordano" :target "_blank"}
      [:img {:src "/img/facebook.png" :alt "facebook" :width "32px" :height "32px"}]]
    [:a {:href "http://twitter.com/#!/jellismymind" :target "_blank"}
      [:img {:src "/img/twitter.png" :alt "twitter" :width "32px" :height "32px"}]]
    [:a {:href "http://www.linkedin.com/in/jeanlouisgiordano" :target "_blank"}
      [:img {:src "/img/linkedin.png" :alt "linkedin" :width "32px" :height "32px"}]]
      )

(defpartial facebook-script []
            [:div#fb-root]
            [:script "(function(d, s, id) {
                     var js, fjs = d.getElementsByTagName(s)[0];
                     if (d.getElementById(id)) {return;}
                     js = d.createElement(s); js.id = id;
                     js.src = '//connect.facebook.net/en_GB/all.js#xfbml=1';
                     fjs.parentNode.insertBefore(js, fjs);
                     }(document, 'script', 'facebook-jssdk'))"])

(defpartial build-body [main side]
  [:body
   (when (re-find #"fb-comments|fb-like" (str main))
     (facebook-script))
   [:div.container-fluid
    [:div.row-fluid
     [:div.span12 (header)]]
    [:div.row-fluid
     [:div.span10
      (map #(vec [:div.row-fluid %]) main)]
     [:div.span2
      [:div.row-fluid (about)]
      [:div.row-fluid (social)]
      (map #(vec [:div.row-fluid %]) side)]]]])

;; Layouts

(defpartial main-layout [head main side]
            (html5 {:xmlns:og "http://opengraphprotocol.org/schema/"
                    :xmlns:fb "http://www.facebook.com/2008/fbml"}
              (build-head [:bootstrap.css :bootstrap.js :default :iphone :jquery :blog.js] head)
              (build-body main side)))
