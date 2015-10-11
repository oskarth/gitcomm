(ns ^:figwheel-always gitcomm.core
 (:require goog.net.XhrIo
           [goog.net.EventType :refer [SUCCESS ERROR]]
           [goog.events :refer [listen]]
           [figwheel.client :as fc]
           [reagent.core :as r :refer [atom]]
           [cljsjs.firebase]))

;; Config
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;(enable-console-print!) ; print to terminal
(fc/enable-repl-print!) ; print to repl

(def fb-ref (js/Firebase. "https://intense-heat-8207.firebaseio.com"))

;; Ajax
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn request [url {:keys [method on-success on-error headers params]}]
  (let [xhr (goog.net.XhrIo.)
        str-params (if params (.stringify js/JSON (clj->js params)) nil)
        default-headers {"Content-Type" "application/json"}]
    (listen xhr SUCCESS #(on-success (js->clj (.getResponseJson (.-target %)))))
    (listen xhr ERROR #(on-error {:status (.getStatus (.-target %))}))
    (.send xhr url method str-params (clj->js (merge headers default-headers)))))

(defn GET [url opts] (request url (assoc opts :method "GET")))
(defn POST [url opts] (request url (assoc opts :method "POST")))
(defn PATCH [url opts] (request url (assoc opts :method "PATCH")))

;; State and communication
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defonce gh-user (r/atom {}))
(defonce challenger (r/atom ""))
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

(defn authed-component []
  [:div
    [:h2.h2-responsive.caps.mt4.mb0.regular "gitcomm"]
    [:p.h3 "Ready to commit " (get-in @gh-user ["github" "username"]) "?"]
    [:form
      [:input {:class "field"
               :type "text"
               :value @challenger
               :placeholder "Another github user"
               :on-change #(reset! challenger (-> % .-target .-value))}]
      [:a.h3.btn.btn-primary.black.bg-yellow
        {:on-click #(req! :challenge!)} "Challenge"]]])

(defn unauthed-component []
  [:div
    [:h2.h2-responsive.caps.mt4.mb0.regular "gitcomm"]
    [:p.h3 "A game of commitment"]
    [:a.h3.btn.btn-primary.mb4.black.bg-yellow
      {:on-click #(req! :auth-github!)} "Play with Github"]])

(defn main-component []
  [:header
    (global-nav)
    [:div.center.px2.py4
      (if (get @gh-user "github") ;; NOTE: Not a security check
          (authed-component)
          (unauthed-component))]])

(defn hello-world []
  [:h1 (:text @app-state)])

(r/render-component [main-component]
  (. js/document (getElementById "app")))

(defn on-js-reload []
  ;(swap! app-state update-in [:__figwheel_counter] inc)
)
