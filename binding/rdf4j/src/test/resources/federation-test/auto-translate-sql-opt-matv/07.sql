SELECT v11."label10m4" AS "label10m4", v11."label10m46" AS "label10m46", v11."name1m12" AS "name1m12", v11."nr0m4" AS "nr0m4", v11."nr1m5" AS "nr1m5", v11."person2m7" AS "person2m7", v11."price1m39" AS "price1m39", v11."rating1m16" AS "rating1m16", v11."rating1m17" AS "rating1m17", v11."title2m11" AS "title2m11", v11."vendor1m8" AS "vendor1m8"
FROM (
       SELECT DISTINCT v7."label" AS "label10m4", v5."label10m46" AS "label10m46", v9."name" AS "name1m12", CASE WHEN v8."rating1" IS NOT NULL THEN v8."nr" ELSE NULL END AS "nr0m16", CASE WHEN v8."rating2" IS NOT NULL THEN v8."nr" ELSE NULL END AS "nr0m17", v8."nr" AS "nr0m4", v6."nr" AS "nr1m5", v8."person" AS "person2m7", v6."price" AS "price1m39", v8."rating1" AS "rating1m16", v8."rating2" AS "rating1m17", v8."title" AS "title2m11", v6."validto" AS "validto1m45", v6."vendor" AS "vendor1m8"
       FROM (
              (
               SELECT v1."label" AS "label10m46"
               FROM "ss1"."product1" v1
               WHERE (v1."label" IS NOT NULL AND 88 = v1."nr")
              )
               UNION ALL
             (
               SELECT v3."label" AS "label10m46"
               FROM "ss5"."product2" v3
               WHERE (v3."label" IS NOT NULL AND 88 = v3."nr")
             )
            ) v5
 LEFT OUTER JOIN
"ss4"."offer" v6
 JOIN
"ss4"."vendor" v7 ON ((v6."validto" > '1988-01-01') AND v6."price" IS NOT NULL AND v7."label" IS NOT NULL AND v6."validto" IS NOT NULL AND v6."vendor" = v7."nr" AND 88 = v6."product" AND 'DE' = v7."country")  ON 1 = 1
 LEFT OUTER JOIN
"ss2"."review" v8
 JOIN
"ss2"."person" v9 ON (v9."name" IS NOT NULL AND v8."title" IS NOT NULL AND v8."rating1" IS NOT NULL AND v8."rating2" IS NOT NULL AND v8."person" = v9."nr" AND 88 = v8."product")  ON 1 = 1
) v11