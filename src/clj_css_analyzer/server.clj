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

;
; /^(https?:\/\/)?([\da-z\.-]+)\.([a-z\.]{2,6})([\/\w \.-]*)*\/?$/



(defn analyze![session-key url-map]
    (.start (Thread.
             (fn []
               (println "analyze " session-key)
               ;(def session (atom (.read-session mem-store session-key)))

               ;(println @session)

               #_(while (not @session)

                 (println "no session for " session-key ". Waiting.")

                 (try
                   (Thread/sleep 1000)
                   (catch Exception e nil)
                 )
                 (reset! session (.read-session mem-store session-key))
                 )

                 #_(let [
                       {css-urls :css
                     html-urls :html} url-map]

                      (println "lets dance! session-key: " session-key)
                      ;(print "css ")
                      ;(pprint css-urls)
                      ;(print "\nhtml ")
                      ;(pprint html-urls)

                      )
                ))))


(defn- prepare-urls[url-map]
  (reduce (fn [a [k v]]
         (assoc a (keyword k) (clojure.string/split v #";"))
         ) {} url-map))


(defn status-handler [{session :session session-key :session/key}]
  (-> (r/response (json/write-str session))
      (assoc :session session)))


(defn start-processing-handler [request]
  (print "analyze: ")
  (pprint (:session request))

  (if (:started (:session request))
     (status-handler request)
     (let [session-data (:session request)
           status { :started (if (:started session-data)
                                 (:started session-data)
                                 (System/currentTimeMillis)) }]
             (->
                 (r/response (json/write-str :ok))
                 (assoc :session (merge session-data status))

              ))))



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

        (println "WRAP: " (:uri request) " session-key" session-key)
        (pprint (.read-session mem-store session-key))

        ;(if (and (= "/analyze" (:uri request))
        ;         (not (:started (:status (:session request)))))
        ;    (analyze! session-key (prepare-urls (:params request))))

          response)))



(defn handler [request]
  (let [{uri :uri} request]
    (->
       (cond
          (= "/" uri) (r/resource-response "index.html" {:root "public"})
          (= "/analyze" uri) (start-processing-handler request)
          (= "/status" uri) (status-handler request)
        :else       (r/response "404!"))
        (r/content-type "text/html")
     )))



(def app
  (-> handler
      ; add wrappers here

      (session/wrap-session {:store mem-store})
      (wrap-async-analysis) ;; catch moment when session is not yet started

      ;(reload/wrap-reload)
      (params/wrap-params)

      (res/wrap-resource "public")

      ))



(defn run[params]
  (jetty/run-jetty app params))

