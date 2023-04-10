--00--
SELECT v12."nr2m23" AS "nr2m23", COUNT(*) AS "v1"
FROM (
	  SELECT DISTINCT v5."nr2m23" AS "nr2m23", v10."productfeature2m2" AS "productfeature2m2"
      FROM (
		    SELECT v1."nr" AS "nr2m23"
            FROM "BSBMS1"."product1" v1
            WHERE (v1."nr" IS NOT NULL AND (v1."nr" < 1000))
            UNION ALL
            SELECT v3."nr" AS "nr2m23"
            FROM "BSBMS5"."product2" v3
            WHERE (v3."nr" IS NOT NULL AND (v3."nr" < 1000))
           ) v5, (
			      SELECT v6."product" AS "nr2m0", v6."productfeature" AS "productfeature2m2"
                  FROM "BSBMS1"."productfeatureproduct1" v6
                  WHERE (v6."product" < 1000)
                  UNION ALL
                  SELECT v8."product" AS "nr2m0", v8."productfeature" AS "productfeature2m2"
                  FROM "BSBMS5"."productfeatureproduct2" v8
                  WHERE (v8."product" < 1000)
                ) v10
      WHERE v5."nr2m23" = v10."nr2m0"
    ) v12
GROUP BY v12."nr2m23"

--01--
SELECT DISTINCT v5."label10m46" AS "label10m46", v5."nr0m55" AS "nr0m55"
FROM (
	  SELECT v1."label" AS "label10m46", v1."nr" AS "nr0m55"
      FROM "BSBMS1"."product1" v1
      WHERE (v1."nr" IS NOT NULL AND v1."label" IS NOT NULL)
      UNION ALL
      SELECT v3."label" AS "label10m46", v3."nr" AS "nr0m55"
      FROM "BSBMS5"."product2" v3
      WHERE (v3."nr" IS NOT NULL AND v3."label" IS NOT NULL)
     ) v5, (
		    SELECT v6."nr" AS "nr0m0"
            FROM "BSBMS1"."product1" v6
            WHERE v6."nr" IS NOT NULL
            UNION ALL
            SELECT v8."nr" AS "nr0m0"
            FROM "BSBMS5"."product2" v8
            WHERE v8."nr" IS NOT NULL
           ) v10,  (
			       SELECT v11."product" AS "nr0m1"
                   FROM "BSBMS1"."productfeatureproduct1" v11
                   WHERE 89 = v11."productfeature"
                   UNION ALL
                   SELECT v13."product" AS "nr0m1"
                   FROM "BSBMS5"."productfeatureproduct2" v13
                   WHERE 89 = v13."productfeature"
                   ) v15, (
					       SELECT v16."product" AS "nr0m2"
                           FROM "BSBMS1"."productfeatureproduct1" v16
                           WHERE 91 = v16."productfeature"
                           UNION ALL
                           SELECT v18."product" AS "nr0m2"
                           FROM "BSBMS5"."productfeatureproduct2" v18
                           WHERE 91 = v18."productfeature"
                           ) v20, (
							       SELECT v21."nr" AS "nr0m3", v21."propertynum1" AS "propertynum1m41"
                                   FROM "BSBMS1"."product1" v21
                                   WHERE ((v21."nr" IS NOT NULL AND v21."propertynum1" IS NOT NULL) AND (v21."propertynum1" < 1000))
                                   UNION ALL
                                   SELECT v23."nr" AS "nr0m3", v23."propertynum1" AS "propertynum1m41"
                                   FROM "BSBMS5"."product2" v23
                                   WHERE ((v23."nr" IS NOT NULL AND v23."propertynum1" IS NOT NULL) AND (v23."propertynum1" < 1000))
                                  ) v25
WHERE (v5."nr0m55" = v10."nr0m0" AND v5."nr0m55" = v15."nr0m1" AND v5."nr0m55" = v20."nr0m2" AND v5."nr0m55" = v25."nr0m3")
run time: 372ms

