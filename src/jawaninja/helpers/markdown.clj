(ns jawaninja.helpers.markdown
  (:import com.petebevin.markdown.MarkdownProcessor))

(def mdp (MarkdownProcessor.))

(defn md->html [text]
  (. mdp (markdown text)))
