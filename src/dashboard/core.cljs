(ns ^:figwheel-always gitcomm.core
 (:require [ajax.core :refer [GET POST]]
           [figwheel.client :as fc]
           [reagent.core :as r :refer [atom]]
           [cljsjs.firebase]))

;(enable-console-print!) ; print to terminal
(fc/enable-repl-print!) ; print to repl

;; Application state
(defonce oskarth (r/atom {}))
(defonce gh-user (r/atom {}))
(defonce gh-koan (r/atom 0))
(defonce app-state (atom {:text "Hello Oskar!"}))

(def fb-ref (js/Firebase. "https://intense-heat-8207.firebaseio.com"))

(defn auth-fb! []
 (.authWithOAuthPopup fb-ref "github"
   (fn [error data]
     (if error
         (println "Login failed" error)
         (swap! gh-user (fn [_] (js->clj data)))))))

; GET https://github.com/login/oauth/authorize
; how to send in params?

(defn handler [response]
  (println (str response)))

(defn error-handler [{:keys [status status-text]}]
  (println (str "something bad happened: " status " " status-text)))

(defn get-oskarth! []
  (GET "https://api.github.com/users/oskarth"
       {:handler handler
        :error-handler error-handler}))

;; Seems like the best way to do an in-browser redirect. Maybe just replace.
(defn set-location! [url]
  (set! (.-location js/window) url))

; this should happen first req no?
(defn get-code-from-url []
  (let [params (str (.-search (.-location js/window)))]
    (second (re-matches #"\?code=(.*)" params))))

;; When is this done so we can get code from url?
;; Also why is this so hacky :< Some wait for code to appear? Bah.
(defn auth-github! []
    (set-location!
    "https://github.com/login/oauth/authorize?client_id=1cdf28ef2c9c5df3827f&redirect_uri=http://localhost:3449&callback=http://localhost:3449"))

(defn req!
  "Dispatches a request on key."
  [key]
  (cond (= key :auth-github!) (auth-fb!)
        :else (println "Error, no such key:" key)))

;; Components

; Not a good solution. Instead, when we click button we put it on a queue or
; whatever. Event loop thingy like you had with your om thing. Use that!
(defn counting-component []
  [:div
    "The atom " [:code "gh-koan"] " has value: "
    @gh-koan ". "
    [:input {:type "button" :value "Click me to get it!"
             :on-click #(swap! gh-koan inc)}]])

(defn timer-component []
  (let [seconds-elapsed (r/atom 0)]
    (fn []
      (js/setTimeout #(swap! seconds-elapsed inc) 1000)
      [:div
        "Seconds Elapsed: " @seconds-elapsed])))

(defn global-nav []
  [:nav.clearfix
    [:div.col
      [:a.btn.py2 {:href "/"} "Home"]]
    [:div.col-right
      [:a.btn.py2 {:href "/"} "About"]]])

(defn authed-component []
 [:div
  [:p.h3 (str "Welcome " (get-code-from-url) "!")]
  [:a.h3.btn.btn-primary.mb4.black.bg-yellow
   {:on-click #(req! :foo!)} "Find someone to play with"]])

(defn unauthed-component []
 [:div
   [:p.h3 "A game of commitment"]
   [:a.h3.btn.btn-primary.mb4.black.bg-yellow
   {:on-click #(req! :auth-github!)} "Play with Github"]])

(defn main-component []
  [:header
    (global-nav)
    [:div.center.px2.py4
      [:h2.h2-responsive.caps.mt4.mb0.regular "gitcomm"]
      ;; TODO: Check auth-state too, and how know if real?
      (if (get-code-from-url)
          (authed-component)
          (unauthed-component))]])

(defn hello-world []
  [:h1 (:text @app-state)])

(r/render-component [main-component]
  (. js/document (getElementById "app")))

(defn on-js-reload []
  ;(swap! app-state update-in [:__figwheel_counter] inc)
)
