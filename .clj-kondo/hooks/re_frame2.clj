(ns hooks.re-frame2
  (:require [clj-kondo.hooks-api :as hooks-api]))

(defn- register-rf-call
  "This implementation might change. At the time of writing this is the way to
  annotate keywords in order to re-use later. We need to return:

  - The event keyword with a `:reg` key, using `hooks-api/reg-keyword!`
  - In a subtree of the ast. If we return the same form the hook will be called
  again in a recursive loop in a recursive loop. So in effect, we only return
  the event-vector without the `subscribe` or `dispatch` call.

  The objective of this hook is to add a `:reg` key to the `:keywords` in the
  `:analysis` of the linter's output. This way we can know if a given keyword
  was used in the context of, a call to `subscribe` or `dispatch` so we later
  can use in an analysis.

  Example:
  ;; code.cljs
  (rf/subscribe [:some-key])

  ;; Linter output
  {...
   :analysis
   {...
    :keywords
    [{:name \"some-key\"
      :reg 're-frame.core/subscribe}]}}
  "
  [register-as]
  (fn [{:keys [node]}]
    (let [[_ event-vec] (:children node)
          [event-key & event-args] (:children event-vec)]
      {:node (hooks-api/list-node
              (list* (hooks-api/reg-keyword! event-key register-as)
                     event-args))})))

(def register-subscribe-call (register-rf-call 're-frame.core/subscribe))
(def register-dispatch-call  (register-rf-call 're-frame.core/dispatch))
