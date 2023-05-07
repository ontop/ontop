-- ${1:productfeatureproduct.productfeature:none}
-- ${2:productfeatureproduct.productfeature:none}

SELECT v32."label10m46" AS "label10m46", v32."product0m2" AS "product0m2"
FROM (
      SELECT DISTINCT v30."label10m10" AS "label10m10", v5."label10m46" AS "label10m46", v5."product0m2" AS "product0m2", CASE WHEN v30."label10m10" IS NOT NULL THEN v5."product0m2" ELSE NULL END AS "product0m4", v20."propertynum1m26" AS "propertynum1m26", v15."propertynum1m41" AS "propertynum1m41"
      FROM (
            SELECT v1."label" AS "label10m46", v1."nr" AS "product0m2"
            FROM "ss1"."product1" v1
            WHERE v1."label" IS NOT NULL
            UNION ALL
            SELECT v3."label" AS "label10m46", v3."nr" AS "product0m2"
            FROM "ss5"."product2" v3
            WHERE v3."label" IS NOT NULL
           ) v5
          JOIN
          (
           SELECT v6."product" AS "product0m1"
           FROM "ss1"."productfeatureproduct1" v6
           WHERE ${1:productfeatureproduct.productfeature:none} = v6."productfeature"
           UNION ALL
           SELECT v8."product" AS "product0m1"
           FROM "ss5"."productfeatureproduct2" v8
           WHERE ${1:productfeatureproduct.productfeature:none} = v8."productfeature"
          ) v10 ON 1 = 1
          JOIN
          (
           SELECT v11."nr" AS "product0m3", v11."propertynum1" AS "propertynum1m41"
           FROM "ss1"."product1" v11
           WHERE v11."propertynum1" IS NOT NULL
           UNION ALL
           SELECT v13."nr" AS "product0m3", v13."propertynum1" AS "propertynum1m41"
           FROM "ss5"."product2" v13
           WHERE v13."propertynum1" IS NOT NULL
          ) v15 ON 1 = 1
          JOIN
          (
           SELECT v16."nr" AS "product0m5", v16."propertynum3" AS "propertynum1m26"
           FROM "ss1"."product1" v16
           WHERE v16."propertynum3" IS NOT NULL
           UNION ALL
           SELECT v18."nr" AS "product0m5", v18."propertynum3" AS "propertynum1m26"
           FROM "ss5"."product2" v18
           WHERE v18."propertynum3" IS NOT NULL
          ) v20 ON (v5."product0m2" = v10."product0m1" AND v5."product0m2" = v15."product0m3" AND v5."product0m2" = v20."product0m5")
          LEFT OUTER JOIN
          (
           SELECT v21."product" AS "product0m0"
           FROM "ss1"."productfeatureproduct1" v21
           WHERE ${2:productfeatureproduct.productfeature:none} = v21."productfeature"
           UNION ALL
           SELECT v23."product" AS "product0m0"
           FROM "ss5"."productfeatureproduct2" v23
           WHERE ${2:productfeatureproduct.productfeature:none} = v23."productfeature"
          ) v25
          JOIN
          (
           SELECT v26."label" AS "label10m10", v26."nr" AS "product0m8"
           FROM "ss1"."product1" v26
           WHERE v26."label" IS NOT NULL
           UNION ALL
           SELECT v28."label" AS "label10m10", v28."nr" AS "product0m8"
           FROM "ss5"."product2" v28
           WHERE v28."label" IS NOT NULL
          ) v30 ON v25."product0m0" = v30."product0m8"  ON v5."product0m2" = v25."product0m0"
          WHERE v30."label10m10" IS NULL
) v32
