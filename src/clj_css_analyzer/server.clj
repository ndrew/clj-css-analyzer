(ns clj-css-analyzer.server
  (:use     [clojure.pprint :only [pprint]])
  (:require [ring.adapter.jetty :as jetty]
            [clj-css-analyzer.core :as core]
            [ring.util.response :as r]
            [ring.middleware.resource :as res]
            [ring.middleware.params :as params]
            ))

(defn handler [request]
  ; add actual routing
  ; (pprint request)

  (let [{uri :uri} request]
    (->
       (cond
          (= "/" uri) (r/resource-response "index.html" {:root "public"})
          :else       (r/response "404!"))
        (r/content-type "text/html"))))


(def app
  (-> handler
      ; add wrappers here
      (res/wrap-resource "public")
      (params/wrap-params)
   )
  )


(defn run[]
  (jetty/run-jetty app {:port 3000}))


(def server (run))


;(defonce server (jetty/run-jetty app {:port 3000 :join? false}))
