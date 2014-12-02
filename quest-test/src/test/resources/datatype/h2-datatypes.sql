
--
-- Table structure for table "Booleans"
--

DROP TABLE IF EXISTS "Booleans";

CREATE TABLE "Booleans" (
  "id" int(11) NOT NULL,
  "type_bit" boolean DEFAULT NULL,
  PRIMARY KEY ("id")
);

INSERT INTO "Booleans" VALUES (1,TRUE),(2,FALSE);

DROP TABLE IF EXISTS "Characters";

CREATE TABLE "Characters" (
  "id" int(11) NOT NULL,
  "type_char" char(1) DEFAULT NULL,
  "type_varchar" varchar(100) DEFAULT NULL,
  "type_nchar" nchar,
  "type_nvarchar" nvarchar,
  "type_text" text,
  "type_longtext" longtext,
  "type_mediumtext" mediumtext,
  "type_tinytext" tinytext,
  PRIMARY KEY ("id")
);

INSERT INTO "Characters" VALUES (1,'a','abc','a','abc','abc','abc','abc','abc');


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


--
-- Table structure for table "Numerics"
--

DROP TABLE IF EXISTS "Numerics";

CREATE TABLE "Numerics" (
  "id" int(11) NOT NULL,
  "type_tinyint" tinyint DEFAULT NULL,
  "type_smallint" smallint DEFAULT NULL,
  "type_mediumint" mediumint DEFAULT NULL,
  "type_int" int(11) DEFAULT NULL,
  "type_bigint" bigint(20) DEFAULT NULL,
  "type_decimal" decimal(16,5) DEFAULT NULL,
  "type_float" float DEFAULT NULL,
  "type_double" double DEFAULT NULL,
  "type_real" real DEFAULT NULL,
  PRIMARY KEY ("id")
);


INSERT INTO "Numerics" VALUES (1,1,1,1,1,1,1.00000,1,1,1);