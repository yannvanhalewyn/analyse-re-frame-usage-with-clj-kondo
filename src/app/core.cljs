(ns app.core
  (:require [re-frame.core :as rf]
            [app.db :as db]))

(rf/subscribe [::db/existing-sub])
(rf/dispatch [::db/existing-event])

(rf/subscribe [::undefined-sub])
(rf/dispatch [::undefined-event])
