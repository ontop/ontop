-- ${1:product.nr:percent}

SELECT v76."comment10m20" AS "comment10m20", v76."label10m18" AS "label10m18", v76."label10m46" AS "label10m46", v76."label10m6" AS "label10m6", v76."propertynum1m25" AS "propertynum1m25", v76."propertynum1m40" AS "propertynum1m40", v76."propertynum1m41" AS "propertynum1m41", v76."propertytex1m30" AS "propertytex1m30", v76."propertytex1m31" AS "propertytex1m31", v76."propertytex1m32" AS "propertytex1m32", v76."propertytex1m33" AS "propertytex1m33", v76."propertytex1m34" AS "propertytex1m34"
FROM (
	  SELECT DISTINCT v15."comment10m20" AS "comment10m20", v34."label10m18" AS "label10m18", v10."label10m46" AS "label10m46", v22."label10m6" AS "label10m6", CASE WHEN v74."propertynum1m25" IS NOT NULL THEN v5."nr2m23" ELSE NULL END AS "nr0m25", CASE WHEN v69."propertytex1m30" IS NOT NULL THEN v5."nr2m23" ELSE NULL END AS "nr0m30", CASE WHEN v64."propertytex1m31" IS NOT NULL THEN v5."nr2m23" ELSE NULL END AS "nr0m31", v5."nr2m23" AS "nr2m23", v22."producer2m9" AS "producer2m9", v34."productfeature2m2" AS "productfeature2m2", v74."propertynum1m25" AS "propertynum1m25", v59."propertynum1m40" AS "propertynum1m40", v54."propertynum1m41" AS "propertynum1m41", v69."propertytex1m30" AS "propertytex1m30", v64."propertytex1m31" AS "propertytex1m31", v49."propertytex1m32" AS "propertytex1m32", v44."propertytex1m33" AS "propertytex1m33", v39."propertytex1m34" AS "propertytex1m34"
      FROM (
		    SELECT v1."nr" AS "nr2m23"
            FROM "ss1"."product1" v1
            WHERE (v1."nr" IS NOT NULL AND (v1."nr" <= ${1:product.nr:percent}))
            UNION ALL
            SELECT v3."nr" AS "nr2m23"
            FROM "ss5"."product2" v3
            WHERE (v3."nr" IS NOT NULL AND (v3."nr" <= ${1:product.nr:percent}))
          ) v5
          JOIN (
			    SELECT v6."label" AS "label10m46", v6."nr" AS "nr2m3"
                FROM "ss1"."product1" v6
                WHERE ((v6."nr" IS NOT NULL AND v6."label" IS NOT NULL) AND (v6."nr" <= ${1:product.nr:percent}))
                UNION ALL
                SELECT v8."label" AS "label10m46", v8."nr" AS "nr2m3"
                FROM "ss5"."product2" v8
                WHERE ((v8."nr" IS NOT NULL AND v8."label" IS NOT NULL) AND (v8."nr" <= ${1:product.nr:percent}))
              ) v10 ON 1 = 1
             JOIN (
		           SELECT v11."comment" AS "comment10m20", v11."nr" AS "nr2m4"
                   FROM "ss1"."product1" v11
                   WHERE ((v11."nr" IS NOT NULL AND v11."comment" IS NOT NULL) AND (v11."nr" <= ${1:product.nr:percent}))
                   UNION ALL
                   SELECT v13."comment" AS "comment10m20", v13."nr" AS "nr2m4"
                   FROM "ss5"."product2" v13
                   WHERE ((v13."nr" IS NOT NULL AND v13."comment" IS NOT NULL) AND (v13."nr" <= ${1:product.nr:percent}))
                  ) v15 ON 1 = 1
                 JOIN (
		               SELECT v17."label" AS "label10m6", v16."nr" AS "nr2m5", v16."producer" AS "producer2m9"
                       FROM "ss1"."product1" v16, "ss4"."producer" v17
                       WHERE (v16."nr" IS NOT NULL AND (v16."nr" <= ${1:product.nr:percent}) AND v17."label" IS NOT NULL AND v16."producer" = v17."nr")
                       UNION ALL
                       SELECT v20."label" AS "label10m6", v19."nr" AS "nr2m5", v19."producer" AS "producer2m9"
                       FROM "ss5"."product2" v19, "ss4"."producer" v20
                       WHERE (v19."nr" IS NOT NULL AND (v19."nr" <= ${1:product.nr:percent}) AND v20."label" IS NOT NULL AND v19."producer" = v20."nr")
                     ) v22 ON 1 = 1
                    JOIN (
						  SELECT v23."nr" AS "nr2m7", v23."producer" AS "producer2m6"
                          FROM "ss1"."product1" v23
                          WHERE ((v23."nr" IS NOT NULL AND v23."producer" IS NOT NULL) AND (v23."nr" <= ${1:product.nr:percent}))
                          UNION ALL
                          SELECT v25."nr" AS "nr2m7", v25."producer" AS "producer2m6"
                          FROM "ss5"."product2" v25
                          WHERE ((v25."nr" IS NOT NULL AND v25."producer" IS NOT NULL) AND (v25."nr" <= ${1:product.nr:percent}))
                        ) v27 ON 1 = 1
                        JOIN (
		                      SELECT v29."label" AS "label10m18", v28."product" AS "nr2m8", v28."productfeature" AS "productfeature2m2"
                              FROM "ss1"."productfeatureproduct1" v28, "ss3"."productfeature" v29
                              WHERE ((v28."product" <= ${1:product.nr:percent}) AND v29."label" IS NOT NULL AND v28."productfeature" = v29."nr")
                              UNION ALL
                              SELECT v32."label" AS "label10m18", v31."product" AS "nr2m8", v31."productfeature" AS "productfeature2m2"
                              FROM "ss5"."productfeatureproduct2" v31, "ss3"."productfeature" v32
                              WHERE ((v31."product" <= ${1:product.nr:percent}) AND v32."label" IS NOT NULL AND v31."productfeature" = v32."nr")
                             ) v34 ON 1 = 1
                             JOIN (
								   SELECT v35."nr" AS "nr2m9", v35."propertytex1" AS "propertytex1m34"
                                   FROM "ss1"."product1" v35
                                   WHERE ((v35."nr" IS NOT NULL AND v35."propertytex1" IS NOT NULL) AND (v35."nr" <= ${1:product.nr:percent}))
                                   UNION ALL
                                   SELECT v37."nr" AS "nr2m9", v37."propertytex1" AS "propertytex1m34"
                                   FROM "ss5"."product2" v37
                                   WHERE ((v37."nr" IS NOT NULL AND v37."propertytex1" IS NOT NULL) AND (v37."nr" <= ${1:product.nr:percent}))
                                  ) v39 ON 1 = 1
                                  JOIN (
		                                SELECT v40."nr" AS "nr2m10", v40."propertytex2" AS "propertytex1m33"
                                        FROM "ss1"."product1" v40
                                        WHERE ((v40."nr" IS NOT NULL AND v40."propertytex2" IS NOT NULL) AND (v40."nr" <= ${1:product.nr:percent}))
                                        UNION ALL
                                        SELECT v42."nr" AS "nr2m10", v42."propertytex2" AS "propertytex1m33"
                                        FROM "ss5"."product2" v42
                                        WHERE ((v42."nr" IS NOT NULL AND v42."propertytex2" IS NOT NULL) AND (v42."nr" <= ${1:product.nr:percent}))
                                       ) v44 ON 1 = 1
                                       JOIN (
		                                     SELECT v45."nr" AS "nr2m11", v45."propertytex3" AS "propertytex1m32"
                                             FROM "ss1"."product1" v45
                                             WHERE ((v45."nr" IS NOT NULL AND v45."propertytex3" IS NOT NULL) AND (v45."nr" <= ${1:product.nr:percent}))
                                             UNION ALL
                                             SELECT v47."nr" AS "nr2m11", v47."propertytex3" AS "propertytex1m32"
                                             FROM "ss5"."product2" v47
                                             WHERE ((v47."nr" IS NOT NULL AND v47."propertytex3" IS NOT NULL) AND (v47."nr" <= ${1:product.nr:percent}))
                                            ) v49 ON 1 = 1
                                            JOIN (
												  SELECT v50."nr" AS "nr2m12", v50."propertynum1" AS "propertynum1m41"
                                                  FROM "ss1"."product1" v50
                                                  WHERE ((v50."nr" IS NOT NULL AND v50."propertynum1" IS NOT NULL) AND (v50."nr" <= ${1:product.nr:percent}))
                                                  UNION ALL
                                                  SELECT v52."nr" AS "nr2m12", v52."propertynum1" AS "propertynum1m41"
                                                  FROM "ss5"."product2" v52
                                                  WHERE ((v52."nr" IS NOT NULL AND v52."propertynum1" IS NOT NULL) AND (v52."nr" <= ${1:product.nr:percent}))
                                           ) v54 ON 1 = 1
                                           JOIN (
                                                 SELECT v55."nr" AS "nr2m13", v55."propertynum2" AS "propertynum1m40"
                                                 FROM "ss1"."product1" v55
                                                 WHERE ((v55."nr" IS NOT NULL AND v55."propertynum2" IS NOT NULL) AND (v55."nr" <= ${1:product.nr:percent}))
                                                 UNION ALL
                                                 SELECT v57."nr" AS "nr2m13", v57."propertynum2" AS "propertynum1m40"
                                                 FROM "ss5"."product2" v57
                                                 WHERE ((v57."nr" IS NOT NULL AND v57."propertynum2" IS NOT NULL) AND (v57."nr" <= ${1:product.nr:percent}))
                                                ) v59 ON (v5."nr2m23" = v10."nr2m3" AND v5."nr2m23" = v15."nr2m4" AND v5."nr2m23" = v22."nr2m5" AND v22."producer2m9" = v27."producer2m6" AND v5."nr2m23" = v27."nr2m7" AND v5."nr2m23" = v34."nr2m8" AND v5."nr2m23" = v39."nr2m9" AND v5."nr2m23" = v44."nr2m10" AND v5."nr2m23" = v49."nr2m11" AND v5."nr2m23" = v54."nr2m12" AND v5."nr2m23" = v59."nr2m13")
                                           LEFT OUTER JOIN (
                                                SELECT v60."nr" AS "nr2m2", v60."propertytex4" AS "propertytex1m31"
                                                FROM "ss1"."product1" v60
                                                WHERE (v60."nr" IS NOT NULL AND v60."propertytex4" IS NOT NULL)
                                                UNION ALL
                                                SELECT v62."nr" AS "nr2m2", v62."propertytex4" AS "propertytex1m31"
                                                FROM "ss5"."product2" v62
                                                WHERE (v62."nr" IS NOT NULL AND v62."propertytex4" IS NOT NULL)
                                               ) v64 ON v5."nr2m23" = v64."nr2m2"
                                           LEFT OUTER JOIN (
                                                SELECT v65."nr" AS "nr2m1", v65."propertytex5" AS "propertytex1m30"
                                                FROM "ss1"."product1" v65
                                                WHERE (v65."nr" IS NOT NULL AND v65."propertytex5" IS NOT NULL)
                                                UNION ALL
                                                SELECT v67."nr" AS "nr2m1", v67."propertytex5" AS "propertytex1m30"
                                                FROM "ss5"."product2" v67
                                                WHERE (v67."nr" IS NOT NULL AND v67."propertytex5" IS NOT NULL)
                                           ) v69 ON v5."nr2m23" = v69."nr2m1"
                                           LEFT OUTER JOIN (
                                                SELECT v70."nr" AS "nr2m0", v70."propertynum4" AS "propertynum1m25"
                                                FROM "ss1"."product1" v70
                                                WHERE (v70."nr" IS NOT NULL AND v70."propertynum4" IS NOT NULL)
                                                UNION ALL
                                                SELECT v72."nr" AS "nr2m0", v72."propertynum4" AS "propertynum1m25"
                                                FROM "ss5"."product2" v72
                                                WHERE (v72."nr" IS NOT NULL AND v72."propertynum4" IS NOT NULL)
                                          ) v74 ON v5."nr2m23" = v74."nr2m0"
) v76
