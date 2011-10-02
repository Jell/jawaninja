(ns jawaninja.views.admin
  (:use noir.core
        hiccup.core
        hiccup.page-helpers
        hiccup.form-helpers)
  (:require [noir.session :as session]
            [noir.validation :as vali]
            [noir.response :as resp]
            [clojure.string :as string]
            [jawaninja.models.post :as posts]
            [jawaninja.models.user :as users]
            [jawaninja.views.common :as common]))

;; Links

(def post-actions [{:url "/blog/admin/post/add" :text "Add a post"}])
(def user-actions [{:url "/blog/admin/user/add" :text "Add a user"}])

;; Partials

(defpartial error-text [errors]
            [:p (string/join "<br/>" errors)])

(defpartial post-fields [{:keys [title body]}]
            (vali/on-error :title error-text)
            (text-field {:placeholder "Title"} :title title)
            (vali/on-error :body error-text)
            (text-area {:placeholder "Body"} :body body))

(defpartial user-fields [{:keys [username] :as usr}]
            (vali/on-error :username error-text)
            (text-field {:placeholder "Username"} :username username)
            (password-field {:placeholder "Password"} :password))

(defpartial post-item [{:keys [title] :as post}]
            [:li
             (link-to (posts/url post) title)])

(defpartial action-item [{:keys [url text]}]
            [:li
             (link-to url text)])

(defpartial user-item [{:keys [username]}]
            [:li
             (link-to (str "/blog/admin/user/edit/" username) username)])

;; Admin pages

;;force you to be an admin to get to the admin section
(pre-route "/blog/admin*" {}
           (when-not (users/admin?)
             (resp/redirect "/blog/login")))

(defpage "/blog/login" {:as user}
         (if (users/admin?)
           (resp/redirect "/blog/admin")
           (common/main-layout
             (form-to [:post "/blog/login"]
                      [:ul.actions
                       [:li (link-to {:class "submit"} "/" "Login")]]
                      (user-fields user)
                      (submit-button {:class "submit"} "submit")))))

(defpage [:post "/blog/login"] {:as user}
         (if (users/login! user)
           (resp/redirect "/blog/admin")
            (render "/blog/login" user)))

(defpage "/blog/logout" {}
         (session/clear!)
         (resp/redirect "/blog/"))

(defpage "/blog/admin" {}
         (common/admin-layout
           [:ul.actions
            (map action-item post-actions)]
           [:ul.items
            (map post-item (posts/find-last 5))]))
