-- ${1:review.nr:percent}

SELECT v24."country3m1" AS "country3m1", v24."mbox_sha1sum1m47" AS "mbox_sha1sum1m47", v24."nr0m4" AS "nr0m4", v24."person2m7" AS "person2m7", v24."product2m4" AS "product2m4", v24."title2m11" AS "title2m11"
FROM (
      SELECT DISTINCT v7."country3m1" AS "country3m1", v7."mbox_sha1sum1m47" AS "mbox_sha1sum1m47", v7."name1m12" AS "name1m12", v12."nr0m4" AS "nr0m4", v7."person2m7" AS "person2m7", v17."product2m4" AS "product2m4", v22."title2m11" AS "title2m11"
      FROM (
            SELECT v5."country" AS "country3m1", v5."mbox_sha1sum" AS "mbox_sha1sum1m47", v5."name" AS "name1m12", v4."person" AS "person2m7"
            FROM "ss2"."review" v4, "ss2"."person" v5
            WHERE (v5."name" IS NOT NULL AND v5."mbox_sha1sum" IS NOT NULL AND v5."country" IS NOT NULL AND v4."person" = v5."nr" AND ${1:review.nr:percent} = v4."nr")
           ) v7, (
                  SELECT v10."nr" AS "nr0m4", v10."person" AS "person2m0"
                  FROM "ss2"."review" v10
                  WHERE v10."person" IS NOT NULL
                 ) v12, (
                         SELECT v15."nr" AS "nr0m1", v15."product" AS "product2m4"
                         FROM "ss2"."review" v15
                         WHERE v15."product" IS NOT NULL
                        ) v17, (
                                SELECT v20."nr" AS "nr0m2", v20."title" AS "title2m11"
                                FROM "ss2"."review" v20
                                WHERE v20."title" IS NOT NULL
                               ) v22
           WHERE (v7."person2m7" = v12."person2m0" AND v12."nr0m4" = v17."nr0m1" AND v12."nr0m4" = v22."nr0m2")
) v24
