-- ${1:product.nr:percent}

SELECT v37."label10m46" AS "label10m46", v37."product0m4" AS "product0m4"
FROM (
	  SELECT DISTINCT v5."label10m46" AS "label10m46", v5."product0m4" AS "product0m4", v10."productfeature2m2" AS "productfeature2m2", v25."propertynum1m10" AS "propertynum1m10", v35."propertynum1m15" AS "propertynum1m15", v30."propertynum1m40" AS "propertynum1m40", v20."propertynum1m41" AS "propertynum1m41"
      FROM (
		    SELECT v1."label" AS "label10m46", v1."nr" AS "product0m4"
            FROM "ss1"."product1" v1
            WHERE ((v1."nr" IS NOT NULL AND v1."label" IS NOT NULL) AND v1."nr" <> ${1:product.nr:percent})
            UNION ALL
            SELECT v3."label" AS "label10m46", v3."nr" AS "product0m4"
            FROM "ss5"."product2" v3
            WHERE ((v3."nr" IS NOT NULL AND v3."label" IS NOT NULL) AND v3."nr" <> ${1:product.nr:percent})
           ) v5, (
			      SELECT v6."productfeature" AS "productfeature2m2"
                  FROM "ss1"."productfeatureproduct1" v6
                  WHERE ${1:product.nr:percent} = v6."product"
                  UNION ALL
                  SELECT v8."productfeature" AS "productfeature2m2"
                  FROM "ss5"."productfeatureproduct2" v8
                  WHERE ${1:product.nr:percent} = v8."product"
                 ) v10, (
					     SELECT v11."product" AS "product0m0", v11."productfeature" AS "productfeature2m1"
                         FROM "ss1"."productfeatureproduct1" v11
                         WHERE v11."product" <> ${1:product.nr:percent}
                         UNION ALL
                         SELECT v13."product" AS "product0m0", v13."productfeature" AS "productfeature2m1"
                         FROM "ss5"."productfeatureproduct2" v13
                         WHERE v13."product" <> ${1:product.nr:percent}
                         ) v15, (
							     SELECT v16."propertynum1" AS "propertynum1m41"
                                 FROM "ss1"."product1" v16
                                 WHERE (v16."propertynum1" IS NOT NULL AND ${1:product.nr:percent} = v16."nr")
                                 UNION ALL
                                 SELECT v18."propertynum1" AS "propertynum1m41"
                                 FROM "ss5"."product2" v18
                                 WHERE (v18."propertynum1" IS NOT NULL AND ${1:product.nr:percent} = v18."nr")
                                ) v20, (
									    SELECT v21."nr" AS "product0m2", v21."propertynum1" AS "propertynum1m10"
                                        FROM "ss1"."product1" v21
                                        WHERE ((v21."nr" IS NOT NULL AND v21."propertynum1" IS NOT NULL) AND v21."nr" <> ${1:product.nr:percent})
                                        UNION ALL
                                        SELECT v23."nr" AS "product0m2", v23."propertynum1" AS "propertynum1m10"
                                        FROM "ss5"."product2" v23
                                        WHERE ((v23."nr" IS NOT NULL AND v23."propertynum1" IS NOT NULL) AND v23."nr" <> ${1:product.nr:percent})
                                       ) v25, (SELECT v26."propertynum2" AS "propertynum1m40"
                                               FROM "ss1"."product1" v26
                                               WHERE (v26."propertynum2" IS NOT NULL AND ${1:product.nr:percent} = v26."nr")
                                               UNION ALL
                                               SELECT v28."propertynum2" AS "propertynum1m40"
                                               FROM "ss5"."product2" v28
                                               WHERE (v28."propertynum2" IS NOT NULL AND ${1:product.nr:percent} = v28."nr")
                                              ) v30, (
												      SELECT v31."nr" AS "product0m3", v31."propertynum2" AS "propertynum1m15"
                                                      FROM "ss1"."product1" v31
                                                      WHERE ((v31."nr" IS NOT NULL AND v31."propertynum2" IS NOT NULL) AND v31."nr" <> ${1:product.nr:percent})
                                                      UNION ALL
                                                      SELECT v33."nr" AS "product0m3", v33."propertynum2" AS "propertynum1m15"
                                                      FROM "ss5"."product2" v33
                                                      WHERE ((v33."nr" IS NOT NULL AND v33."propertynum2" IS NOT NULL) AND v33."nr" <> ${1:product.nr:percent})
                                                      ) v35
       WHERE ((v35."propertynum1m15" < (v30."propertynum1m40" + 170)) AND (v25."propertynum1m10" < (v20."propertynum1m41" + 120)) AND v5."product0m4" = v15."product0m0" AND v10."productfeature2m2" = v15."productfeature2m1" AND v5."product0m4" = v25."product0m2" AND v5."product0m4" = v35."product0m3")
) v37
