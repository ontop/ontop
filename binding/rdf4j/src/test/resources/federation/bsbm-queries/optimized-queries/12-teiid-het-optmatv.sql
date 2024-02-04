SELECT DISTINCT v7."deliverydays1m33" AS "deliverydays1m33", v7."homepage2m42" AS "homepage2m42", v7."label10m4" AS "label10m4", v7."label10m40" AS "label10m40", v7."offerwebpage1m21" AS "offerwebpage1m21", v7."price1m34" AS "price1m34", v7."product1m5" AS "product1m5", v7."validto1m39" AS "validto1m39", v7."vendor1m8" AS "vendor1m8"
FROM ((SELECT v1."1_7" AS "deliverydays1m33", v2."homepage" AS "homepage2m42", v2."label" AS "label10m4", v1."2_1" AS "label10m40", v1."1_8" AS "offerwebpage1m21", v1."1_4" AS "price1m34", v1."1_1" AS "product1m5", v1."1_6" AS "validto1m39", v1."1_3" AS "vendor1m8"
FROM "smatv"."matv_s4_offer_s1_product1" v1, "s4"."vendor" v2
WHERE (v1."2_1" IS NOT NULL AND v2."label" IS NOT NULL AND v2."homepage" IS NOT NULL AND v1."1_8" IS NOT NULL AND v1."1_4" IS NOT NULL AND v1."1_7" IS NOT NULL AND v1."1_6" IS NOT NULL AND v1."1_3" = v2."nr" AND 2838 = v1."1_0")
)UNION ALL 
(SELECT v4."1_7" AS "deliverydays1m33", v5."homepage" AS "homepage2m42", v5."label" AS "label10m4", v4."2_1" AS "label10m40", v4."1_8" AS "offerwebpage1m21", v4."1_4" AS "price1m34", v4."1_1" AS "product1m5", v4."1_6" AS "validto1m39", v4."1_3" AS "vendor1m8"
FROM "smatv"."matv_s4_offer_s5_product2" v4, "s4"."vendor" v5
WHERE (v4."2_1" IS NOT NULL AND v5."label" IS NOT NULL AND v5."homepage" IS NOT NULL AND v4."1_8" IS NOT NULL AND v4."1_4" IS NOT NULL AND v4."1_7" IS NOT NULL AND v4."1_6" IS NOT NULL AND v4."1_3" = v5."nr" AND 2838 = v4."1_0")
)) v7
