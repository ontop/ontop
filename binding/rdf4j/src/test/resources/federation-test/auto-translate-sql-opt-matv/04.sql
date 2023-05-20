SELECT DISTINCT v25."label10m9" AS "label10m9", v25."product0m8" AS "product0m8", v25."propertytex1m10" AS "propertytex1m10"
FROM (
      (
        SELECT v11."label10m9" AS "label10m9", v11."product0m8" AS "product0m8", v11."propertytex1m10" AS "propertytex1m10"
        FROM (
              SELECT DISTINCT v9."label10m9" AS "label10m9", v9."product0m8" AS "product0m8", v9."propertynum1m41" AS "propertynum1m41", v9."propertytex1m10" AS "propertytex1m10"
              FROM (
                    (
                     SELECT v1."label" AS "label10m9", v1."nr" AS "product0m8", v1."propertynum1" AS "propertynum1m41", v1."propertytex1" AS "propertytex1m10"
                     FROM "ss1"."product1" v1, "ss1"."productfeatureproduct1" v2, "ss1"."productfeatureproduct1" v3
                     WHERE ((v1."propertynum1" > 30) AND v1."label" IS NOT NULL AND v1."propertytex1" IS NOT NULL AND v1."propertynum1" IS NOT NULL AND v1."nr" = v2."product" AND v1."nr" = v3."product" AND 89 = v2."productfeature" AND 91 = v3."productfeature")
                    )
                     UNION ALL
                   (
                     SELECT v5."label" AS "label10m9", v5."nr" AS "product0m8", v5."propertynum1" AS "propertynum1m41", v5."propertytex1" AS "propertytex1m10"
                     FROM "ss5"."product2" v5, "ss5"."productfeatureproduct2" v6, "ss5"."productfeatureproduct2" v7
                     WHERE ((v5."propertynum1" > 30) AND v5."label" IS NOT NULL AND v5."propertytex1" IS NOT NULL AND v5."propertynum1" IS NOT NULL AND v5."nr" = v6."product" AND v5."nr" = v7."product" AND 89 = v6."productfeature" AND 91 = v7."productfeature")
                   )
                  ) v9
             ) v11
      )
       UNION ALL
     (
       SELECT v23."label10m9" AS "label10m9", v23."product0m8" AS "product0m8", v23."propertytex1m10" AS "propertytex1m10"
       FROM (
             SELECT DISTINCT v21."label10m9" AS "label10m9", v21."product0m8" AS "product0m8", v21."propertynum1m40" AS "propertynum1m40", v21."propertytex1m10" AS "propertytex1m10"
             FROM (
                   (
                    SELECT v13."label" AS "label10m9", v13."nr" AS "product0m8", v13."propertynum2" AS "propertynum1m40", v13."propertytex1" AS "propertytex1m10"
                    FROM "ss1"."product1" v13, "ss1"."productfeatureproduct1" v14, "ss1"."productfeatureproduct1" v15
                    WHERE ((v13."propertynum2" > 50) AND v13."label" IS NOT NULL AND v13."propertytex1" IS NOT NULL AND v13."propertynum2" IS NOT NULL AND v13."nr" = v14."product" AND v13."nr" = v15."product" AND 89 = v14."productfeature" AND 86 = v15."productfeature")
                   )
                    UNION ALL
                   (
                    SELECT v17."label" AS "label10m9", v17."nr" AS "product0m8", v17."propertynum2" AS "propertynum1m40", v17."propertytex1" AS "propertytex1m10"
                    FROM "ss5"."product2" v17, "ss5"."productfeatureproduct2" v18, "ss5"."productfeatureproduct2" v19
                    WHERE ((v17."propertynum2" > 50) AND v17."label" IS NOT NULL AND v17."propertytex1" IS NOT NULL AND v17."propertynum2" IS NOT NULL AND v17."nr" = v18."product" AND v17."nr" = v19."product" AND 89 = v18."productfeature" AND 86 = v19."productfeature")
                   )
                  ) v21
            ) v23
     )
) v25