(ns clj-css-analyzer.server
  (:use     [clojure.pprint :only [pprint]])
  (:require [ring.adapter.jetty :as jetty]
            [clj-css-analyzer.core :as core]
            [ring.util.response :as r]
            [ring.middleware.resource :as res]
            [ring.middleware.reload :as reload]
            [ring.middleware.params :as params]
            [ring.middleware.session :as session]
            [ring.middleware.session.memory :as mem]
            [clojure.data.json :as json]))

;;
;; global state
;;
(def mem-store (mem/memory-store))



; /^(https?:\/\/)?([\da-z\.-]+)\.([a-z\.]{2,6})([\/\w \.-]*)*\/?$/



(defn analyze![session-key url-map]
  (println "foo")
  (let [{css-urls :css
         html-urls :html} url-map]

          (println "session is: " session-key)
          (print "css ")
          (pprint css-urls)
          (print "\nhtml ")
          (pprint html-urls)

          ; do async processing here

          ))


(defn- prepare-urls[url-map]
  (reduce (fn [a [k v]]
         (assoc a (keyword k) (clojure.string/split v #";"))
         ) {} url-map))


(defn status-handler [{session :session session-key :session/key}]
  (-> (r/response (if (:status session)
                      (json/write-str (:status session))
                     "{}"))
      (assoc :session session)))


(defn analyze-handler [{data :form-params session-data :session session-key :session/key}]
  (if (:started session-data)
     (status-handler {:session session-data :session/key session-key})
     (let [status { :started (System/currentTimeMillis) }]
             (->
                 (r/response (json/write-str session-key))
                 (assoc :session (assoc session-data :status status))))))


(defn handler [request]
  (let [{uri :uri} request]
    (->
       (cond
          (= "/" uri) (r/resource-response "index.html" {:root "public"})
          (= "/analyze" uri) (analyze-handler request)
          (= "/status" uri) (status-handler request)
        :else       (r/response "404!"))
        (r/content-type "text/html")
     )))



(defn get-cookies [response]
  (get-in response [:headers "Set-Cookie"]))

(defn is-session-cookie? [c]
  (.contains c "ring-session="))

(defn get-session-cookie [response]
  (first (filter is-session-cookie? (get-cookies response))))

(defn get-session-key[request response]
  (if-let [session-key (:session/key request)]
    session-key
    (if-let [cookie (get-session-cookie response)]
      (second (re-find #"ring-session=([^;]+)" cookie))
      nil)))


(defn wrap-async-analysis [app]
  (fn [request]
      (let [response (app request)
            session-key (get-session-key request response)]

        (if (and (= "/analyze" (:uri request))
                 (not (:started (:status (:session request)))))
          (analyze! session-key (prepare-urls (:params request))))

          response)))


(def app
  (-> handler
      ; add wrappers here

      (wrap-async-analysis)
      (session/wrap-session {:store mem-store})

      ;(reload/wrap-reload)
      (params/wrap-params)

      (res/wrap-resource "public")

      ))


(defn run[params]
  (jetty/run-jetty app params))

