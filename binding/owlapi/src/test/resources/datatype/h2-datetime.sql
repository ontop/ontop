
--
-- Table structure for table "DateTimes"
--

DROP TABLE IF EXISTS "DateTimes";

CREATE TABLE "DateTimes" (
  "id" int(11) NOT NULL,
  "type_date" date DEFAULT NULL,
  "type_datetime" datetime DEFAULT NULL,
  "type_timestamp" timestamp NULL DEFAULT NULL,
  "type_smalldatetime" smalldatetime NULL DEFAULT NULL,
  "type_time" time DEFAULT NULL,
  "type_year" year DEFAULT NULL,
  PRIMARY KEY ("id")
);


INSERT INTO "DateTimes" VALUES (1,'2013-03-18','2013-03-18 10:12:10','2013-03-18 09:12:10','2013-03-18 09:12:10','10:12:10',2013);
