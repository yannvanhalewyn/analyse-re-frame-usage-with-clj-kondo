(ns hooks.re-frame2
  (:require [clj-kondo.hooks-api :as hooks-api]))

(defn register-subscribe-call [{:keys [node]}]
  (let [subscription-key (-> node :children second :children first)]
    (println "Registering" (hooks-api/sexpr subscription-key)) ;; prints Registering :my-sub
    (hooks-api/reg-keyword! subscription-key 're-frame.core/subscribe)
    nil))
