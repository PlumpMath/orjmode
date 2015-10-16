(ns orjmode.core
  (:gen-class)
  (:require [instaparse.core :as insta])
  )

(def line-parser
  (insta/parser (clojure.java.io/resource "org.bnf")))

