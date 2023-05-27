SELECT v16."label10m46" AS "label10m46", v16."product0m4" AS "product0m4"
FROM (
      SELECT DISTINCT v7."label10m46" AS "label10m46", v7."product0m4" AS "product0m4", v7."productfeature2m2" AS "productfeature2m2", v7."propertynum1m10" AS "propertynum1m10", v7."propertynum1m15" AS "propertynum1m15", v14."propertynum1m40" AS "propertynum1m40", v14."propertynum1m41" AS "propertynum1m41"
      FROM (
            (
             SELECT v1."label" AS "label10m46", v1."nr" AS "product0m4", v2."productfeature" AS "productfeature2m2", v1."propertynum1" AS "propertynum1m10", v1."propertynum2" AS "propertynum1m15"
             FROM "ss1"."product1" v1, "ss1"."productfeatureproduct1" v2
             WHERE (v1."label" IS NOT NULL AND v1."propertynum1" IS NOT NULL AND v1."propertynum2" IS NOT NULL AND v1."nr" <> 88 AND v1."nr" = v2."product")
            )
             UNION ALL
           (
             SELECT v4."label" AS "label10m46", v4."nr" AS "product0m4", v5."productfeature" AS "productfeature2m2", v4."propertynum1" AS "propertynum1m10", v4."propertynum2" AS "propertynum1m15"
             FROM "ss5"."product2" v4, "ss5"."productfeatureproduct2" v5
             WHERE (v4."label" IS NOT NULL AND v4."propertynum1" IS NOT NULL AND v4."propertynum2" IS NOT NULL AND v4."nr" <> 88 AND v4."nr" = v5."product")
           )
          ) v7,
              (
               (
                SELECT v8."productfeature" AS "productfeature2m0", v9."propertynum2" AS "propertynum1m40", v9."propertynum1" AS "propertynum1m41"
                FROM "ss1"."productfeatureproduct1" v8, "ss1"."product1" v9
                WHERE (v9."propertynum1" IS NOT NULL AND v9."propertynum2" IS NOT NULL AND 88 = v8."product" AND 88 = v9."nr")
               )
                UNION ALL
              (
                SELECT v11."productfeature" AS "productfeature2m0", v12."propertynum2" AS "propertynum1m40", v12."propertynum1" AS "propertynum1m41"
                FROM "ss5"."productfeatureproduct2" v11, "ss5"."product2" v12
                WHERE (v12."propertynum1" IS NOT NULL AND v12."propertynum2" IS NOT NULL AND 88 = v11."product" AND 88 = v12."nr")
              )
             ) v14
WHERE ((v7."propertynum1m15" < (v14."propertynum1m40" + 170)) AND (v7."propertynum1m10" < (v14."propertynum1m41" + 120)) AND v7."productfeature2m2" = v14."productfeature2m0")
) v16