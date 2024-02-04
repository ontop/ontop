SELECT DISTINCT v9."nr1m5" AS "nr1m5", v9."nr20m18" AS "nr20m18", v9."price1m34" AS "price1m34"
FROM ((SELECT v2."deliverydays" AS "deliverydays1m33", v2."nr" AS "nr1m5", v1."nr" AS "nr20m18", v2."price" AS "price1m34", v2."validto" AS "validto1m39"
FROM "product1" v1, "offer" v2, "vendor" v3
WHERE ((v2."deliverydays" <= 3) AND (1 <= v1."nr") AND (v1."nr" <= 500) AND v2."deliverydays" IS NOT NULL AND v2."price" IS NOT NULL AND v2."validto" IS NOT NULL AND v1."nr" = v2."product" AND v2."vendor" = v3."nr" AND 'US' = v3."country")
)UNION ALL 
(SELECT v6."deliverydays" AS "deliverydays1m33", v6."nr" AS "nr1m5", v5."nr" AS "nr20m18", v6."price" AS "price1m34", v6."validto" AS "validto1m39"
FROM "product2" v5, "offer" v6, "vendor" v7
WHERE ((v6."deliverydays" <= 3) AND (1 <= v5."nr") AND (v5."nr" <= 500) AND v6."deliverydays" IS NOT NULL AND v6."price" IS NOT NULL AND v6."validto" IS NOT NULL AND v5."nr" = v6."product" AND v6."vendor" = v7."nr" AND 'US' = v7."country")
)) v9
