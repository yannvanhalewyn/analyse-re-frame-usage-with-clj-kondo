(ns lint
  (:require [clj-kondo.core :as clj-kondo]))

(defn- safe-name [x]
  (when x (name x)))

(defn- analyze-code [paths]
  (clj-kondo/run!
   {:lint paths
    :config
    {:output {:analysis {:keywords true}}
     :hooks {:analyze-call
             ;; See .clj-kondo/hooks/re_frame2.clj for hook implementations
             '{re-frame.core/subscribe hooks.re-frame2/register-subscribe-call
               re-frame.core/dispatch hooks.re-frame2/register-dispatch-call}}}}))

(defn- filter-on-usage [syms]
  (fn [analysis-keywords]
    (filter (comp syms :reg) analysis-keywords)))

(def get-used-subscription-keys       (filter-on-usage #{'re-frame.core/subscribe}))
(def get-registered-subscription-keys (filter-on-usage #{'re-frame.core/reg-sub}))
(def get-used-event-keys              (filter-on-usage #{'re-frame.core/dispatch}))
(def get-registered-event-keys        (filter-on-usage #{'re-frame.core/reg-event-db
                                                         're-frame.core/reg-event-fx}))

(defn- ->keyword [analysis-keyword]
  (keyword (safe-name (:ns analysis-keyword)) (:name analysis-keyword)))

(defn- find-incorrect-usages [reason subset superset]
  (->> subset
       (remove (comp (set (map ->keyword superset)) ->keyword))
       (map #(assoc % :reason reason))))

(defn- report-issues [incorrect-usages]
  (doseq [incorrect-usage incorrect-usages]
    (println (format "%s:%s %s %s"
                     (:filename incorrect-usage)
                     (:row incorrect-usage)
                     (:reason incorrect-usage)
                     (->keyword incorrect-usage)))))

(defn main []
  (let [result (analyze-code ["src"])
        analysis-keywords (get-in result [:analysis :keywords])]
    (report-issues
     (concat
      (find-incorrect-usages "Call to unregistered subscription"
       (get-used-subscription-keys analysis-keywords)
       (get-registered-subscription-keys analysis-keywords))

      (find-incorrect-usages "Dispatching unregistered event"
       (get-used-event-keys analysis-keywords)
       (get-registered-event-keys analysis-keywords))

      (find-incorrect-usages "Registering unused subscription"
       (get-registered-subscription-keys analysis-keywords)
       (get-used-subscription-keys analysis-keywords))

      (find-incorrect-usages "Registering unused event"
       (get-registered-event-keys analysis-keywords)
       (get-used-event-keys analysis-keywords))))))

(main)
;; =>
;; src/app/core.cljs:8 Call to unregistered subscription :app.core/undefined-sub
;; src/app/core.cljs:9 Dispatching unregistered event :app.core/undefined-event
;; src/app/db.cljs:8 Registering unused subscription :app.db/orphaned-sub
;; src/app/db.cljs:7 Registering unused event :app.db/orphaned-event
