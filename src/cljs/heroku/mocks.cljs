(ns heroku.mocks)
(def tenants-response
  {:success true, :tenants_links [], :tenants [{:description nil, :enabled true, :id "a5d2a0dec98e4f14b31557ddd88c6bf0", :name "admin"} {:description nil, :enabled true, :id "38d8f3599c134dee82aca6cb82586b08", :name "demo"}]})

(def tenants (:tenants tenants-response))
