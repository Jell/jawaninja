(ns jawaninja.helpers.timestamps
  (:require [clojure.contrib.string :as string]))

(defn timestamp->date [timestamp]
  (string/replace-re #"\s.+$" "" (string/as-str timestamp)))

(defn timestamp->time [timestamp]
  (string/replace-re #"^.+\s|:[^:]+$" "" (string/as-str timestamp)))
