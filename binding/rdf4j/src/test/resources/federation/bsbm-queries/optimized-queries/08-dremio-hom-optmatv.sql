SELECT v2."name" AS "name1m12", v1."person" AS "person2m7", v1."rating3" AS "rating1m13", v1."rating4" AS "rating1m15", v1."rating1" AS "rating1m16", v1."rating2" AS "rating1m17", v1."reviewdate" AS "reviewdate2m37", v1."text" AS "text2m14", v1."title" AS "title2m11"
FROM "bsbm"."review" v1, "bsbm"."person" v2
WHERE (v1."person" = v2."nr" AND (94 = v1."product" AND 'en' = v1."language"))
ORDER BY v1."reviewdate" DESC NULLS LAST
