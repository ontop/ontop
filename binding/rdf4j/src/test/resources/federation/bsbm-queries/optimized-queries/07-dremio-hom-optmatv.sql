SELECT DISTINCT v11."label10m4" AS "label10m4", v11."label10m40" AS "label10m40", v15."name1m12" AS "name1m12", v15."nr0m4" AS "nr0m4", v11."nr1m5" AS "nr1m5", v15."person2m7" AS "person2m7", v11."price1m34" AS "price1m34", v15."rating1m16" AS "rating1m16", v15."rating1m17" AS "rating1m17", v15."title2m11" AS "title2m11", v11."validto1m39" AS "validto1m39", v11."vendor1m8" AS "vendor1m8"
FROM (SELECT v9."label10m4" AS "label10m4", v5."label10m40" AS "label10m40", v9."nr1m5" AS "nr1m5", v9."price1m34" AS "price1m34", v9."v3" AS "v3", v9."v4" AS "v4", v9."validto1m39" AS "validto1m39", v9."vendor1m2" AS "vendor1m2", v9."vendor1m8" AS "vendor1m8"
FROM ((SELECT v1."label" AS "label10m40"
FROM "bsbm"."product1" v1
WHERE 94 = v1."nr"
)UNION ALL 
(SELECT v3."label" AS "label10m40"
FROM "bsbm"."product2" v3
WHERE 94 = v3."nr"
)) v5
 LEFT OUTER JOIN 
(SELECT v7."label" AS "label10m4", v6."nr" AS "nr1m5", v6."price" AS "price1m34", v6."product" AS "v3", v7."country" AS "v4", v6."validto" AS "validto1m39", v7."nr" AS "vendor1m2", v6."vendor" AS "vendor1m8"
FROM "bsbm"."offer" v6, "bsbm"."vendor" v7
WHERE ((v6."validto" > CAST('2008-06-13' AS DATE)) AND v6."vendor" = v7."nr" AND 94 = v6."product" AND 'DE' = v7."country")
) v9 ON 1 = 1 
) v11
 LEFT OUTER JOIN 
(SELECT v13."name" AS "name1m12", v12."nr" AS "nr0m4", v13."nr" AS "person2m5", v12."person" AS "person2m7", v12."rating1" AS "rating1m16", v12."rating2" AS "rating1m17", v12."title" AS "title2m11", v12."product" AS "v6"
FROM "bsbm"."review" v12, "bsbm"."person" v13
WHERE (v12."person" = v13."nr" AND 94 = v12."product")
) v15 ON 1 = 1 
