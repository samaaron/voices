(ns demo.core
  (:use [overtone.live]
        [overtone.inst.sampled-piano])
  (:require [clojure.xml :as xml]
            [clojure.zip :as zip]
            [clojure.data.zip.xml :as zx]))

(def st-xml (xml/parse "resources/summertime.xml"))
(def st-zip (zip/xml-zip st-xml))

(defn extract-pitch-data
  [raw]
  (let [step   (first (:content (first (filter #(= :step (:tag %)) raw))))
        octave (first (:content (first (filter #(= :octave (:tag %)) raw))))]
    (when (and step octave)
      (note (str step octave)))))

(defn sensible-notes
  [raw]
  (let [raw-pitch    (:content (first (filter #(= :pitch (:tag %)) raw)))
        raw-duration (:content (first (filter #(= :duration (:tag %)) raw)))
        pitch        (extract-pitch-data raw-pitch)
        duration     (Integer. (first raw-duration))]
    {:pitch pitch
     :duration duration}))

(defn sensible-measure
  [raw]
  (let [num       (Integer. (get-in raw [:attrs :number]))
        raw-notes (filter #(= :note (:tag %)) (:content raw))
        notes     (map #(sensible-notes (:content %))  raw-notes)
        notes     (remove empty? notes)]
    {:num   num
     :notes notes}))

(defn measures
  [st-zip]
  (let [raw (:content (ffirst (zx/xml-> st-zip :part)))]
    (filter identity (map sensible-measure raw))))

(defsynth foo [note 0]
  (out 0 (pan2 (sin-osc (midicps note)))))

(def f (foo))

(defn play-measure
  [m]
  (doseq [note (:notes m)]
    ;;    (ctl f :note (or (:pitch note) 0))
    (when-let [n (:pitch note)]
      (sampled-piano n))
    (println (or (:pitch note) 0))
    (Thread/sleep (* (:duration note) 300))))

(defn play-score
  [measures]
  (doseq [m measures]
    (play-measure m)))

(play-score (measures st-zip))

(stop)

(sampled-piano)
