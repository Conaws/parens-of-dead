(ns undead.util)

(defn solo
  "Like first, but throws if more than one item."
  [coll]
  (assert (not (next coll)))
  (first coll))

(defn only
  "Like first, but throws unless exactly one item."
  [coll]
  (assert (not (next coll)))
  (if-let [result (first coll)]
    result
    (assert false)))

(defn ssolo
  "Same as (solo (solo coll))"
  [coll]
  (solo (solo coll)))

(defn oonly
  "Same as (only (only coll))"
  [coll]
  (only (only coll)))
