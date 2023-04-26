-- ${1:product.label:none}

SELECT v24."label10m46" AS "label10m46", v24."nr1m1" AS "nr1m1", v24."nr3m2" AS "nr3m2", v24."nr8m3" AS "nr8m3", v24."v0" AS "v0"
FROM (SELECT v18."label10m46" AS "label10m46", v18."nr1m1" AS "nr1m1", NULL AS "nr3m2", NULL AS "nr8m3", 0 AS "v0"
      FROM (
               SELECT DISTINCT v5."label10m46" AS "label10m46", v5."nr1m1" AS "nr1m1"
               FROM (SELECT v1."label" AS "label10m46", v1."nr" AS "nr1m1"
                     FROM "ss1"."product1" v1
                     WHERE (v1."label" IS NOT NULL AND (POSITION('${1:product.label:none}' IN v1."label") > 0))
                     UNION ALL
                     SELECT v3."label" AS "label10m46", v3."nr" AS "nr1m1"
                     FROM "ss5"."product2" v3
                     WHERE (v3."label" IS NOT NULL AND (POSITION('${1:product.label:none}' IN v3."label") > 0))
                    ) v5

           ) v18
      UNION ALL
      SELECT v20."label" AS "label10m46", NULL AS "nr1m1", v20."nr" AS "nr3m2", NULL AS "nr8m3", 1 AS "v0"
      FROM "ss3"."productfeature" v20
      WHERE ((POSITION('${1:product.label:none}' IN v20."label") > 0) AND v20."label" IS NOT NULL AND v20."publisher" IS NOT NULL)
      UNION ALL
      SELECT v22."label" AS "label10m46", NULL AS "nr1m1", NULL AS "nr3m2", v22."nr" AS "nr8m3", 2 AS "v0"
      FROM "ss3"."producttype" v22
      WHERE ((POSITION('${1:product.label:none}' IN v22."label") > 0) AND v22."label" IS NOT NULL AND v22."publisher" IS NOT NULL)
     ) v24
