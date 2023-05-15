SELECT v14."label10m4" AS "label10m4", v14."label10m46" AS "label10m46", v14."name1m12" AS "name1m12", v14."nr0m4" AS "nr0m4", v14."nr1m5" AS "nr1m5", v14."person2m7" AS "person2m7", v14."price1m39" AS "price1m39", v14."rating1m16" AS "rating1m16", v14."rating1m17" AS "rating1m17", v14."title2m11" AS "title2m11", v14."vendor1m8" AS "vendor1m8"
FROM (SELECT DISTINCT v7."label" AS "label10m4", v5."label10m46" AS "label10m46", v10."name" AS "name1m12", CASE WHEN v11."rating1" IS NOT NULL THEN v8."nr" ELSE NULL END AS "nr0m16", CASE WHEN v12."rating2" IS NOT NULL THEN v8."nr" ELSE NULL END AS "nr0m17", v8."nr" AS "nr0m4", v6."nr" AS "nr1m5", v9."person" AS "person2m7", v6."price" AS "price1m39", v11."rating1" AS "rating1m16", v12."rating2" AS "rating1m17", v8."title" AS "title2m11", v6."validto" AS "validto1m45", v6."vendor" AS "vendor1m8"
FROM ((SELECT v1."label" AS "label10m46"
FROM "ss1"."product1" v1
WHERE (v1."label" IS NOT NULL AND 88 = v1."nr")
)UNION ALL
(SELECT v3."label" AS "label10m46"
FROM "ss5"."product2" v3
WHERE (v3."label" IS NOT NULL AND 88 = v3."nr")
)) v5
 LEFT OUTER JOIN
"ss4"."offer" v6
 JOIN
"ss4"."vendor" v7 ON ((v6."validto" > '1988-01-01') AND v6."price" IS NOT NULL AND v7."label" IS NOT NULL AND v6."validto" IS NOT NULL AND v6."vendor" = v7."nr" AND 88 = v6."product" AND 'DE' = v7."country")  ON 1 = 1
 LEFT OUTER JOIN
"ss2"."review" v8
 JOIN
"ss2"."review" v9 ON 1 = 1
 JOIN
"ss2"."person" v10 ON (v10."name" IS NOT NULL AND v8."title" IS NOT NULL AND v8."nr" = v9."nr" AND v9."person" = v10."nr" AND 88 = v8."product")
 LEFT OUTER JOIN
"ss2"."review" v11 ON (v11."rating1" IS NOT NULL AND v8."nr" = v11."nr")
 LEFT OUTER JOIN
"ss2"."review" v12 ON (v12."rating2" IS NOT NULL AND v8."nr" = v12."nr")  ON 1 = 1
) v14