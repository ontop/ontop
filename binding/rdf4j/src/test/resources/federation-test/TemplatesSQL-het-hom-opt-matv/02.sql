-- ${1:product.nr:percent}

SELECT v76."comment10m20" AS "comment10m20", v76."label10m18" AS "label10m18", v76."label10m46" AS "label10m46", v76."label10m6" AS "label10m6", v76."propertynum1m25" AS "propertynum1m25", v76."propertynum1m40" AS "propertynum1m40", v76."propertynum1m41" AS "propertynum1m41", v76."propertytex1m30" AS "propertytex1m30", v76."propertytex1m31" AS "propertytex1m31", v76."propertytex1m32" AS "propertytex1m32", v76."propertytex1m33" AS "propertytex1m33", v76."propertytex1m34" AS "propertytex1m34"
FROM (
      SELECT v1o."p_nr" AS "nr2m23", v1o."p_label" AS "label10m46", v1o."p_comment" AS "comment10m20", v1o."pd_producer" AS "producer2m9",
             v1o."pd_label" AS "label10m6", v1o."p_propertytex1" AS "propertytex1m34", v1o."p_propertytex2" AS "propertytex1m33",
             v1o."p_propertytex3" AS "propertytex1m32", v1o."p_propertynum1" AS "propertynum1m41", v1o."p_propertynum2" AS "propertynum1m40",
             v1o."p_propertytex4" AS "propertytex1m31", v1o."p_propertytex5" AS "propertytex1m30", v1o."p_propertynum4" AS "propertynum1m25",
             v2o."f_label" AS "label10m18", v2o."productfeature" AS "productfeature2m2"
      FROM "smatv"."ppd1" v1o, "smatv"."pfpf1" v2o
      WHERE (v1o."p_nr" IS NOT NULL AND (v1o."p_nr" <= ${1:product.nr:percent}) AND v1o."p_label" IS NOT NULL AND v1o."p_comment" IS NOT NULL AND v1o."p_propertytex1" IS NOT NULL
	   AND v1o."p_propertytex2" IS NOT NULL AND v1o."p_propertytex3" IS NOT NULL AND v1o."p_propertynum1" IS NOT NULL
	   AND v1o."p_propertynum2" IS NOT NULL AND v1o."pd_producer" IS NOT NULL AND (v2o."product"<=${1:product.nr:percent})
	   AND v2o."product"=v1o."p_nr"
	  )
      UNION ALL
      SELECT v1o."p_nr" AS "nr2m23", v1o."p_label" AS "label10m46", v1o."p_comment" AS "comment10m20", v1o."pd_producer" AS "producer2m9",
             v1o."pd_label" AS "label10m6", v1o."p_propertytex1" AS "propertytex1m34", v1o."p_propertytex2" AS "propertytex1m33",
             v1o."p_propertytex3" AS "propertytex1m32", v1o."p_propertynum1" AS "propertynum1m41", v1o."p_propertynum2" AS "propertynum1m40",
             v1o."p_propertytex4" AS "propertytex1m31", v1o."p_propertytex5" AS "propertytex1m30", v1o."p_propertynum4" AS "propertynum1m25",
             v2o."f_label" AS "label10m18", v2o."productfeature" AS "productfeature2m2"
      FROM "smatv"."ppd2" v1o, "smatv"."pfpf2" v2o
      WHERE (v1o."p_nr" IS NOT NULL AND (v1o."p_nr" <= ${1:product.nr:percent}) AND v1o."p_label" IS NOT NULL AND v1o."p_comment" IS NOT NULL AND v1o."p_propertytex1" IS NOT NULL
	   AND v1o."p_propertytex2" IS NOT NULL AND v1o."p_propertytex3" IS NOT NULL AND v1o."p_propertynum1" IS NOT NULL
	   AND v1o."p_propertynum2" IS NOT NULL AND v1o."pd_producer" IS NOT NULL AND (v2o."product"<=${1:product.nr:percent})
	   AND v2o."product"=v1o."p_nr"
	  )
) v76
