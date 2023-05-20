SELECT v13."label10m46" AS "label10m46", v13."product0m2" AS "product0m2"
FROM (
      SELECT DISTINCT v11."label10m10" AS "label10m10", v11."label10m46" AS "label10m46", v11."product0m2" AS "product0m2", CASE WHEN v11."label10m10" IS NOT NULL THEN v11."product0m2" ELSE NULL END AS "product0m4", v11."propertynum1m26" AS "propertynum1m26", v11."propertynum1m41" AS "propertynum1m41"
      FROM (
            (
             SELECT v4."label" AS "label10m10", v1."label" AS "label10m46", v1."nr" AS "product0m2", v1."propertynum3" AS "propertynum1m26", v1."propertynum1" AS "propertynum1m41"
             FROM "ss1"."product1" v1
             JOIN
             "ss1"."productfeatureproduct1" v2 ON (v1."label" IS NOT NULL AND v1."propertynum1" IS NOT NULL AND v1."propertynum3" IS NOT NULL AND (v1."propertynum3" < 5000) AND (v1."propertynum1" > 10) AND v1."nr" = v2."product" AND 89 = v2."productfeature")
             LEFT OUTER JOIN
             "ss1"."productfeatureproduct1" v3
             JOIN
             "ss1"."product1" v4 ON (v4."label" IS NOT NULL AND v3."product" = v4."nr" AND 91 = v3."productfeature")  ON v1."nr" = v3."product"
             WHERE v4."label" IS NULL
           )
            UNION ALL
          (
            SELECT v9."label" AS "label10m10", v6."label" AS "label10m46", v6."nr" AS "product0m2", v6."propertynum3" AS "propertynum1m26", v6."propertynum1" AS "propertynum1m41"
            FROM "ss5"."product2" v6
            JOIN
            "ss5"."productfeatureproduct2" v7 ON (v6."label" IS NOT NULL AND v6."propertynum1" IS NOT NULL AND v6."propertynum3" IS NOT NULL AND (v6."propertynum3" < 5000) AND (v6."propertynum1" > 10) AND v6."nr" = v7."product" AND 89 = v7."productfeature")
            LEFT OUTER JOIN
            "ss5"."productfeatureproduct2" v8
            JOIN
            "ss5"."product2" v9 ON (v9."label" IS NOT NULL AND v8."product" = v9."nr" AND 91 = v8."productfeature")  ON v6."nr" = v8."product"
            WHERE v9."label" IS NULL
          )
        ) v11
) v13