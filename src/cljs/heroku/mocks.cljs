(ns heroku.mocks)
(def tenants-response
  {:success true, :tenants_links [], :tenants [{:description nil, :enabled true, :id "a5d2a0dec98e4f14b31557ddd88c6bf0", :name "admin"} {:description nil, :enabled true, :id "38d8f3599c134dee82aca6cb82586b08", :name "demo"}]})

(def tenants (:tenants tenants-response))

(def eps
  {:volumev2 {:name "cinderv2", :available-calls [], :id "485fed9caeaf4f79aefdaaa6bad7f2fa", :publicURL "http://192.168.1.11:8776/v2/a5d2a0dec98e4f14b31557ddd88c6bf0"}, :network {:name "neutron", :available-calls [{:url "/v2.0/networks", :id :networks} {:url "/v2.0/subnets", :id :subnets}], :id "7e6db2ae20ac49fbb4a671e7d20fee96", :publicURL "http://192.168.1.11:9696/"}, :compute {:name "nova", :available-calls [{:url "/images", :id :images} {:url "/flavors", :id :flavors} {:url "/servers", :id :servers}], :id "16f86997f9954a51a48c974d02638de3", :publicURL "http://192.168.1.11:8774/v2/a5d2a0dec98e4f14b31557ddd88c6bf0"}, :image {:name "glance", :available-calls [], :id "0a8dbffb0fa749b3961f32090e75f7a6", :publicURL "http://192.168.1.11:9292"}, :identity {:name "keystone", :available-calls [], :id "63da0f9a62e54edaa2b33bc76ccb9d6c", :publicURL "http://192.168.1.11:5000/v2.0"}, :ec2 {:name "ec2", :available-calls [], :id "032b9a6abf2a4c329523e444f2ca93b5", :publicURL "http://192.168.1.11:8773/services/Cloud"}, :s3 {:name "s3", :available-calls [], :id "41759f7fbdea4e8b8a44331cacbbc4d5", :publicURL "http://192.168.1.11:3333"}, :computev3 {:name "novav3", :available-calls [], :id "7e2e87ca711844aeb8546094a77a256c", :publicURL "http://192.168.1.11:8774/v3"}, :volume {:name "cinder", :available-calls [], :id "70d3f85f76d84ef1b590a544f45e22c3", :publicURL "http://192.168.1.11:8776/v1/a5d2a0dec98e4f14b31557ddd88c6bf0"}})

(def images
  [{:id "fcaea71a-97b0-4553-b931-7acaa07ab1eb",
    :links
    [{:href "http://192.168.1.23:8774/v2/542461de33ee417aae7358f204764f47/images/fcaea71a-97b0-4553-b931-7acaa07ab1eb", :rel "self"}
     {:href "http://192.168.1.23:8774/542461de33ee417aae7358f204764f47/images/fcaea71a-97b0-4553-b931-7acaa07ab1eb", :rel "bookmark"}
     {:href "http://192.168.1.23:9292/542461de33ee417aae7358f204764f47/images/fcaea71a-97b0-4553-b931-7acaa07ab1eb", :type "application/vnd.openstack.image", :rel "alternate"}],
    :name "cirros-0.3.1-x86_64-uec"} {:id "fbad4512-95c2-4a8f-acba-63c0aad3752c", :links [{:href "http://192.168.1.23:8774/v2/542461de33ee417aae7358f204764f47/images/fbad4512-95c2-4a8f-acba-63c0aad3752c", :rel "self"} {:href "http://192.168.1.23:8774/542461de33ee417aae7358f204764f47/images/fbad4512-95c2-4a8f-acba-63c0aad3752c", :rel "bookmark"} {:href "http://192.168.1.23:9292/542461de33ee417aae7358f204764f47/images/fbad4512-95c2-4a8f-acba-63c0aad3752c", :type "application/vnd.openstack.image", :rel "alternate"}], :name "cirros-0.3.1-x86_64-uec-ramdisk"} {:id "2275c088-b5a7-4b7a-9d06-c681eaba80eb", :links [{:href "http://192.168.1.23:8774/v2/542461de33ee417aae7358f204764f47/images/2275c088-b5a7-4b7a-9d06-c681eaba80eb", :rel "self"} {:href "http://192.168.1.23:8774/542461de33ee417aae7358f204764f47/images/2275c088-b5a7-4b7a-9d06-c681eaba80eb", :rel "bookmark"} {:href "http://192.168.1.23:9292/542461de33ee417aae7358f204764f47/images/2275c088-b5a7-4b7a-9d06-c681eaba80eb", :type "application/vnd.openstack.image", :rel "alternate"}], :name "cirros-0.3.1-x86_64-uec-kernel"}])


