(ns clj-css-analyzer.core
  (:use     [clojure.pprint :only [pprint]])
  (:require [clj-css-analyzer.css :as css]
            [me.raynes.laser :as l]
            ))

;;
;; inputs
;;

(def test-css (slurp "sample1.css"))
(def test-html (slurp "sample.html"))

;;
;; inputs -> edn
;;

;; css - vector of :*.selector [:*.selector-after-comma] [:sub-selector-with-no-asterisk] {css-body}, [...], ...
(def css-data (css/parse-css test-css))
;; html
(def html-data (l/parse test-html))

(def styled-els (l/select html-data (l/attr? :class)))

;;
;; aggregate n analyze
;;

; css
(defn- parsed-css-to-vectors[css-data]
  (cons (vec (filter #(not (vector? %)) css-data))
       (filter vector? css-data)))

(defn- css-map[css-data]
  (reduce
    #(let [v (peek %2)
        k (set (pop %2))]
      (assoc %1 k v))
      {} (parsed-css-to-vectors css-data)))


; html
(defn normalize-class-selector [class-clause]
  (-> class-clause
    (clojure.string/trim)
    (clojure.string/split #"\s+")
    (->> (map #(keyword (str "*." %))))
      set ;vec
      ))

(defn- css-from-html-map[elements]
  (reduce (fn [res node]
                            (assoc res
                              (normalize-class-selector (:class (:attrs node)))
                              {} ;node ; instead of actual content
                              ))
                        {} elements))


;; results

(def styles-from-css (css-map css-data))
(def styles-from-html (css-from-html-map styled-els))

(pprint (keys styles-from-css))
(pprint (keys styles-from-html))


;;;;;;;;;;;;;;;;;
;; todo
; diff
;  + unify resulting hash-map keys' to a set, not vector for diff/intersect/etc.
;  - multiple css & html files
;  - ui
;    ? web
;    ? local files
;    ? curl
