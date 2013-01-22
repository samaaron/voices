(ns demo.xerces
  (:use [clojure.xml])
  (:import [javax.xml.parsers  DocumentBuilderFactory]
           [org.w3c.dom Document Element NodeList]
           [org.xml.sax SAXException]))

(defonce dbf (DocumentBuilderFactory/newInstance))
(defonce db (.newDocumentBuilder dbf))
(defonce dom (.parse db "resources/summertime.xml"))

(defonce root (.getDocumentElement dom))
