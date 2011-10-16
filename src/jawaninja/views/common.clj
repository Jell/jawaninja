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
               :default (include-css "/css/default.css")
               :iphone [:meta {:name "viewport" :content "width=device-width,minimum-scale=1.0, maximum-scale=1.0" }]
               :reset (include-css "/css/reset.css")
               :blog.js (include-js "/js/blog.js")})

;; Helpers

(defpartial build-head [incls]
            [:head
             [:title "Jawaninja"]
             (map #(get includes %) incls)])

(defpartial link-item [{:keys [url cls text]}]
            [:li
             (link-to {:class cls} url text)])

;; Partials

(defpartial header []
  [:div#header
      [:div#sun2]
    [:h1 (link-to "/blog/" "Jawaninja")]
    [:ul.nav
      (map link-item (if (users/admin?) admin-links main-links))]
   ])

(defpartial about []
  [:div.right-panel
    [:img {:src "/img/profile.jpg" :alt "profile picture" :width "150px" :height "150px"}]
    [:p [:strong "About me"]]
    [:p "I'm a passionate developer that loves programming, learning stuff, playing guitar, programming and home-brewing beer."]])

(defpartial github-banner []
  [:a {:id "githubbanner" :href "https://github.com/Jell"}
    [:img {:src "/img/githubbanner.png" :alt "Fork me on GitHub"}]
  ])

(defpartial social []
  [:div.right-panel
    [:p [:strong "Social"]]
    [:a {:href "http://www.facebook.com/jeanlouis.giordano"}
      [:img {:src "/img/facebook.png" :alt "facebook" :width "32px" :height "32px"}]]
    [:a {:href "http://twitter.com/#!/jellismymind"}
      [:img {:src "/img/twitter.png" :alt "twitter" :width "32px" :height "32px"}]]
    [:a {:href "http://www.linkedin.com/in/jeanlouisgiordano"}
      [:img {:src "/img/linkedin.png" :alt "linkedin" :width "32px" :height "32px"}]]
      ])

;; Layouts

(defpartial main-layout [& content]
  (html5
    (build-head [:reset :default :iphone :jquery :blog.js])
    [:body
           [:div#fb-root]
           [:script "(function(d, s, id) {
                    var js, fjs = d.getElementsByTagName(s)[0];
                    if (d.getElementById(id)) {return;}
                    js = d.createElement(s); js.id = id;
                    js.src = '//connect.facebook.net/en_GB/all.js#xfbml=1';
                    fjs.parentNode.insertBefore(js, fjs);
                    }(document, 'script', 'facebook-jssdk'))"]
     [:div#body-wrapper
       (github-banner)
       [:div#page
       [:div#sun1]
        [:div#wrapper
          [:div.content
            (header)
            content
            (about)
            (social)
          ]
         ]
        ]
      ]
    ]))


(defpartial admin-layout [& content]
  (html5
    (build-head [:reset :default :iphone :jquery :blog.js])
    [:body
     [:div#body-wrapper
       (github-banner)
       [:div#page
       [:div#sun1]
        [:div#wrapper
          [:div.content
            (header)
            content
            (about)
            (social)
          ]
         ]
        ]
      ]
    ]))
