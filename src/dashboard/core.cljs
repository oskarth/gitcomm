(ns ^:figwheel-always gitcomm.core
 (:require [ajax.core :refer [GET POST]]
           [figwheel.client :as fc]
           [reagent.core :as r :refer [atom]]
           [cljsjs.firebase]))

;; Config
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;(enable-console-print!) ; print to terminal
(fc/enable-repl-print!) ; print to repl

(def fb-ref (js/Firebase. "https://intense-heat-8207.firebaseio.com"))

;; State
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defonce gh-user (r/atom {}))
(defonce app-state (atom {:text "Hello Oskar!"}))


(defn auth-fb! []
 (.authWithOAuthPopup fb-ref "github"
   (fn [error data]
     (if error
         (println "Login failed" error)
         (swap! gh-user (fn [_] (js->clj data)))))))

(defn req!
  "Dispatches a request on key."
  [key]
  (cond (= key :auth-github!) (auth-fb!)
        :else (println "Error, no such key:" key)))

;; Components
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn global-nav []
  [:nav.clearfix
    [:div.col
      [:a.btn.py2 {:href "/"} "Home"]]
    [:div.col-right
      [:a.btn.py2 {:href "/"} "About"]]])

(defn main-component []
  [:header
    (global-nav)
    [:div.center.px2.py4
      [:h2.h2-responsive.caps.mt4.mb0.regular "gitcomm"]
      [:p.h3 "A game of commitment"]
      [:a.h3.btn.btn-primary.mb4.black.bg-yellow
        {:on-click #(req! :auth-github!)} "Play with Github"]]])

(defn hello-world []
  [:h1 (:text @app-state)])

(r/render-component [main-component]
  (. js/document (getElementById "app")))

(defn on-js-reload []
  ;(swap! app-state update-in [:__figwheel_counter] inc)
)