--02--
SELECT v76."comment10m20" AS "comment10m20", v76."label10m18" AS "label10m18", v76."label10m46" AS "label10m46", v76."label10m6" AS "label10m6", v76."propertynum1m25" AS "propertynum1m25", v76."propertynum1m40" AS "propertynum1m40", v76."propertynum1m41" AS "propertynum1m41", v76."propertytex1m30" AS "propertytex1m30", v76."propertytex1m31" AS "propertytex1m31", v76."propertytex1m32" AS "propertytex1m32", v76."propertytex1m33" AS "propertytex1m33", v76."propertytex1m34" AS "propertytex1m34"
FROM (
	  SELECT DISTINCT v15."comment10m20" AS "comment10m20", v34."label10m18" AS "label10m18", v10."label10m46" AS "label10m46", v22."label10m6" AS "label10m6", CASE WHEN v74."propertynum1m25" IS NOT NULL THEN v5."nr2m23" ELSE NULL END AS "nr0m25", CASE WHEN v69."propertytex1m30" IS NOT NULL THEN v5."nr2m23" ELSE NULL END AS "nr0m30", CASE WHEN v64."propertytex1m31" IS NOT NULL THEN v5."nr2m23" ELSE NULL END AS "nr0m31", v5."nr2m23" AS "nr2m23", v22."producer2m9" AS "producer2m9", v34."productfeature2m2" AS "productfeature2m2", v74."propertynum1m25" AS "propertynum1m25", v59."propertynum1m40" AS "propertynum1m40", v54."propertynum1m41" AS "propertynum1m41", v69."propertytex1m30" AS "propertytex1m30", v64."propertytex1m31" AS "propertytex1m31", v49."propertytex1m32" AS "propertytex1m32", v44."propertytex1m33" AS "propertytex1m33", v39."propertytex1m34" AS "propertytex1m34"
      FROM (
		    SELECT v1."nr" AS "nr2m23"
            FROM "BSBMS1"."product1" v1
            WHERE (v1."nr" IS NOT NULL AND (v1."nr" < 1000))
            UNION ALL
            SELECT v3."nr" AS "nr2m23"
            FROM "BSBMS5"."product2" v3
            WHERE (v3."nr" IS NOT NULL AND (v3."nr" < 1000))
          ) v5
          JOIN (
			    SELECT v6."label" AS "label10m46", v6."nr" AS "nr2m3"
                FROM "BSBMS1"."product1" v6
                WHERE ((v6."nr" IS NOT NULL AND v6."label" IS NOT NULL) AND (v6."nr" < 1000))
                UNION ALL
                SELECT v8."label" AS "label10m46", v8."nr" AS "nr2m3"
                FROM "BSBMS5"."product2" v8
                WHERE ((v8."nr" IS NOT NULL AND v8."label" IS NOT NULL) AND (v8."nr" < 1000))
              ) v10 ON 1 = 1
             JOIN (
		           SELECT v11."comment" AS "comment10m20", v11."nr" AS "nr2m4"
                   FROM "BSBMS1"."product1" v11
                   WHERE ((v11."nr" IS NOT NULL AND v11."comment" IS NOT NULL) AND (v11."nr" < 1000))
                   UNION ALL
                   SELECT v13."comment" AS "comment10m20", v13."nr" AS "nr2m4"
                   FROM "BSBMS5"."product2" v13
                   WHERE ((v13."nr" IS NOT NULL AND v13."comment" IS NOT NULL) AND (v13."nr" < 1000))
                  ) v15 ON 1 = 1
                 JOIN (
		               SELECT v17."label" AS "label10m6", v16."nr" AS "nr2m5", v16."producer" AS "producer2m9"
                       FROM "BSBMS1"."product1" v16, "BSBMS4"."producer" v17
                       WHERE (v16."nr" IS NOT NULL AND (v16."nr" < 1000) AND v17."label" IS NOT NULL AND v16."producer" = v17."nr")
                       UNION ALL
                       SELECT v20."label" AS "label10m6", v19."nr" AS "nr2m5", v19."producer" AS "producer2m9"
                       FROM "BSBMS5"."product2" v19, "BSBMS4"."producer" v20
                       WHERE (v19."nr" IS NOT NULL AND (v19."nr" < 1000) AND v20."label" IS NOT NULL AND v19."producer" = v20."nr")
                     ) v22 ON 1 = 1
                    JOIN (
						  SELECT v23."nr" AS "nr2m7", v23."producer" AS "producer2m6"
                          FROM "BSBMS1"."product1" v23
                          WHERE ((v23."nr" IS NOT NULL AND v23."producer" IS NOT NULL) AND (v23."nr" < 1000))
                          UNION ALL
                          SELECT v25."nr" AS "nr2m7", v25."producer" AS "producer2m6"
                          FROM "BSBMS5"."product2" v25
                          WHERE ((v25."nr" IS NOT NULL AND v25."producer" IS NOT NULL) AND (v25."nr" < 1000))
                        ) v27 ON 1 = 1
                        JOIN (
		                      SELECT v29."label" AS "label10m18", v28."product" AS "nr2m8", v28."productfeature" AS "productfeature2m2"
                              FROM "BSBMS1"."productfeatureproduct1" v28, "BSBMS3"."productfeature" v29
                              WHERE ((v28."product" < 1000) AND v29."label" IS NOT NULL AND v28."productfeature" = v29."nr")
                              UNION ALL
                              SELECT v32."label" AS "label10m18", v31."product" AS "nr2m8", v31."productfeature" AS "productfeature2m2"
                              FROM "BSBMS5"."productfeatureproduct2" v31, "BSBMS3"."productfeature" v32
                              WHERE ((v31."product" < 1000) AND v32."label" IS NOT NULL AND v31."productfeature" = v32."nr")
                             ) v34 ON 1 = 1
                             JOIN (
								   SELECT v35."nr" AS "nr2m9", v35."propertytex1" AS "propertytex1m34"
                                   FROM "BSBMS1"."product1" v35
                                   WHERE ((v35."nr" IS NOT NULL AND v35."propertytex1" IS NOT NULL) AND (v35."nr" < 1000))
                                   UNION ALL
                                   SELECT v37."nr" AS "nr2m9", v37."propertytex1" AS "propertytex1m34"
                                   FROM "BSBMS5"."product2" v37
                                   WHERE ((v37."nr" IS NOT NULL AND v37."propertytex1" IS NOT NULL) AND (v37."nr" < 1000))
                                  ) v39 ON 1 = 1
                                  JOIN (
		                                SELECT v40."nr" AS "nr2m10", v40."propertytex2" AS "propertytex1m33"
                                        FROM "BSBMS1"."product1" v40
                                        WHERE ((v40."nr" IS NOT NULL AND v40."propertytex2" IS NOT NULL) AND (v40."nr" < 1000))
                                        UNION ALL
                                        SELECT v42."nr" AS "nr2m10", v42."propertytex2" AS "propertytex1m33"
                                        FROM "BSBMS5"."product2" v42
                                        WHERE ((v42."nr" IS NOT NULL AND v42."propertytex2" IS NOT NULL) AND (v42."nr" < 1000))
                                       ) v44 ON 1 = 1
                                       JOIN (
		                                     SELECT v45."nr" AS "nr2m11", v45."propertytex3" AS "propertytex1m32"
                                             FROM "BSBMS1"."product1" v45
                                             WHERE ((v45."nr" IS NOT NULL AND v45."propertytex3" IS NOT NULL) AND (v45."nr" < 1000))
                                             UNION ALL
                                             SELECT v47."nr" AS "nr2m11", v47."propertytex3" AS "propertytex1m32"
                                             FROM "BSBMS5"."product2" v47
                                             WHERE ((v47."nr" IS NOT NULL AND v47."propertytex3" IS NOT NULL) AND (v47."nr" < 1000))
                                            ) v49 ON 1 = 1
                                            JOIN (
												  SELECT v50."nr" AS "nr2m12", v50."propertynum1" AS "propertynum1m41"
                                                  FROM "BSBMS1"."product1" v50
                                                  WHERE ((v50."nr" IS NOT NULL AND v50."propertynum1" IS NOT NULL) AND (v50."nr" < 1000))
                                                  UNION ALL
                                                  SELECT v52."nr" AS "nr2m12", v52."propertynum1" AS "propertynum1m41"
                                                  FROM "BSBMS5"."product2" v52
                                                  WHERE ((v52."nr" IS NOT NULL AND v52."propertynum1" IS NOT NULL) AND (v52."nr" < 1000))
                                           ) v54 ON 1 = 1
                                           JOIN (
                                                 SELECT v55."nr" AS "nr2m13", v55."propertynum2" AS "propertynum1m40"
                                                 FROM "BSBMS1"."product1" v55
                                                 WHERE ((v55."nr" IS NOT NULL AND v55."propertynum2" IS NOT NULL) AND (v55."nr" < 1000))
                                                 UNION ALL
                                                 SELECT v57."nr" AS "nr2m13", v57."propertynum2" AS "propertynum1m40"
                                                 FROM "BSBMS5"."product2" v57
                                                 WHERE ((v57."nr" IS NOT NULL AND v57."propertynum2" IS NOT NULL) AND (v57."nr" < 1000))
                                                ) v59 ON (v5."nr2m23" = v10."nr2m3" AND v5."nr2m23" = v15."nr2m4" AND v5."nr2m23" = v22."nr2m5" AND v22."producer2m9" = v27."producer2m6" AND v5."nr2m23" = v27."nr2m7" AND v5."nr2m23" = v34."nr2m8" AND v5."nr2m23" = v39."nr2m9" AND v5."nr2m23" = v44."nr2m10" AND v5."nr2m23" = v49."nr2m11" AND v5."nr2m23" = v54."nr2m12" AND v5."nr2m23" = v59."nr2m13")
                                           LEFT OUTER JOIN (
                                                SELECT v60."nr" AS "nr2m2", v60."propertytex4" AS "propertytex1m31"
                                                FROM "BSBMS1"."product1" v60
                                                WHERE (v60."nr" IS NOT NULL AND v60."propertytex4" IS NOT NULL)
                                                UNION ALL
                                                SELECT v62."nr" AS "nr2m2", v62."propertytex4" AS "propertytex1m31"
                                                FROM "BSBMS5"."product2" v62
                                                WHERE (v62."nr" IS NOT NULL AND v62."propertytex4" IS NOT NULL)
                                               ) v64 ON v5."nr2m23" = v64."nr2m2"
                                           LEFT OUTER JOIN (
                                                SELECT v65."nr" AS "nr2m1", v65."propertytex5" AS "propertytex1m30"
                                                FROM "BSBMS1"."product1" v65
                                                WHERE (v65."nr" IS NOT NULL AND v65."propertytex5" IS NOT NULL)
                                                UNION ALL
                                                SELECT v67."nr" AS "nr2m1", v67."propertytex5" AS "propertytex1m30"
                                                FROM "BSBMS5"."product2" v67
                                                WHERE (v67."nr" IS NOT NULL AND v67."propertytex5" IS NOT NULL)
                                           ) v69 ON v5."nr2m23" = v69."nr2m1"
                                           LEFT OUTER JOIN (
                                                SELECT v70."nr" AS "nr2m0", v70."propertynum4" AS "propertynum1m25"
                                                FROM "BSBMS1"."product1" v70
                                                WHERE (v70."nr" IS NOT NULL AND v70."propertynum4" IS NOT NULL)
                                                UNION ALL
                                                SELECT v72."nr" AS "nr2m0", v72."propertynum4" AS "propertynum1m25"
                                                FROM "BSBMS5"."product2" v72
                                                WHERE (v72."nr" IS NOT NULL AND v72."propertynum4" IS NOT NULL)
                                          ) v74 ON v5."nr2m23" = v74."nr2m0"
) v76

