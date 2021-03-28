(ns stock-levels.core
  (:require
   [reagent.core :as reagent :refer [atom]]
   [reagent.dom :as rdom]
   [reagent.session :as session]
   [reitit.frontend :as reitit]
   [clerk.core :as clerk]
   [accountant.core :as accountant]
   [clojure.string :as string]))

;; -------------------------
;; Routes

(def router
  (reitit/router
   [["/" :index]
    ["/items"
     ["" :items]
     ["/:item-id" :item]]
    ["/about" :about]]))

(defn path-for [route & [params]]
  (if params
    (:path (reitit/match-by-name router route params))
    (:path (reitit/match-by-name router route))))

;; -------------------------
;; Page components

(def app-state (reagent/atom {:item-stock-list [{:item-id "A" :quantity 1} {:item-id "B" :quantity 1} {:item-id "C" :quantity 2}],
                              :instruction-line "Sample instruction"})) ;; Added this to avoid applying on-change, but it ended up not being required. Still, left it there as sign of the learning process

(defn get-item-stock-list! []
  (:item-stock-list @app-state))

(defn get-instruction-line! []
  (:instruction-line @app-state))

(defn update-stock! [f & args]
  (apply swap! app-state update-in [:item-stock-list] f args))

;; contains only works on keys, so had to make it into a set (?)
(defn contains-item [item-id]
  (contains? (set (map :item-id (get-item-stock-list!))) item-id))

(defn add-item [{:keys [item-id quantity] :as item}]
  (update-stock! conj item))

(defn set-stock! [{:keys [item-id quantity] :as update}]
  (update-stock! (fn [item-stock-list]
                       (vec (map #(if (= (:item-id %) item-id)
                                    (assoc % :quantity quantity) %)
                                  item-stock-list)))
                    update))

(defn add-stock! [{:keys [item-id quantity] :as update}]
  (update-stock! (fn [item-stock-list]
                       (vec (map #(if (= (:item-id %) item-id)
                                    (update-in % [:quantity] + quantity) %)
                                  item-stock-list)))
                    update))

(defn order-stock! [{:keys [item-id quantity] :as update}]
  (update-stock! (fn [item-stock-list]
                       (vec (map #(if (= (:item-id %) item-id)
                                    (update-in % [:quantity] - quantity) %)
                                  item-stock-list)))
                    update))

