Run with:

``` sh
clj -m clj-kondo.main --lint src | jet --from edn --pretty
```

Example output:

``` clojure
{...
 :analysis
 {...
  :keywords [{:row 4,
              :col 16,
              :end-row 4,
              :end-col 23,
              :name "my-sub",                   ;; <- Did not register subscribe
              :filename "src/app/core.cljs"}
             {:row 5,
              :col 13,
              :end-row 5,
              :end-col 20,
              :reg re-frame.core/reg-sub,       ;; <- Correcty registered reg-sub
              :name "my-sub",
              :filename "src/app/core.cljs"}]}}
```
