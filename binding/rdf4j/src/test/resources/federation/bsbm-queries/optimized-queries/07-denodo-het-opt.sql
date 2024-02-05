SELECT DISTINCT v9."label10m4" AS "label10m4", v5."label10m40" AS "label10m40", v13."name1m12" AS "name1m12", v13."nr0m4" AS "nr0m4", v9."nr1m5" AS "nr1m5", v13."person2m7" AS "person2m7", v9."price1m34" AS "price1m34", v13."rating1m16" AS "rating1m16", v13."rating1m17" AS "rating1m17", v13."title2m11" AS "title2m11", v9."validto1m39" AS "validto1m39", v9."vendor1m8" AS "vendor1m8"
FROM ((SELECT v1."label" AS "label10m40"
FROM "product1" v1
WHERE (v1."label" IS NOT NULL AND 94 = v1."nr")
)UNION ALL 
(SELECT v3."label" AS "label10m40"
FROM "product2" v3
WHERE (v3."label" IS NOT NULL AND 94 = v3."nr")
)) v5
 LEFT OUTER JOIN 
(SELECT v7."label" AS "label10m4", v6."nr" AS "nr1m5", v6."price" AS "price1m34", v6."product" AS "v3", v7."country" AS "v4", v6."validto" AS "validto1m39", v7."nr" AS "vendor1m2", v6."vendor" AS "vendor1m8"
FROM "offer" v6, "vendor" v7
WHERE ((v6."validto" > '2008-06-13') AND v6."price" IS NOT NULL AND v7."label" IS NOT NULL AND v6."validto" IS NOT NULL AND v6."nr" IS NOT NULL AND v6."vendor" = v7."nr" AND 94 = v6."product" AND 'DE' = v7."country")
) v9 ON 1 = 1 
 LEFT OUTER JOIN 
(SELECT v11."name" AS "name1m12", v10."nr" AS "nr0m4", v11."nr" AS "person2m5", v10."person" AS "person2m7", v10."rating1" AS "rating1m16", v10."rating2" AS "rating1m17", v10."title" AS "title2m11", v10."product" AS "v6"
FROM "review" v10, "person" v11
WHERE (v11."name" IS NOT NULL AND v10."title" IS NOT NULL AND v10."person" = v11."nr" AND 94 = v10."product")
) v13 ON 1 = 1 
