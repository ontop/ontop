-- ${1:product.nr:none}
-- ${1:vendor.country:none}

SELECT DISTINCT v9."nr1m5" AS "nr1m5", v9."product1m5" AS "product1m5"
FROM (SELECT v2."deliverydays" AS "deliverydays1m37", v2."nr" AS "nr1m5", v2."price" AS "price1m39", v1."nr" AS "product1m5", v2."validto" AS "validto1m45"
FROM "ss1"."product1" v1, "ss4"."offer" v2, "ss4"."vendor" v3
WHERE ((v2."deliverydays" <= 3) AND (v1."nr" <= ${1:product.nr:none}) AND v2."deliverydays" IS NOT NULL AND v2."price" IS NOT NULL AND v2."validto" IS NOT NULL AND v1."nr" = v2."product" AND v2."vendor" = v3."nr" AND '${1:vendor.country:none}' = v3."country")
UNION ALL
SELECT v6."deliverydays" AS "deliverydays1m37", v6."nr" AS "nr1m5", v6."price" AS "price1m39", v5."nr" AS "product1m5", v6."validto" AS "validto1m45"
FROM "ss5"."product2" v5, "ss4"."offer" v6, "ss4"."vendor" v7
WHERE ((v6."deliverydays" <= 3) AND (v5."nr" <= ${1:product.nr:none}) AND v6."deliverydays" IS NOT NULL AND v6."price" IS NOT NULL AND v6."validto" IS NOT NULL AND v5."nr" = v6."product" AND v6."vendor" = v7."nr" AND '${1:vendor.country:none}' = v7."country")
) v9
