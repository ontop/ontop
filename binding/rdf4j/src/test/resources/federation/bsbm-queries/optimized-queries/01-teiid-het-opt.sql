SELECT DISTINCT v11."label10m40" AS "label10m40", v11."nr0m49" AS "nr0m49"
FROM ((SELECT v1."label" AS "label10m40", v1."nr" AS "nr0m49"
FROM "s1"."product1" v1, "s1"."producttypeproduct1" v2, "s1"."productfeatureproduct1" v3, "s1"."productfeatureproduct1" v4
WHERE ((v1."propertynum1" > 53) AND v1."nr" = v2."product" AND v1."nr" = v3."product" AND v1."nr" = v4."product" AND 4 = v2."producttype" AND 79 = v3."productfeature" AND 116 = v4."productfeature")
)UNION ALL 
(SELECT v6."label" AS "label10m40", v6."nr" AS "nr0m49"
FROM "s5"."product2" v6, "s5"."producttypeproduct2" v7, "s5"."productfeatureproduct2" v8, "s5"."productfeatureproduct2" v9
WHERE ((v6."propertynum1" > 53) AND v6."nr" = v7."product" AND v6."nr" = v8."product" AND v6."nr" = v9."product" AND 4 = v7."producttype" AND 79 = v8."productfeature" AND 116 = v9."productfeature")
)) v11
ORDER BY v11."label10m40" NULLS FIRST
LIMIT 10