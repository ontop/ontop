SELECT DISTINCT v1."label" AS "label10m46", v1."nr" AS "product0m2"
FROM "ss5"."product2" v1, "ss5"."productfeatureproduct2" v2, "ss5"."productfeatureproduct2" v3, ((SELECT v4."nr" AS "product0m3", v4."propertynum1" AS "propertynum1m41"
FROM "ss1"."product1" v4
WHERE (v4."propertynum1" IS NOT NULL AND (v4."propertynum1" < 1000))
)UNION ALL
(SELECT v6."nr" AS "product0m3", v6."propertynum1" AS "propertynum1m41"
FROM "ss5"."product2" v6
WHERE (v6."propertynum1" IS NOT NULL AND (v6."propertynum1" < 1000))
)) v8
WHERE (v1."label" IS NOT NULL AND v1."nr" = v2."product" AND v1."nr" = v3."product" AND v1."nr" = v8."product0m3" AND 89 = v2."productfeature" AND 91 = v3."productfeature")