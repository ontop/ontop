SELECT v23."v25" AS "v25", v23."v6" AS "v6", v23."v9" AS "v9"
FROM ((SELECT 'http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/Offer' AS "v25", 0 AS "v6", 'http://www.w3.org/1999/02/22-rdf-syntax-ns#type' AS "v9"
FROM "bsbm"."offer" v1
WHERE 2838 = v1."nr"
)UNION ALL 
(SELECT ('http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer/Product' || CAST(v3."product" AS VARCHAR)) AS "v25", 0 AS "v6", 'http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/product' AS "v9"
FROM "bsbm"."offer" v3
WHERE 2838 = v3."nr"
)UNION ALL 
(SELECT ('http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor/Vendor' || CAST(v5."vendor" AS VARCHAR)) AS "v25", 0 AS "v6", 'http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/vendor' AS "v9"
FROM "bsbm"."offer" v5
WHERE 2838 = v5."nr"
)UNION ALL 
(SELECT ('http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor/Vendor' || CAST(v7."vendor" AS VARCHAR)) AS "v25", 0 AS "v6", 'http://purl.org/dc/elements/1.1/publisher' AS "v9"
FROM "bsbm"."offer" v7
WHERE 2838 = v7."nr"
)UNION ALL 
(SELECT '2838' AS "v25", 1 AS "v6", 'http://purl.org/dc/elements/1.1/identifier' AS "v9"
FROM "bsbm"."offer" v9
WHERE 2838 = v9."nr"
)UNION ALL 
(SELECT CAST(v11."publishdate" AS VARCHAR) AS "v25", 2 AS "v6", 'http://purl.org/dc/elements/1.1/date' AS "v9"
FROM "bsbm"."offer" v11
WHERE 2838 = v11."nr"
)UNION ALL 
(SELECT v13."offerwebpage" AS "v25", 3 AS "v6", 'http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/offerWebpage' AS "v9"
FROM "bsbm"."offer" v13
WHERE 2838 = v13."nr"
)UNION ALL 
(SELECT CAST(v15."deliverydays" AS VARCHAR) AS "v25", 1 AS "v6", 'http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/deliveryDays' AS "v9"
FROM "bsbm"."offer" v15
WHERE 2838 = v15."nr"
)UNION ALL 
(SELECT CAST(v17."price" AS VARCHAR) AS "v25", 4 AS "v6", 'http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/price' AS "v9"
FROM "bsbm"."offer" v17
WHERE 2838 = v17."nr"
)UNION ALL 
(SELECT CAST(v19."validfrom" AS VARCHAR) AS "v25", 2 AS "v6", 'http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/validFrom' AS "v9"
FROM "bsbm"."offer" v19
WHERE 2838 = v19."nr"
)UNION ALL 
(SELECT CAST(v21."validto" AS VARCHAR) AS "v25", 2 AS "v6", 'http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/validTo' AS "v9"
FROM "bsbm"."offer" v21
WHERE 2838 = v21."nr"
)) v23
