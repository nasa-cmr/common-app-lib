(ns cmr.common-app.services.search.parameter-validation
  "Contains functions for validating query parameters"
  (:require [clojure.set :as set]
            [cmr.common.services.errors :as errors]
            [cmr.common.services.messages :as c-msg]
            [cmr.common.parameter-parser :as parser]
            [cmr.common.config :as cfg :refer [defconfig]]
            [cmr.common.util :as util]
            [clojure.string :as s]
            [cmr.common.date-time-parser :as dt-parser]
            [cmr.common.date-time-range-parser :as dtr-parser]
            [cmr.common-app.services.search.params :as p]
            ;; TODO do we need common messages in common-app?
            ; [cmr.search.services.messages.common-messages :as msg]
            ; [cmr.search.data.messages :as d-msg]
            [camel-snake-kebab.core :as csk]
            [clj-time.core :as t])
  (:import clojure.lang.ExceptionInfo))

(defn by-concept-type
  "TODO"
  [concept-type]
  concept-type)

(defconfig search-paging-depth-limit
  "The maximum value for page-num * page-size"
  {:type Long
   :default 1000000})


(def basic-params-config
  "TODO"
  {;; Parameters that must take a single value, never a vector of values.
   :single-value #{:page-size :page-num :result-format}
   ;; Parameters that must take a single value or a vector of values, never a map of values.
   :multiple-value #{:concept-id}
   ;; Parameters which do not allow option with ignore_case set to true.
   :always-case-sensitive #{:concept-id}
   ;; Parameters which do not allow pattern search option.
   :disallow-pattern #{:concept-id}
   ;; Parameter which allow search with the OR option.
   :allow-or #{}
   ;; Parameters which can be used to exclude items from results.
   :allow-exclude #{}})

(defn merge-params-config
  "TODO"
  [params1 params2]
  (merge-with set/union params1 params2))

(defn- concept-type->valid-param-names
  "A set of the valid parameter names for the given concept-type."
  [concept-type]
  (set (concat
         (keys (p/param-mappings concept-type))
         [:options])))

;; TODO continue from here

; (defn- get-ivalue-from-params
;   "Get a value from the params as an Integer or nil value. Throws NumberFormatException
;   if the value cannot be converted to an Integer."
;   [params value-keyword]
;   (when-let [value (value-keyword params)]
;     ; Return null if value is a vector.  Assumes single-value-validation handles vectors.
;     (when-not (sequential? value)
;       (Integer. value))))
;
; (defn single-value-validation
;   "Validates that parameters which, if present, must have a single value and cannot not be
;   passed as a vector of values."
;   [concept-type params]
;   (->> (select-keys params single-value-params)
;        (filter #(sequential? (second %)))
;        (map first)
;        (map #(format "Parameter [%s] must have a single value." (csk/->snake_case_string %)))))
;
; (defn multiple-value-validation
;   "Validates that parameters which, if present, must have a single value or a vector of values."
;   [concept-type params]
;   (->> (select-keys params multiple-value-params)
;        (filter #(not (or (string? (second %)) (sequential? (second %)))))
;        (map first)
;        (map #(format "Parameter [%s] must have a single value or multiple values."
;                      (csk/->snake_case_string %)))))
;
; (defn page-size-validation
;   "Validates that the page-size (if present) is a number in the valid range."
;   [concept-type params]
;   (try
;     (if-let [page-size-i (get-ivalue-from-params params :page-size)]
;       (cond
;         (< page-size-i 0)
;         ["page_size must be a number between 0 and 2000"]
;
;         (> page-size-i 2000)
;         ["page_size must be a number between 0 and 2000"]
;
;         :else
;         [])
;       [])
;     (catch NumberFormatException e
;       ["page_size must be a number between 0 and 2000"])))
;
; (defn page-num-validation
;   "Validates that the page-num (if present) is a number in the valid range."
;   [concept-type params]
;   (try
;     (if-let [page-num-i (get-ivalue-from-params params :page-num)]
;       (if (> 1 page-num-i)
;         ["page_num must be a number greater than or equal to 1"]
;         [])
;       [])
;     (catch NumberFormatException e
;       ["page_num must be a number greater than or equal to 1"])))
;
; (defn paging-depth-validation
;   "Validates that the paging depths (page-num * page-size) does not exceed a set limit."
;   [concept-type params]
;   (try
;     (let [limit (search-paging-depth-limit)
;           page-size (get-ivalue-from-params params :page-size)
;           page-num (get-ivalue-from-params params :page-num)]
;       (when (and page-size
;                  page-num
;                  (> (* page-size page-num) limit))
;         [(format "The paging depth (page_num * page_size) of [%d] exceeds the limit of %d."
;                  (* page-size page-num)
;                  limit)]))
;     (catch NumberFormatException e
;       ;; This should be handled separately by page-size and page-num validiation
;       [])))
;
; (defn boosts-validation
;   "Validates that all the provided fields in the boosts parameter are valid and that the values
;   are numeric."
;   [concept-type params]
;   (let [boosts (:boosts params)]
;     (keep (fn [[field value]]
;             (if (or (field k2e/default-boosts)
;                     (= field :provider))
;               (when-not (util/numeric-string? value)
;                 (format "Relevance boost value [%s] for field [%s] is not a number."
;                         (csk/->snake_case_string value) (csk/->snake_case_string field)))
;               (when-not (= field :include-defaults)
;                 (format "Cannot set relevance boost on field [%s]." (csk/->snake_case_string field)))))
;           (seq boosts))))
;
; (def string-param-options #{:pattern :ignore-case})
; (def pattern-option #{:pattern})
; (def or-option #{:or})
; (def and-option #{:and})
; (def and-or-option #{:and :or})
; (def exclude-plus-or-option #{:exclude-collection :or :exclude-boundary})
; (def exclude-plus-and-or-option #{:exclude-boundary :and :or})
; (def string-plus-and-options #{:pattern :ignore-case :and})
; (def string-plus-or-options #{:pattern :ignore-case :or})
; (def highlights-option #{:begin-tag :end-tag :snippet-length :num-snippets})
;
;
; (def param->valid-options
;   "Map of parameters to options that are valid for them."
;   {:collection-concept-id pattern-option
;    :native-id pattern-option
;    :data-center string-plus-and-options
;    :archive-center string-param-options
;    :dataset-id pattern-option
;    :entry-title string-plus-and-options
;    :short-name string-plus-and-options
;    :entry-id string-plus-and-options
;    :version string-param-options
;    :granule-ur string-param-options
;    :producer-granule-id string-param-options
;    :readable-granule-name string-plus-and-options
;    :project string-plus-and-options
;    :campaign string-plus-and-options
;    :platform string-plus-and-options
;    :sensor string-plus-and-options
;    :instrument string-plus-and-options
;    :collection-data-type string-param-options
;    :day-night string-param-options
;    :two-d-coordinate-system string-param-options
;    :grid string-param-options
;    :keyword pattern-option
;    :processing-level string-param-options
;    :science-keywords string-plus-or-options
;    :spatial-keyword string-plus-and-options
;    :dif-entry-id string-plus-and-options
;    :provider string-param-options
;    :attribute exclude-plus-or-option
;    :temporal exclude-plus-and-or-option
;    :revision-date and-option
;    :highlights highlights-option
;
;    ;; Tag parameters
;    :namespace string-param-options
;    :value string-param-options
;    :category string-param-options
;    :originator-id pattern-option
;
;    ;; Tag parameters for use querying other concepts.
;    :tag-namespace string-param-options
;    :tag-value string-param-options
;    :tag-category string-param-options
;    :tag-originator-id pattern-option})
;
; (defn parameter-options-validation
;   [concept-type params]
;   "Validates that no invalid parameter names in the options were supplied"
;   [concept-type params]
;   (if-let [options (:options params)]
;     (apply concat
;            (map
;              (fn [[param settings]]
;                ;; handle these parameters separately since they don't allow any options
;                (if (always-case-sensitive-params param)
;                  (map #(msg/invalid-opt-for-param param %) (keys settings))
;                  (let [valid-options (param->valid-options param)]
;                    ;; Only check params we recognize - other validations will handle the rest
;                    (when valid-options
;                      (map #(msg/invalid-opt-for-param param %)
;                           (set/difference (set (keys settings))
;                                           valid-options))))))
;              options))))
;
; (def concept-type->valid-sort-keys
;   "A map of concept type to sets of valid sort keys"
;   {:collection #{:entry-title
;                  :entry-id
;                  :dataset-id
;                  :start-date
;                  :end-date
;                  :provider
;                  :platform
;                  :instrument
;                  :sensor
;                  :revision-date
;                  :score}
;    :granule #{:granule-ur
;               :producer-granule-id
;               :readable-granule-name
;               :start-date
;               :end-date
;               :entry-title
;               :dataset-id
;               :short-name
;               :version
;               :provider
;               :data-size
;               :cloud-cover
;               :campaign
;               :platform
;               :instrument
;               :sensor
;               :project
;               :day-night
;               :downloadable
;               :browsable
;               :revision-date}
;    :tag #{}})
;
; (defn sort-key-validation
;   "Validates the sort-key parameter if present"
;   [concept-type params]
;   (if-let [sort-key (:sort-key params)]
;     (let [sort-keys (if (sequential? sort-key) sort-key [sort-key])]
;       (mapcat (fn [sort-key]
;                 (let [[_ field] (re-find #"[\-+]?(.*)" sort-key)
;                       valid-params (concept-type->valid-sort-keys concept-type)]
;                   (when-not (valid-params (keyword field))
;                     [(msg/invalid-sort-key (csk/->snake_case_string field ) concept-type)])))
;               sort-keys))
;     []))
;
;
; (defn unrecognized-params-validation
;   "Validates that no invalid parameters were supplied"
;   [concept-type params]
;   ;; this test does not apply to page_size, page_num, etc.
;   (let [params (dissoc params :page-size :page-num :boosts :sort-key :result-format :echo-compatible)
;         params (if (= :collection concept-type)
;                  ;; Parameters only supported on collections
;                  (dissoc params :include-granule-counts :include-has-granules :include-facets
;                          :hierarchical-facets :include-highlights :include-tags :all-revisions)
;                  params)]
;     (map #(format "Parameter [%s] was not recognized." (csk/->snake_case_string %))
;          (set/difference (set (keys params))
;                          (concept-type->valid-param-names concept-type)))))
;
; (defn unrecognized-params-in-options-validation
;   "Validates that no invalid parameters names in the options were supplied"
;   [concept-type params]
;   (if-let [options (:options params)]
;     (map #(str "Parameter [" (csk/->snake_case_string %)"] with option was not recognized.")
;          (set/difference (set (keys options))
;                          ;; Adding in :highlights since this option does not have any
;                          ;; corresponding search parameters
;                          (conj (concept-type->valid-param-names concept-type) :highlights)))
;     []))
;
; (defn- validate-date-time
;   "Validates datetime string is in the given format"
;   [date-name dt]
;   (try
;     (when-not (s/blank? dt)
;       (dt-parser/parse-datetime dt))
;     []
;     (catch ExceptionInfo e
;       [(format "%s datetime is invalid: %s." date-name (first (:errors (ex-data e))))])))
;
; (defn- validate-date-time-range
;   "Validates datetime range string is in the correct format"
;   [dtr]
;   (try
;     (when-not (s/blank? dtr)
;       (dtr-parser/parse-datetime-range dtr))
;     []
;     (catch ExceptionInfo e
;       [(format "temporal range is invalid: %s." (first (:errors (ex-data e))))])))
;
; (defn- day-valid?
;   "Validates if the given day in temporal is an integer between 1 and 366 inclusive"
;   [day tag]
;   (if-not (s/blank? day)
;     (try
;       (let [num (Integer/parseInt day)]
;         (when (or (< num 1) (> num 366))
;           [(format "%s [%s] must be an integer between 1 and 366" tag day)]))
;       (catch NumberFormatException e
;         [(format "%s [%s] must be an integer between 1 and 366" tag day)]))
;     []))
;
; (defn temporal-format-validation
;   "Validates that temporal datetime parameter conforms to the :date-time-no-ms format,
;   start-day and end-day are integer between 1 and 366"
;   [concept-type params]
;   (if-let [temporal (:temporal params)]
;     (let [temporal (if (sequential? temporal)
;                      temporal
;                      [temporal])]
;       (mapcat
;         (fn [value]
;           (if (re-find #"/" value)
;             (let [[iso-range start-day end-day] (map s/trim (s/split value #","))]
;               (concat
;                 (validate-date-time-range nil)
;                 (day-valid? start-day "temporal_start_day")
;                 (day-valid? end-day "temporal_end_day")))
;             (let [[start-date end-date start-day end-day] (map s/trim (s/split value #","))]
;               (concat
;                 (validate-date-time "temporal start" start-date)
;                 (validate-date-time "temporal end" end-date)
;                 (day-valid? start-day "temporal_start_day")
;                 (day-valid? end-day "temporal_end_day")))))
;         temporal))
;     []))
;
; (defn updated-since-validation
;   "Validates updated-since parameter conforms to formats in data-time-parser NS"
;   [concept-type params]
;   (if-let [param-value (:updated-since params)]
;     (if (and (sequential? (:updated-since params)) (> (count (:updated-since params)) 1))
;       ["Search not allowed with multiple updated_since values"]
;       (let [updated-since-val (if (sequential? param-value) (first param-value) param-value)]
;         (validate-date-time "updated_since" updated-since-val)))
;     []))
;
; (defn revision-date-validation
;   "Validates that revision date parameter contains valid date time strings."
;   [concept-type params]
;   (if-let [revision-date (:revision-date params)]
;     (let [revision-date (if (sequential? revision-date)
;                           revision-date
;                           [revision-date])]
;       (mapcat
;         (fn [value]
;           (let [parts (map s/trim (s/split value #"," -1))
;                 [start-date end-date] parts]
;             (if (> (count parts) 2)
;               [(format "Too many commas in revision-date %s" value)]
;               (concat
;                 (validate-date-time "revision-date start" start-date)
;                 (validate-date-time "revision-date end" end-date)))))
;         revision-date))
;     []))
;
; (defn attribute-validation
;   [concept-type params]
;   (if-let [attributes (:attribute params)]
;     (if (sequential? attributes)
;       (mapcat #(-> % attrib/parse-value :errors) attributes)
;       [(attrib-msg/attributes-must-be-sequence-msg)])
;     []))
;
; (defn science-keywords-validation
;   [concept-type params]
;   (if-let [science-keywords (:science-keywords params)]
;     (if (map? science-keywords)
;       (let [values (vals science-keywords)]
;         (if (some #(not (map? %)) values)
;           [(msg/science-keyword-invalid-format-msg)]
;           (reduce
;             (fn [errors param]
;               (if-not (some #{param} (nf/get-subfield-names :science-keywords))
;                 (conj errors (format "parameter [%s] is not a valid science keyword search term."
;                                      (name param)))
;                 errors))
;             []
;             (mapcat keys values))))
;       [(msg/science-keyword-invalid-format-msg)])
;     []))
;
; ;; This method is for processing legacy numeric ranges in the form of
; ;; param_nam[value], param_name[minValue], and param_name[maxValue].
; ;; It simply validates that the provided values are numbers and that
; ;; at least one is present.
; (defn- validate-legacy-numeric-range-param
;   "Validates a numeric parameter in the form of a map, appending the message argument
;   to the error array on failure."
;   [param-map error-message-fn & args]
;   (let [{:keys [value min-value max-value]} param-map]
;     (try
;       (when value
;         (Double. value))
;       (when min-value
;         (Double. min-value))
;       (when max-value
;         (Double. max-value))
;       (if (or value min-value max-value)
;         []
;         (if error-message-fn
;           [(apply error-message-fn args)]
;           [(d-msg/nil-min-max-msg)]))
;       (catch NumberFormatException e
;         [(apply error-message-fn args)]))))
;
; (defn- validate-numeric-range-param
;   "Validates a numeric parameter in the form parameter=value or
;   parameter=min,max, appending the message argument to the error array on failure."
;   [param error-message-fn & args]
;   (let [errors (parser/numeric-range-string-validation param)]
;     (if-not (empty? errors)
;       (if error-message-fn
;         (concat [(apply error-message-fn args)] errors)
;         errors)
;       [])))
;
; (defn cloud-cover-validation
;   "Validates cloud cover range values are numeric"
;   [concept-type params]
;   (if-let [cloud-cover (:cloud-cover params)]
;     (if (string? cloud-cover)
;       (validate-numeric-range-param cloud-cover nil)
;       (validate-legacy-numeric-range-param cloud-cover nil))
;     []))
;
; (defn orbit-number-validation
;   "Validates that the orbital number is either a single number or a range in the format
;   start,stop, or in the catlog-rest style orbit_number[value], orbit_number[minValue],
;   orbit_number[maxValue]."
;   [concept-type params]
;   (if-let [orbit-number-param (:orbit-number params)]
;     (if (string? orbit-number-param)
;       (validate-numeric-range-param orbit-number-param on-msg/invalid-orbit-number-msg)
;       (validate-legacy-numeric-range-param orbit-number-param on-msg/invalid-orbit-number-msg))
;     []))
;
; (defn equator-crossing-longitude-validation
;   "Validates that the equator-crossing-longitude parameter is a single number or
;   a valid range string."
;   [concept-type params]
;   (if-let [equator-crossing-longitude (:equator-crossing-longitude params)]
;     (if (string? equator-crossing-longitude)
;       (validate-numeric-range-param equator-crossing-longitude nil)
;       (validate-legacy-numeric-range-param equator-crossing-longitude
;                                            on-msg/non-numeric-equator-crossing-longitude-parameter))
;     []))
;
; (defn equator-crossing-date-validation
;   "Validates that the equator_crossing_date parameter is a valid date range string."
;   [concept-type params]
;   (if-let [equator-crossing-date (:equator-crossing-date params)]
;     (parser/date-time-range-string-validation equator-crossing-date)
;     []))
;
; (defn exclude-validation
;   "Validates that the key(s) supplied in 'exclude' param value are in exclude-params set"
;   [concept-type params]
;   (if-let [exclude-kv (:exclude params)]
;     (let [invalid-exclude-params (set/difference (set (keys exclude-kv))
;                                                  (exclude-params concept-type))]
;       (if (empty? invalid-exclude-params)
;         (let [exclude-values (flatten (vals exclude-kv))]
;           (if (every? string? exclude-values)
;             (if (some #(.startsWith % "C") exclude-values)
;               [(str "Exclude collection is not supported, " exclude-kv)]
;               [])
;             ["Invalid format for exclude parameter, must be in the format of exclude[name][]=value"]))
;         [(msg/invalid-exclude-param-msg invalid-exclude-params)]))
;     []))
;
; (defn boolean-value-validation
;   "Validates that all of the boolean parameters have values of true, false or unset."
;   [concept-type params]
;   (let [bool-params (select-keys params [:downloadable :browsable :include-granule-counts
;                                          :include-has-granules :include-facets
;                                          :hierarchical-facets :include-highlights :all-revisions])]
;     (mapcat
;       (fn [[param value]]
;         (if (contains? #{"true" "false" "unset"} (when value (s/lower-case value)))
;           []
;           [(format "Parameter %s must take value of true, false, or unset, but was [%s]"
;                    (csk/->snake_case_string param) value)]))
;       bool-params)))
;
; (defn- spatial-validation
;   "Validate a geometry of the given type in the params"
;   [params spatial-type]
;   (when-let [spatial-param (spatial-type params)]
;     (mapcat #(:errors (spatial-codec/url-decode spatial-type %)) (flatten [spatial-param]))))
;
; (defn polygon-validation
;   ([params] (polygon-validation nil params))
;   ([_ params] (spatial-validation params :polygon)))
;
; (defn bounding-box-validation
;   ([params] (bounding-box-validation nil params))
;   ([_ params] (spatial-validation params :bounding-box)))
;
; (defn point-validation
;   ([params] (point-validation nil params))
;   ([_ params] (spatial-validation params :point)))
;
; (defn line-validation
;   ([params] (line-validation nil params))
;   ([_ params] (spatial-validation params :line)))
;
; (defn unrecognized-standard-query-params-validation
;   "Validates that any query parameters passed to the AQL or JSON search endpoints are valid."
;   [concept-type params]
;   (map #(str "Parameter [" (csk/->snake_case_string % )"] was not recognized.")
;        (set/difference (set (keys params))
;                        (set [:page-size :page-num :sort-key :result-format :options
;                              :include-granule-counts :include-has-granules :include-facets
;                              :echo-compatible :hierarchical-facets :include-highlights
;                              :include-tags]))))
;
; (defn timeline-start-date-validation
;   "Validates the timeline start date parameter"
;   [concept-type params]
;   (let [start-date (:start-date params)]
;     (if-not (s/blank? start-date)
;       (validate-date-time "Timeline parameter start_date" start-date)
;       ["start_date is a required parameter for timeline searches"])))
;
; (defn timeline-end-date-validation
;   "Validates the timeline end date parameter"
;   [concept-type params]
;   (let [end-date (:end-date params)]
;     (if-not (s/blank? end-date)
;       (validate-date-time "Timeline parameter end_date" end-date)
;       ["end_date is a required parameter for timeline searches"])))
;
; (defn timeline-range-validation
;   "Validates the start date is before the end date"
;   [concept-type params]
;   (try
;     (let [{:keys [start-date end-date]} params]
;       (when (and start-date end-date
;                  (t/after? (dt-parser/parse-datetime start-date)
;                            (dt-parser/parse-datetime end-date)))
;         [(format "start_date [%s] must be before the end_date [%s]"
;                  start-date end-date)]))
;     (catch ExceptionInfo e
;       ;; The date times are invalid. This error should be handled by other validations
;       [])))
;
; (defn- no-highlight-options-without-highlights-validation
;   "Validates that the include-highlights parameter is set to true if any of the highlights
;   options params are set."
;   [concept-type params]
;   (if (and (get-in params [:options :highlights])
;            (not= "true" (:include-highlights params)))
;     ["Highlights options are not allowed unless the include-highlights is true."]
;     []))
;
; (defn highlights-numeric-options-validation
;   "Validates that the highlights option (if present) is an integer greater than zero."
;   [concept-type params]
;   (keep
;     (fn [param]
;       (when-let [value (get-in params [:options :highlights param])]
;         (try
;           (let [int-value (Integer/parseInt value)]
;             (when (< int-value 1)
;               (format "%s option [%d] for highlights must be an integer greater than 0."
;                       (csk/->snake_case_string param) int-value)))
;           (catch NumberFormatException e
;             (format
;               "%s option [%s] for highlights is not a valid integer."
;               (csk/->snake_case_string param) value)))))
;     [:snippet-length :num-snippets]))
;
; (defn- result-format-parameters-validation
;   "Validates parameters against result format."
;   [concept-type params]
;   (concat
;     (when (and (not= :json (:result-format params))
;                (not (s/blank? (:include-tags params))))
;       [(format "Parameter [include_tags] is only supported in JSON format search.")])))
;
; (def valid-timeline-intervals
;   "A list of the valid values for timeline intervals."
;   #{"year" "month" "day" "hour" "minute" "second"})
;
; (defn timeline-interval-validation
;   "Validates the timeline interval parameter"
;   [concept-type params]
;   (if-let [interval (:interval params)]
;     (when-not (valid-timeline-intervals interval)
;       [(str "Timeline interval is a required parameter for timeline search and must be one of"
;             " year, month, day, hour, minute, or second.")])
;     ["interval is a required parameter for timeline searches"]))
;
; (defn- assoc-keys->param-name
;   "Given a set of parameter assoc keys, returns the URL string for the parameter at that path.  For
;   instance, [:foo :bar :baz] returns \"foo[bar][baz]\""
;   [keys]
;   (let [[root & descendants] (map csk/->snake_case_string keys)
;         subscripts (s/join (map #(str "[" % "]") descendants))]
;     (str root subscripts)))
;
; (defn- validate-map
;   "Validates that the parameter value found by following keys is a map or null.  Dissocs the
;   parameter from params if it is invalid, returning [valid-params error-strings].
;   Examples:
;   => (validate-map [:parent :child] {:parent {:child {:gchild 0}}})
;   [{:parent {:child {:gchild 0}}} []]
;   => (validate-map [:parent :child] {:parent {:child 0}})
;   [{:parent {}} [\"Parameter [parent[child]] must contain a nested value, parent[child][...]=value.\"]]"
;   [keys params]
;   (let [value (get-in params keys)]
;     (if (or (nil? value) (map? value))
;       [params []]
;       (let [param-name (assoc-keys->param-name keys)]
;         [(util/dissoc-in params keys)
;          [(str "Parameter [" param-name "] must include a nested key, " param-name "[...]=value.")]]))))
;
; (defn- apply-type-validations
;   "Validates data types of parameters.  Returns a tuple of [safe-params errors] where errors
;   contains a list of type error strings and safe-params contains the original params with
;   error those that have type errors dissoc'ed out."
;   [params validation-functions]
;   (loop [[validation & validations] validation-functions
;          safe-params params
;          errors []]
;     (let [[new-safe-params new-errors] (validation safe-params)
;           all-errors (concat new-errors errors)]
;       (if (seq validations)
;         (recur validations new-safe-params all-errors)
;         [new-safe-params all-errors]))))
;
; (defn- validate-all-map-values
;   "Applies the validation function to all values in the map and aggregates the result.  Useful
;   for places like science keywords where we don't know all of the keys up front."
;   [validation-fn path params]
;   (let [entries (get-in params path)]
;     (if (seq entries)
;       (let [validations (map #(partial validation-fn (concat path [%])) (keys entries))]
;         (apply-type-validations params validations))
;       [params []])))
;
; (def parameter-validations
;   "A list of the functions that can validate parameters. They all accept parameters as an argument
;   and return a list of errors."
;   [single-value-validation
;    multiple-value-validation
;    page-size-validation
;    page-num-validation
;    paging-depth-validation
;    boosts-validation
;    sort-key-validation
;    unrecognized-params-validation
;    unrecognized-params-in-options-validation
;    parameter-options-validation
;    temporal-format-validation
;    updated-since-validation
;    revision-date-validation
;    orbit-number-validation
;    equator-crossing-longitude-validation
;    equator-crossing-date-validation
;    cloud-cover-validation
;    attribute-validation
;    science-keywords-validation
;    exclude-validation
;    boolean-value-validation
;    polygon-validation
;    bounding-box-validation
;    point-validation
;    line-validation
;    no-highlight-options-without-highlights-validation
;    highlights-numeric-options-validation
;    result-format-parameters-validation])
;
; (def standard-query-parameter-validations
;   "A list of functions that can validate the query parameters passed in with an AQL or JSON search.
;   They all accept parameters as an argument and return a list of errors."
;   [single-value-validation
;    page-size-validation
;    page-num-validation
;    paging-depth-validation
;    boosts-validation
;    sort-key-validation
;    unrecognized-standard-query-params-validation])
;
; (def timeline-parameter-validations
;   "A list of function that can validate timeline query parameters. It will only validate the timeline
;   parameters specifically. Parameter validation on the "
;   [timeline-start-date-validation
;    timeline-end-date-validation
;    timeline-interval-validation
;    timeline-range-validation])
;
; (def parameter-data-type-validations
;   "Validations of the data type of various parameters, used to ensure the data is the correct
;   shape before we manipulate it further."
;   [(partial validate-map [:options])
;    (partial validate-map [:options :entry-title])
;    (partial validate-map [:options :platform])
;    (partial validate-map [:options :instrument])
;    (partial validate-map [:options :sensor])
;    (partial validate-map [:options :project])
;    (partial validate-map [:options :attribute])
;    (partial validate-map [:exclude])
;    (partial validate-map [:science-keywords])
;    (partial validate-all-map-values validate-map [:science-keywords])])
;
; (defn validate-parameter-data-types
;   "Validates data types of parameters.  Unlike other validations, this returns a tuple of
;   [safe-params errors] where errors contains the usual list of errors and safe-params
;   contains only params whose data type is correct.  Dissoc'ing invalid data types from
;   the list allows other validations to make assumptions about their shapes / types."
;   [params]
;   (apply-type-validations params parameter-data-type-validations))
;
; (defn validate-parameters
;   "Validates parameters. Throws exceptions to send to the user. Returns parameters if validation
;   was successful so it can be chained with other calls."
;   [concept-type params]
;   (let [[safe-params type-errors] (validate-parameter-data-types params)
;         errors (concat type-errors
;                        (mapcat #(% concept-type safe-params) parameter-validations))]
;     (when (seq errors)
;       (errors/throw-service-errors :bad-request errors)))
;   params)
;
; (defn validate-standard-query-parameters
;   "Validates the query parameters passed in with an AQL or JSON search.
;   Throws exceptions to send to the user. Returns parameters if validation
;   was successful so it can be chained with other calls."
;   [concept-type params]
;   (let [errors (mapcat #(% concept-type params) standard-query-parameter-validations)]
;     (when (seq errors)
;       (errors/throw-service-errors :bad-request errors)))
;   params)
;
; (defn validate-timeline-parameters
;   "Validates the query parameters passed in with a timeline search.
;   Throws exceptions to send to the user. Returns parameters if validation
;   was successful so it can be chained with other calls."
;   [params]
;   (let [[safe-params type-errors] (validate-parameter-data-types params)
;         timeline-params (select-keys safe-params [:interval :start-date :end-date])
;         regular-params (dissoc safe-params :interval :start-date :end-date)
;         errors (concat type-errors
;                        (mapcat #(% :granule regular-params) parameter-validations)
;                        (mapcat #(% :granule timeline-params) timeline-parameter-validations))]
;     (when (seq errors)
;       (errors/throw-service-errors :bad-request errors)))
;   params)
;
; (def valid-tile-search-params
;   "Valid parameters for tile search"
;   #{:bounding-box
;     :line
;     :point
;     :polygon})
;
; (defn unrecognized-tile-params-validation
;   "Validates that no invalid parameters were supplied to tile search"
;   [params]
;   (map #(format "Parameter [%s] was not recognized." (csk/->snake_case_string %))
;        (set/difference (set (keys params)) valid-tile-search-params)))
;
; (defn validate-tile-parameters
;   "Validates the query parameters passed in with a tile search. Throws exceptions to send
;   to the user if a validation fails. Returns parameters if validation is successful."
;   [params]
;   (let [errors (mapcat #(% params)
;                        [unrecognized-tile-params-validation
;                         polygon-validation
;                         bounding-box-validation
;                         point-validation
;                         line-validation])]
;     (when (seq errors)
;       (errors/throw-service-errors :bad-request errors)))
;   params)
