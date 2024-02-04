SELECT DISTINCT v7."label" AS "label10m4", v5."label10m40" AS "label10m40", v9."name" AS "name1m12", v8."nr" AS "nr0m4", v6."nr" AS "nr1m5", v8."person" AS "person2m7", v6."price" AS "price1m34", v10."rating1" AS "rating1m16", v11."rating2" AS "rating1m17", v8."title" AS "title2m11", v6."validto" AS "validto1m39", v6."vendor" AS "vendor1m8"
FROM ((SELECT v1."label" AS "label10m40"
FROM "s1"."product1" v1
WHERE (v1."label" IS NOT NULL AND 94 = v1."nr")
)UNION ALL 
(SELECT v3."label" AS "label10m40"
FROM "s5"."product2" v3
WHERE (v3."label" IS NOT NULL AND 94 = v3."nr")
)) v5
 LEFT OUTER JOIN 
"s4"."offer" v6
 JOIN 
"s4"."vendor" v7 ON ((v6."validto" > '2008-06-13') AND v6."price" IS NOT NULL AND v7."label" IS NOT NULL AND v6."validto" IS NOT NULL AND v6."vendor" = v7."nr" AND 94 = v6."product" AND 'DE' = v7."country")  ON 1 = 1 
 LEFT OUTER JOIN 
"s2"."review" v8
 JOIN 
"s2"."person" v9 ON (v9."name" IS NOT NULL AND v8."title" IS NOT NULL AND v8."person" = v9."nr" AND 94 = v8."product") 
 LEFT OUTER JOIN 
"s2"."review" v10 ON (v10."rating1" IS NOT NULL AND v8."nr" = v10."nr") 
 LEFT OUTER JOIN 
"s2"."review" v11 ON (v11."rating2" IS NOT NULL AND v8."nr" = v11."nr")  ON 1 = 1 