;; wanted to import the file, but couldn't get the Blob to read in read-file
(defn atom-file [file]
  [:input {:type "file"
           :value @file
           :on-change #(reset! file (-> % .-target .-value))}])

(defn instruction-line [line]
  [:input {:type "text"
           :value @line
           :on-change #(reset! line (-> % .-target .-value))}])

(defn submit-instruction! [instruction]
  (swap! app-state assoc :instruction-line instruction))

;; (defn read-file [event]
;;   (let [reader (js/FileReader.)]
;;     #(reset! event (-> % .-target .-value))
;;     (println (.readAsText reader (-> event .-target .-files (first))))))

(defn submit-file [file]
  ;; (let [reader (js/FileReader.)]
  ;;   (.readAsText reader file)
    (println "Submitted!" file))

(defn item [{:keys [item-id quantity]}]
  [:li {:name item-id :key item-id}
   [:span (str item-id ": " quantity)]])

(defn apply-set-instruction-line [items]
  (when-not (empty? items) (let [item-id (first items) quantity (second items)]
    (cond
      (contains-item item-id) (set-stock! {:item-id item-id :quantity (int quantity)})
      :else (add-item {:item-id item-id :quantity (int quantity)})
    )
    (recur (drop 2 items))
  )))

(defn apply-add-instruction-line [items]
  (when-not (empty? items) (let [item-id (first items) quantity (second items)]
    (cond
      (contains-item item-id) (add-stock! {:item-id item-id :quantity (int quantity)})
      :else (add-item {:item-id item-id :quantity (int quantity)})
    )
    (recur (drop 2 items))
  )))

;; could we use when-let instead of when-not empty?
(defn apply-order-instruction-line [items]
  (when-not (empty? items) (let [item-id (first items) quantity (second items)]
    (order-stock! {:item-id item-id :quantity (int quantity)})
    (recur (drop 2 items))
  )))

(defn get-instruction-words []
  (string/split (get-instruction-line!) #"\s+")
)

(defn get-instruction-type []
  (first (get-instruction-words))
)

(defn apply-instruction-line []
  (let [instruction (get-instruction-type)]
    (cond
      (= instruction "set-stock") (apply-set-instruction-line (rest (get-instruction-words)))
      (= instruction "add-stock") (apply-add-instruction-line (rest (get-instruction-words)))
      (= instruction "order") (apply-order-instruction-line (drop 2 (get-instruction-words)))
    )))

;; Couldn't read file so this is what we get
(defn instruction-feeder []
  (let [line (reagent/atom "")]
    (fn []
      [:div
        [instruction-line line]
        [:input {:type "button" :value "submit instruction" :on-click #(submit-instruction! @line)}]
        [:input {:type "button" :value "apply" :on-click #(apply-instruction-line)}]
        [:p (str (get-instruction-line!))]
      ]
    )))

;; testing UI
(defn main []
  (let [file (reagent/atom "")]
    (fn []
      [:div
        [atom-file file]
        [:input {:type "button" :value "submit" :on-click #(submit-file file)}]
        [:input {:type "button" :value "increment stock" :on-click #(add-stock! {:item-id "A" :quantity 1})}]
        [:input {:type "button" :value "set stock" :on-click #(set-stock! {:item-id "A" :quantity 1})}]
        [:input {:type "button" :value "sub stock" :on-click #(order-stock! {:item-id "A" :quantity 1})}]
        [:h4 "Item Stock"]
        [:ul {:id "stock-list-items"}
          (for [i (:item-stock-list @app-state)]
            [item i])]])))

(defn home-page []
  (fn []
    [:span.main
      [:h1 "Stock Management"]
      [:h5 "Import instruction feed:"]
      [:div
        [instruction-feeder]
        [main]
      ]
    ]
  ))

;; Generated by Reagent
;; ------------------------

(defn items-page []
  (fn []
    [:span.main
     [:h1 "The items of stock-levels"]
     [:ul {:id "stock-list-items"} (map (fn [item-id]
                 [:li {:name (str "item-" item-id) :key (str "item-" item-id)}
                  [:a {:href (path-for :item {:item-id item-id})} "Item: " item-id]])
               (range 1 60))]]))

(defn item-page []
  (fn []
    (let [routing-data (session/get :route)
          item (get-in routing-data [:route-params :item-id])]
      [:span.main
       [:h1 (str "Item " item " of stock-levels")]
       [:p [:a {:href (path-for :items)} "Back to the list of items"]]])))

(defn about-page []
  (fn [] [:span.main
          [:h1 "About stock-levels"]]))

;; -------------------------
;; Translate routes -> page components

(defn page-for [route]
  (case route
    :index #'home-page
    :about #'about-page
    :items #'items-page
    :item #'item-page))


;; -------------------------
;; Page mounting component

(defn current-page []
  (fn []
    (let [page (:current-page (session/get :route))]
      [:div
       [:header
        [:p [:a {:href (path-for :index)} "Home"] " | "
         [:a {:href (path-for :about)} "About stock-levels"]]]
       [page]
       [:footer
        [:p "stock-levels was generated by the "
         [:a {:href "https://github.com/reagent-project/reagent-template"} "Reagent Template"] "."]]])))

;; -------------------------
;; Initialize app

(defn mount-root []
  (rdom/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (clerk/initialize!)
  (accountant/configure-navigation!
   {:nav-handler
    (fn [path]
      (let [match (reitit/match-by-path router path)
            current-page (:name (:data  match))
            route-params (:path-params match)]
        (reagent/after-render clerk/after-render!)
        (session/put! :route {:current-page (page-for current-page)
                              :route-params route-params})
        (clerk/navigate-page! path)
        ))
    :path-exists?
    (fn [path]
      (boolean (reitit/match-by-path router path)))})
  (accountant/dispatch-current!)
  (mount-root))

