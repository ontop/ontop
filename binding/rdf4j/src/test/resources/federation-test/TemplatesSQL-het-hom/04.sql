-- ${1:productfeatureproduct.productfeature:percent}
-- ${2:productfeatureproduct.productfeature:percent}
-- ${3:productfeatureproduct.productfeature:percent}

SELECT DISTINCT v57."label10m9" AS "label10m9", v57."product0m8" AS "product0m8", v57."propertytex1m10" AS "propertytex1m10"
FROM (
      SELECT v27."label10m9" AS "label10m9", v27."product0m8" AS "product0m8", v27."propertytex1m10" AS "propertytex1m10"
      FROM (
            SELECT DISTINCT v5."label10m9" AS "label10m9", v5."product0m8" AS "product0m8", v25."propertynum1m41" AS "propertynum1m41", v20."propertytex1m10" AS "propertytex1m10"
            FROM (
                  SELECT v1."label" AS "label10m9", v1."nr" AS "product0m8"
                  FROM "ss1"."product1" v1
                  WHERE v1."label" IS NOT NULL
                  UNION ALL
                  SELECT v3."label" AS "label10m9", v3."nr" AS "product0m8"
                  FROM "ss5"."product2" v3
                  WHERE v3."label" IS NOT NULL
                 ) v5, (
                        SELECT v6."product" AS "product0m0"
                        FROM "ss1"."productfeatureproduct1" v6
                        WHERE ${1:productfeatureproduct.productfeature:percent} = v6."productfeature"
                        UNION ALL
                        SELECT v8."product" AS "product0m0"
                        FROM "ss5"."productfeatureproduct2" v8
                        WHERE ${1:productfeatureproduct.productfeature:percent} = v8."productfeature"
                       ) v10, (
                               SELECT v11."product" AS "product0m1"
                               FROM "ss1"."productfeatureproduct1" v11
                               WHERE ${2:productfeatureproduct.productfeature:percent} = v11."productfeature"
                               UNION ALL
                               SELECT v13."product" AS "product0m1"
                               FROM "ss5"."productfeatureproduct2" v13
                               WHERE ${2:productfeatureproduct.productfeature:percent} = v13."productfeature"
                              ) v15, (
                                      SELECT v16."nr" AS "product0m2", v16."propertytex1" AS "propertytex1m10"
                                      FROM "ss1"."product1" v16
                                      WHERE v16."propertytex1" IS NOT NULL
                                      UNION ALL
                                      SELECT v18."nr" AS "product0m2", v18."propertytex1" AS "propertytex1m10"
                                      FROM "ss5"."product2" v18
                                      WHERE v18."propertytex1" IS NOT NULL
                                     ) v20, (
                                             SELECT v21."nr" AS "product0m3", v21."propertynum1" AS "propertynum1m41"
                                             FROM "ss1"."product1" v21
                                             WHERE v21."propertynum1" IS NOT NULL
                                             UNION ALL
                                             SELECT v23."nr" AS "product0m3", v23."propertynum1" AS "propertynum1m41"
                                             FROM "ss5"."product2" v23
                                             WHERE v23."propertynum1" IS NOT NULL
                                             ) v25
                 WHERE (v5."product0m8" = v10."product0m0" AND v5."product0m8" = v15."product0m1" AND v5."product0m8" = v20."product0m2" AND v5."product0m8" = v25."product0m3")
      ) v27
      UNION ALL
      SELECT v55."label10m9" AS "label10m9", v55."product0m8" AS "product0m8", v55."propertytex1m10" AS "propertytex1m10"
      FROM (
            SELECT DISTINCT v33."label10m9" AS "label10m9", v33."product0m8" AS "product0m8", v53."propertynum1m40" AS "propertynum1m40", v48."propertytex1m10" AS "propertytex1m10"
            FROM (
                  SELECT v29."label" AS "label10m9", v29."nr" AS "product0m8"
                  FROM "ss1"."product1" v29
                  WHERE v29."label" IS NOT NULL
                  UNION ALL
                  SELECT v31."label" AS "label10m9", v31."nr" AS "product0m8"
                  FROM "ss5"."product2" v31
                  WHERE v31."label" IS NOT NULL
                  ) v33, (
                          SELECT v34."product" AS "product0m9"
                          FROM "ss1"."productfeatureproduct1" v34
                          WHERE ${1:productfeatureproduct.productfeature:percent} = v34."productfeature"
                          UNION ALL
                          SELECT v36."product" AS "product0m9"
                          FROM "ss5"."productfeatureproduct2" v36
                          WHERE ${1:productfeatureproduct.productfeature:percent} = v36."productfeature"
                         ) v38, (
                                 SELECT v39."product" AS "product0m10"
                                 FROM "ss1"."productfeatureproduct1" v39
                                 WHERE ${3:productfeatureproduct.productfeature:percent} = v39."productfeature"
                                 UNION ALL
                                 SELECT v41."product" AS "product0m10"
                                 FROM "ss5"."productfeatureproduct2" v41
                                 WHERE ${3:productfeatureproduct.productfeature:percent} = v41."productfeature"
                                 ) v43, (
                                         SELECT v44."nr" AS "product0m11", v44."propertytex1" AS "propertytex1m10"
                                         FROM "ss1"."product1" v44
                                         WHERE v44."propertytex1" IS NOT NULL
                                         UNION ALL
                                         SELECT v46."nr" AS "product0m11", v46."propertytex1" AS "propertytex1m10"
                                         FROM "ss5"."product2" v46
                                         WHERE v46."propertytex1" IS NOT NULL
                                        ) v48, (
                                                SELECT v49."nr" AS "product0m12", v49."propertynum2" AS "propertynum1m40"
                                                FROM "ss1"."product1" v49
                                                WHERE v49."propertynum2" IS NOT NULL
                                                UNION ALL
                                                SELECT v51."nr" AS "product0m12", v51."propertynum2" AS "propertynum1m40"
                                                FROM "ss5"."product2" v51
                                                WHERE v51."propertynum2" IS NOT NULL
                                               ) v53
                   WHERE (v33."product0m8" = v38."product0m9" AND v33."product0m8" = v43."product0m10" AND v33."product0m8" = v48."product0m11" AND v33."product0m8" = v53."product0m12")
      ) v55
) v57
