(ns app.db
  (:require [re-frame.core :as rf]))

(rf/reg-event-db ::existing-event (fn [db]))
(rf/reg-sub ::existing-sub (fn [db]))

(rf/reg-event-db ::orphaned-event (fn [db]))
(rf/reg-sub ::orphaned-sub (fn [db]))
