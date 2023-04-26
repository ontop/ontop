-- ${1:product.nr:percent}
-- ${1:offer.validto:none}

SELECT v36."label10m4" AS "label10m4", v36."label10m46" AS "label10m46", v36."name1m12" AS "name1m12", v36."nr0m4" AS "nr0m4", v36."nr1m5" AS "nr1m5", v36."person2m7" AS "person2m7", v36."price1m39" AS "price1m39", v36."rating1m16" AS "rating1m16", v36."rating1m17" AS "rating1m17", v36."title2m11" AS "title2m11", v36."vendor1m8" AS "vendor1m8"
FROM (
      SELECT DISTINCT v7."label" AS "label10m4", v5."label10m46" AS "label10m46", v19."name1m12" AS "name1m12",
                      CASE WHEN v29."rating1m16" IS NOT NULL THEN v12."nr0m4" ELSE NULL END AS "nr0m16",
                      CASE WHEN v34."rating1m17" IS NOT NULL THEN v12."nr0m4" ELSE NULL END AS "nr0m17", v12."nr0m4" AS "nr0m4",
                      v6."nr" AS "nr1m5", v19."person2m7" AS "person2m7", v6."price" AS "price1m39", v29."rating1m16" AS "rating1m16",
                      v34."rating1m17" AS "rating1m17", v24."title2m11" AS "title2m11", v6."validto" AS "validto1m45",
                      v6."vendor" AS "vendor1m8"
      FROM (
            SELECT v1."label" AS "label10m46"
            FROM "ss1"."product1" v1
            WHERE (v1."label" IS NOT NULL AND ${1:product.nr:percent} = v1."nr")
            UNION ALL
            SELECT v3."label" AS "label10m46"
            FROM "ss5"."product2" v3
            WHERE (v3."label" IS NOT NULL AND ${1:product.nr:percent} = v3."nr")
           ) v5
      LEFT OUTER JOIN
      "ss4"."offer" v6
      JOIN
      "ss4"."vendor" v7 ON ((v6."validto" >= '${1:offer.validto:none}') AND v6."price" IS NOT NULL AND v7."label" IS NOT NULL AND v6."validto" IS NOT NULL AND v6."vendor" = v7."nr" AND ${1:product.nr:percent} = v6."product" AND 'DE' = v7."country")  ON 1 = 1
      LEFT OUTER JOIN
      (
       SELECT v10."nr" AS "nr0m4"
       FROM "ss2"."review" v10
       WHERE ${1:product.nr:percent} = v10."product"
       ) v12
      JOIN
      (
       SELECT v17."name" AS "name1m12", v16."nr" AS "nr0m7", v16."person" AS "person2m7"
       FROM "ss2"."review" v16, "ss2"."person" v17
       WHERE (v17."name" IS NOT NULL AND v16."person" = v17."nr")
     ) v19 ON 1 = 1
     JOIN
    (
      SELECT v22."nr" AS "nr0m8", v22."title" AS "title2m11"
      FROM "ss2"."review" v22
    WHERE v22."title" IS NOT NULL
    ) v24 ON (v12."nr0m4" = v19."nr0m7" AND v12."nr0m4" = v24."nr0m8")
    LEFT OUTER JOIN
    (
      SELECT v27."nr" AS "nr0m6", v27."rating1" AS "rating1m16"
      FROM "ss2"."review" v27
      WHERE v27."rating1" IS NOT NULL
    ) v29 ON v12."nr0m4" = v29."nr0m6"
    LEFT OUTER JOIN
    (
      SELECT v32."nr" AS "nr0m5", v32."rating2" AS "rating1m17"
      FROM "ss2"."review" v32
      WHERE v32."rating2" IS NOT NULL
     ) v34 ON v12."nr0m4" = v34."nr0m5"  ON 1 = 1
) v36
