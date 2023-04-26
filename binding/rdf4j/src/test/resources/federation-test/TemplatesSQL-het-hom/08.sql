-- ${1:review.product:percent}

SELECT v49."name1m12" AS "name1m12", v49."person2m7" AS "person2m7", v49."rating1m13" AS "rating1m13", v49."rating1m15" AS "rating1m15", v49."rating1m16" AS "rating1m16", v49."rating1m17" AS "rating1m17", v49."reviewdate2m43" AS "reviewdate2m43", v49."text2m14" AS "text2m14", v49."title2m11" AS "title2m11"
FROM (
    SELECT DISTINCT v27."name1m12" AS "name1m12", CASE WHEN v42."rating1m13" IS NOT NULL THEN v5."nr0m4" ELSE NULL END AS "nr0m13",
                   CASE WHEN v47."rating1m15" IS NOT NULL THEN v5."nr0m4" ELSE NULL END AS "nr0m15", CASE WHEN v32."rating1m16" IS NOT NULL THEN v5."nr0m4" ELSE NULL END AS "nr0m16",
                   CASE WHEN v37."rating1m17" IS NOT NULL THEN v5."nr0m4" ELSE NULL END AS "nr0m17", v5."nr0m4" AS "nr0m4",
                   v27."person2m7" AS "person2m7", v42."rating1m13" AS "rating1m13", v47."rating1m15" AS "rating1m15", v32."rating1m16" AS "rating1m16",
                  v37."rating1m17" AS "rating1m17", v20."reviewdate2m43" AS "reviewdate2m43", v15."text2m14" AS "text2m14", v10."title2m11" AS "title2m11"
    FROM (
          SELECT v1."nr" AS "nr0m4"
          FROM "ss1"."reviewc" v1
          WHERE ${1:review.product:percent} = v1."product"
          UNION ALL
          SELECT v3."nr" AS "nr0m4"
          FROM "ss2"."review" v3
          WHERE ${1:review.product:percent} = v3."product"
         ) v5
    JOIN
        (
         SELECT v6."nr" AS "nr0m5", v6."title" AS "title2m11"
         FROM "ss1"."reviewc" v6
         WHERE v6."title" IS NOT NULL
         UNION ALL
         SELECT v8."nr" AS "nr0m5", v8."title" AS "title2m11"
         FROM "ss2"."review" v8
         WHERE v8."title" IS NOT NULL
        ) v10 ON 1 = 1
        JOIN
            (
             SELECT v11."nr" AS "nr0m6", v11."text" AS "text2m14"
             FROM "ss1"."reviewc" v11
             WHERE v11."text" IS NOT NULL
             UNION ALL
             SELECT v13."nr" AS "nr0m6", v13."text" AS "text2m14"
             FROM "ss2"."review" v13
             WHERE v13."text" IS NOT NULL
            ) v15 ON 1 = 1
            JOIN
                (
                 SELECT v16."nr" AS "nr0m7", v16."reviewdate" AS "reviewdate2m43"
                 FROM "ss1"."reviewc" v16
                 WHERE v16."reviewdate" IS NOT NULL
                 UNION ALL
                 SELECT v18."nr" AS "nr0m7", v18."reviewdate" AS "reviewdate2m43"
                 FROM "ss2"."review" v18
                 WHERE v18."reviewdate" IS NOT NULL
                ) v20 ON 1 = 1
                JOIN
                    (
                     SELECT v22."name" AS "name1m12", v21."nr" AS "nr0m8", v21."person" AS "person2m7"
                     FROM "ss1"."reviewc" v21, "ss2"."person" v22
                     WHERE (v22."name" IS NOT NULL AND v21."person" = v22."nr")
                     UNION ALL
                     SELECT v25."name" AS "name1m12", v24."nr" AS "nr0m8", v24."person" AS "person2m7"
                     FROM "ss2"."review" v24, "ss2"."person" v25
                     WHERE (v25."name" IS NOT NULL AND v24."person" = v25."nr")
                    ) v27 ON (v5."nr0m4" = v10."nr0m5" AND v5."nr0m4" = v15."nr0m6" AND v5."nr0m4" = v20."nr0m7" AND v5."nr0m4" = v27."nr0m8")
                    LEFT OUTER JOIN
                         (
                          SELECT v28."nr" AS "nr0m3", v28."rating1" AS "rating1m16"
                          FROM "ss1"."reviewc" v28
                          WHERE v28."rating1" IS NOT NULL
                          UNION ALL
                          SELECT v30."nr" AS "nr0m3", v30."rating1" AS "rating1m16"
                          FROM "ss2"."review" v30
                          WHERE v30."rating1" IS NOT NULL
                          ) v32 ON v5."nr0m4" = v32."nr0m3"
                          LEFT OUTER JOIN
                              (
                               SELECT v33."nr" AS "nr0m2", v33."rating2" AS "rating1m17"
                               FROM "ss1"."reviewc" v33
                               WHERE v33."rating2" IS NOT NULL
                               UNION ALL
                               SELECT v35."nr" AS "nr0m2", v35."rating2" AS "rating1m17"
                               FROM "ss2"."review" v35
                               WHERE v35."rating2" IS NOT NULL
                              ) v37 ON v5."nr0m4" = v37."nr0m2"
                              LEFT OUTER JOIN
                                   (
                                    SELECT v38."nr" AS "nr0m1", v38."rating3" AS "rating1m13"
                                    FROM "ss1"."reviewc" v38
                                    WHERE v38."rating3" IS NOT NULL
                                    UNION ALL
                                    SELECT v40."nr" AS "nr0m1", v40."rating3" AS "rating1m13"
                                    FROM "ss2"."review" v40
                                    WHERE v40."rating3" IS NOT NULL
                                   ) v42 ON v5."nr0m4" = v42."nr0m1"
                                   LEFT OUTER JOIN
                                       (
                                        SELECT v43."nr" AS "nr0m0", v43."rating4" AS "rating1m15"
                                        FROM "ss1"."reviewc" v43
                                        WHERE v43."rating4" IS NOT NULL
                                        UNION ALL
                                        SELECT v45."nr" AS "nr0m0", v45."rating4" AS "rating1m15"
                                        FROM "ss2"."review" v45
                                        WHERE v45."rating4" IS NOT NULL
                                       ) v47 ON v5."nr0m4" = v47."nr0m0"
) v49