--03--
SELECT v37."label10m46" AS "label10m46", v37."nr0m55" AS "nr0m55"
FROM (
	  SELECT DISTINCT v35."label10m10" AS "label10m10", v5."label10m46" AS "label10m46", v5."nr0m55" AS "nr0m55", CASE WHEN v35."label10m10" IS NOT NULL THEN v5."nr0m55" ELSE NULL END AS "product0m4", v25."propertynum1m26" AS "propertynum1m26", v20."propertynum1m41" AS "propertynum1m41"
      FROM (
		    SELECT v1."label" AS "label10m46", v1."nr" AS "nr0m55"
            FROM "BSBMS1"."product1" v1
            WHERE (v1."nr" IS NOT NULL AND v1."label" IS NOT NULL)
            UNION ALL
            SELECT v3."label" AS "label10m46", v3."nr" AS "nr0m55"
            FROM "BSBMS5"."product2" v3
            WHERE (v3."nr" IS NOT NULL AND v3."label" IS NOT NULL)
          ) v5
         JOIN (
			   SELECT v6."nr" AS "nr0m1"
               FROM "BSBMS1"."product1" v6
               WHERE v6."nr" IS NOT NULL
               UNION ALL
               SELECT v8."nr" AS "nr0m1"
               FROM "BSBMS5"."product2" v8
               WHERE v8."nr" IS NOT NULL
             ) v10 ON 1 = 1
            JOIN (
				  SELECT v11."product" AS "nr0m2"
                  FROM "BSBMS1"."productfeatureproduct1" v11
                  WHERE 89 = v11."productfeature"
                  UNION ALL
                  SELECT v13."product" AS "nr0m2"
                  FROM "BSBMS5"."productfeatureproduct2" v13
                  WHERE 89 = v13."productfeature"
                 ) v15 ON 1 = 1
                 JOIN (
					   SELECT v16."nr" AS "nr0m3", v16."propertynum1" AS "propertynum1m41"
                       FROM "BSBMS1"."product1" v16
                       WHERE ((v16."nr" IS NOT NULL AND v16."propertynum1" IS NOT NULL) AND (v16."propertynum1" > 10))
                       UNION ALL
                       SELECT v18."nr" AS "nr0m3", v18."propertynum1" AS "propertynum1m41"
                       FROM "BSBMS5"."product2" v18
                       WHERE ((v18."nr" IS NOT NULL AND v18."propertynum1" IS NOT NULL) AND (v18."propertynum1" > 10))
                      ) v20 ON 1 = 1
                      JOIN (
						    SELECT v21."nr" AS "nr0m4", v21."propertynum3" AS "propertynum1m26"
                            FROM "BSBMS1"."product1" v21
                            WHERE ((v21."nr" IS NOT NULL AND v21."propertynum3" IS NOT NULL) AND (v21."propertynum3" < 5000))
                            UNION ALL
                            SELECT v23."nr" AS "nr0m4", v23."propertynum3" AS "propertynum1m26"
                            FROM "BSBMS5"."product2" v23
                            WHERE ((v23."nr" IS NOT NULL AND v23."propertynum3" IS NOT NULL) AND (v23."propertynum3" < 5000))
                           ) v25 ON (v5."nr0m55" = v10."nr0m1" AND v5."nr0m55" = v15."nr0m2" AND v5."nr0m55" = v20."nr0m3" AND v5."nr0m55" = v25."nr0m4")
                          LEFT OUTER JOIN (
							   SELECT v26."product" AS "nr0m0"
                               FROM "BSBMS1"."productfeatureproduct1" v26
                               WHERE 91 = v26."productfeature"
                               UNION ALL
                               SELECT v28."product" AS "nr0m0"
                               FROM "BSBMS5"."productfeatureproduct2" v28
                               WHERE 91 = v28."productfeature"
                             ) v30
                            JOIN (
								  SELECT v31."label" AS "label10m10", v31."nr" AS "nr0m7"
                                  FROM "BSBMS1"."product1" v31
                                  WHERE (v31."nr" IS NOT NULL AND v31."label" IS NOT NULL)
                                  UNION ALL
                                  SELECT v33."label" AS "label10m10", v33."nr" AS "nr0m7"
                                  FROM "BSBMS5"."product2" v33
                                  WHERE (v33."nr" IS NOT NULL AND v33."label" IS NOT NULL)
                                 ) v35 ON v30."nr0m0" = v35."nr0m7"  ON v5."nr0m55" = v30."nr0m0"
                            WHERE v35."label10m10" IS NULL
) v37

