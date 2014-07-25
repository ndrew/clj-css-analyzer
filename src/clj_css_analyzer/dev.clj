(ns clj-css-analyzer.dev
  (:use     [clojure.pprint :only [pprint]])
  (:require [ring.adapter.jetty :as jetty]
          [ring.util.response :as r]
          [ring.middleware.resource :as res]
          [ring.middleware.params :as params]
          [ring.middleware.session :as session]
          [ring.middleware.session.memory :as mem]))


(comment
(def mem-store (mem/memory-store))

(defn handler[req]
  (let [debug req
        old-session (:session req)
        new-session (assoc old-session :started (when-let [{v :started} old-session] v))
        res (r/response (str "<pre>" (clojure.string/replace (pr-str debug) #", " ",\n ") "</pre>"))
        ]
            ; {:session (:session req)}]

    (-> res
        (merge {:session new-session
                :data-to-proces [:dummy]
                })
        )))


;

(defn- get-cookies [response]
  (get-in response [:headers "Set-Cookie"]))

(defn- is-session-cookie? [c]
  (.contains c "ring-session="))

(defn- get-session-cookie [response]
  (first (filter is-session-cookie? (get-cookies response))))

(defn- get-session-key[request response]
  (if-let [session-key (:session/key request)]
    session-key
    (if-let [cookie (get-session-cookie response)]
      (second (re-find #"ring-session=([^;]+)" cookie))
      nil)))

(defn wrap-async! [app]
  (fn [req]
    (let [res (app req)
          ;{session :session session-key :session/key} req
          ]

      (when-let [{data :data-to-proces} req]
        (pprint (get-session-key req res))
        ;(pprint session-key)
        ;(pprint session)
        ;(pprint data)
        )

      res
      )))


(def app
  (->
    handler
     (session/wrap-session {:store mem-store})
     (wrap-async!)


   ))

(.stop server)
(def server (jetty/run-jetty app {:port 3000 :join? false}))

)


;(require ['ring.middleware.session.store :as 'store])
;(require ['ring.middleware.session.memory :as 'mem])
;(require ['ring.middleware.session.cookie :as 'cookie])


(use 'clj-css-analyzer.server)
(.stop server)
(def server (run {:port 3000 :join? false}))


;mem-store
;(store/read-session (cookie/cookie-store) "d6fd3402-be36-4c06-b8db-775394beaefd")


