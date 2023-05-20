SELECT v9."name1m12" AS "name1m12", v9."person2m7" AS "person2m7", v9."rating1m13" AS "rating1m13", v9."rating1m15" AS "rating1m15", v9."rating1m16" AS "rating1m16", v9."rating1m17" AS "rating1m17", v9."reviewdate2m43" AS "reviewdate2m43", v9."text2m14" AS "text2m14", v9."title2m11" AS "title2m11"
FROM (
      SELECT DISTINCT v3."name" AS "name1m12", CASE WHEN v6."rating3" IS NOT NULL THEN v1."nr" ELSE NULL END AS "nr0m13", CASE WHEN v7."rating4" IS NOT NULL THEN v1."nr" ELSE NULL END AS "nr0m15", CASE WHEN v4."rating1" IS NOT NULL THEN v1."nr" ELSE NULL END AS "nr0m16", CASE WHEN v5."rating2" IS NOT NULL THEN v1."nr" ELSE NULL END AS "nr0m17", v1."nr" AS "nr0m4", v2."person" AS "person2m7", v6."rating3" AS "rating1m13", v7."rating4" AS "rating1m15", v4."rating1" AS "rating1m16", v5."rating2" AS "rating1m17", v1."reviewdate" AS "reviewdate2m43", v1."text" AS "text2m14", v1."title" AS "title2m11"
      FROM "ss2"."review" v1
      JOIN
        "ss2"."review" v2 ON 1 = 1
      JOIN
        "ss2"."person" v3 ON (v3."name" IS NOT NULL AND v1."title" IS NOT NULL AND v1."text" IS NOT NULL AND v1."reviewdate" IS NOT NULL AND v1."nr" = v2."nr" AND v2."person" = v3."nr" AND 88 = v1."product")
      LEFT OUTER JOIN
        "ss2"."review" v4 ON (v4."rating1" IS NOT NULL AND v1."nr" = v4."nr")
      LEFT OUTER JOIN
        "ss2"."review" v5 ON (v5."rating2" IS NOT NULL AND v1."nr" = v5."nr")
      LEFT OUTER JOIN
        "ss2"."review" v6 ON (v6."rating3" IS NOT NULL AND v1."nr" = v6."nr")
      LEFT OUTER JOIN
        "ss2"."review" v7 ON (v7."rating4" IS NOT NULL AND v1."nr" = v7."nr")
) v9