(ns reitit-example.core
  (:require [reitit.ring :as ring]
            [reitit.swagger :as swagger]
            [reitit.swagger-ui :as swagger-ui]
            [reitit.ring.coercion :as rrc]
            [reitit.coercion.spec :as spec]
            [reitit.coercion.schema :as schema]
            [schema.core :refer [Int]]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.params]
            [muuntaja.middleware]))

(def app
  (ring/ring-handler
   (ring/router
    ["/api"

     ["/swagger.json"
      {:get {:no-doc true
             :swagger {:info {:title "my-api"}}
             :handler (swagger/create-swagger-handler)}}]

     ["/spec"
      {:coercion spec/coercion
       :swagger {:tags ["spec"]}}

      ["/plus"
       {:get {:summary "plus with spec query parameters"
              :parameters {:query {:x int?, :y int?}}
              :responses {200 {:body {:total int?}}}
              :handler (fn [{{{:keys [x y]} :query} :parameters}]
                         {:status 200
                          :body {:total (+ x y)}})}
        :post {:summary "plus with spec body parameters"
               :parameters {:body {:x int?, :y int?}}
               :responses {200 {:body {:total int?}}}
               :handler (fn [{{{:keys [x y]} :body} :parameters}]
                          {:status 200
                           :body {:total (+ x y)}})}}]]

     ["/schema"
      {:coercion schema/coercion
       :swagger {:tags ["schema"]}}

      ["/plus"
       {:get {:summary "plus with schema query parameters"
              :parameters {:query {:x Int, :y Int}}
              :responses {200 {:body {:total Int}}}
              :handler (fn [{{{:keys [x y]} :query} :parameters}]
                         {:status 200
                          :body {:total (+ x y)}})}
        :post {:summary "plus with schema body parameters"
               :parameters {:body {:x Int, :y Int}}
               :responses {200 {:body {:total Int}}}
               :handler (fn [{{{:keys [x y]} :body} :parameters}]
                          {:status 200
                           :body {:total (+ x y)}})}}]]]

    {:data {:middleware [ring.middleware.params/wrap-params
                         muuntaja.middleware/wrap-format
                         swagger/swagger-feature
                         rrc/coerce-exceptions-middleware
                         rrc/coerce-request-middleware
                         rrc/coerce-response-middleware]
            :swagger {:produces #{"application/json"
                                  "application/edn"
                                  "application/transit+json"}
                      :consumes #{"application/json"
                                  "application/edn"
                                  "application/transit+json"}}}})
   (ring/routes
    (swagger-ui/create-swagger-ui-handler
     {:path "/", :url "/api/swagger.json"})
    (ring/create-default-handler))))

(defn start []
  (jetty/run-jetty #'app {:port 3000 :join? false}))

(defn stop [s]
  (.stop s))
