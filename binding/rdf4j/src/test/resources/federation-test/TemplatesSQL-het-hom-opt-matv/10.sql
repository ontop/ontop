-- ${1:product.nr:none}
-- ${1:vendor.country:none}

SELECT DISTINCT v9."nr1m5" AS "nr1m5", v9."product1m5" AS "product1m5"
FROM (
	  SELECT v1."o_deliverydays" AS "deliverydays1m37", v1."o_nr" AS "nr1m5", v1."o_price" AS "price1m39", v1."p_nr" AS "product1m5", v1."o_validto" AS "validto1m45"
      FROM "smatv"."op1" v1, "ss4"."vendor" v7
      WHERE ((v1."o_deliverydays" <= 3) AND (v1."p_nr" <= ${1:product.nr:none}) AND v1."o_deliverydays" IS NOT NULL AND v1."o_price" IS NOT NULL AND v1."o_validto" IS NOT NULL AND v1."o_vendor" = v7."nr" AND '${1:vendor.country:none}' = v7."country")
      UNION ALL
      SELECT v1."o_deliverydays" AS "deliverydays1m37", v1."o_nr" AS "nr1m5", v1."o_price" AS "price1m39", v1."p_nr" AS "product1m5", v1."o_validto" AS "validto1m45"
      FROM "smatv"."op2" v1, "ss4"."vendor" v7
      WHERE ((v1."o_deliverydays" <= 3) AND (v1."p_nr" <= ${1:product.nr:none}) AND v1."o_deliverydays" IS NOT NULL AND v1."o_price" IS NOT NULL AND v1."o_validto" IS NOT NULL AND v1."o_vendor" = v7."nr" AND '${1:vendor.country:none}' = v7."country")
) v9
