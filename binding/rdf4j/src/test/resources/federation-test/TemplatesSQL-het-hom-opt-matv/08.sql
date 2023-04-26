-- ${1:review.product:percent}

SELECT v49."name1m12" AS "name1m12", v49."person2m7" AS "person2m7", v49."rating1m13" AS "rating1m13", v49."rating1m15" AS "rating1m15", v49."rating1m16" AS "rating1m16", v49."rating1m17" AS "rating1m17", v49."reviewdate2m43" AS "reviewdate2m43", v49."text2m14" AS "text2m14", v49."title2m11" AS "title2m11"
FROM (
	  SELECT DISTINCT v27."name1m12" AS "name1m12", CASE WHEN v5."rating1m13" IS NOT NULL THEN v5."nr0m4" ELSE NULL END AS "nr0m13",
	                  CASE WHEN v5."rating1m15" IS NOT NULL THEN v5."nr0m4" ELSE NULL END AS "nr0m15",
	                  CASE WHEN v5."rating1m16" IS NOT NULL THEN v5."nr0m4" ELSE NULL END AS "nr0m16",
	                  CASE WHEN v5."rating1m17" IS NOT NULL THEN v5."nr0m4" ELSE NULL END AS "nr0m17", v5."nr0m4" AS "nr0m4",
	                  v5."person2m7" AS "person2m7", v5."rating1m13" AS "rating1m13", v5."rating1m15" AS "rating1m15",
	                  v5."rating1m16" AS "rating1m16", v5."rating1m17" AS "rating1m17", v5."reviewdate2m43" AS "reviewdate2m43",
	                  v5."text2m14" AS "text2m14", v5."title2m11" AS "title2m11"
      FROM (
		    SELECT v1."nr" AS "nr0m4", v1."title" AS "title2m11", v1."text" AS "text2m14", v1."reviewdate" AS "reviewdate2m43",
		           v1."person" AS "person2m7", v1."rating1" AS "rating1m16", v1."rating2" AS "rating1m17", v1."rating3" AS "rating1m13",
		           v1."rating4" AS "rating1m15"
            FROM "ss1"."reviewc" v1
            WHERE (v1."nr" IS NOT NULL AND ${1:review.product:percent} = v1."product" AND v1."title" IS NOT NULL AND v1."text" IS NOT NULL AND v1."reviewdate" IS NOT NULL
                  )
          ) v5
              JOIN (
				    SELECT v22."name" AS "name1m12", v22."nr" AS "nr"
                    FROM "ss2"."person" v22
                    WHERE (v22."nr" IS NOT NULL AND v22."name" IS NOT NULL)
                   ) v27 ON (v5."person2m7" = v27."nr")
) v49