(def flavors
  [{:id "1", :links [{:href "http://192.168.1.23:8774/v2/542461de33ee417aae7358f204764f47/flavors/1", :rel "self"} {:href "http://192.168.1.23:8774/542461de33ee417aae7358f204764f47/flavors/1", :rel "bookmark"}], :name "m1.tiny"} {:id "2", :links [{:href "http://192.168.1.23:8774/v2/542461de33ee417aae7358f204764f47/flavors/2", :rel "self"} {:href "http://192.168.1.23:8774/542461de33ee417aae7358f204764f47/flavors/2", :rel "bookmark"}], :name "m1.small"} {:id "3", :links [{:href "http://192.168.1.23:8774/v2/542461de33ee417aae7358f204764f47/flavors/3", :rel "self"} {:href "http://192.168.1.23:8774/542461de33ee417aae7358f204764f47/flavors/3", :rel "bookmark"}], :name "m1.medium"} {:id "4", :links [{:href "http://192.168.1.23:8774/v2/542461de33ee417aae7358f204764f47/flavors/4", :rel "self"} {:href "http://192.168.1.23:8774/542461de33ee417aae7358f204764f47/flavors/4", :rel "bookmark"}], :name "m1.large"} {:id "42", :links [{:href "http://192.168.1.23:8774/v2/542461de33ee417aae7358f204764f47/flavors/42", :rel "self"} {:href "http://192.168.1.23:8774/542461de33ee417aae7358f204764f47/flavors/42", :rel "bookmark"}], :name "m1.nano"} {:id "5", :links [{:href "http://192.168.1.23:8774/v2/542461de33ee417aae7358f204764f47/flavors/5", :rel "self"} {:href "http://192.168.1.23:8774/542461de33ee417aae7358f204764f47/flavors/5", :rel "bookmark"}], :name "m1.xlarge"} {:id "84", :links [{:href "http://192.168.1.23:8774/v2/542461de33ee417aae7358f204764f47/flavors/84", :rel "self"} {:href "http://192.168.1.23:8774/542461de33ee417aae7358f204764f47/flavors/84", :rel "bookmark"}], :name "m1.micro"}])



(def networks
  [{:status "ACTIVE",
    :provider:network_type "local",
    :provider:physical_network nil,
    :router:external true,
    :admin_state_up true,
    :name "public",
    :subnets ["52c536a1-6bee-4b87-892c-14871423335d"], :shared false, :tenant_id "542461de33ee417aae7358f204764f47", :provider:segmentation_id nil, :id "dafc72f2-1395-4913-a376-d512801447db"} {:status "ACTIVE", :provider:network_type "local", :provider:physical_network nil, :router:external false, :admin_state_up true, :name "private", :subnets ["d45a6bbe-fd3d-450b-a96f-3e13a4d27048"], :shared false, :tenant_id "2a4e13a9da1e43b0bf3cb16b3e0e2aeb", :provider:segmentation_id nil, :id "f9661eaa-39ee-4ddb-8693-05efd1a92136"}])


(def subnets
  [{:host_routes [], :enable_dhcp false, :ip_version 4, :name "public-subnet", :dns_nameservers [], :gateway_ip "172.24.4.1", :tenant_id "542461de33ee417aae7358f204764f47", :allocation_pools [{:start "172.24.4.2", :end "172.24.4.254"}], :network_id "dafc72f2-1395-4913-a376-d512801447db", :cidr "172.24.4.0/24", :id "52c536a1-6bee-4b87-892c-14871423335d"} {:host_routes [], :enable_dhcp true, :ip_version 4, :name "private-subnet", :dns_nameservers [], :gateway_ip "10.0.0.1", :tenant_id "2a4e13a9da1e43b0bf3cb16b3e0e2aeb", :allocation_pools [{:start "10.0.0.2", :end "10.0.0.254"}], :network_id "f9661eaa-39ee-4ddb-8693-05efd1a92136", :cidr "10.0.0.0/24", :id "d45a6bbe-fd3d-450b-a96f-3e13a4d27048"}])


(def server-created
  {
  :security_groups [
    {
      :name "default"
    }
  ]
  "OS-DCF:diskConfig" "MANUAL"
  :id "ca7da24f-98a2-4de5-9a66-e94cc4ed5c1f"
  :links [
    {
      :href "http://8.21.28.222:8774/v2/da05a30dff7746b9a20027a68cfe6076/servers/ca7da24f-98a2-4de5-9a66-e94cc4ed5c1f"
      :rel "self"
    }
    {
      :href "http://8.21.28.222:8774/da05a30dff7746b9a20027a68cfe6076/servers/ca7da24f-98a2-4de5-9a66-e94cc4ed5c1f"
      :rel "bookmark"
    }
  ]
   :adminPass "a5hzY25UHoS5"
})


(def create-server
  {:flavors flavors :networks networks :images images }
  )

(comment {:content {:menu {}
                    :connections {:navbar {}
                                  :base {}
                                  :tenant {}}
                    :endpoints {}
                    :tenants {}
                    :service {}}})
