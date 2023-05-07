-- ${1:product.nr:percent}

SELECT v12."nr2m23" AS "nr2m23", COUNT(*) AS "v1"
FROM (SELECT DISTINCT v5."nr2m23" AS "nr2m23", v10."productfeature2m2" AS "productfeature2m2"
FROM (SELECT v1."nr" AS "nr2m23"
FROM "ss1"."product1" v1
WHERE (v1."nr" <= ${1:product.nr:percent})
UNION ALL
SELECT v3."nr" AS "nr2m23"
FROM "ss5"."product2" v3
WHERE (v3."nr" <= ${1:product.nr:percent})
) v5, (SELECT v6."product" AS "nr2m0", v6."productfeature" AS "productfeature2m2"
FROM "ss1"."productfeatureproduct1" v6
WHERE (v6."product" <= ${1:product.nr:percent})
UNION ALL
SELECT v8."product" AS "nr2m0", v8."productfeature" AS "productfeature2m2"
FROM "ss5"."productfeatureproduct2" v8
WHERE (v8."product" <= ${1:product.nr:percent})
) v10
WHERE v5."nr2m23" = v10."nr2m0"
) v12
GROUP BY v12."nr2m23"
