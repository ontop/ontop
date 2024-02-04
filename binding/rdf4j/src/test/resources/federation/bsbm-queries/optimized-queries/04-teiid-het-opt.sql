SELECT DISTINCT v29."label10m13" AS "label10m13", v29."nr0m12" AS "nr0m12", v29."propertytex1m14" AS "propertytex1m14"
FROM ((SELECT v13."label10m13" AS "label10m13", v13."nr0m12" AS "nr0m12", v13."propertytex1m14" AS "propertytex1m14"
FROM (SELECT DISTINCT v11."label10m13" AS "label10m13", v11."nr0m12" AS "nr0m12", v11."propertynum1m36" AS "propertynum1m36", v11."propertytex1m14" AS "propertytex1m14"
FROM ((SELECT v1."label" AS "label10m13", v1."nr" AS "nr0m12", v1."propertynum1" AS "propertynum1m36", v1."propertytex1" AS "propertytex1m14"
FROM "s1"."product1" v1, "s1"."producttypeproduct1" v2, "s1"."productfeatureproduct1" v3, "s1"."productfeatureproduct1" v4
WHERE ((v1."propertynum1" > 53) AND v1."label" IS NOT NULL AND v1."propertytex1" IS NOT NULL AND v1."propertynum1" IS NOT NULL AND v1."nr" = v2."product" AND v1."nr" = v3."product" AND v1."nr" = v4."product" AND 4 = v2."producttype" AND 90 = v3."productfeature" AND 79 = v4."productfeature")
)UNION ALL 
(SELECT v6."label" AS "label10m13", v6."nr" AS "nr0m12", v6."propertynum1" AS "propertynum1m36", v6."propertytex1" AS "propertytex1m14"
FROM "s5"."product2" v6, "s5"."producttypeproduct2" v7, "s5"."productfeatureproduct2" v8, "s5"."productfeatureproduct2" v9
WHERE ((v6."propertynum1" > 53) AND v6."label" IS NOT NULL AND v6."propertytex1" IS NOT NULL AND v6."propertynum1" IS NOT NULL AND v6."nr" = v7."product" AND v6."nr" = v8."product" AND v6."nr" = v9."product" AND 4 = v7."producttype" AND 90 = v8."productfeature" AND 79 = v9."productfeature")
)) v11
) v13
)UNION ALL 
(SELECT v27."label10m13" AS "label10m13", v27."nr0m12" AS "nr0m12", v27."propertytex1m14" AS "propertytex1m14"
FROM (SELECT DISTINCT v25."label10m13" AS "label10m13", v25."nr0m12" AS "nr0m12", v25."propertynum1m35" AS "propertynum1m35", v25."propertytex1m14" AS "propertytex1m14"
FROM ((SELECT v15."label" AS "label10m13", v15."nr" AS "nr0m12", v15."propertynum2" AS "propertynum1m35", v15."propertytex1" AS "propertytex1m14"
FROM "s1"."product1" v15, "s1"."producttypeproduct1" v16, "s1"."productfeatureproduct1" v17, "s1"."productfeatureproduct1" v18
WHERE ((v15."propertynum2" > 134) AND v15."label" IS NOT NULL AND v15."propertytex1" IS NOT NULL AND v15."propertynum2" IS NOT NULL AND v15."nr" = v16."product" AND v15."nr" = v17."product" AND v15."nr" = v18."product" AND 4 = v16."producttype" AND 90 = v17."productfeature" AND 116 = v18."productfeature")
)UNION ALL 
(SELECT v20."label" AS "label10m13", v20."nr" AS "nr0m12", v20."propertynum2" AS "propertynum1m35", v20."propertytex1" AS "propertytex1m14"
FROM "s5"."product2" v20, "s5"."producttypeproduct2" v21, "s5"."productfeatureproduct2" v22, "s5"."productfeatureproduct2" v23
WHERE ((v20."propertynum2" > 134) AND v20."label" IS NOT NULL AND v20."propertytex1" IS NOT NULL AND v20."propertynum2" IS NOT NULL AND v20."nr" = v21."product" AND v20."nr" = v22."product" AND v20."nr" = v23."product" AND 4 = v21."producttype" AND 90 = v22."productfeature" AND 116 = v23."productfeature")
)) v25
) v27
)) v29
ORDER BY v29."label10m13" NULLS FIRST
LIMIT 5, 10