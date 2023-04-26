-- ${1:offer.nr:none}

SELECT v11."deliverydays1m37" AS "deliverydays1m37", v11."label10m4" AS "label10m4", v11."label10m46" AS "label10m46", v11."nr1m21" AS "nr1m21", v11."offerwebpage1m24" AS "offerwebpage1m24", v11."price1m39" AS "price1m39", v11."product1m5" AS "product1m5", v11."validto1m45" AS "validto1m45", v11."vendor1m8" AS "vendor1m8"
FROM (
         SELECT DISTINCT v9."deliverydays1m37" AS "deliverydays1m37", v9."homepage2m48" AS "homepage2m48",
                         v9."label10m4" AS "label10m4", v9."label10m46" AS "label10m46", v9."nr1m21" AS "nr1m21",
                         v9."offerwebpage1m24" AS "offerwebpage1m24", v9."price1m39" AS "price1m39", v9."product1m5" AS "product1m5",
                         v9."validto1m45" AS "validto1m45", v9."vendor1m8" AS "vendor1m8"
         FROM (
                  SELECT v2."deliverydays" AS "deliverydays1m37", v3."homepage" AS "homepage2m48", v3."label" AS "label10m4",
                         v1."label" AS "label10m46", v2."nr" AS "nr1m21", v2."offerwebpage" AS "offerwebpage1m24",
                         v2."price" AS "price1m39", v1."nr" AS "product1m5", v2."validto" AS "validto1m45", v2."vendor" AS "vendor1m8"
                  FROM "ss1"."product1" v1, "ss4"."offer" v2, "ss4"."vendor" v3
                  WHERE (v1."label" IS NOT NULL AND (v2."nr" <= ${1:offer.nr:none}) AND v3."label" IS NOT NULL AND v3."homepage" IS NOT NULL AND v2."offerwebpage" IS NOT NULL AND v2."price" IS NOT NULL AND v2."deliverydays" IS NOT NULL AND v2."validto" IS NOT NULL AND v1."nr" = v2."product" AND v2."vendor" = v3."nr")
                  UNION ALL
                  SELECT v6."deliverydays" AS "deliverydays1m37", v7."homepage" AS "homepage2m48", v7."label" AS "label10m4",
                         v5."label" AS "label10m46", v6."nr" AS "nr1m21", v6."offerwebpage" AS "offerwebpage1m24",
                         v6."price" AS "price1m39", v5."nr" AS "product1m5", v6."validto" AS "validto1m45", v6."vendor" AS "vendor1m8"
                  FROM "ss5"."product2" v5, "ss4"."offer" v6, "ss4"."vendor" v7
                  WHERE (v5."label" IS NOT NULL AND (v6."nr" <= ${1:offer.nr:none}) AND v7."label" IS NOT NULL AND v7."homepage" IS NOT NULL AND v6."offerwebpage" IS NOT NULL AND v6."price" IS NOT NULL AND v6."deliverydays" IS NOT NULL AND v6."validto" IS NOT NULL AND v5."nr" = v6."product" AND v6."vendor" = v7."nr")
              ) v9
     ) v11
