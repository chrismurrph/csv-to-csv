(defproject csv_to_csv "1.0.0"
  :description "Untangled Cookbook Recipe"
  :url ""
  :license {:name "MIT"
            :url  "https://opensource.org/licenses/MIT"}

  :dependencies [
                 [com.taoensso/timbre "4.7.4"]
                 [commons-codec "1.10"]
                 [org.clojure/clojure "1.9.0-alpha14" :scope "provided"]
                 [org.clojure/clojurescript "1.9.473" :scope "provided"]
                 [org.omcljs/om "1.0.0-alpha48" :scope "provided"]
                 [binaryage/devtools "0.9.4"]
                 [figwheel-sidecar "0.5.9" :exclusions [ring/ring-core joda-time org.clojure/tools.reader]]
                 ;[com.cemerick/piggieback "0.2.1"]
                 [org.clojure/tools.nrepl "0.2.12"]
                 [org.clojure/core.async "0.3.442"]
                 [org.clojure/tools.reader "1.0.0-beta4"]
                 [juxt/dirwatch "0.2.3"]
                 [garden "1.3.2"]
                 [untangled/om-css "1.0.0"]
                 [com.rpl/specter "0.13.0"]
                 [navis/untangled-client "0.8.2-SNAPSHOT" :scope "provided"]
                 [navis/untangled-server "0.7.0-SNAPSHOT" :scope "provided"]
                 [navis/untangled-spec "0.3.7-1"]
                 [navis/untangled-ui "0.1.0-SNAPSHOT"]]

  :plugins [[lein-cljsbuild "1.1.5"]]

  :source-paths ["dev/server" "src/server" "test/server"]
  :test-paths ["test/client" "test/server"]
  ;:jvm-opts ["-server" "-Xmx1024m" "-Xms512m" "-XX:-OmitStackTraceInFastThrow"]
  :clean-targets ^{:protect false} ["resources/public/js" "target"]

  :cljsbuild {:builds
              [{:id           "dev"
                :source-paths ["src/client" "dev/client"]
                :figwheel     true
                :compiler     {
                               :main                 cljs.user
                               :asset-path           "js/compiled/dev"
                               :output-to            "resources/public/js/compiled/app.js"
                               :output-dir           "resources/public/js/compiled/dev"
                               :preloads             [devtools.preload]
                               :optimizations        :none
                               :parallel-build       false
                               :verbose              false
                               :recompile-dependents true
                               :source-map-timestamp true}}
               {:id           "test"
                :source-paths ["test/client" "src/client"]
                :figwheel     true
                :compiler     {:main                 app.suite
                               :output-to            "resources/public/js/specs/specs.js"
                               :output-dir           "resources/public/js/compiled/specs"
                               :asset-path           "js/compiled/specs"
                               :recompile-dependents true
                               :optimizations        :none}}]}

  :figwheel {:css-dirs ["resources/public/css"]}

  :repl-options {:init-ns          user
                 :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
  )
