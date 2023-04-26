-- ${1:productfeatureproduct.productfeature:percent}
-- ${2:productfeatureproduct.productfeature:percent}
-- ${1:product.propertynum1:none}

(
SELECT DISTINCT v5."label10m46" AS "label10m46", v5."nr0m55" AS "nr0m55"
FROM (
	  SELECT v1."label" AS "label10m46", v1."nr" AS "nr0m55"
      FROM "ss1"."product1" v1
      WHERE (v1."nr" IS NOT NULL AND v1."label" IS NOT NULL AND v1."propertynum1" IS NOT NULL) AND (v1."propertynum1" <= ${1:product.propertynum1:none})
     ) v5, (
			SELECT v11."product" AS "nr0m1"
            FROM "ss1"."productfeatureproduct1" v11
            WHERE ${1:productfeatureproduct.productfeature:percent} = v11."productfeature"
           ) v15, (
				   SELECT v16."product" AS "nr0m2"
                   FROM "ss1"."productfeatureproduct1" v16
                   WHERE ${2:productfeatureproduct.productfeature:percent} = v16."productfeature"
                  ) v20
WHERE (v5."nr0m55" = v15."nr0m1" AND v5."nr0m55" = v20."nr0m2")
)
UNION ALL
(
SELECT DISTINCT v5."label10m46" AS "label10m46", v5."nr0m55" AS "nr0m55"
FROM (
      SELECT v3."label" AS "label10m46", v3."nr" AS "nr0m55"
      FROM "ss5"."product2" v3
      WHERE (v3."nr" IS NOT NULL AND v3."label" IS NOT NULL AND v3."propertynum1" IS NOT NULL) AND (v3."propertynum1" <= ${1:product.propertynum1:none})
     ) v5, (
            SELECT v13."product" AS "nr0m1"
            FROM "ss5"."productfeatureproduct2" v13
            WHERE ${1:productfeatureproduct.productfeature:percent} = v13."productfeature"
           ) v15, (
                   SELECT v18."product" AS "nr0m2"
                   FROM "ss5"."productfeatureproduct2" v18
                   WHERE ${2:productfeatureproduct.productfeature:percent} = v18."productfeature"
                  ) v20
WHERE (v5."nr0m55" = v15."nr0m1" AND v5."nr0m55" = v20."nr0m2")
)