--04--
SELECT DISTINCT v63."label10m11" AS "label10m11", v63."nr0m10" AS "nr0m10", v63."propertytex1m12" AS "propertytex1m12"
FROM (
	  SELECT DISTINCT v5."label10m11" AS "label10m11", v5."nr0m10" AS "nr0m10", v25."propertytex1m12" AS "propertytex1m12"
      FROM (
		    SELECT v1."label" AS "label10m11", v1."nr" AS "nr0m10"
            FROM "BSBMS1"."product1" v1
            WHERE (v1."nr" IS NOT NULL AND v1."label" IS NOT NULL)
            UNION ALL
            SELECT v3."label" AS "label10m11", v3."nr" AS "nr0m10"
            FROM "BSBMS5"."product2" v3
            WHERE (v3."nr" IS NOT NULL AND v3."label" IS NOT NULL)
           ) v5, (
			      SELECT v6."nr" AS "nr0m0"
                  FROM "BSBMS1"."product1" v6
                  WHERE v6."nr" IS NOT NULL
                  UNION ALL
                  SELECT v8."nr" AS "nr0m0"
                  FROM "BSBMS5"."product2" v8
                  WHERE v8."nr" IS NOT NULL
                  ) v10, (
					      SELECT v11."product" AS "nr0m1"
                          FROM "BSBMS1"."productfeatureproduct1" v11
                          WHERE 89 = v11."productfeature"
                          UNION ALL
                          SELECT v13."product" AS "nr0m1"
                          FROM "BSBMS5"."productfeatureproduct2" v13
                          WHERE 89 = v13."productfeature"
                          ) v15, (
							      SELECT v16."product" AS "nr0m2"
                                  FROM "BSBMS1"."productfeatureproduct1" v16
                                  WHERE 91 = v16."productfeature"
                                  UNION ALL
                                  SELECT v18."product" AS "nr0m2"
                                  FROM "BSBMS5"."productfeatureproduct2" v18
                                  WHERE 91 = v18."productfeature"
                                 ) v20, (
									     SELECT v21."nr" AS "nr0m3", v21."propertytex1" AS "propertytex1m12"
                                         FROM "BSBMS1"."product1" v21
                                         WHERE (v21."nr" IS NOT NULL AND v21."propertytex1" IS NOT NULL)
                                         UNION ALL
                                         SELECT v23."nr" AS "nr0m3", v23."propertytex1" AS "propertytex1m12"
                                         FROM "BSBMS5"."product2" v23
                                         WHERE (v23."nr" IS NOT NULL AND v23."propertytex1" IS NOT NULL)
                                         ) v25, (
											     SELECT v26."nr" AS "nr0m4", v26."propertynum1" AS "propertynum1m41"
                                                 FROM "BSBMS1"."product1" v26
                                                 WHERE ((v26."nr" IS NOT NULL AND v26."propertynum1" IS NOT NULL) AND (v26."propertynum1" > 30))
                                                 UNION ALL
                                                 SELECT v28."nr" AS "nr0m4", v28."propertynum1" AS "propertynum1m41"
                                                 FROM "BSBMS5"."product2" v28
                                                 WHERE ((v28."nr" IS NOT NULL AND v28."propertynum1" IS NOT NULL) AND (v28."propertynum1" > 30))
                                                ) v30
       WHERE (v5."nr0m10" = v10."nr0m0" AND v5."nr0m10" = v15."nr0m1" AND v5."nr0m10" = v20."nr0m2" AND v5."nr0m10" = v25."nr0m3" AND v5."nr0m10" = v30."nr0m4")
       UNION ALL
       SELECT DISTINCT v36."label10m11" AS "label10m11", v36."nr0m10" AS "nr0m10", v56."propertytex1m12" AS "propertytex1m12"
       FROM (
			 SELECT v32."label" AS "label10m11", v32."nr" AS "nr0m10"
             FROM "BSBMS1"."product1" v32
             WHERE (v32."nr" IS NOT NULL AND v32."label" IS NOT NULL)
             UNION ALL
             SELECT v34."label" AS "label10m11", v34."nr" AS "nr0m10"
             FROM "BSBMS5"."product2" v34
             WHERE (v34."nr" IS NOT NULL AND v34."label" IS NOT NULL)
            ) v36, (
				    SELECT v37."nr" AS "nr0m9"
                    FROM "BSBMS1"."product1" v37
                    WHERE v37."nr" IS NOT NULL
                    UNION ALL
                    SELECT v39."nr" AS "nr0m9"
                    FROM "BSBMS5"."product2" v39
                    WHERE v39."nr" IS NOT NULL
                   ) v41, (
						   SELECT v42."product" AS "nr0m11"
                           FROM "BSBMS1"."productfeatureproduct1" v42
                           WHERE 89 = v42."productfeature"
                           UNION ALL
                           SELECT v44."product" AS "nr0m11"
                           FROM "BSBMS5"."productfeatureproduct2" v44
                           WHERE 89 = v44."productfeature"
                           ) v46, (
								   SELECT v47."product" AS "nr0m12"
                                   FROM "BSBMS1"."productfeatureproduct1" v47
                                   WHERE 86 = v47."productfeature"
                                   UNION ALL
                                   SELECT v49."product" AS "nr0m12"
                                   FROM "BSBMS5"."productfeatureproduct2" v49
                                   WHERE 86 = v49."productfeature"
                                  ) v51, (
										  SELECT v52."nr" AS "nr0m13", v52."propertytex1" AS "propertytex1m12"
                                          FROM "BSBMS1"."product1" v52
                                          WHERE (v52."nr" IS NOT NULL AND v52."propertytex1" IS NOT NULL)
                                          UNION ALL
                                          SELECT v54."nr" AS "nr0m13", v54."propertytex1" AS "propertytex1m12"
                                          FROM "BSBMS5"."product2" v54
                                          WHERE (v54."nr" IS NOT NULL AND v54."propertytex1" IS NOT NULL)
                                         ) v56, (
											     SELECT v57."nr" AS "nr0m14", v57."propertynum2" AS "propertynum1m40"
                                                 FROM "BSBMS1"."product1" v57
                                                 WHERE ((v57."nr" IS NOT NULL AND v57."propertynum2" IS NOT NULL) AND (v57."propertynum2" > 50))
                                                 UNION ALL
                                                 SELECT v59."nr" AS "nr0m14", v59."propertynum2" AS "propertynum1m40"
                                                 FROM "BSBMS5"."product2" v59
                                                 WHERE ((v59."nr" IS NOT NULL AND v59."propertynum2" IS NOT NULL) AND (v59."propertynum2" > 50))
                                                ) v61
       WHERE (v36."nr0m10" = v41."nr0m9" AND v36."nr0m10" = v46."nr0m11" AND v36."nr0m10" = v51."nr0m12" AND v36."nr0m10" = v56."nr0m13" AND v36."nr0m10" = v61."nr0m14")
) v63

