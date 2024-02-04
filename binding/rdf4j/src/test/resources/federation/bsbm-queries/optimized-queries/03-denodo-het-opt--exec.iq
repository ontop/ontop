ans1(product, label)
CONSTRUCT [product, label] [product/RDF(http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer/Product{}(INTEGERToTEXT(nr0m49)),IRI), label/RDF(VARCHARToTEXT(label10m40),xsd:string)]
   NATIVE [label10m11, label10m40, nr0m49, product0m5, propertynum1m23, propertynum1m36, v14]
SELECT DISTINCT v23."label10m11" AS "label10m11", v23."label10m40" AS "label10m40", v23."nr0m49" AS "nr0m49", v23."product0m5" AS "product0m5", v23."propertynum1m23" AS "propertynum1m23", v23."propertynum1m36" AS "propertynum1m36", v23."v14" AS "v14"
FROM (SELECT v21."label10m11" AS "label10m11", v21."label10m40" AS "label10m40", v21."nr0m49" AS "nr0m49", CASE WHEN v21."label10m11" IS NOT NULL THEN v21."nr0m49" ELSE NULL END AS "product0m5", v21."propertynum1m23" AS "propertynum1m23", v21."propertynum1m36" AS "propertynum1m36", v21."label10m40" AS "v14"
FROM ((SELECT v9."label10m11" AS "label10m11", v5."label10m40" AS "label10m40", v5."nr0m49" AS "nr0m49", v5."propertynum1m23" AS "propertynum1m23", v5."propertynum1m36" AS "propertynum1m36"
FROM (SELECT v1."label" AS "label10m40", v2."product" AS "nr0m1", v3."product" AS "nr0m2", v1."nr" AS "nr0m49", v1."propertynum3" AS "propertynum1m23", v1."propertynum1" AS "propertynum1m36", v2."producttype" AS "v3", v3."productfeature" AS "v4"
FROM "product1" v1, "producttypeproduct1" v2, "productfeatureproduct1" v3
WHERE (v1."label" IS NOT NULL AND v1."propertynum1" IS NOT NULL AND v1."propertynum3" IS NOT NULL AND (v1."propertynum3" < 1874) AND (v1."propertynum1" > 53) AND v1."nr" = v2."product" AND v1."nr" = v3."product" AND 4 = v2."producttype" AND 79 = v3."productfeature")
) v5
 LEFT OUTER JOIN 
(SELECT v7."label" AS "label10m11", v6."product" AS "nr0m0", v7."nr" AS "nr0m5", v6."productfeature" AS "v6"
FROM "productfeatureproduct1" v6, "product1" v7
WHERE (v7."label" IS NOT NULL AND v6."product" = v7."nr" AND 814 = v6."productfeature")
) v9 ON v5."nr0m49" = v9."nr0m0" 
WHERE v9."label10m11" IS NULL
)UNION ALL 
(SELECT v19."label10m11" AS "label10m11", v15."label10m40" AS "label10m40", v15."nr0m49" AS "nr0m49", v15."propertynum1m23" AS "propertynum1m23", v15."propertynum1m36" AS "propertynum1m36"
FROM (SELECT v11."label" AS "label10m40", v11."nr" AS "nr0m49", v12."product" AS "nr0m8", v13."product" AS "nr0m9", v11."propertynum3" AS "propertynum1m23", v11."propertynum1" AS "propertynum1m36", v12."producttype" AS "v10", v13."productfeature" AS "v11"
FROM "product2" v11, "producttypeproduct2" v12, "productfeatureproduct2" v13
WHERE (v11."label" IS NOT NULL AND v11."propertynum1" IS NOT NULL AND v11."propertynum3" IS NOT NULL AND (v11."propertynum3" < 1874) AND (v11."propertynum1" > 53) AND v11."nr" = v12."product" AND v11."nr" = v13."product" AND 4 = v12."producttype" AND 79 = v13."productfeature")
) v15
 LEFT OUTER JOIN 
(SELECT v17."label" AS "label10m11", v17."nr" AS "nr0m12", v16."product" AS "nr0m7", v16."productfeature" AS "v13"
FROM "productfeatureproduct2" v16, "product2" v17
WHERE (v17."label" IS NOT NULL AND v16."product" = v17."nr" AND 814 = v16."productfeature")
) v19 ON v15."nr0m49" = v19."nr0m7" 
WHERE v19."label10m11" IS NULL
)) v21
) v23
ORDER BY v23."v14" ASC NULLS FIRST
LIMIT 10
