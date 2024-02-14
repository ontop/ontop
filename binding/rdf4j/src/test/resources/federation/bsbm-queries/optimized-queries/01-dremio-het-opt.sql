SELECT DISTINCT v11."label10m40" AS "label10m40", v11."nr0m49" AS "nr0m49"
FROM ((SELECT v1."label" AS "label10m40", v1."nr" AS "nr0m49"
FROM "bsbm"."product1" v1, "bsbm"."producttypeproduct1" v2, "bsbm"."productfeatureproduct1" v3, "bsbm"."productfeatureproduct1" v4
WHERE ((v1."propertynum1" > 53) AND v1."nr" = v2."product" AND v1."nr" = v3."product" AND v1."nr" = v4."product" AND 4 = v2."producttype" AND 79 = v3."productfeature" AND 116 = v4."productfeature")
)UNION ALL 
(SELECT v6."label" AS "label10m40", v6."nr" AS "nr0m49"
FROM "bsbm"."product2" v6, "bsbm"."producttypeproduct2" v7, "bsbm"."productfeatureproduct2" v8, "bsbm"."productfeatureproduct2" v9
WHERE ((v6."propertynum1" > 53) AND v6."nr" = v7."product" AND v6."nr" = v8."product" AND v6."nr" = v9."product" AND 4 = v7."producttype" AND 79 = v8."productfeature" AND 116 = v9."productfeature")
)) v11
ORDER BY v11."label10m40" NULLS FIRST
LIMIT 10