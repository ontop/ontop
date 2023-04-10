--[SQL q0']
--EJP--
SELECT v12."nr2m23" AS "nr2m23", COUNT(*) AS "v1"
FROM (
	  SELECT DISTINCT v1."nr" AS "nr2m23", v6."productfeature" AS "productfeature2m2"
	  FROM "BSBMS1"."product1" v1, "BSBMS1"."productfeatureproduct1" v6
	  WHERE v1."nr"<1000 AND v1."nr"=v6."product"
	  UNION ALL
	  SELECT DISTINCT v3."nr" AS "nr2m23", v8."productfeature" AS "productfeature2m2"
	  FROM "BSBMS5"."product2" v3, "BSBMS5"."productfeatureproduct2" v8
	  WHERE v3."nr"<1000 AND v3."nr"=v8."product"
    ) v12
GROUP BY v12."nr2m23"

--[SQL q1']
--EJP--
(
SELECT DISTINCT v5."label10m46" AS "label10m46", v5."nr0m55" AS "nr0m55"
FROM (
	  SELECT v1."label" AS "label10m46", v1."nr" AS "nr0m55"
      FROM "BSBMS1"."product1" v1
      WHERE (v1."nr" IS NOT NULL AND v1."label" IS NOT NULL AND v1."propertynum1" IS NOT NULL) AND (v1."propertynum1" < 1000)
     ) v5, (
			SELECT v11."product" AS "nr0m1"
            FROM "BSBMS1"."productfeatureproduct1" v11
            WHERE 89 = v11."productfeature"
           ) v15, (
				   SELECT v16."product" AS "nr0m2"
                   FROM "BSBMS1"."productfeatureproduct1" v16
                   WHERE 91 = v16."productfeature"
                  ) v20
WHERE (v5."nr0m55" = v15."nr0m1" AND v5."nr0m55" = v20."nr0m2")
)
UNION ALL
(
SELECT DISTINCT v5."label10m46" AS "label10m46", v5."nr0m55" AS "nr0m55"
FROM (
      SELECT v3."label" AS "label10m46", v3."nr" AS "nr0m55"
      FROM "BSBMS5"."product2" v3
      WHERE (v3."nr" IS NOT NULL AND v3."label" IS NOT NULL AND v3."propertynum1" IS NOT NULL) AND (v3."propertynum1" < 1000)
     ) v5, (
            SELECT v13."product" AS "nr0m1"
            FROM "BSBMS5"."productfeatureproduct2" v13
            WHERE 89 = v13."productfeature"
           ) v15, (
                   SELECT v18."product" AS "nr0m2"
                   FROM "BSBMS5"."productfeatureproduct2" v18
                   WHERE 91 = v18."productfeature"
                  ) v20
WHERE (v5."nr0m55" = v15."nr0m1" AND v5."nr0m55" = v20."nr0m2")
)

--[SQL q2']
--EJP+SJR--
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
            FROM "BSBMS1"."product1" v1, "BSBMS4"."producer" v17, "BSBMS1"."productfeatureproduct1" v28, "BSBMS3"."productfeature" v29
            WHERE (v1."nr" IS NOT NULL AND (v1."nr" < 1000) AND v1."label" IS NOT NULL AND v1."comment" IS NOT NULL AND v1."producer"=v17."nr" AND
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
            FROM "BSBMS5"."product2" v1, "BSBMS4"."producer" v17, "BSBMS5"."productfeatureproduct2" v28, "BSBMS3"."productfeature" v29
            WHERE (v1."nr" IS NOT NULL AND (v1."nr" < 1000) AND v1."label" IS NOT NULL AND v1."comment" IS NOT NULL AND v1."producer"=v17."nr" AND
                   v1."nr"=v28."product" AND v29."label" IS NOT NULL and v28."productfeature"=v29."nr" AND v1."propertytex1" IS NOT NULL AND
                   v1."propertytex2" IS NOT NULL AND v1."propertytex3" IS NOT NULL AND v1."propertynum1" IS NOT NULL AND
                   v1."propertynum2" IS NOT NULL)
          ) v5
      )
) v76
--EJP+SJR+MatMV--
SELECT v76."comment10m20" AS "comment10m20", v76."label10m18" AS "label10m18", v76."label10m46" AS "label10m46", v76."label10m6" AS "label10m6", v76."propertynum1m25" AS "propertynum1m25", v76."propertynum1m40" AS "propertynum1m40", v76."propertynum1m41" AS "propertynum1m41", v76."propertytex1m30" AS "propertytex1m30", v76."propertytex1m31" AS "propertytex1m31", v76."propertytex1m32" AS "propertytex1m32", v76."propertytex1m33" AS "propertytex1m33", v76."propertytex1m34" AS "propertytex1m34"
FROM (
      SELECT v1o."p_nr" AS "nr2m23", v1o."p_label" AS "label10m46", v1o."p_comment" AS "comment10m20", v1o."pd_producer" AS "producer2m9",
             v1o."pd_label" AS "label10m6", v1o."p_propertytex1" AS "propertytex1m34", v1o."p_propertytex2" AS "propertytex1m33",
             v1o."p_propertytex3" AS "propertytex1m32", v1o."p_propertynum1" AS "propertynum1m41", v1o."p_propertynum2" AS "propertynum1m40",
             v1o."p_propertytex4" AS "propertytex1m31", v1o."p_propertytex5" AS "propertytex1m30", v1o."p_propertynum4" AS "propertynum1m25",
             v2o."f_label" AS "label10m18", v2o."productfeature" AS "productfeature2m2"
      FROM "BSBMMV"."ppd1" v1o, "BSBMMV"."pfpf1" v2o
      WHERE (v1o."p_nr" IS NOT NULL AND (v1o."p_nr" < 1000) AND v1o."p_label" IS NOT NULL AND v1o."p_comment" IS NOT NULL AND v1o."p_propertytex1" IS NOT NULL
	   AND v1o."p_propertytex2" IS NOT NULL AND v1o."p_propertytex3" IS NOT NULL AND v1o."p_propertynum1" IS NOT NULL
	   AND v1o."p_propertynum2" IS NOT NULL AND v1o."pd_producer" IS NOT NULL AND (v2o."product"<1000)
	   AND v2o."product"=v1o."p_nr"
	  )
      UNION ALL
      SELECT v1o."p_nr" AS "nr2m23", v1o."p_label" AS "label10m46", v1o."p_comment" AS "comment10m20", v1o."pd_producer" AS "producer2m9",
             v1o."pd_label" AS "label10m6", v1o."p_propertytex1" AS "propertytex1m34", v1o."p_propertytex2" AS "propertytex1m33",
             v1o."p_propertytex3" AS "propertytex1m32", v1o."p_propertynum1" AS "propertynum1m41", v1o."p_propertynum2" AS "propertynum1m40",
             v1o."p_propertytex4" AS "propertytex1m31", v1o."p_propertytex5" AS "propertytex1m30", v1o."p_propertynum4" AS "propertynum1m25",
             v2o."f_label" AS "label10m18", v2o."productfeature" AS "productfeature2m2"
      FROM "BSBMMV"."ppd2" v1o, "BSBMMV"."pfpf2" v2o
      WHERE (v1o."p_nr" IS NOT NULL AND (v1o."p_nr" < 1000) AND v1o."p_label" IS NOT NULL AND v1o."p_comment" IS NOT NULL AND v1o."p_propertytex1" IS NOT NULL
	   AND v1o."p_propertytex2" IS NOT NULL AND v1o."p_propertytex3" IS NOT NULL AND v1o."p_propertynum1" IS NOT NULL
	   AND v1o."p_propertynum2" IS NOT NULL AND v1o."pd_producer" IS NOT NULL AND (v2o."product"<1000)
	   AND v2o."product"=v1o."p_nr"
	  )
) v76

--[SQL q3']
--EJP+SJR--
SELECT v37."label10m46" AS "label10m46", v37."nr0m55" AS "nr0m55"
FROM (
     (
     SELECT DISTINCT v35."label10m10" AS "label10m10", v5."label10m46" AS "label10m46", v5."nr0m55" AS "nr0m55",
     CASE WHEN v35."label10m10" IS NOT NULL THEN v5."nr0m55" ELSE NULL END AS "product0m4",
     v5."propertynum1m26" AS "propertynum1m26", v5."propertynum1m41" AS "propertynum1m41"
      FROM (
		    SELECT v1."label" AS "label10m46", v1."nr" AS "nr0m55", v1."propertynum1" AS "propertynum1m41", v1."propertynum3" AS "propertynum1m26"
            FROM "BSBMS1"."product1" v1
            WHERE (v1."nr" IS NOT NULL AND v1."label" IS NOT NULL AND v1."propertynum1" IS NOT NULL AND (v1."propertynum1">10)
                   AND v1."propertynum3" IS NOT NULL AND (v1."propertynum3"<5000))
          ) v5
            JOIN (
				  SELECT v11."product" AS "nr0m2"
                  FROM "BSBMS1"."productfeatureproduct1" v11
                  WHERE 89 = v11."productfeature"
                 ) v15 ON (v5."nr0m55" = v15."nr0m2")
                      LEFT OUTER JOIN (
							SELECT v26."product" AS "nr0m0"
                            FROM "BSBMS1"."productfeatureproduct1" v26
                            WHERE 91 = v26."productfeature"
                          ) v30
                            JOIN (
								  SELECT v31."label" AS "label10m10", v31."nr" AS "nr0m7"
                                  FROM "BSBMS1"."product1" v31
                                  WHERE (v31."nr" IS NOT NULL AND v31."label" IS NOT NULL)
                                 ) v35 ON v30."nr0m0" = v35."nr0m7"  ON v5."nr0m55" = v30."nr0m0"
                            WHERE v35."label10m10" IS NULL
     )
     UNION ALL
     (
     SELECT DISTINCT v35."label10m10" AS "label10m10", v5."label10m46" AS "label10m46", v5."nr0m55" AS "nr0m55",
     CASE WHEN v35."label10m10" IS NOT NULL THEN v5."nr0m55" ELSE NULL END AS "product0m4",
     v5."propertynum1m26" AS "propertynum1m26", v5."propertynum1m41" AS "propertynum1m41"
      FROM (
            SELECT v3."label" AS "label10m46", v3."nr" AS "nr0m55", v3."propertynum1" AS "propertynum1m41", v3."propertynum3" AS "propertynum1m26"
            FROM "BSBMS5"."product2" v3
            WHERE (v3."nr" IS NOT NULL AND v3."label" IS NOT NULL AND v3."propertynum1" IS NOT NULL AND (v3."propertynum1">10)
                   AND v3."propertynum3" IS NOT NULL AND (v3."propertynum3"<5000))
          ) v5
            JOIN (
                  SELECT v13."product" AS "nr0m2"
                  FROM "BSBMS5"."productfeatureproduct2" v13
                  WHERE 89 = v13."productfeature"
                 ) v15 ON (v5."nr0m55" = v15."nr0m2")
                       LEFT OUTER JOIN (
                               SELECT v28."product" AS "nr0m0"
                               FROM "BSBMS5"."productfeatureproduct2" v28
                               WHERE 91 = v28."productfeature"
                             ) v30
                            JOIN (
                                  SELECT v33."label" AS "label10m10", v33."nr" AS "nr0m7"
                                  FROM "BSBMS5"."product2" v33
                                  WHERE (v33."nr" IS NOT NULL AND v33."label" IS NOT NULL)
                                 ) v35 ON v30."nr0m0" = v35."nr0m7"  ON v5."nr0m55" = v30."nr0m0"
                            WHERE v35."label10m10" IS NULL
     )
) v37

--[SQL q4']
--EPJ--
SELECT DISTINCT v63."label10m11" AS "label10m11", v63."nr0m10" AS "nr0m10", v63."propertytex1m12" AS "propertytex1m12"
FROM (
	  SELECT DISTINCT v5."label10m11" AS "label10m11", v5."nr0m10" AS "nr0m10",
	  v5."propertytex1m12" AS "propertytex1m12"
      FROM (
		    SELECT v1."label" AS "label10m11", v1."nr" AS "nr0m10", v1."propertytex1" AS "propertytex1m12"
            FROM "BSBMS1"."product1" v1
            WHERE (v1."nr" IS NOT NULL AND v1."label" IS NOT NULL AND v1."propertytex1" IS NOT NULL AND v1."propertynum1" IS NOT NULL AND (v1."propertynum1">30))
           ) v5,  (
				   SELECT v11."product" AS "nr0m1"
                   FROM "BSBMS1"."productfeatureproduct1" v11
                   WHERE 89 = v11."productfeature"
                  ) v15, (
						  SELECT v16."product" AS "nr0m2"
                          FROM "BSBMS1"."productfeatureproduct1" v16
                          WHERE 91 = v16."productfeature"
                         ) v20
       WHERE (v5."nr0m10" = v15."nr0m1" AND v5."nr0m10" = v20."nr0m2")
       UNION ALL

      SELECT DISTINCT v5."label10m11" AS "label10m11", v5."nr0m10" AS "nr0m10", v5."propertytex1m12" AS "propertytex1m12"
      FROM (
            SELECT v3."label" AS "label10m11", v3."nr" AS "nr0m10", v3."propertytex1" AS "propertytex1m12"
            FROM "BSBMS5"."product2" v3
            WHERE (v3."nr" IS NOT NULL AND v3."label" IS NOT NULL AND v3."propertytex1" IS NOT NULL AND v3."propertynum1" IS NOT NULL AND (v3."propertynum1">30))
           ) v5, (
                  SELECT v13."product" AS "nr0m1"
                  FROM "BSBMS5"."productfeatureproduct2" v13
                  WHERE 89 = v13."productfeature"
                 ) v15, (
                         SELECT v18."product" AS "nr0m2"
                         FROM "BSBMS5"."productfeatureproduct2" v18
                         WHERE 91 = v18."productfeature"
                        ) v20
       WHERE (v5."nr0m10" = v15."nr0m1" AND v5."nr0m10" = v20."nr0m2")
       UNION ALL
       SELECT DISTINCT v36."label10m11" AS "label10m11", v36."nr0m10" AS "nr0m10",
       v36."propertytex1m12" AS "propertytex1m12"
       FROM (
			 SELECT v32."label" AS "label10m11", v32."nr" AS "nr0m10", v32."propertytex1" AS "propertytex1m12", v32."propertynum2" AS "propertynum1m40"
             FROM "BSBMS1"."product1" v32
             WHERE (v32."nr" IS NOT NULL AND v32."label" IS NOT NULL AND v32."propertytex1" IS NOT NULL AND v32."propertynum2" IS NOT NULL AND (v32."propertynum2" >50))
            ) v36, (
					SELECT v42."product" AS "nr0m11"
                    FROM "BSBMS1"."productfeatureproduct1" v42
                    WHERE 89 = v42."productfeature"
                   ) v46, (
						   SELECT v47."product" AS "nr0m12"
                           FROM "BSBMS1"."productfeatureproduct1" v47
                           WHERE 86 = v47."productfeature"
                          ) v51
       WHERE (v36."nr0m10" = v46."nr0m11" AND v36."nr0m10" = v51."nr0m12")
       UNION ALL
       SELECT DISTINCT v36."label10m11" AS "label10m11", v36."nr0m10" AS "nr0m10", v36."propertytex1m12" AS "propertytex1m12"
       FROM (
             SELECT v34."label" AS "label10m11", v34."nr" AS "nr0m10", v34."propertytex1" AS "propertytex1m12", v34."propertynum2" AS "propertynum1m40"
             FROM "BSBMS5"."product2" v34
             WHERE (v34."nr" IS NOT NULL AND v34."label" IS NOT NULL AND v34."propertytex1" IS NOT NULL AND v34."propertynum2" IS NOT NULL AND (v34."propertynum2">50))
            ) v36, (
                    SELECT v44."product" AS "nr0m11"
                    FROM "BSBMS5"."productfeatureproduct2" v44
                    WHERE 89 = v44."productfeature"
                   ) v46, (
                           SELECT v49."product" AS "nr0m12"
                           FROM "BSBMS5"."productfeatureproduct2" v49
                           WHERE 86 = v49."productfeature"
                          ) v51
       WHERE (v36."nr0m10" = v46."nr0m11" AND v36."nr0m10" = v51."nr0m12")
) v63

--[SQL q5']
--EJP+SJR
SELECT v37."label10m46" AS "label10m46", v37."product0m4" AS "product0m4"
FROM (
	  SELECT DISTINCT v5."label10m46" AS "label10m46", v5."product0m4" AS "product0m4",
	                  v10."productfeature2m2" AS "productfeature2m2", v5."propertynum1m10" AS "propertynum1m10",
	                  v5."propertynum1m15" AS "propertynum1m15", v30."propertynum1m40" AS "propertynum1m40",
	                  v20."propertynum1m41" AS "propertynum1m41"
      FROM (
		    SELECT v1."label" AS "label10m46", v1."nr" AS "product0m4", v11."productfeature" AS "productfeature2m1",
		           v1."propertynum1" AS "propertynum1m10", v1."propertynum2" AS "propertynum1m15"
            FROM "BSBMS1"."product1" v1, "BSBMS1"."productfeatureproduct1" v11
            WHERE ((v1."nr" IS NOT NULL AND v1."label" IS NOT NULL) AND (v1."nr" <> 88) AND (v1."nr"=v11."product")
                   AND (v1."propertynum1" IS NOT NULL) AND (v1."propertynum2" IS NOT NULL) )
            UNION ALL
            SELECT v3."label" AS "label10m46", v3."nr" AS "product0m4", v13."productfeature" AS "productfeature2m1",
                   v3."propertynum1" AS "propertynum1m10", v3."propertynum2" AS "propertynum1m15"
            FROM "BSBMS5"."product2" v3, "BSBMS5"."productfeatureproduct2" v13
            WHERE ((v3."nr" IS NOT NULL AND v3."label" IS NOT NULL) AND (v3."nr" <> 88) AND v3."nr"=v13."product"
                   AND (v3."propertynum1" IS NOT NULL) AND (v3."propertynum2" IS NOT NULL) )
           ) v5, (
			      SELECT v6."productfeature" AS "productfeature2m2"
                  FROM "BSBMS1"."productfeatureproduct1" v6
                  WHERE 88 = v6."product"
                  UNION ALL
                  SELECT v8."productfeature" AS "productfeature2m2"
                  FROM "BSBMS5"."productfeatureproduct2" v8
                  WHERE 88 = v8."product"
                 ) v10, (
						 SELECT v16."propertynum1" AS "propertynum1m41"
                         FROM "BSBMS1"."product1" v16
                         WHERE (v16."propertynum1" IS NOT NULL AND 88 = v16."nr")
                         UNION ALL
                         SELECT v18."propertynum1" AS "propertynum1m41"
                         FROM "BSBMS5"."product2" v18
                         WHERE (v18."propertynum1" IS NOT NULL AND 88 = v18."nr")
                        ) v20, (
                                SELECT v26."propertynum2" AS "propertynum1m40"
                                FROM "BSBMS1"."product1" v26
                                WHERE (v26."propertynum2" IS NOT NULL AND 88 = v26."nr")
                                UNION ALL
                                SELECT v28."propertynum2" AS "propertynum1m40"
                                FROM "BSBMS5"."product2" v28
                                WHERE (v28."propertynum2" IS NOT NULL AND 88 = v28."nr")
                               ) v30
       WHERE ((v5."propertynum1m15" < (v30."propertynum1m40" + 170)) AND (v5."propertynum1m10" < (v20."propertynum1m41" + 120)) AND
              v10."productfeature2m2" = v5."productfeature2m1")
) v37

--[SQL q6']
NA

--[SQL q7']
--DCER-Removing BSBMS1.review1
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
       SELECT v10."nr" AS "nr0m4"
       FROM "BSBMS2"."review" v10
       WHERE 88 = v10."product"
       ) v12
      JOIN
      (
       SELECT v17."name" AS "name1m12", v16."nr" AS "nr0m7", v16."person" AS "person2m7"
       FROM "BSBMS2"."review" v16, "BSBMS2"."person" v17
       WHERE (v17."name" IS NOT NULL AND v16."person" = v17."nr")
     ) v19 ON 1 = 1
     JOIN
    (
      SELECT v22."nr" AS "nr0m8", v22."title" AS "title2m11"
      FROM "BSBMS2"."review" v22
    WHERE v22."title" IS NOT NULL
    ) v24 ON (v12."nr0m4" = v19."nr0m7" AND v12."nr0m4" = v24."nr0m8")
    LEFT OUTER JOIN
    (
      SELECT v27."nr" AS "nr0m6", v27."rating1" AS "rating1m16"
      FROM "BSBMS2"."review" v27
      WHERE v27."rating1" IS NOT NULL
    ) v29 ON v12."nr0m4" = v29."nr0m6"
    LEFT OUTER JOIN
    (
      SELECT v32."nr" AS "nr0m5", v32."rating2" AS "rating1m17"
      FROM "BSBMS2"."review" v32
      WHERE v32."rating2" IS NOT NULL
     ) v34 ON v12."nr0m4" = v34."nr0m5"  ON 1 = 1
) v36

--Removing BSBMS2.review
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
       ) v12
      JOIN
      (
       SELECT v14."name" AS "name1m12", v13."nr" AS "nr0m7", v13."person" AS "person2m7"
       FROM "BSBMS1"."review1" v13, "BSBMS2"."person" v14
       WHERE (v14."name" IS NOT NULL AND v13."person" = v14."nr")
     ) v19 ON 1 = 1
     JOIN
    (
      SELECT v20."nr" AS "nr0m8", v20."title" AS "title2m11"
      FROM "BSBMS1"."review1" v20
      WHERE v20."title" IS NOT NULL
    ) v24 ON (v12."nr0m4" = v19."nr0m7" AND v12."nr0m4" = v24."nr0m8")
    LEFT OUTER JOIN
    (
      SELECT v25."nr" AS "nr0m6", v25."rating1" AS "rating1m16"
      FROM "BSBMS1"."review1" v25
      WHERE v25."rating1" IS NOT NULL
    ) v29 ON v12."nr0m4" = v29."nr0m6"
    LEFT OUTER JOIN
    (
      SELECT v30."nr" AS "nr0m5", v30."rating2" AS "rating1m17"
      FROM "BSBMS1"."review1" v30
      WHERE v30."rating2" IS NOT NULL
     ) v34 ON v12."nr0m4" = v34."nr0m5"  ON 1 = 1
) v36

--[SQL q8']
--CDER, removing BSBMS1.review1 +SJR--
SELECT v49."name1m12" AS "name1m12", v49."person2m7" AS "person2m7", v49."rating1m13" AS "rating1m13", v49."rating1m15" AS "rating1m15", v49."rating1m16" AS "rating1m16", v49."rating1m17" AS "rating1m17", v49."reviewdate2m43" AS "reviewdate2m43", v49."text2m14" AS "text2m14", v49."title2m11" AS "title2m11"
FROM (
	  SELECT DISTINCT v27."name1m12" AS "name1m12", CASE WHEN v5."rating1m13" IS NOT NULL THEN v5."nr0m4" ELSE NULL END AS "nr0m13",
	                  CASE WHEN v5."rating1m15" IS NOT NULL THEN v5."nr0m4" ELSE NULL END AS "nr0m15",
	                  CASE WHEN v5."rating1m16" IS NOT NULL THEN v5."nr0m4" ELSE NULL END AS "nr0m16",
	                  CASE WHEN v5."rating1m17" IS NOT NULL THEN v5."nr0m4" ELSE NULL END AS "nr0m17", v5."nr0m4" AS "nr0m4",
	                  v5."person2m7" AS "person2m7", v5."rating1m13" AS "rating1m13", v5."rating1m15" AS "rating1m15",
	                  v5."rating1m16" AS "rating1m16", v5."rating1m17" AS "rating1m17", v5."reviewdate2m43" AS "reviewdate2m43",
	                  v5."text2m14" AS "text2m14", v5."title2m11" AS "title2m11"
      FROM (
		    SELECT v1."nr" AS "nr0m4", v1."title" AS "title2m11", v1."text" AS "text2m14", v1."reviewdate" AS "reviewdate2m43",
		           v1."person" AS "person2m7", v1."rating1" AS "rating1m16", v1."rating2" AS "rating1m17", v1."rating3" AS "rating1m13",
		           v1."rating4" AS "rating1m15"
            FROM "BSBMS1"."review1" v1
            WHERE (v1."nr" IS NOT NULL AND 88 = v1."product" AND v1."title" IS NOT NULL AND v1."text" IS NOT NULL AND v1."reviewdate" IS NOT NULL
                  )
          ) v5
              JOIN (
				    SELECT v22."name" AS "name1m12", v22."nr" AS "nr"
                    FROM "BSBMS2"."person" v22
                    WHERE (v22."nr" IS NOT NULL AND v22."name" IS NOT NULL)
                   ) v27 ON (v5."person2m7" = v27."nr")
) v49

--[SQL q9']
--Remove BSBMS1.review
SELECT v24."country3m1" AS "country3m1", v24."mbox_sha1sum1m47" AS "mbox_sha1sum1m47", v24."nr0m4" AS "nr0m4", v24."person2m7" AS "person2m7", v24."product2m4" AS "product2m4", v24."title2m11" AS "title2m11"
FROM (
      SELECT DISTINCT v7."country3m1" AS "country3m1", v7."mbox_sha1sum1m47" AS "mbox_sha1sum1m47", v7."name1m12" AS "name1m12", v12."nr0m4" AS "nr0m4", v7."person2m7" AS "person2m7", v17."product2m4" AS "product2m4", v22."title2m11" AS "title2m11"
      FROM (
            SELECT v5."country" AS "country3m1", v5."mbox_sha1sum" AS "mbox_sha1sum1m47", v5."name" AS "name1m12", v4."person" AS "person2m7"
            FROM "BSBMS2"."review" v4, "BSBMS2"."person" v5
            WHERE (v5."name" IS NOT NULL AND v5."mbox_sha1sum" IS NOT NULL AND v5."country" IS NOT NULL AND v4."person" = v5."nr" AND 88 = v4."nr")
           ) v7, (
                  SELECT v10."nr" AS "nr0m4", v10."person" AS "person2m0"
                  FROM "BSBMS2"."review" v10
                  WHERE v10."person" IS NOT NULL
                 ) v12, (
                         SELECT v15."nr" AS "nr0m1", v15."product" AS "product2m4"
                         FROM "BSBMS2"."review" v15
                         WHERE v15."product" IS NOT NULL
                        ) v17, (
                                SELECT v20."nr" AS "nr0m2", v20."title" AS "title2m11"
                                FROM "BSBMS2"."review" v20
                                WHERE v20."title" IS NOT NULL
                               ) v22
           WHERE (v7."person2m7" = v12."person2m0" AND v12."nr0m4" = v17."nr0m1" AND v12."nr0m4" = v22."nr0m2")
) v24

--Remove BSBMS2.review
SELECT v24."country3m1" AS "country3m1", v24."mbox_sha1sum1m47" AS "mbox_sha1sum1m47", v24."nr0m4" AS "nr0m4", v24."person2m7" AS "person2m7", v24."product2m4" AS "product2m4", v24."title2m11" AS "title2m11"
FROM (
      SELECT DISTINCT v7."country3m1" AS "country3m1", v7."mbox_sha1sum1m47" AS "mbox_sha1sum1m47", v7."name1m12" AS "name1m12", v12."nr0m4" AS "nr0m4", v7."person2m7" AS "person2m7", v17."product2m4" AS "product2m4", v22."title2m11" AS "title2m11"
      FROM (
            SELECT v2."country" AS "country3m1", v2."mbox_sha1sum" AS "mbox_sha1sum1m47", v2."name" AS "name1m12", v1."person" AS "person2m7"
            FROM "BSBMS1"."review1" v1, "BSBMS2"."person" v2
            WHERE (v2."name" IS NOT NULL AND v2."mbox_sha1sum" IS NOT NULL AND v2."country" IS NOT NULL AND v1."person" = v2."nr" AND 88 = v1."nr")
           ) v7, (
                  SELECT v8."nr" AS "nr0m4", v8."person" AS "person2m0"
                  FROM "BSBMS1"."review1" v8
                  WHERE v8."person" IS NOT NULL
                 ) v12, (
                         SELECT v13."nr" AS "nr0m1", v13."product" AS "product2m4"
                         FROM "BSBMS1"."review1" v13
                         WHERE v13."product" IS NOT NULL
                        ) v17, (
                                SELECT v18."nr" AS "nr0m2", v18."title" AS "title2m11"
                                FROM "BSBMS1"."review1" v18
                                WHERE v18."title" IS NOT NULL
                               ) v22
           WHERE (v7."person2m7" = v12."person2m0" AND v12."nr0m4" = v17."nr0m1" AND v12."nr0m4" = v22."nr0m2")
) v24

--[SQL q10']
--MatV
SELECT DISTINCT v17."nr1m5" AS "nr1m5", v17."product1m5" AS "product1m5"
FROM (
	  SELECT v1."o_deliverydays" AS "deliverydays1m37", v1."o_nr" AS "nr1m5", v1."o_price" AS "price1m39", v1."p_nr" AS "product1m5", v1."o_validto" AS "validto1m45"
      FROM "BSBMMV"."op1" v1, "BSBMS4"."vendor" v7
      WHERE ((v1."o_validto" > '1988-01-01') AND (v1."o_deliverydays" <= 3) AND (v1."p_nr" < 100) AND v1."o_deliverydays" IS NOT NULL AND v1."o_price" IS NOT NULL AND v1."o_validto" IS NOT NULL AND v1."o_vendor" = v7."nr" AND 'US' = v7."country")
      UNION ALL
      SELECT v1."o_deliverydays" AS "deliverydays1m37", v1."o_nr" AS "nr1m5", v1."o_price" AS "price1m39", v1."p_nr" AS "product1m5", v1."o_validto" AS "validto1m45"
      FROM "BSBMMV"."op2" v1, "BSBMS4"."vendor" v7
      WHERE ((v1."o_validto" > '1988-01-01') AND (v1."o_deliverydays" <= 3) AND (v1."p_nr" < 100) AND v1."o_deliverydays" IS NOT NULL AND v1."o_price" IS NOT NULL AND v1."o_validto" IS NOT NULL AND v1."o_vendor" = v7."nr" AND 'US' = v7."country")
) v17

--[SQL q11']
NA

--[SQL q12']
--MatV--
SELECT v23."deliverydays1m37" AS "deliverydays1m37", v23."label10m4" AS "label10m4", v23."label10m46" AS "label10m46", v23."nr1m21" AS "nr1m21", v23."offerwebpage1m24" AS "offerwebpage1m24", v23."price1m39" AS "price1m39", v23."product1m5" AS "product1m5", v23."validto1m45" AS "validto1m45", v23."vendor1m8" AS "vendor1m8"
FROM (
	  SELECT DISTINCT v21."deliverydays1m37" AS "deliverydays1m37", v21."homepage2m48" AS "homepage2m48",
	  v21."label10m4" AS "label10m4", v21."label10m46" AS "label10m46", v21."nr1m21" AS "nr1m21",
	  v21."offerwebpage1m24" AS "offerwebpage1m24", v21."price1m39" AS "price1m39", v21."product1m5" AS "product1m5",
	  v21."validto1m45" AS "validto1m45", v21."vendor1m8" AS "vendor1m8"
      FROM (
		    SELECT v."o_deliverydays" AS "deliverydays1m37", v8."homepage" AS "homepage2m48", v8."label" AS "label10m4",
		           v."p_label" AS "label10m46", v."o_nr" AS "nr1m21", v."o_offerwebpages" AS "offerwebpage1m24",
		           v."o_price" AS "price1m39", v."p_nr" AS "product1m5", v."o_validto" AS "validto1m45", v."o_vendor" AS "vendor1m8"
            FROM "BSBMMV"."op1" v, "BSBMS4"."vendor" v8
            WHERE (v."p_label" IS NOT NULL AND (v."o_nr" < 1000) AND v8."label" IS NOT NULL AND v8."homepage" IS NOT NULL AND
                   v."o_offerwebpages" IS NOT NULL AND v."o_price" IS NOT NULL AND v."o_deliverydays" IS NOT NULL AND
                   v."o_validto" IS NOT NULL AND v."o_vendor" = v8."nr")
            UNION ALL
            SELECT v."o_deliverydays" AS "deliverydays1m37", v18."homepage" AS "homepage2m48", v18."label" AS "label10m4",
                   v."p_label" AS "label10m46", v."o_nr" AS "nr1m21", v."o_offerwebpages" AS "offerwebpage1m24",
                   v."o_price" AS "price1m39", v."p_nr" AS "product1m5", v."o_validto" AS "validto1m45", v."o_vendor" AS "vendor1m8"
            FROM "BSBMMV"."op2" v, "BSBMS4"."vendor" v18
            WHERE (v."p_label" IS NOT NULL AND (v."o_nr" < 1000) AND v18."label" IS NOT NULL AND v18."homepage" IS NOT NULL AND
                   v."o_offerwebpages" IS NOT NULL AND v."o_price" IS NOT NULL AND v."o_deliverydays" IS NOT NULL AND
                   v."o_validto" IS NOT NULL AND v."o_vendor" = v18."nr")
           ) v21
) v23