--05--
SELECT v37."label10m46" AS "label10m46", v37."product0m4" AS "product0m4"
FROM (
	  SELECT DISTINCT v5."label10m46" AS "label10m46", v5."product0m4" AS "product0m4", v10."productfeature2m2" AS "productfeature2m2", v25."propertynum1m10" AS "propertynum1m10", v35."propertynum1m15" AS "propertynum1m15", v30."propertynum1m40" AS "propertynum1m40", v20."propertynum1m41" AS "propertynum1m41"
      FROM (
		    SELECT v1."label" AS "label10m46", v1."nr" AS "product0m4"
            FROM "BSBMS1"."product1" v1
            WHERE ((v1."nr" IS NOT NULL AND v1."label" IS NOT NULL) AND v1."nr" <> 88)
            UNION ALL
            SELECT v3."label" AS "label10m46", v3."nr" AS "product0m4"
            FROM "BSBMS5"."product2" v3
            WHERE ((v3."nr" IS NOT NULL AND v3."label" IS NOT NULL) AND v3."nr" <> 88)
           ) v5, (
			      SELECT v6."productfeature" AS "productfeature2m2"
                  FROM "BSBMS1"."productfeatureproduct1" v6
                  WHERE 88 = v6."product"
                  UNION ALL
                  SELECT v8."productfeature" AS "productfeature2m2"
                  FROM "BSBMS5"."productfeatureproduct2" v8
                  WHERE 88 = v8."product"
                 ) v10, (
					     SELECT v11."product" AS "product0m0", v11."productfeature" AS "productfeature2m1"
                         FROM "BSBMS1"."productfeatureproduct1" v11
                         WHERE v11."product" <> 88
                         UNION ALL
                         SELECT v13."product" AS "product0m0", v13."productfeature" AS "productfeature2m1"
                         FROM "BSBMS5"."productfeatureproduct2" v13
                         WHERE v13."product" <> 88
                         ) v15, (
							     SELECT v16."propertynum1" AS "propertynum1m41"
                                 FROM "BSBMS1"."product1" v16
                                 WHERE (v16."propertynum1" IS NOT NULL AND 88 = v16."nr")
                                 UNION ALL
                                 SELECT v18."propertynum1" AS "propertynum1m41"
                                 FROM "BSBMS5"."product2" v18
                                 WHERE (v18."propertynum1" IS NOT NULL AND 88 = v18."nr")
                                ) v20, (
									    SELECT v21."nr" AS "product0m2", v21."propertynum1" AS "propertynum1m10"
                                        FROM "BSBMS1"."product1" v21
                                        WHERE ((v21."nr" IS NOT NULL AND v21."propertynum1" IS NOT NULL) AND v21."nr" <> 88)
                                        UNION ALL
                                        SELECT v23."nr" AS "product0m2", v23."propertynum1" AS "propertynum1m10"
                                        FROM "BSBMS5"."product2" v23
                                        WHERE ((v23."nr" IS NOT NULL AND v23."propertynum1" IS NOT NULL) AND v23."nr" <> 88)
                                       ) v25, (SELECT v26."propertynum2" AS "propertynum1m40"
                                               FROM "BSBMS1"."product1" v26
                                               WHERE (v26."propertynum2" IS NOT NULL AND 88 = v26."nr")
                                               UNION ALL
                                               SELECT v28."propertynum2" AS "propertynum1m40"
                                               FROM "BSBMS5"."product2" v28
                                               WHERE (v28."propertynum2" IS NOT NULL AND 88 = v28."nr")
                                              ) v30, (
												      SELECT v31."nr" AS "product0m3", v31."propertynum2" AS "propertynum1m15"
                                                      FROM "BSBMS1"."product1" v31
                                                      WHERE ((v31."nr" IS NOT NULL AND v31."propertynum2" IS NOT NULL) AND v31."nr" <> 88)
                                                      UNION ALL
                                                      SELECT v33."nr" AS "product0m3", v33."propertynum2" AS "propertynum1m15"
                                                      FROM "BSBMS5"."product2" v33
                                                      WHERE ((v33."nr" IS NOT NULL AND v33."propertynum2" IS NOT NULL) AND v33."nr" <> 88)
                                                      ) v35
       WHERE ((v35."propertynum1m15" < (v30."propertynum1m40" + 170)) AND (v25."propertynum1m10" < (v20."propertynum1m41" + 120)) AND v5."product0m4" = v15."product0m0" AND v10."productfeature2m2" = v15."productfeature2m1" AND v5."product0m4" = v25."product0m2" AND v5."product0m4" = v35."product0m3")
) v37

--06--
NA

