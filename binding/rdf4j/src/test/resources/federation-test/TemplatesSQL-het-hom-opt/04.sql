-- ${1:productfeatureproduct.productfeature:percent}
-- ${2:productfeatureproduct.productfeature:percent}
-- ${3:productfeatureproduct.productfeature:percent}

SELECT DISTINCT v63."label10m11" AS "label10m11", v63."nr0m10" AS "nr0m10", v63."propertytex1m12" AS "propertytex1m12"
FROM (
	  SELECT DISTINCT v5."label10m11" AS "label10m11", v5."nr0m10" AS "nr0m10",
	  v5."propertytex1m12" AS "propertytex1m12"
      FROM (
		    SELECT v1."label" AS "label10m11", v1."nr" AS "nr0m10", v1."propertytex1" AS "propertytex1m12"
            FROM "ss1"."product1" v1
            WHERE (v1."nr" IS NOT NULL AND v1."label" IS NOT NULL AND v1."propertytex1" IS NOT NULL AND v1."propertynum1" IS NOT NULL)
           ) v5,  (
				   SELECT v11."product" AS "nr0m1"
                   FROM "ss1"."productfeatureproduct1" v11
                   WHERE ${1:productfeatureproduct.productfeature:percent} = v11."productfeature"
                  ) v15, (
						  SELECT v16."product" AS "nr0m2"
                          FROM "ss1"."productfeatureproduct1" v16
                          WHERE ${2:productfeatureproduct.productfeature:percent} = v16."productfeature"
                         ) v20
       WHERE (v5."nr0m10" = v15."nr0m1" AND v5."nr0m10" = v20."nr0m2")
       UNION ALL

      SELECT DISTINCT v5."label10m11" AS "label10m11", v5."nr0m10" AS "nr0m10", v5."propertytex1m12" AS "propertytex1m12"
      FROM (
            SELECT v3."label" AS "label10m11", v3."nr" AS "nr0m10", v3."propertytex1" AS "propertytex1m12"
            FROM "ss5"."product2" v3
            WHERE (v3."nr" IS NOT NULL AND v3."label" IS NOT NULL AND v3."propertytex1" IS NOT NULL AND v3."propertynum1" IS NOT NULL)
           ) v5, (
                  SELECT v13."product" AS "nr0m1"
                  FROM "ss5"."productfeatureproduct2" v13
                  WHERE ${1:productfeatureproduct.productfeature:percent} = v13."productfeature"
                 ) v15, (
                         SELECT v18."product" AS "nr0m2"
                         FROM "ss5"."productfeatureproduct2" v18
                         WHERE ${2:productfeatureproduct.productfeature:percent} = v18."productfeature"
                        ) v20
       WHERE (v5."nr0m10" = v15."nr0m1" AND v5."nr0m10" = v20."nr0m2")
       UNION ALL
       SELECT DISTINCT v36."label10m11" AS "label10m11", v36."nr0m10" AS "nr0m10",
       v36."propertytex1m12" AS "propertytex1m12"
       FROM (
			 SELECT v32."label" AS "label10m11", v32."nr" AS "nr0m10", v32."propertytex1" AS "propertytex1m12", v32."propertynum2" AS "propertynum1m40"
             FROM "ss1"."product1" v32
             WHERE (v32."nr" IS NOT NULL AND v32."label" IS NOT NULL AND v32."propertytex1" IS NOT NULL AND v32."propertynum2" IS NOT NULL)
            ) v36, (
					SELECT v42."product" AS "nr0m11"
                    FROM "ss1"."productfeatureproduct1" v42
                    WHERE ${1:productfeatureproduct.productfeature:percent} = v42."productfeature"
                   ) v46, (
						   SELECT v47."product" AS "nr0m12"
                           FROM "ss1"."productfeatureproduct1" v47
                           WHERE ${3:productfeatureproduct.productfeature:percent} = v47."productfeature"
                          ) v51
       WHERE (v36."nr0m10" = v46."nr0m11" AND v36."nr0m10" = v51."nr0m12")
       UNION ALL
       SELECT DISTINCT v36."label10m11" AS "label10m11", v36."nr0m10" AS "nr0m10", v36."propertytex1m12" AS "propertytex1m12"
       FROM (
             SELECT v34."label" AS "label10m11", v34."nr" AS "nr0m10", v34."propertytex1" AS "propertytex1m12", v34."propertynum2" AS "propertynum1m40"
             FROM "ss5"."product2" v34
             WHERE (v34."nr" IS NOT NULL AND v34."label" IS NOT NULL AND v34."propertytex1" IS NOT NULL AND v34."propertynum2" IS NOT NULL)
            ) v36, (
                    SELECT v44."product" AS "nr0m11"
                    FROM "ss5"."productfeatureproduct2" v44
                    WHERE ${1:productfeatureproduct.productfeature:percent} = v44."productfeature"
                   ) v46, (
                           SELECT v49."product" AS "nr0m12"
                           FROM "ss5"."productfeatureproduct2" v49
                           WHERE ${3:productfeatureproduct.productfeature:percent} = v49."productfeature"
                          ) v51
       WHERE (v36."nr0m10" = v46."nr0m11" AND v36."nr0m10" = v51."nr0m12")
) v63
