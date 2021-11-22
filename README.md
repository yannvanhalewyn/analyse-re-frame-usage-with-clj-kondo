# Analysing re-frame subscriptions using Clj-Kondo

This is a simple project to demonstrate and give the boilerplate for how to
analyse re-frame keywords in your codebase. This implementation prints out 4
types of warnings:

- A subscription is used but never registered
- A subscription is registered but never used
- An event is called but never registered
- An event is registered but never used

Keep in mind that this is performing a static analysis on the codebase, and any
dynamic uses of subscribing and dispatching will not be caught.

```clojure
(let [event-vec [:do-something!]]
  ;; Cannot extract the event keyword from the `event-vec` symbol
  (rf/dispatch event-vec)) 
```

There are two parts to this implementation:

- Some [.clj-kondo/hooks](https://github.com/yannvanhalewyn/kondo-register-keyword/blob/master/.clj-kondo/hooks/re_frame2.clj)
  to annotate keywords in the codebase when they are used by re-frame.
- A [script.clj](https://github.com/yannvanhalewyn/analyse-re-frame-usage-with-clj-kondo/blob/master/scripts/analyse_re_frame.clj)
  using the analysis output of Kondo to match and print the warnings.

Here's output when linting the bit of code in the src directory:

``` sh
src/app/core.cljs:8 Call to unregistered subscription :app.core/undefined-sub
src/app/core.cljs:9 Dispatching unregistered event :app.core/undefined-event
src/app/db.cljs:8 Registering unused subscription :app.db/orphaned-sub
src/app/db.cljs:7 Registering unused event :app.db/orphaned-event
```

---

## Using this script

> This project is not intended to be a library (yet?). It's informational, and
> intended to be copied and modified for your own use cases.

But to get this example working clone the repo, cd into the directory and then:

### Using from JVM Clojure  
_💡Easy and quick_

``` sh
clj -M -m analyse-re-frame
```

This should run the `-main` function in `scripts/analyse_re_frame.clj` and print
out the example output as seen above.

### Using as [Babashka](https://github.com/babashka/babashka) script 
_👍 Recommended for you project_

A small tweak needs to be done to lint from babashka. You need to call
`clj-kondo.main/run!` from a pod. See:
https://github.com/babashka/pod-registry/blob/master/examples/clj-kondo.clj

You need to make this changes to the script:

1) **Require babashka pods**

Remove the existing namespace declaration (along with the kondo require) and
replace it with:

```clojure
(ns analyse-re-frame
  (:require [babashka.pods :as pods]))
```

2) **Require the Clj-Kondo pod**

Add these lines at the top of the file after the NS declaration:

```clojure
;; Replace the version with latest or whatever version you want to use.
(pods/load-pod 'clj-kondo/clj-kondo "2021.10.19") 
(require '[pod.borkdude.clj-kondo :as clj-kondo])
```

3) **Run the script using Babashka!**


``` clojure
bb --classpath "scripts" -f scripts/analyse_re_frame.clj -m analyse-re-frame/-main
```

And you should get similar results.

---

## Modifying this script

You might be using your own custom dispatch, subscribe, reg-xx functions that
wrap re-frame. If you do so you need to make some changes. Annotating re-frame
reg-xx functions is already supported in kondo, this repo contains hooks to
annotate subscribe and dispatch calls. Both hooks work a bit differently so
different changes are required:

**Your own dispatch / subscribe calls**

1. Add an entry to the hooks defined
[here](https://github.com/yannvanhalewyn/analyze-re-frame-usage-with-clj-kondo/blob/master/scripts/analyse_re_frame.clj#L14)
following the same pattern. This will annotate the keyword used by the
subscription. You can add as many variants as you like, as long as the shape
(e.g:`(subscribe [key & args])`) stays the same. If it's not the same structure
you will need to edit the hooks to annotate the keyword correctly.

You could get the same result with adding a `:lint-as` entry for these as well.

**Your own reg-sub / reg-event**

For your own reg-xx functions, two things need to be done:

1. Add a `:lint-as` entry in the Kondo config.
2. Add your custom symbol to the correct set in the filter functions [here](https://github.com/yannvanhalewyn/analyze-re-frame-usage-with-clj-kondo/blob/master/scripts/analyse_re_frame.clj#L26)

This is needed because the annotation will have your own custom symbol (that's
the behavior of the built-in re-frame hooks) and the script needs to know what
kind of annotation it is.

## Make this into a library / linter?

If you'd like to see this packaged in a library or custom linter of some sorts,
feel free to show this interest in the form of a Github Issue or post in the
#clj-kondo Slack channel.
