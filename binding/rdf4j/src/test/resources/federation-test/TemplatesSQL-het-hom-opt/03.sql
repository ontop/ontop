-- ${1:productfeatureproduct.productfeature:none}
-- ${2:productfeatureproduct.productfeature:none}

SELECT v37."label10m46" AS "label10m46", v37."nr0m55" AS "nr0m55"
FROM (
     (
     SELECT DISTINCT v35."label10m10" AS "label10m10", v5."label10m46" AS "label10m46", v5."nr0m55" AS "nr0m55",
     CASE WHEN v35."label10m10" IS NOT NULL THEN v5."nr0m55" ELSE NULL END AS "product0m4",
     v5."propertynum1m26" AS "propertynum1m26", v5."propertynum1m41" AS "propertynum1m41"
      FROM (
		    SELECT v1."label" AS "label10m46", v1."nr" AS "nr0m55", v1."propertynum1" AS "propertynum1m41", v1."propertynum3" AS "propertynum1m26"
            FROM "ss1"."product1" v1
            WHERE (v1."nr" IS NOT NULL AND v1."label" IS NOT NULL AND v1."propertynum1" IS NOT NULL
                   AND v1."propertynum3" IS NOT NULL)
          ) v5
            JOIN (
				  SELECT v11."product" AS "nr0m2"
                  FROM "ss1"."productfeatureproduct1" v11
                  WHERE ${1:productfeatureproduct.productfeature:none} = v11."productfeature"
                 ) v15 ON (v5."nr0m55" = v15."nr0m2")
                      LEFT OUTER JOIN (
							SELECT v26."product" AS "nr0m0"
                            FROM "ss1"."productfeatureproduct1" v26
                            WHERE ${2:productfeatureproduct.productfeature:none} = v26."productfeature"
                          ) v30
                            JOIN (
								  SELECT v31."label" AS "label10m10", v31."nr" AS "nr0m7"
                                  FROM "ss1"."product1" v31
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
            FROM "ss5"."product2" v3
            WHERE (v3."nr" IS NOT NULL AND v3."label" IS NOT NULL AND v3."propertynum1" IS NOT NULL
                   AND v3."propertynum3" IS NOT NULL)
          ) v5
            JOIN (
                  SELECT v13."product" AS "nr0m2"
                  FROM "ss5"."productfeatureproduct2" v13
                  WHERE ${1:productfeatureproduct.productfeature:none} = v13."productfeature"
                 ) v15 ON (v5."nr0m55" = v15."nr0m2")
                       LEFT OUTER JOIN (
                               SELECT v28."product" AS "nr0m0"
                               FROM "ss5"."productfeatureproduct2" v28
                               WHERE ${2:productfeatureproduct.productfeature:none} = v28."productfeature"
                             ) v30
                            JOIN (
                                  SELECT v33."label" AS "label10m10", v33."nr" AS "nr0m7"
                                  FROM "ss5"."product2" v33
                                  WHERE (v33."nr" IS NOT NULL AND v33."label" IS NOT NULL)
                                 ) v35 ON v30."nr0m0" = v35."nr0m7"  ON v5."nr0m55" = v30."nr0m0"
                            WHERE v35."label10m10" IS NULL
     )
) v37
