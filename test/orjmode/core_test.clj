(ns orjmode.core-test
  (:use midje.sweet)
  (:require [orjmode.core :as core]))

(facts "about Headline Stars"
       (fact "Headline starts with Stars"
             (-> (core/line-parser "*") first)
             => [:Headline [:Stars "*"]])
       (fact "All Stars are captured"
             (-> (core/line-parser "**" ) first second second)
             => "**")
       (fact "All Stars are captured"
             (-> (core/line-parser "*****" ) first second second)
             => "*****"))

(facts "about Headline keywords"
       (fact "TODO is a keyword"
             (-> (core/line-parser "***** TODO" ) first (get 2))
             => [:TitleKeyword "TODO"])
       (fact "DONE is a keyword"
             (-> (core/line-parser "***** DONE" ) first (get 2))
             => [:TitleKeyword "DONE"])
       (fact "Other words are not keywords"
             (-> (core/line-parser "***** BUTT" ) first (get 2))
             =not=> [:TitleKeyword "BUTT"]))

(facts "about Headline Priorities"
       (fact "Priorities are [#(letter)]"
             (-> (core/line-parser "***** [#A]" ) first (get 2))
             => [:Priority "A"])
       (fact "Priorities Work when Keywords are present"
             (-> (core/line-parser "***** DONE [#A]" ) first (get 3))
             => [:Priority "A"]))

(facts "about Headline Titles"
       (fact "Titles don't contain Keywords"
             (-> (core/line-parser "***** DONE banana banana" ) first (get 3))
             => (contains [:Title]))
       (fact "Titles don't contain Priorities"
             (-> (core/line-parser "***** DONE [#A] banana banana" ) first (get 4))
             => (contains [:Title]))
       (fact "Titles don't contain tags "
             (-> (core/line-parser "***** DONE [#A] banana banana :fruits:" ) first last)
             => (contains [:Tags]))
       (fact "Titles can contain Timestamps"
             (-> (core/line-parser "***** DONE [#A] banana banana [2014-12-12 Mon 12:34]" ) 
                 first (get 4) rest (#(map first %)))
             => (contains [:InactiveTimeStamp])))

(facts "about Headline Tags"
       (fact "Ending a title with :(word):(word): adds tags"
             (-> (core/line-parser "***** DONE [#A] banana banana [2014-12-12 Mon 12:34] :one:two:")
                 first last)
             =>  [:Tags "one" "two"]))

(facts "about TimeStamps"
       (fact "[ is for inactive, < is for active."
         (-> (core/line-parser "[2012-12-12]") (get-in [0 1 0])) => :InactiveTimeStamp
         (-> (core/line-parser "<2012-12-12>") (get-in [0 1 0])) => :ActiveTimeStamp
         (-> (core/line-parser "[2012-12-12]--[2014-12-12]") (get-in [0 1 0])) => :InactiveRange
         (-> (core/line-parser "<2012-12-12>--<2014-12-12>") (get-in [0 1 0])) => :ActiveRange
         )

       (fact "all Parts of timestamp are read"
             (-> (core/line-parser "[2012-12-12 Mon 12:12]") 
                 (get-in [0 1]))
             => [:InactiveTimeStamp [:Date "2012-12-12" [:Day "Mon"]] [:Time "12:12"]])

       (fact "time Ranges are read"
             (-> (core/line-parser "[2012-12-12 Mon 12:12-14:21]") (get-in [0 1 2])) 
             =>  [:TimeRange [:Time "12:12"] [:Time "14:21"]])

       (fact "Repeaters are read"
             (-> (core/line-parser "[2012-12-12 Mon 12:12-14:21 ++2w]") (get-in [0 1 3])) 
             =>  [:Repeater "++" "2" "w"])

       (fact "Delays are read"
             (-> (core/line-parser "[2012-12-12 Mon 12:12-14:21 --23d]") (get-in [0 1 3])) 
             =>  [:Delay "--" "23" "d"]))

