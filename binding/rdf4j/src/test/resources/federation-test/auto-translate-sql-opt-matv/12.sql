SELECT v9."deliverydays1m37" AS "deliverydays1m37", v9."label10m4" AS "label10m4", v9."label10m46" AS "label10m46", v9."nr1m21" AS "nr1m21", v9."offerwebpage1m24" AS "offerwebpage1m24", v9."price1m39" AS "price1m39", v9."product1m5" AS "product1m5", v9."validto1m45" AS "validto1m45", v9."vendor1m8" AS "vendor1m8"
FROM (SELECT DISTINCT v7."deliverydays1m37" AS "deliverydays1m37", v7."homepage2m48" AS "homepage2m48", v7."label10m4" AS "label10m4", v7."label10m46" AS "label10m46", v7."nr1m21" AS "nr1m21", v7."offerwebpage1m24" AS "offerwebpage1m24", v7."price1m39" AS "price1m39", v7."product1m5" AS "product1m5", v7."validto1m45" AS "validto1m45", v7."vendor1m8" AS "vendor1m8"
FROM ((SELECT v1."2_7" AS "deliverydays1m37", v2."homepage" AS "homepage2m48", v2."label" AS "label10m4", v1."1_1" AS "label10m46", v1."2_0" AS "nr1m21", v1."2_8" AS "offerwebpage1m24", v1."2_4" AS "price1m39", v1."1_0" AS "product1m5", v1."2_6" AS "validto1m45", v1."2_3" AS "vendor1m8"
FROM "smatv"."MatV_ss1_product1_ss4_offer" v1, "ss4"."vendor" v2
WHERE (v1."1_1" IS NOT NULL AND (v1."2_0" < 1000) AND v2."label" IS NOT NULL AND v2."homepage" IS NOT NULL AND v1."2_8" IS NOT NULL AND v1."2_4" IS NOT NULL AND v1."2_7" IS NOT NULL AND v1."2_6" IS NOT NULL AND v1."2_3" = v2."nr")
)UNION ALL
(SELECT v4."2_7" AS "deliverydays1m37", v5."homepage" AS "homepage2m48", v5."label" AS "label10m4", v4."1_1" AS "label10m46", v4."2_0" AS "nr1m21", v4."2_8" AS "offerwebpage1m24", v4."2_4" AS "price1m39", v4."1_0" AS "product1m5", v4."2_6" AS "validto1m45", v4."2_3" AS "vendor1m8"
FROM "smatv"."MatV_ss5_product2_ss4_offer" v4, "ss4"."vendor" v5
WHERE (v4."1_1" IS NOT NULL AND (v4."2_0" < 1000) AND v5."label" IS NOT NULL AND v5."homepage" IS NOT NULL AND v4."2_8" IS NOT NULL AND v4."2_4" IS NOT NULL AND v4."2_7" IS NOT NULL AND v4."2_6" IS NOT NULL AND v4."2_3" = v5."nr")
)) v7
) v9