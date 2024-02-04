SELECT DISTINCT v5."label10m40" AS "label10m40", v5."nr0m49" AS "nr0m49"
FROM ((SELECT v1."label" AS "label10m40", v1."nr" AS "nr0m49"
FROM "product1" v1
WHERE ((POSITION('banded' IN v1."label") > 0) AND v1."label" IS NOT NULL)
)UNION ALL 
(SELECT v3."label" AS "label10m40", v3."nr" AS "nr0m49"
FROM "product2" v3
WHERE ((POSITION('banded' IN v3."label") > 0) AND v3."label" IS NOT NULL)
)) v5
