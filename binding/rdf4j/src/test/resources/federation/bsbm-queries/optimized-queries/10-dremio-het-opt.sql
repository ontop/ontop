SELECT v1."deliverydays" AS "deliverydays1m33", v1."nr" AS "nr1m5", v1."price" AS "price1m34", v1."validto" AS "validto1m39", v1."vendor" AS "vendor1m8"
FROM "bsbm"."offer" v1, "bsbm"."vendor" v2
WHERE ((v1."validto" > CAST('2008-06-13' AS DATE)) AND (v1."deliverydays" <= 3) AND v1."deliverydays" IS NOT NULL AND v1."price" IS NOT NULL AND v1."validto" IS NOT NULL AND v1."nr" IS NOT NULL AND v1."vendor" = v2."nr" AND 94 = v1."product" AND 'US' = v2."country")
ORDER BY v1."price" NULLS FIRST
LIMIT 10