SELECT DISTINCT v7."nr1m5" AS "nr1m5", v7."product1m5" AS "product1m5"
FROM ((SELECT v1."2_7" AS "deliverydays1m37", v1."2_0" AS "nr1m5", v1."2_4" AS "price1m39", v1."1_0" AS "product1m5", v1."2_6" AS "validto1m45"
FROM "smatv"."MatV_ss1_product1_ss4_offer" v1, "ss4"."vendor" v2
WHERE ((v1."2_6" > '1988-01-01') AND (v1."2_7" <= 3) AND (v1."1_0" < 100) AND v1."2_7" IS NOT NULL AND v1."2_4" IS NOT NULL AND v1."2_6" IS NOT NULL AND v1."2_3" = v2."nr" AND 'US' = v2."country")
)UNION ALL
(SELECT v4."2_7" AS "deliverydays1m37", v4."2_0" AS "nr1m5", v4."2_4" AS "price1m39", v4."1_0" AS "product1m5", v4."2_6" AS "validto1m45"
FROM "smatv"."MatV_ss5_product2_ss4_offer" v4, "ss4"."vendor" v5
WHERE ((v4."2_6" > '1988-01-01') AND (v4."2_7" <= 3) AND (v4."1_0" < 100) AND v4."2_7" IS NOT NULL AND v4."2_4" IS NOT NULL AND v4."2_6" IS NOT NULL AND v4."2_3" = v5."nr" AND 'US' = v5."country")
)) v7