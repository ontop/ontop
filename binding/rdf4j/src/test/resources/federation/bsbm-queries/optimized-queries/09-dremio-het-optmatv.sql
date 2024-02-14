SELECT v2."country" AS "country3m1", v2."mbox_sha1sum" AS "mbox_sha1sum1m41", v2."name" AS "name1m12", v3."nr" AS "nr0m4", v1."person" AS "person2m7", v3."product" AS "product2m4", v3."title" AS "title2m11"
FROM "bsbm"."review" v1, "bsbm"."person" v2, "bsbm"."review" v3
WHERE (v1."person" = v2."nr" AND v1."person" = v3."person" AND 1356 = v1."nr")
