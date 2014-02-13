(ns heroku.mocks)
(def tenants-response
  {:success true, :tenants_links [], :tenants [{:description nil, :enabled true, :id "a5d2a0dec98e4f14b31557ddd88c6bf0", :name "admin"} {:description nil, :enabled true, :id "38d8f3599c134dee82aca6cb82586b08", :name "demo"}]})

(def tenants (:tenants tenants-response))

(def eps
  {:volumev2 {:name "cinderv2", :available-calls [], :id "485fed9caeaf4f79aefdaaa6bad7f2fa", :publicURL "http://192.168.1.11:8776/v2/a5d2a0dec98e4f14b31557ddd88c6bf0"}, :network {:name "neutron", :available-calls [{:url "/v2.0/networks", :id :networks} {:url "/v2.0/subnets", :id :subnets}], :id "7e6db2ae20ac49fbb4a671e7d20fee96", :publicURL "http://192.168.1.11:9696/"}, :compute {:name "nova", :available-calls [{:url "/images", :id :images} {:url "/flavors", :id :flavors} {:url "/servers", :id :servers}], :id "16f86997f9954a51a48c974d02638de3", :publicURL "http://192.168.1.11:8774/v2/a5d2a0dec98e4f14b31557ddd88c6bf0"}, :image {:name "glance", :available-calls [], :id "0a8dbffb0fa749b3961f32090e75f7a6", :publicURL "http://192.168.1.11:9292"}, :identity {:name "keystone", :available-calls [], :id "63da0f9a62e54edaa2b33bc76ccb9d6c", :publicURL "http://192.168.1.11:5000/v2.0"}, :ec2 {:name "ec2", :available-calls [], :id "032b9a6abf2a4c329523e444f2ca93b5", :publicURL "http://192.168.1.11:8773/services/Cloud"}, :s3 {:name "s3", :available-calls [], :id "41759f7fbdea4e8b8a44331cacbbc4d5", :publicURL "http://192.168.1.11:3333"}, :computev3 {:name "novav3", :available-calls [], :id "7e2e87ca711844aeb8546094a77a256c", :publicURL "http://192.168.1.11:8774/v3"}, :volume {:name "cinder", :available-calls [], :id "70d3f85f76d84ef1b590a544f45e22c3", :publicURL "http://192.168.1.11:8776/v1/a5d2a0dec98e4f14b31557ddd88c6bf0"}})

(comment {:content {:menu {}
                    :connections {:navbar {}
                                  :base {}
                                  :tenant {}}
                    :endpoints {}
                    :tenants {}
                    :service {}}})
