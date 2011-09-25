(ns jawaninja.models
  (:require [jawaninja.models.user :as users]
            [jawaninja.models.post :as posts])
  (:use [jawaninja.database]))

(defn initialize! []
  (users/init!)
  (posts/init!))

