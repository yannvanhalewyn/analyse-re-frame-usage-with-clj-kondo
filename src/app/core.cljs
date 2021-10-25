(ns app.core
  (:require [re-frame.core :as rf]))

(rf/subscribe [:my-sub])
(rf/reg-sub :my-sub (fn []))