--07--
SELECT v36."label10m4" AS "label10m4", v36."label10m46" AS "label10m46", v36."name1m12" AS "name1m12", v36."nr0m4" AS "nr0m4", v36."nr1m5" AS "nr1m5", v36."person2m7" AS "person2m7", v36."price1m39" AS "price1m39", v36."rating1m16" AS "rating1m16", v36."rating1m17" AS "rating1m17", v36."title2m11" AS "title2m11", v36."vendor1m8" AS "vendor1m8"
FROM (
      SELECT DISTINCT v7."label" AS "label10m4", v5."label10m46" AS "label10m46", v19."name1m12" AS "name1m12",
                      CASE WHEN v29."rating1m16" IS NOT NULL THEN v12."nr0m4" ELSE NULL END AS "nr0m16",
                      CASE WHEN v34."rating1m17" IS NOT NULL THEN v12."nr0m4" ELSE NULL END AS "nr0m17", v12."nr0m4" AS "nr0m4",
                      v6."nr" AS "nr1m5", v19."person2m7" AS "person2m7", v6."price" AS "price1m39", v29."rating1m16" AS "rating1m16",
                      v34."rating1m17" AS "rating1m17", v24."title2m11" AS "title2m11", v6."validto" AS "validto1m45",
                      v6."vendor" AS "vendor1m8"
      FROM (
            SELECT v1."label" AS "label10m46"
            FROM "BSBMS1"."product1" v1
            WHERE (v1."label" IS NOT NULL AND 88 = v1."nr")
            UNION ALL
            SELECT v3."label" AS "label10m46"
            FROM "BSBMS5"."product2" v3
            WHERE (v3."label" IS NOT NULL AND 88 = v3."nr")
           ) v5
      LEFT OUTER JOIN
      "BSBMS4"."offer" v6
      JOIN
      "BSBMS4"."vendor" v7 ON ((v6."validto" > '1988-01-01') AND v6."price" IS NOT NULL AND v7."label" IS NOT NULL AND v6."validto" IS NOT NULL AND v6."vendor" = v7."nr" AND 88 = v6."product" AND 'DE' = v7."country")  ON 1 = 1
      LEFT OUTER JOIN
      (
       SELECT v8."nr" AS "nr0m4"
       FROM "BSBMS1"."review1" v8
       WHERE 88 = v8."product"
       UNION ALL
       SELECT v10."nr" AS "nr0m4"
       FROM "BSBMS2"."review" v10
       WHERE 88 = v10."product"
       ) v12
      JOIN
      (
       SELECT v14."name" AS "name1m12", v13."nr" AS "nr0m7", v13."person" AS "person2m7"
       FROM "BSBMS1"."review1" v13, "BSBMS2"."person" v14
       WHERE (v14."name" IS NOT NULL AND v13."person" = v14."nr")
       UNION ALL
       SELECT v17."name" AS "name1m12", v16."nr" AS "nr0m7", v16."person" AS "person2m7"
       FROM "BSBMS2"."review" v16, "BSBMS2"."person" v17
       WHERE (v17."name" IS NOT NULL AND v16."person" = v17."nr")
     ) v19 ON 1 = 1
     JOIN
    (
      SELECT v20."nr" AS "nr0m8", v20."title" AS "title2m11"
      FROM "BSBMS1"."review1" v20
      WHERE v20."title" IS NOT NULL
      UNION ALL
      SELECT v22."nr" AS "nr0m8", v22."title" AS "title2m11"
      FROM "BSBMS2"."review" v22
    WHERE v22."title" IS NOT NULL
    ) v24 ON (v12."nr0m4" = v19."nr0m7" AND v12."nr0m4" = v24."nr0m8")
    LEFT OUTER JOIN
    (
      SELECT v25."nr" AS "nr0m6", v25."rating1" AS "rating1m16"
      FROM "BSBMS1"."review1" v25
      WHERE v25."rating1" IS NOT NULL
      UNION ALL
      SELECT v27."nr" AS "nr0m6", v27."rating1" AS "rating1m16"
      FROM "BSBMS2"."review" v27
      WHERE v27."rating1" IS NOT NULL
    ) v29 ON v12."nr0m4" = v29."nr0m6"
    LEFT OUTER JOIN
    (
      SELECT v30."nr" AS "nr0m5", v30."rating2" AS "rating1m17"
      FROM "BSBMS1"."review1" v30
      WHERE v30."rating2" IS NOT NULL
      UNION ALL
      SELECT v32."nr" AS "nr0m5", v32."rating2" AS "rating1m17"
      FROM "BSBMS2"."review" v32
      WHERE v32."rating2" IS NOT NULL
     ) v34 ON v12."nr0m4" = v34."nr0m5"  ON 1 = 1
) v36

--08--
SELECT v49."name1m12" AS "name1m12", v49."person2m7" AS "person2m7", v49."rating1m13" AS "rating1m13", v49."rating1m15" AS "rating1m15", v49."rating1m16" AS "rating1m16", v49."rating1m17" AS "rating1m17", v49."reviewdate2m43" AS "reviewdate2m43", v49."text2m14" AS "text2m14", v49."title2m11" AS "title2m11"
FROM (
    SELECT DISTINCT v27."name1m12" AS "name1m12", CASE WHEN v42."rating1m13" IS NOT NULL THEN v5."nr0m4" ELSE NULL END AS "nr0m13",
                   CASE WHEN v47."rating1m15" IS NOT NULL THEN v5."nr0m4" ELSE NULL END AS "nr0m15", CASE WHEN v32."rating1m16" IS NOT NULL THEN v5."nr0m4" ELSE NULL END AS "nr0m16",
                   CASE WHEN v37."rating1m17" IS NOT NULL THEN v5."nr0m4" ELSE NULL END AS "nr0m17", v5."nr0m4" AS "nr0m4",
                   v27."person2m7" AS "person2m7", v42."rating1m13" AS "rating1m13", v47."rating1m15" AS "rating1m15", v32."rating1m16" AS "rating1m16",
                  v37."rating1m17" AS "rating1m17", v20."reviewdate2m43" AS "reviewdate2m43", v15."text2m14" AS "text2m14", v10."title2m11" AS "title2m11"
    FROM (
          SELECT v1."nr" AS "nr0m4"
          FROM "BSBMS1"."review1" v1
          WHERE 88 = v1."product"
          UNION ALL
          SELECT v3."nr" AS "nr0m4"
          FROM "BSBMS2"."review" v3
          WHERE 88 = v3."product"
         ) v5
    JOIN
        (
         SELECT v6."nr" AS "nr0m5", v6."title" AS "title2m11"
         FROM "BSBMS1"."review1" v6
         WHERE v6."title" IS NOT NULL
         UNION ALL
         SELECT v8."nr" AS "nr0m5", v8."title" AS "title2m11"
         FROM "BSBMS2"."review" v8
         WHERE v8."title" IS NOT NULL
        ) v10 ON 1 = 1
        JOIN
            (
             SELECT v11."nr" AS "nr0m6", v11."text" AS "text2m14"
             FROM "BSBMS1"."review1" v11
             WHERE v11."text" IS NOT NULL
             UNION ALL
             SELECT v13."nr" AS "nr0m6", v13."text" AS "text2m14"
             FROM "BSBMS2"."review" v13
             WHERE v13."text" IS NOT NULL
            ) v15 ON 1 = 1
            JOIN
                (
                 SELECT v16."nr" AS "nr0m7", v16."reviewdate" AS "reviewdate2m43"
                 FROM "BSBMS1"."review1" v16
                 WHERE v16."reviewdate" IS NOT NULL
                 UNION ALL
                 SELECT v18."nr" AS "nr0m7", v18."reviewdate" AS "reviewdate2m43"
                 FROM "BSBMS2"."review" v18
                 WHERE v18."reviewdate" IS NOT NULL
                ) v20 ON 1 = 1
                JOIN
                    (
                     SELECT v22."name" AS "name1m12", v21."nr" AS "nr0m8", v21."person" AS "person2m7"
                     FROM "BSBMS1"."review1" v21, "BSBMS2"."person" v22
                     WHERE (v22."name" IS NOT NULL AND v21."person" = v22."nr")
                     UNION ALL
                     SELECT v25."name" AS "name1m12", v24."nr" AS "nr0m8", v24."person" AS "person2m7"
                     FROM "BSBMS2"."review" v24, "BSBMS2"."person" v25
                     WHERE (v25."name" IS NOT NULL AND v24."person" = v25."nr")
                    ) v27 ON (v5."nr0m4" = v10."nr0m5" AND v5."nr0m4" = v15."nr0m6" AND v5."nr0m4" = v20."nr0m7" AND v5."nr0m4" = v27."nr0m8")
                    LEFT OUTER JOIN
                         (
                          SELECT v28."nr" AS "nr0m3", v28."rating1" AS "rating1m16"
                          FROM "BSBMS1"."review1" v28
                          WHERE v28."rating1" IS NOT NULL
                          UNION ALL
                          SELECT v30."nr" AS "nr0m3", v30."rating1" AS "rating1m16"
                          FROM "BSBMS2"."review" v30
                          WHERE v30."rating1" IS NOT NULL
                          ) v32 ON v5."nr0m4" = v32."nr0m3"
                          LEFT OUTER JOIN
                              (
                               SELECT v33."nr" AS "nr0m2", v33."rating2" AS "rating1m17"
                               FROM "BSBMS1"."review1" v33
                               WHERE v33."rating2" IS NOT NULL
                               UNION ALL
                               SELECT v35."nr" AS "nr0m2", v35."rating2" AS "rating1m17"
                               FROM "BSBMS2"."review" v35
                               WHERE v35."rating2" IS NOT NULL
                              ) v37 ON v5."nr0m4" = v37."nr0m2"
                              LEFT OUTER JOIN
                                   (
                                    SELECT v38."nr" AS "nr0m1", v38."rating3" AS "rating1m13"
                                    FROM "BSBMS1"."review1" v38
                                    WHERE v38."rating3" IS NOT NULL
                                    UNION ALL
                                    SELECT v40."nr" AS "nr0m1", v40."rating3" AS "rating1m13"
                                    FROM "BSBMS2"."review" v40
                                    WHERE v40."rating3" IS NOT NULL
                                   ) v42 ON v5."nr0m4" = v42."nr0m1"
                                   LEFT OUTER JOIN
                                       (
                                        SELECT v43."nr" AS "nr0m0", v43."rating4" AS "rating1m15"
                                        FROM "BSBMS1"."review1" v43
                                        WHERE v43."rating4" IS NOT NULL
                                        UNION ALL
                                        SELECT v45."nr" AS "nr0m0", v45."rating4" AS "rating1m15"
                                        FROM "BSBMS2"."review" v45
                                        WHERE v45."rating4" IS NOT NULL
                                       ) v47 ON v5."nr0m4" = v47."nr0m0"
) v49

