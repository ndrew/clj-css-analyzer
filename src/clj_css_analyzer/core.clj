(ns clj-css-analyzer.core
  (:use     [clojure.pprint :only [pprint]])
  (:require [clj-css-analyzer.css :as css]
            [me.raynes.laser :as l]
            ))

;;
;; inputs
;;

(def test-css (slurp "sample.css"))
(def test-html (slurp "sample.html"))

;;
;; inputs -> edn
;;

;; css - vector of :*.selector [:*.selector-after-comma] [:sub-selector-with-no-asterisk] {css-body}, [...], ...
(def css-data (css/parse-css test-css))

; how to format extracted classes from html below?
(css/property-as-hash-map "baz buzz.bizz")


;; html
(def html-data (l/parse test-html))

(def styled-els (l/select html-data (l/attr? :class)))

;; aggregate n analyze
(def all-styles (reduce (fn [res node]
                            (assoc res (:class (:attrs node)) node))
                        {} styled-els))


