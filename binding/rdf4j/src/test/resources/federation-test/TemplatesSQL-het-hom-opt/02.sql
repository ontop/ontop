-- ${1:product.nr:percent}

SELECT v76."comment10m20" AS "comment10m20", v76."label10m18" AS "label10m18", v76."label10m46" AS "label10m46", v76."label10m6" AS "label10m6", v76."propertynum1m25" AS "propertynum1m25", v76."propertynum1m40" AS "propertynum1m40", v76."propertynum1m41" AS "propertynum1m41", v76."propertytex1m30" AS "propertytex1m30", v76."propertytex1m31" AS "propertytex1m31", v76."propertytex1m32" AS "propertytex1m32", v76."propertytex1m33" AS "propertytex1m33", v76."propertytex1m34" AS "propertytex1m34"
FROM (
         (
             SELECT DISTINCT v5."comment10m20" AS "comment10m20", v5."label10m18" AS "label10m18", v5."label10m46" AS "label10m46",
                             v5."label10m6" AS "label10m6", CASE WHEN v5."propertynum1m25" IS NOT NULL THEN v5."nr2m23" ELSE NULL END AS "nr0m25",
                             CASE WHEN v5."propertytex1m30" IS NOT NULL THEN v5."nr2m23" ELSE NULL END AS "nr0m30",
                             CASE WHEN v5."propertytex1m31" IS NOT NULL THEN v5."nr2m23" ELSE NULL END AS "nr0m31",
                             v5."nr2m23" AS "nr2m23", v5."producer2m9" AS "producer2m9", v5."productfeature2m2" AS "productfeature2m2",
                             v5."propertynum1m25" AS "propertynum1m25", v5."propertynum1m40" AS "propertynum1m40",
                             v5."propertynum1m41" AS "propertynum1m41", v5."propertytex1m30" AS "propertytex1m30",
                             v5."propertytex1m31" AS "propertytex1m31", v5."propertytex1m32" AS "propertytex1m32",
                             v5."propertytex1m33" AS "propertytex1m33", v5."propertytex1m34" AS "propertytex1m34"
             FROM (
                      SELECT v1."nr" AS "nr2m23", v1."label" AS "label10m46", v1."comment" AS "comment10m20", v1."producer" AS "producer2m9", v17."label" AS "label10m6",
                             v29."label" AS "label10m18", v28."productfeature" AS "productfeature2m2",
                             v1."propertytex1" AS "propertytex1m34", v1."propertytex2" AS "propertytex1m33", v1."propertytex3" AS "propertytex1m32",
                             v1."propertynum1" AS "propertynum1m41", v1."propertynum2" AS "propertynum1m40", v1."propertytex4" AS "propertytex1m31",
                             v1."propertytex5" AS "propertytex1m30", v1."propertynum4" AS "propertynum1m25"
                      FROM "ss1"."product1" v1, "ss4"."producer" v17, "ss1"."productfeatureproduct1" v28, "ss3"."productfeature" v29
                      WHERE (v1."nr" IS NOT NULL AND (v1."nr" <= ${1:product.nr:percent}) AND v1."label" IS NOT NULL AND v1."comment" IS NOT NULL AND v1."producer"=v17."nr" AND
                             v1."nr"=v28."product" AND v29."label" IS NOT NULL and v28."productfeature"=v29."nr" AND v1."propertytex1" IS NOT NULL AND
                             v1."propertytex2" IS NOT NULL AND v1."propertytex3" IS NOT NULL AND v1."propertynum1" IS NOT NULL AND
                             v1."propertynum2" IS NOT NULL)
                  ) v5
         )
         UNION ALL
         (
             SELECT DISTINCT v5."comment10m20" AS "comment10m20", v5."label10m18" AS "label10m18", v5."label10m46" AS "label10m46",
                             v5."label10m6" AS "label10m6", CASE WHEN v5."propertynum1m25" IS NOT NULL THEN v5."nr2m23" ELSE NULL END AS "nr0m25",
                             CASE WHEN v5."propertytex1m30" IS NOT NULL THEN v5."nr2m23" ELSE NULL END AS "nr0m30",
                             CASE WHEN v5."propertytex1m31" IS NOT NULL THEN v5."nr2m23" ELSE NULL END AS "nr0m31",
                             v5."nr2m23" AS "nr2m23", v5."producer2m9" AS "producer2m9", v5."productfeature2m2" AS "productfeature2m2",
                             v5."propertynum1m25" AS "propertynum1m25", v5."propertynum1m40" AS "propertynum1m40",
                             v5."propertynum1m41" AS "propertynum1m41", v5."propertytex1m30" AS "propertytex1m30",
                             v5."propertytex1m31" AS "propertytex1m31", v5."propertytex1m32" AS "propertytex1m32",
                             v5."propertytex1m33" AS "propertytex1m33", v5."propertytex1m34" AS "propertytex1m34"
             FROM (
                      SELECT v1."nr" AS "nr2m23", v1."label" AS "label10m46", v1."comment" AS "comment10m20", v1."producer" AS "producer2m9", v17."label" AS "label10m6",
                             v29."label" AS "label10m18", v28."productfeature" AS "productfeature2m2",
                             v1."propertytex1" AS "propertytex1m34", v1."propertytex2" AS "propertytex1m33", v1."propertytex3" AS "propertytex1m32",
                             v1."propertynum1" AS "propertynum1m41", v1."propertynum2" AS "propertynum1m40", v1."propertytex4" AS "propertytex1m31",
                             v1."propertytex5" AS "propertytex1m30", v1."propertynum4" AS "propertynum1m25"
                      FROM "ss5"."product2" v1, "ss4"."producer" v17, "ss5"."productfeatureproduct2" v28, "ss3"."productfeature" v29
                      WHERE (v1."nr" IS NOT NULL AND (v1."nr" <= ${1:product.nr:percent}) AND v1."label" IS NOT NULL AND v1."comment" IS NOT NULL AND v1."producer"=v17."nr" AND
                             v1."nr"=v28."product" AND v29."label" IS NOT NULL and v28."productfeature"=v29."nr" AND v1."propertytex1" IS NOT NULL AND
                             v1."propertytex2" IS NOT NULL AND v1."propertytex3" IS NOT NULL AND v1."propertynum1" IS NOT NULL AND
                             v1."propertynum2" IS NOT NULL)
                  ) v5
         )
     ) v76
