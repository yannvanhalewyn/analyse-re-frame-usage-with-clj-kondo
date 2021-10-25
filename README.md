### Analysing re-frame subscriptions and event keywords using Clj-Kondo

This is a simple project to demonstrate and give the boilerplate for how to
analyse re-frame keywords in your codebase. This implementation prints out 4 types of warnings:

- A subscription is used but never registered
- A subscription is registered but never used
- An event is called but never registered
- An event is registered but never used

Keep in mind that this is performing a static analysis on the codebase, and any
dynamic uses of subscribing and dispatching will not be caught.

```clojure
(let [event-vec [:do-something!]]
  (rf/dispatch event-vec)) 
;; Cannot extract the event keyword from the `event-vec` symbol
```

There are two parts to this implementation:

- Some [.clj-kondo/hooks](https://github.com/yannvanhalewyn/kondo-register-keyword/blob/master/.clj-kondo/hooks/re_frame2.clj) to annotate keywords in the codebase when they are used by re-frame.
- A [script](https://github.com/yannvanhalewyn/kondo-register-keyword/blob/master/scripts/lint.clj) using the analysis output of Kondo to match and print the warnings.

Here's output when linting the bit of code in the src directory:

``` sh
src/app/core.cljs:8 Call to unregistered subscription :app.core/undefined-sub
src/app/core.cljs:9 Dispatching unregistered event :app.core/undefined-event
src/app/db.cljs:8 Registering unused subscription :app.db/orphaned-sub
src/app/db.cljs:7 Registering unused event :app.db/orphaned-event
```


#### If you use your own re-frame calls

Simply change the hooks defined
[here](https://github.com/yannvanhalewyn/analyze-re-frame-usage-with-clj-kondo/blob/master/scripts/lint.clj)
with your own subscribe / dispatch functions. You can add multiple variants, as
long as the shape (e.g:`(subscribe [key arg])`) stays the same. If you have a
different call structure you will need to edit the hooks to annotate the keyword
correctly.

For your own reg-xx functions, follow the comments. Two things need to be done:

1. Add a `:lint-as` entry
2. Add your custom reg-xx symbol to the correct set in the `get-xxx-` functions.

#### Using with [Babashka](https://github.com/babashka/babashka)

A small tweak needs to be done to lint from babashka. You need to call `clj-kondo.main/run!` from a pod. See: https://github.com/babashka/pod-registry/blob/master/examples/clj-kondo.clj
