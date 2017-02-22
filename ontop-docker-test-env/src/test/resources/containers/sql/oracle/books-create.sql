--------------------------------------------------------
--  File created - Tuesday-February-21-2017
--------------------------------------------------------
--------------------------------------------------------
--  DDL for Table BOOKS
--------------------------------------------------------

  CREATE TABLE "SYSTEM"."BOOKS"
   (	"ID" NUMBER(*,0),
	"TITLE" VARCHAR2(100),
	"PRICE" NUMBER(8,2),
	"DISCOUNT" NUMBER(8,2),
	"DESCRIPTION" VARCHAR2(100),
	"LANG" VARCHAR2(100),
	"PUBLICATION_DATE" TIMESTAMP (6) WITH TIME ZONE
   ) ;
REM INSERTING into SYSTEM.BOOKS
SET DEFINE OFF;
Insert into SYSTEM.BOOKS (ID,TITLE,PRICE,DISCOUNT,DESCRIPTION,LANG,PUBLICATION_DATE) values ('1','SPARQL Tutorial','42.5','0.2','good','en',to_timestamp_tz('05-JUN-14 18:47:52.000000000 +08:00','DD-MON-RR HH24:MI:SSXFF TZR'));
Insert into SYSTEM.BOOKS (ID,TITLE,PRICE,DISCOUNT,DESCRIPTION,LANG,PUBLICATION_DATE) values ('2','The Semantic Web','23','0.25','bad','en',to_timestamp_tz('08-DEC-11 12:30:00.000000000 EUROPE/ROME','DD-MON-RR HH24:MI:SSXFF TZR'));
Insert into SYSTEM.BOOKS (ID,TITLE,PRICE,DISCOUNT,DESCRIPTION,LANG,PUBLICATION_DATE) values ('3','Crime and Punishment','33.5','0.2','good','en',to_timestamp_tz('21-SEP-15 09:23:06.000000000 ETC/GMT-0','DD-MON-RR HH24:MI:SSXFF TZR'));
Insert into SYSTEM.BOOKS (ID,TITLE,PRICE,DISCOUNT,DESCRIPTION,LANG,PUBLICATION_DATE) values ('4','The Logic Book: Introduction, Second Edition','10','0.15','good','en',to_timestamp_tz('05-NOV-67 07:50:00.000000000 EUROPE/ROME','DD-MON-RR HH24:MI:SSXFF TZR'));
--------------------------------------------------------
--  DDL for Index BOOKS_PKEY
--------------------------------------------------------

  CREATE UNIQUE INDEX "SYSTEM"."BOOKS_PKEY" ON "SYSTEM"."BOOKS" ("ID")
  ;
--------------------------------------------------------
--  Constraints for Table BOOKS
--------------------------------------------------------

  ALTER TABLE "SYSTEM"."BOOKS" ADD CONSTRAINT "BOOKS_PKEY" PRIMARY KEY ("ID") ENABLE;
  ALTER TABLE "SYSTEM"."BOOKS" MODIFY ("ID" NOT NULL ENABLE);
