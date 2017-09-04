(defproject csv-to-csv "1.0.0"
  :description "Untangled Cookbook Recipe"
  :url ""
  :license {:name "MIT"
            :url  "https://opensource.org/licenses/MIT"}

  :dependencies [[com.taoensso/timbre "4.7.4"]
                 [commons-codec "1.10"]
                 [org.clojure/clojure "1.9.0-alpha14" :scope "provided"]
                 [org.clojure/tools.nrepl "0.2.12"]
                 [org.clojure/core.async "0.3.442"]
                 [org.clojure/tools.reader "1.0.0-beta4"]
                 [juxt/dirwatch "0.2.3"]
                 [com.rpl/specter "0.13.0"]
                 [org.clojure/tools.namespace "0.2.11"]
                 [com.stuartsierra/component "0.3.2"]
                 ]

  :source-paths ["dev/server" "src/server" "test/server"]
  :test-paths ["test/client" "test/server"]
  ;:jvm-opts ["-server" "-Xmx1024m" "-Xms512m" "-XX:-OmitStackTraceInFastThrow"]
  :clean-targets ^{:protect false} ["resources/public/js" "target"]

  :repl-options {:init-ns          user}
  )
