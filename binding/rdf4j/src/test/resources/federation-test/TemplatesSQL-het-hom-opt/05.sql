-- ${1:product.nr:percent}

SELECT v37."label10m46" AS "label10m46", v37."product0m4" AS "product0m4"
FROM (
	  SELECT DISTINCT v5."label10m46" AS "label10m46", v5."product0m4" AS "product0m4",
	                  v10."productfeature2m2" AS "productfeature2m2", v5."propertynum1m10" AS "propertynum1m10",
	                  v5."propertynum1m15" AS "propertynum1m15", v30."propertynum1m40" AS "propertynum1m40",
	                  v20."propertynum1m41" AS "propertynum1m41"
      FROM (
		    SELECT v1."label" AS "label10m46", v1."nr" AS "product0m4", v11."productfeature" AS "productfeature2m1",
		           v1."propertynum1" AS "propertynum1m10", v1."propertynum2" AS "propertynum1m15"
            FROM "ss1"."product1" v1, "ss1"."productfeatureproduct1" v11
            WHERE ((v1."nr" IS NOT NULL AND v1."label" IS NOT NULL) AND (v1."nr" <> ${1:product.nr:percent}) AND (v1."nr"=v11."product")
                   AND (v1."propertynum1" IS NOT NULL) AND (v1."propertynum2" IS NOT NULL) )
            UNION ALL
            SELECT v3."label" AS "label10m46", v3."nr" AS "product0m4", v13."productfeature" AS "productfeature2m1",
                   v3."propertynum1" AS "propertynum1m10", v3."propertynum2" AS "propertynum1m15"
            FROM "ss5"."product2" v3, "ss5"."productfeatureproduct2" v13
            WHERE ((v3."nr" IS NOT NULL AND v3."label" IS NOT NULL) AND (v3."nr" <> ${1:product.nr:percent}) AND v3."nr"=v13."product"
                   AND (v3."propertynum1" IS NOT NULL) AND (v3."propertynum2" IS NOT NULL) )
           ) v5, (
			      SELECT v6."productfeature" AS "productfeature2m2"
                  FROM "ss1"."productfeatureproduct1" v6
                  WHERE ${1:product.nr:percent} = v6."product"
                  UNION ALL
                  SELECT v8."productfeature" AS "productfeature2m2"
                  FROM "ss5"."productfeatureproduct2" v8
                  WHERE ${1:product.nr:percent} = v8."product"
                 ) v10, (
						 SELECT v16."propertynum1" AS "propertynum1m41"
                         FROM "ss1"."product1" v16
                         WHERE (v16."propertynum1" IS NOT NULL AND ${1:product.nr:percent} = v16."nr")
                         UNION ALL
                         SELECT v18."propertynum1" AS "propertynum1m41"
                         FROM "ss5"."product2" v18
                         WHERE (v18."propertynum1" IS NOT NULL AND ${1:product.nr:percent} = v18."nr")
                        ) v20, (
                                SELECT v26."propertynum2" AS "propertynum1m40"
                                FROM "ss1"."product1" v26
                                WHERE (v26."propertynum2" IS NOT NULL AND ${1:product.nr:percent} = v26."nr")
                                UNION ALL
                                SELECT v28."propertynum2" AS "propertynum1m40"
                                FROM "ss5"."product2" v28
                                WHERE (v28."propertynum2" IS NOT NULL AND ${1:product.nr:percent} = v28."nr")
                               ) v30
       WHERE ((v5."propertynum1m15" < (v30."propertynum1m40" + 170)) AND (v5."propertynum1m10" < (v20."propertynum1m41" + 120)) AND
              v10."productfeature2m2" = v5."productfeature2m1")
) v37
