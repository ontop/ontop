SELECT DISTINCT v9."label10m46" AS "label10m46", v9."product0m2" AS "product0m2"
FROM (
      (
       SELECT v1."label" AS "label10m46", v1."nr" AS "product0m2"
       FROM "ss1"."product1" v1, "ss1"."productfeatureproduct1" v2, "ss1"."productfeatureproduct1" v3
       WHERE ((v1."propertynum1" < 1000) AND v1."label" IS NOT NULL AND v1."propertynum1" IS NOT NULL AND v1."nr" = v2."product" AND v1."nr" = v3."product" AND 89 = v2."productfeature" AND 91 = v3."productfeature")
      )
      UNION ALL
     (
      SELECT v5."label" AS "label10m46", v5."nr" AS "product0m2"
      FROM "ss5"."product2" v5, "ss5"."productfeatureproduct2" v6, "ss5"."productfeatureproduct2" v7
      WHERE ((v5."propertynum1" < 1000) AND v5."label" IS NOT NULL AND v5."propertynum1" IS NOT NULL AND v5."nr" = v6."product" AND v5."nr" = v7."product" AND 89 = v6."productfeature" AND 91 = v7."productfeature")
     )
) v9