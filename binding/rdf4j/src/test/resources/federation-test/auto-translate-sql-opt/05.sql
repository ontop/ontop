SELECT v24."label10m46" AS "label10m46", v24."product0m4" AS "product0m4"
FROM (SELECT DISTINCT v7."label10m46" AS "label10m46", v7."product0m4" AS "product0m4", v7."productfeature2m2" AS "productfeature2m2", v7."propertynum1m10" AS "propertynum1m10", v7."propertynum1m15" AS "propertynum1m15", v22."propertynum1m40" AS "propertynum1m40", v17."propertynum1m41" AS "propertynum1m41"
FROM ((SELECT v1."label" AS "label10m46", v1."nr" AS "product0m4", v2."productfeature" AS "productfeature2m2", v1."propertynum1" AS "propertynum1m10", v1."propertynum2" AS "propertynum1m15"
FROM "ss1"."product1" v1, "ss1"."productfeatureproduct1" v2
WHERE (v1."label" IS NOT NULL AND v1."propertynum1" IS NOT NULL AND v1."propertynum2" IS NOT NULL AND v1."nr" <> 88 AND v1."nr" = v2."product")
)UNION ALL
(SELECT v4."label" AS "label10m46", v4."nr" AS "product0m4", v5."productfeature" AS "productfeature2m2", v4."propertynum1" AS "propertynum1m10", v4."propertynum2" AS "propertynum1m15"
FROM "ss5"."product2" v4, "ss5"."productfeatureproduct2" v5
WHERE (v4."label" IS NOT NULL AND v4."propertynum1" IS NOT NULL AND v4."propertynum2" IS NOT NULL AND v4."nr" <> 88 AND v4."nr" = v5."product")
)) v7, ((SELECT v8."productfeature" AS "productfeature2m0"
FROM "ss1"."productfeatureproduct1" v8
WHERE 88 = v8."product"
)UNION ALL
(SELECT v10."productfeature" AS "productfeature2m0"
FROM "ss5"."productfeatureproduct2" v10
WHERE 88 = v10."product"
)) v12, ((SELECT v13."propertynum1" AS "propertynum1m41"
FROM "ss1"."product1" v13
WHERE (v13."propertynum1" IS NOT NULL AND 88 = v13."nr")
)UNION ALL
(SELECT v15."propertynum1" AS "propertynum1m41"
FROM "ss5"."product2" v15
WHERE (v15."propertynum1" IS NOT NULL AND 88 = v15."nr")
)) v17, ((SELECT v18."propertynum2" AS "propertynum1m40"
FROM "ss1"."product1" v18
WHERE (v18."propertynum2" IS NOT NULL AND 88 = v18."nr")
)UNION ALL
(SELECT v20."propertynum2" AS "propertynum1m40"
FROM "ss5"."product2" v20
WHERE (v20."propertynum2" IS NOT NULL AND 88 = v20."nr")
)) v22
WHERE ((v7."propertynum1m15" < (v22."propertynum1m40" + 170)) AND (v7."propertynum1m10" < (v17."propertynum1m41" + 120)) AND v7."productfeature2m2" = v12."productfeature2m0")
) v24