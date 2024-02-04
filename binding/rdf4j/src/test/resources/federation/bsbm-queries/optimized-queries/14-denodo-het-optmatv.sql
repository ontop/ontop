SELECT DISTINCT v7."nr1m5" AS "nr1m5", v7."nr20m18" AS "nr20m18", v7."price1m34" AS "price1m34"
FROM ((SELECT v1."1_7" AS "deliverydays1m33", v1."1_0" AS "nr1m5", v1."1_1" AS "nr20m18", v1."1_4" AS "price1m34", v1."1_6" AS "validto1m39"
FROM "matv_s4_offer_s1_product1" v1, "vendor" v2
WHERE ((v1."1_7" <= 3) AND (1 <= v1."1_1") AND (v1."1_1" <= 500) AND v1."1_7" IS NOT NULL AND v1."1_4" IS NOT NULL AND v1."1_6" IS NOT NULL AND v1."1_0" IS NOT NULL AND v1."1_3" = v2."nr" AND 'US' = v2."country")
)UNION ALL 
(SELECT v4."1_7" AS "deliverydays1m33", v4."1_0" AS "nr1m5", v4."1_1" AS "nr20m18", v4."1_4" AS "price1m34", v4."1_6" AS "validto1m39"
FROM "matv_s4_offer_s5_product2" v4, "vendor" v5
WHERE ((v4."1_7" <= 3) AND (1 <= v4."1_1") AND (v4."1_1" <= 500) AND v4."1_7" IS NOT NULL AND v4."1_4" IS NOT NULL AND v4."1_6" IS NOT NULL AND v4."1_0" IS NOT NULL AND v4."1_3" = v5."nr" AND 'US' = v5."country")
)) v7
