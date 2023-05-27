SELECT v13."label10m46" AS "label10m46", v13."nr1m1" AS "nr1m1", v13."nr3m2" AS "nr3m2", v13."nr8m3" AS "nr8m3", v13."v0" AS "v0"
FROM (
       (
        SELECT v7."label10m46" AS "label10m46", v7."nr1m1" AS "nr1m1", NULL AS "nr3m2", NULL AS "nr8m3", 0 AS "v0"
        FROM (
              SELECT DISTINCT v5."label10m46" AS "label10m46", v5."nr1m1" AS "nr1m1"
              FROM (
                    (
                     SELECT v1."label" AS "label10m46", v1."nr" AS "nr1m1"
                     FROM "ss1"."product1" v1
                     WHERE ((POSITION('%word1%' IN v1."label") > 0) AND v1."label" IS NOT NULL)
                    )
                    UNION ALL
                   (
                     SELECT v3."label" AS "label10m46", v3."nr" AS "nr1m1"
                     FROM "ss5"."product2" v3
                     WHERE ((POSITION('%word1%' IN v3."label") > 0) AND v3."label" IS NOT NULL)
                   )
                  ) v5
            ) v7
      )
       UNION ALL
     (
      SELECT v9."label" AS "label10m46", NULL AS "nr1m1", v9."nr" AS "nr3m2", NULL AS "nr8m3", 1 AS "v0"
      FROM "ss3"."productfeature" v9
      WHERE ( ( POSITION('%word1%' IN v9."label") > 0) AND v9."label" IS NOT NULL AND v9."publisher" IS NOT NULL)
     )
      UNION ALL
    (
      SELECT v11."label" AS "label10m46", NULL AS "nr1m1", NULL AS "nr3m2", v11."nr" AS "nr8m3", 2 AS "v0"
      FROM "ss3"."producttype" v11
      WHERE ((POSITION('%word1%' IN v11."label") > 0) AND v11."label" IS NOT NULL AND v11."publisher" IS NOT NULL)
    )
) v13