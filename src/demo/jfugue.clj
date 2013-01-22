(ns demo.jfugue
  (:import [org.jfugue MusicXmlParser]))

(defonce parser (MusicXmlParser.))
(defonce summer-time-xml (slurp "resources/summertime.xml"))
(defonce score (.parse parser summer-time-xml))
