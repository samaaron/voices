(ns demo.core
  (:use [overtone.live]
        [overtone.inst.sampled-piano])
  (:require [clojure.xml :as xml]
            [clojure.zip :as zip]
            [clojure.data.zip.xml :as zx]))

(def xml-path "resources/summertime.xml")

(defn mk-st-zip
  []
  (let [st-xml (xml/parse xml-path)]
    (zip/xml-zip st-xml)))

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
        tie          (:type (:attrs (first (filter #(= :tie (:tag %)) raw))))
        pitch        (extract-pitch-data raw-pitch)
        duration     (Integer. (first raw-duration))]
    {:pitch pitch
     :duration duration
     :tie tie}))

(defn sensible-measure
  [raw]
  (let [num       (Integer. (get-in raw [:attrs :number]))
        raw-notes (filter #(= :note (:tag %)) (:content raw))
        notes     (map #(sensible-notes (:content %))  raw-notes)
        notes     (remove empty? notes)]
    {:num   num
     :notes notes}))

(defn parse-measures
  [st-zip]
  (let [raw (:content (ffirst (zx/xml-> st-zip :part)))]
    (filter identity (map sensible-measure raw))))

(defn play-measure
  [m]
  (doseq [note (:notes m)]
    (when (and (:pitch note) (< 0 (:pitch note)))
      (if (not= "stop" (:tie note))
        (do
          (println "Playing: "(or (:pitch note) 0))
          (sampled-piano (:pitch note)))
        (println "(Cont):  " (or (:pitch note) 0))))

    (Thread/sleep (* (:duration note) 300))))

(defn mk-score
  [st-zip]
  (let [measures (parse-measures st-zip)
        composer (first (:content (ffirst (zx/xml-> st-zip :identification :creator))))
        title    (first (:content (ffirst (zx/xml-> st-zip :movement-title))))]
    {:measures measures
     :composer composer
     :title title}))

(defn play-score
  [measures]
  (doseq [m measures]
    (play-measure m)))

(defn play-xml
  []
  (let [score (mk-score (mk-st-zip))]
    (println "\n\n===")
    (println "Found" (:title score) "by" (:composer score))
    (println "===")
    (play-score (:measures score))))

;;(play-xml)
