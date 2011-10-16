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

(defpartial user-item [{:keys [id username]}]
            [:li
             (link-to (str "/blog/admin/user/edit/" id) username)])

;; Admin pages

;;force you to be an admin to get to the admin section
(pre-route "/blog/admin*" {}
           (when-not (users/admin?)
             (resp/redirect "/blog/login")))

(defpage "/blog/login" {:as user}
         (if (users/admin?)
           (resp/redirect "/blog/admin")
           (common/main-layout
             [:ul.actions
              [:li (link-to {:class "submit"} "/" "Login")]]
             (form-to [:post "/blog/login"]
                      (user-fields user)
                      (submit-button {:class "submit"} "submit")))))

(defpage [:post "/blog/login"] {:as user}
         (if (users/login! user)
           (resp/redirect "/blog/admin")
            (render "/blog/login" user)))

(defpage "/blog/logout" {}
         (session/clear!)
         (resp/redirect "/blog/"))

;; Posts admin

(defpage "/blog/admin" {}
         (common/main-layout
           [:ul.actions
            (map action-item post-actions)]
           [:div.items
            [:ul.items
            (map post-item (posts/find-last 5))]]))

(defpage "/blog/admin/post/add" {:as post}
         (common/main-layout
           [:ul.actions
            [:li (link-to {:class "submit"} "/" "Add")]]
           (form-to [:post "/blog/admin/post/add"]
                    (post-fields post)
                    (submit-button {:class "submit"} "add post"))))

(defpage [:post "/blog/admin/post/add"] {:as post}
           (if (posts/add! post)
             (resp/redirect "/blog/admin")
             (render "/blog/admin/post/add" post)))

(defpage "/blog/admin/post/edit/:id" {:keys [id]}
         (if-let [post (posts/find-by-id id)]
           (common/main-layout
             [:ul.actions
              [:li (link-to {:class "submit"} "/" "Submit")]
              [:li (link-to {:class "delete"} (str "/blog/admin/post/remove/" id) "Remove")]]
             (form-to [:post (str "/blog/admin/post/edit/" id)]
                      (post-fields post)
                      (submit-button {:class "submit"} "submit")))))

(defpage [:post "/blog/admin/post/edit/:id"] {:keys [id] :as post}
         (if (posts/edit! post)
           (resp/redirect (posts/url post))
           (render "/blog/admin/post/edit/:id" post)))

(defpage "/blog/admin/post/remove/:id" {:keys [id] :as post}
         (posts/remove! post)
         (resp/redirect "/blog/admin"))


;; Users admin

(defpage "/blog/admin/users" {}
         (common/main-layout
           [:ul.actions
            (map action-item user-actions)]
           [:div.items
           [:ul.items
            (map user-item (users/all))]]))

(defpage "/blog/admin/user/add" {}
         (common/main-layout
           [:ul.actions
            [:li (link-to {:class "submit"} "/" "Add")]]
           (form-to [:post "/blog/admin/user/add"]
                    (user-fields {})
                    (submit-button {:class "submit"} "add user"))))

(defpage [:post "/blog/admin/user/add"] {:keys [username password] :as user}
         (if (users/add! user)
           (resp/redirect "/blog/admin/users")
           (render "/blog/admin/user/add" user)))

(defpage "/blog/admin/user/edit/:id" {:keys [id]}
         (let [user (users/find-by-id id)]
           (common/main-layout
             [:ul.actions
              [:li (link-to {:class "submit"} "/" "Submit")]
              [:li (link-to {:class "delete"} (str "/blog/admin/user/remove/" id) "Remove")]]
             (form-to [:post (str "/blog/admin/user/edit/" id)]
                      (user-fields user)))))

(defpage [:post "/blog/admin/user/edit/:id"] {:keys [id] :as user}
         (if (users/edit! user)
           (resp/redirect "/blog/admin/users")
           (render "/blog/admin/user/edit/:id" user)))

(defpage "/blog/admin/user/remove/:id" {:keys [id] :as user}
         (users/remove! user)
         (resp/redirect "/blog/admin/users"))