--09--
SELECT v24."country3m1" AS "country3m1", v24."mbox_sha1sum1m47" AS "mbox_sha1sum1m47", v24."nr0m4" AS "nr0m4", v24."person2m7" AS "person2m7", v24."product2m4" AS "product2m4", v24."title2m11" AS "title2m11"
FROM (
      SELECT DISTINCT v7."country3m1" AS "country3m1", v7."mbox_sha1sum1m47" AS "mbox_sha1sum1m47", v7."name1m12" AS "name1m12", v12."nr0m4" AS "nr0m4", v7."person2m7" AS "person2m7", v17."product2m4" AS "product2m4", v22."title2m11" AS "title2m11"
      FROM (
            SELECT v2."country" AS "country3m1", v2."mbox_sha1sum" AS "mbox_sha1sum1m47", v2."name" AS "name1m12", v1."person" AS "person2m7"
            FROM "BSBMS1"."review1" v1, "BSBMS2"."person" v2
            WHERE (v2."name" IS NOT NULL AND v2."mbox_sha1sum" IS NOT NULL AND v2."country" IS NOT NULL AND v1."person" = v2."nr" AND 88 = v1."nr")
            UNION ALL
            SELECT v5."country" AS "country3m1", v5."mbox_sha1sum" AS "mbox_sha1sum1m47", v5."name" AS "name1m12", v4."person" AS "person2m7"
            FROM "BSBMS2"."review" v4, "BSBMS2"."person" v5
            WHERE (v5."name" IS NOT NULL AND v5."mbox_sha1sum" IS NOT NULL AND v5."country" IS NOT NULL AND v4."person" = v5."nr" AND 88 = v4."nr")
           ) v7, (
                  SELECT v8."nr" AS "nr0m4", v8."person" AS "person2m0"
                  FROM "BSBMS1"."review1" v8
                  WHERE v8."person" IS NOT NULL
                  UNION ALL
                  SELECT v10."nr" AS "nr0m4", v10."person" AS "person2m0"
                  FROM "BSBMS2"."review" v10
                  WHERE v10."person" IS NOT NULL
                 ) v12, (
                         SELECT v13."nr" AS "nr0m1", v13."product" AS "product2m4"
                         FROM "BSBMS1"."review1" v13
                         WHERE v13."product" IS NOT NULL
                         UNION ALL
                         SELECT v15."nr" AS "nr0m1", v15."product" AS "product2m4"
                         FROM "BSBMS2"."review" v15
                         WHERE v15."product" IS NOT NULL
                        ) v17, (
                                SELECT v18."nr" AS "nr0m2", v18."title" AS "title2m11"
                                FROM "BSBMS1"."review1" v18
                                WHERE v18."title" IS NOT NULL
                                UNION ALL
                                SELECT v20."nr" AS "nr0m2", v20."title" AS "title2m11"
                                FROM "BSBMS2"."review" v20
                                WHERE v20."title" IS NOT NULL
                               ) v22
           WHERE (v7."person2m7" = v12."person2m0" AND v12."nr0m4" = v17."nr0m1" AND v12."nr0m4" = v22."nr0m2")
) v24

--10--
SELECT DISTINCT v9."nr1m5" AS "nr1m5", v9."product1m5" AS "product1m5"
FROM (
      SELECT v2."deliverydays" AS "deliverydays1m37", v2."nr" AS "nr1m5", v2."price" AS "price1m39", v1."nr" AS "product1m5", v2."validto" AS "validto1m45"
      FROM "BSBMS1"."product1" v1, "BSBMS4"."offer" v2, "BSBMS4"."vendor" v3
      WHERE ((v2."validto" > '1988-01-01') AND (v2."deliverydays" <= 3) AND (v1."nr" < 100) AND v2."deliverydays" IS NOT NULL AND v2."price" IS NOT NULL AND v2."validto" IS NOT NULL AND v1."nr" = v2."product" AND v2."vendor" = v3."nr" AND 'US' = v3."country")
      UNION ALL
      SELECT v6."deliverydays" AS "deliverydays1m37", v6."nr" AS "nr1m5", v6."price" AS "price1m39", v5."nr" AS "product1m5", v6."validto" AS "validto1m45"
      FROM "BSBMS5"."product2" v5, "BSBMS4"."offer" v6, "BSBMS4"."vendor" v7
      WHERE ((v6."validto" > '1988-01-01') AND (v6."deliverydays" <= 3) AND (v5."nr" < 100) AND v6."deliverydays" IS NOT NULL AND v6."price" IS NOT NULL AND v6."validto" IS NOT NULL AND v5."nr" = v6."product" AND v6."vendor" = v7."nr" AND 'US' = v7."country")
) v9

