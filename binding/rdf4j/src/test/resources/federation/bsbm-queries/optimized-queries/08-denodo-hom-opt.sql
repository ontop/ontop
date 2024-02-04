SELECT v4."name1m12" AS "name1m12", v4."person2m7" AS "person2m7", v4."rating1m13" AS "rating1m13", v4."rating1m15" AS "rating1m15", v4."rating1m16" AS "rating1m16", v4."rating1m17" AS "rating1m17", v4."reviewdate2m37" AS "reviewdate2m37", v4."text2m14" AS "text2m14", v4."title2m11" AS "title2m11"
FROM (SELECT v2."name" AS "name1m12", v1."person" AS "person2m7", v1."rating3" AS "rating1m13", v1."rating4" AS "rating1m15", v1."rating1" AS "rating1m16", v1."rating2" AS "rating1m17", v1."reviewdate" AS "reviewdate2m37", v1."text" AS "text2m14", v1."title" AS "title2m11"
FROM "review" v1, "person" v2
WHERE (v2."name" IS NOT NULL AND v1."title" IS NOT NULL AND v1."text" IS NOT NULL AND v1."reviewdate" IS NOT NULL AND v1."person" = v2."nr" AND (94 = v1."product" AND 'en' = v1."language"))
) v4
ORDER BY v4."reviewdate2m37" DESC NULLS LAST
