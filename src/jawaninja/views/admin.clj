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

(def post-actions [{:url "/blog/admin/post/add" :text "Add a post" :class "btn btn-success"}])
(def user-actions [{:url "/blog/admin/user/add" :text "Add a user" :class "btn btn-success"}])

;; Partials

(defpartial error-text [errors]
  [:p {:class "alert alert-error"} (string/join "<br/>" errors)])

(defpartial action-item [{:keys [url text class]}]
  (link-to {:class class} url text))

;; Posts

(defpartial post-item [{:keys [title] :as post}]
  (link-to (posts/url post) title))

(defpartial post-fields [{:keys [title body]}]
  (vali/on-error :title error-text)
  (text-field {:placeholder "Title" :class "large"} :title title)
  (vali/on-error :body error-text)
  (text-area {:placeholder "Body" :class "large"} :body body))

(defpartial create-post-form []
  (form-to [:post "/blog/admin/post/add"]
           (post-fields {})
           (submit-button {:class "btn btn-success"} "Add")))

(defpartial edit-post-form [{:keys [id] :as post}]
  (form-to [:post (str "/blog/admin/post/edit/" id)]
           (post-fields post)
           (submit-button {:class "btn btn-success"} "Submit")
           (link-to {:class "btn btn-danger"} (str "/blog/admin/post/remove/" id) "Remove")))

;; Users

(defpartial user-item [{:keys [id username]}]
  (link-to (str "/blog/admin/user/edit/" id) username))

(defpartial user-fields [{:keys [username] :as usr}]
  (text-field {:placeholder "Username"} :username username)
  (vali/on-error :username error-text)
  [:br]
  (password-field {:placeholder "Password"} :password)
  (vali/on-error :password error-text)
  [:br])

(defpartial create-user-form []
  (form-to [:post "/blog/admin/user/add"]
           (user-fields {})
           (submit-button {:class "btn btn-success"} "Add")))

(defpartial edit-user-form [{:keys [id] :as user}]
  (form-to [:post (str "/blog/admin/user/edit/" id)]
           (user-fields user)
           (submit-button {:class "btn btn-success"} "Submit")
           (link-to {:class "btn btn-danger"} (str "/blog/admin/user/remove/" id) "Remove")))

;; Admin pages

;;force you to be an admin to get to the admin section
(pre-route "/blog/admin*" {}
           (when-not (users/admin?)
             (resp/redirect "/blog/login")))

(defpage "/blog/login" {:as user}
  (if (users/admin?)
    (resp/redirect "/blog/admin")
    (common/main-layout {:main [(form-to [:post "/blog/login"]
                                         (user-fields user)
                                         (submit-button {:class "btn btn-success"} "Login"))]})))

(defpage [:post "/blog/login"] {:as user}
  (if (users/login! user)
    (resp/redirect "/blog/admin")
    (render "/blog/login" user)))

(defpage "/blog/logout" {}
  (session/clear!)
  (resp/redirect "/blog/"))

;; Posts admin

(defpage "/blog/admin" {}
  (common/main-layout {:main (map post-item (posts/all))
                       :side (map action-item post-actions)}))

(defpage "/blog/admin/post/add" {:as post}
  (common/main-layout {:main [(create-post-form)]}))

(defpage [:post "/blog/admin/post/add"] {:as post}
  (if (posts/add! post)
    (resp/redirect "/blog/admin")
    (render "/blog/admin/post/add" post)))

(defpage "/blog/admin/post/edit/:id" {:keys [id]}
  (if-let [post (posts/find-by-id id)]
    (common/main-layout {:main [(edit-post-form post)]})))

(defpage [:post "/blog/admin/post/edit/:id"] {:keys [id] :as post}
  (if (posts/edit! post)
    (resp/redirect (posts/url post))
    (render "/blog/admin/post/edit/:id" post)))

(defpage "/blog/admin/post/remove/:id" {:keys [id] :as post}
  (posts/remove! post)
  (resp/redirect "/blog/admin"))

;; Users admin

(defpage "/blog/admin/users" {}
  (common/main-layout {:main (map user-item (users/all))
                       :side (map action-item user-actions)}))

(defpage "/blog/admin/user/add" {}
  (common/main-layout {:main [(create-user-form)]}))

(defpage [:post "/blog/admin/user/add"] {:keys [username password] :as user}
         (if (users/add! user)
           (resp/redirect "/blog/admin/users")
           (render "/blog/admin/user/add" user)))

(defpage "/blog/admin/user/edit/:id" {:keys [id]}
  (let [user (users/find-by-id id)]
    (common/main-layout {:main [(edit-user-form user)]})))

(defpage [:post "/blog/admin/user/edit/:id"] {:keys [id] :as user}
  (if (users/edit! user)
    (resp/redirect "/blog/admin/users")
    (render "/blog/admin/user/edit/:id" user)))

(defpage "/blog/admin/user/remove/:id" {:keys [id] :as user}
  (users/remove! user)
  (resp/redirect "/blog/admin/users"))
