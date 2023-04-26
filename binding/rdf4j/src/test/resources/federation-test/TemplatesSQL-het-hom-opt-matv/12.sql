-- ${1:offer.nr:none}

SELECT v11."deliverydays1m37" AS "deliverydays1m37", v11."label10m4" AS "label10m4", v11."label10m46" AS "label10m46", v11."nr1m21" AS "nr1m21", v11."offerwebpage1m24" AS "offerwebpage1m24", v11."price1m39" AS "price1m39", v11."product1m5" AS "product1m5", v11."validto1m45" AS "validto1m45", v11."vendor1m8" AS "vendor1m8"
FROM (
	  SELECT DISTINCT v9."deliverydays1m37" AS "deliverydays1m37", v9."homepage2m48" AS "homepage2m48",
	  v9."label10m4" AS "label10m4", v9."label10m46" AS "label10m46", v9."nr1m21" AS "nr1m21",
	  v9."offerwebpage1m24" AS "offerwebpage1m24", v9."price1m39" AS "price1m39", v9."product1m5" AS "product1m5",
	  v9."validto1m45" AS "validto1m45", v9."vendor1m8" AS "vendor1m8"
      FROM (
		    SELECT v."o_deliverydays" AS "deliverydays1m37", v3."homepage" AS "homepage2m48", v3."label" AS "label10m4",
		           v."p_label" AS "label10m46", v."o_nr" AS "nr1m21", v."o_offerwebpages" AS "offerwebpage1m24",
		           v."o_price" AS "price1m39", v."p_nr" AS "product1m5", v."o_validto" AS "validto1m45", v."o_vendor" AS "vendor1m8"
            FROM "smatv"."op1" v, "ss4"."vendor" v3
            WHERE (v."p_label" IS NOT NULL AND (v."o_nr" <= ${1:offer.nr:none}) AND v3."label" IS NOT NULL AND v3."homepage" IS NOT NULL AND
                   v."o_offerwebpages" IS NOT NULL AND v."o_price" IS NOT NULL AND v."o_deliverydays" IS NOT NULL AND
                   v."o_validto" IS NOT NULL AND v."o_vendor" = v3."nr")
            UNION ALL
            SELECT v."o_deliverydays" AS "deliverydays1m37", v7."homepage" AS "homepage2m48", v7."label" AS "label10m4",
                   v."p_label" AS "label10m46", v."o_nr" AS "nr1m21", v."o_offerwebpages" AS "offerwebpage1m24",
                   v."o_price" AS "price1m39", v."p_nr" AS "product1m5", v."o_validto" AS "validto1m45", v."o_vendor" AS "vendor1m8"
            FROM "smatv"."op2" v, "ss4"."vendor" v7
            WHERE (v."p_label" IS NOT NULL AND (v."o_nr" <= ${1:offer.nr:none}) AND v7."label" IS NOT NULL AND v7."homepage" IS NOT NULL AND
                   v."o_offerwebpages" IS NOT NULL AND v."o_price" IS NOT NULL AND v."o_deliverydays" IS NOT NULL AND
                   v."o_validto" IS NOT NULL AND v."o_vendor" = v7."nr")
           ) v9
) v11
