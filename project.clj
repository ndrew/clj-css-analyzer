(defproject clj-css-analyzer "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [net.sourceforge.cssparser/cssparser "0.9.7"]
                 [enlive "1.1.5"]
                 [me.raynes/laser "2.0.0-SNAPSHOT"]
                 [ring "1.3.0"]
                 [clj-time "0.6.0"] ; fix ring
                 [org.clojure/data.json "0.2.5"]
                 ])
