(ns gyotaku.app-test
  (:require [cljs.test :refer [deftest is testing]]
            [re-frame.core :as rf]
            [gyotaku.app :as app]))

(deftest default-db-matches-original-scaffold-text
  (testing "app-db data holds the exact heading/description text App.svelte
            used to render as markup literals, before the migration"
    (is (= "etzhayyim-wasm-gyotaku-i3zinrs2" (:page/heading app/default-db)))
    (is (= "ClojureScript entry scaffold (reagent + re-frame + jp-go-dds) after the Svelte migration."
           (:page/description app/default-db)))))

(deftest initialize-db-event-sets-heading-sub
  (testing "dispatching the :initialize-db reg-event-db handler makes the
            :page/heading reg-sub resolve to default-db's value"
    (rf/dispatch-sync [:initialize-db])
    (is (= (:page/heading app/default-db) @(rf/subscribe [:page/heading])))))

(deftest initialize-db-event-sets-description-sub
  (testing "same, for :page/description"
    (rf/dispatch-sync [:initialize-db])
    (is (= (:page/description app/default-db) @(rf/subscribe [:page/description])))))

(deftest initialize-db-is-idempotent
  (testing "dispatching :initialize-db twice leaves subs unchanged"
    (rf/dispatch-sync [:initialize-db])
    (rf/dispatch-sync [:initialize-db])
    (is (= (:page/heading app/default-db) @(rf/subscribe [:page/heading])))
    (is (= (:page/description app/default-db) @(rf/subscribe [:page/description])))))