--11--
SELECT v25."v26" AS "v26", v25."v6" AS "v6", v25."v9" AS "v9"
FROM (SELECT 'http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/Offer' AS "v26", 0 AS "v6", 'http://www.w3.org/1999/02/22-rdf-syntax-ns#type' AS "v9"
FROM "BSBMS4"."offer" v1
WHERE 88 = v1."nr"
UNION ALL
SELECT 'http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/Product' AS "v26", 0 AS "v6", 'http://www.w3.org/1999/02/22-rdf-syntax-ns#type' AS "v9"
FROM "BSBMS4"."offer" v3
WHERE (v3."vendor" IS NOT NULL AND 88 = v3."nr")
UNION ALL
SELECT ('http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer/Product' || CAST(v5."product" AS STRING)) AS "v26", 0 AS "v6", 'http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/product' AS "v9"
FROM "BSBMS4"."offer" v5
WHERE (v5."product" IS NOT NULL AND 88 = v5."nr")
UNION ALL
SELECT ('http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor/Vendor' || CAST(v7."vendor" AS STRING)) AS "v26", 0 AS "v6", 'http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/vendor' AS "v9"
FROM "BSBMS4"."offer" v7
WHERE (v7."vendor" IS NOT NULL AND 88 = v7."nr")
UNION ALL
SELECT ('http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor/Vendor' || CAST(v9."vendor" AS STRING)) AS "v26", 0 AS "v6", 'http://purl.org/dc/elements/1.1/publisher' AS "v9"
FROM "BSBMS4"."offer" v9
WHERE (v9."vendor" IS NOT NULL AND 88 = v9."nr")
UNION ALL
SELECT '88' AS "v26", 1 AS "v6", 'http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/offerId' AS "v9"
FROM "BSBMS4"."offer" v11
WHERE 88 = v11."nr"
UNION ALL
SELECT CAST(v13."publishdate" AS STRING) AS "v26", 2 AS "v6", 'http://purl.org/dc/elements/1.1/date' AS "v9"
FROM "BSBMS4"."offer" v13
WHERE (v13."publishdate" IS NOT NULL AND 88 = v13."nr")
UNION ALL
SELECT v15."offerwebpage" AS "v26", 3 AS "v6", 'http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/offerWebpage' AS "v9"
FROM "BSBMS4"."offer" v15
WHERE (v15."offerwebpage" IS NOT NULL AND 88 = v15."nr")
UNION ALL
SELECT CAST(v17."deliverydays" AS STRING) AS "v26", 1 AS "v6", 'http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/deliveryDays' AS "v9"
FROM "BSBMS4"."offer" v17
WHERE (v17."deliverydays" IS NOT NULL AND 88 = v17."nr")
UNION ALL
SELECT CAST(v19."price" AS STRING) AS "v26", 4 AS "v6", 'http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/price' AS "v9"
FROM "BSBMS4"."offer" v19
WHERE (v19."price" IS NOT NULL AND 88 = v19."nr")
UNION ALL
SELECT CAST(v21."validfrom" AS STRING) AS "v26", 2 AS "v6", 'http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/validFrom' AS "v9"
FROM "BSBMS4"."offer" v21
WHERE (v21."validfrom" IS NOT NULL AND 88 = v21."nr")
UNION ALL
SELECT CAST(v23."validto" AS STRING) AS "v26", 2 AS "v6", 'http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/validTo' AS "v9"
FROM "BSBMS4"."offer" v23
WHERE (v23."validto" IS NOT NULL AND 88 = v23."nr")
) v25

--12--
SELECT v11."deliverydays1m37" AS "deliverydays1m37", v11."label10m4" AS "label10m4", v11."label10m46" AS "label10m46", v11."nr1m21" AS "nr1m21", v11."offerwebpage1m24" AS "offerwebpage1m24", v11."price1m39" AS "price1m39", v11."product1m5" AS "product1m5", v11."validto1m45" AS "validto1m45", v11."vendor1m8" AS "vendor1m8"
FROM (
      SELECT DISTINCT v9."deliverydays1m37" AS "deliverydays1m37", v9."homepage2m48" AS "homepage2m48",
                      v9."label10m4" AS "label10m4", v9."label10m46" AS "label10m46", v9."nr1m21" AS "nr1m21",
                      v9."offerwebpage1m24" AS "offerwebpage1m24", v9."price1m39" AS "price1m39", v9."product1m5" AS "product1m5",
                      v9."validto1m45" AS "validto1m45", v9."vendor1m8" AS "vendor1m8"
    FROM (
          SELECT v2."deliverydays" AS "deliverydays1m37", v3."homepage" AS "homepage2m48", v3."label" AS "label10m4",
                 v1."label" AS "label10m46", v2."nr" AS "nr1m21", v2."offerwebpage" AS "offerwebpage1m24",
                 v2."price" AS "price1m39", v1."nr" AS "product1m5", v2."validto" AS "validto1m45", v2."vendor" AS "vendor1m8"
          FROM "BSBMS1"."product1" v1, "BSBMS4"."offer" v2, "BSBMS4"."vendor" v3
          WHERE (v1."label" IS NOT NULL AND (v2."nr" < 1000) AND v3."label" IS NOT NULL AND v3."homepage" IS NOT NULL AND v2."offerwebpage" IS NOT NULL AND v2."price" IS NOT NULL AND v2."deliverydays" IS NOT NULL AND v2."validto" IS NOT NULL AND v1."nr" = v2."product" AND v2."vendor" = v3."nr")
          UNION ALL
          SELECT v6."deliverydays" AS "deliverydays1m37", v7."homepage" AS "homepage2m48", v7."label" AS "label10m4",
                 v5."label" AS "label10m46", v6."nr" AS "nr1m21", v6."offerwebpage" AS "offerwebpage1m24",
                 v6."price" AS "price1m39", v5."nr" AS "product1m5", v6."validto" AS "validto1m45", v6."vendor" AS "vendor1m8"
          FROM "BSBMS5"."product2" v5, "BSBMS4"."offer" v6, "BSBMS4"."vendor" v7
          WHERE (v5."label" IS NOT NULL AND (v6."nr" < 1000) AND v7."label" IS NOT NULL AND v7."homepage" IS NOT NULL AND v6."offerwebpage" IS NOT NULL AND v6."price" IS NOT NULL AND v6."deliverydays" IS NOT NULL AND v6."validto" IS NOT NULL AND v5."nr" = v6."product" AND v6."vendor" = v7."nr")
    ) v9
) v11