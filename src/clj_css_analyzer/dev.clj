(ns clj-css-analyzer.dev)

(use 'clj-css-analyzer.server)


;(require ['ring.middleware.session.store :as 'store])
;(require ['ring.middleware.session.memory :as 'mem])
;(require ['ring.middleware.session.cookie :as 'cookie])


(.stop server)
(def server (run {:port 3000 :join? false}))


;(store/read-session (cookie/cookie-store) "d6fd3402-be36-4c06-b8db-775394beaefd")

;mem-store


