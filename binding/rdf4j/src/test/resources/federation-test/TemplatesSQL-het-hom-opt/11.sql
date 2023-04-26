-- ${1:offer.nr:percent}

SELECT v25."v26" AS "v26", v25."v6" AS "v6", v25."v9" AS "v9"
FROM (SELECT 'http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/Offer' AS "v26", 0 AS "v6", 'http://www.w3.org/1999/02/22-rdf-syntax-ns#type' AS "v9"
FROM "ss4"."offer" v1
WHERE ${1:offer.nr:percent} = v1."nr"
UNION ALL 
SELECT 'http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/Product' AS "v26", 0 AS "v6", 'http://www.w3.org/1999/02/22-rdf-syntax-ns#type' AS "v9"
FROM "ss4"."offer" v3
WHERE (v3."vendor" IS NOT NULL AND ${1:offer.nr:percent} = v3."nr")
UNION ALL 
SELECT ('http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer/Product' || CAST(v5."product" AS STRING)) AS "v26", 0 AS "v6", 'http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/product' AS "v9"
FROM "ss4"."offer" v5
WHERE (v5."product" IS NOT NULL AND ${1:offer.nr:percent} = v5."nr")
UNION ALL 
SELECT ('http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor/Vendor' || CAST(v7."vendor" AS STRING)) AS "v26", 0 AS "v6", 'http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/vendor' AS "v9"
FROM "ss4"."offer" v7
WHERE (v7."vendor" IS NOT NULL AND ${1:offer.nr:percent} = v7."nr")
UNION ALL 
SELECT ('http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor/Vendor' || CAST(v9."vendor" AS STRING)) AS "v26", 0 AS "v6", 'http://purl.org/dc/elements/1.1/publisher' AS "v9"
FROM "ss4"."offer" v9
WHERE (v9."vendor" IS NOT NULL AND ${1:offer.nr:percent} = v9."nr")
UNION ALL 
SELECT '${1:offer.nr:percent}' AS "v26", 1 AS "v6", 'http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/offerId' AS "v9"
FROM "ss4"."offer" v11
WHERE ${1:offer.nr:percent} = v11."nr"
UNION ALL 
SELECT CAST(v13."publishdate" AS STRING) AS "v26", 2 AS "v6", 'http://purl.org/dc/elements/1.1/date' AS "v9"
FROM "ss4"."offer" v13
WHERE (v13."publishdate" IS NOT NULL AND ${1:offer.nr:percent} = v13."nr")
UNION ALL 
SELECT v15."offerwebpage" AS "v26", 3 AS "v6", 'http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/offerWebpage' AS "v9"
FROM "ss4"."offer" v15
WHERE (v15."offerwebpage" IS NOT NULL AND ${1:offer.nr:percent} = v15."nr")
UNION ALL 
SELECT CAST(v17."deliverydays" AS STRING) AS "v26", 1 AS "v6", 'http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/deliveryDays' AS "v9"
FROM "ss4"."offer" v17
WHERE (v17."deliverydays" IS NOT NULL AND ${1:offer.nr:percent} = v17."nr")
UNION ALL 
SELECT CAST(v19."price" AS STRING) AS "v26", 4 AS "v6", 'http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/price' AS "v9"
FROM "ss4"."offer" v19
WHERE (v19."price" IS NOT NULL AND ${1:offer.nr:percent} = v19."nr")
UNION ALL 
SELECT CAST(v21."validfrom" AS STRING) AS "v26", 2 AS "v6", 'http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/validFrom' AS "v9"
FROM "ss4"."offer" v21
WHERE (v21."validfrom" IS NOT NULL AND ${1:offer.nr:percent} = v21."nr")
UNION ALL 
SELECT CAST(v23."validto" AS STRING) AS "v26", 2 AS "v6", 'http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/validTo' AS "v9"
FROM "ss4"."offer" v23
WHERE (v23."validto" IS NOT NULL AND ${1:offer.nr:percent} = v23."nr")
) v25
