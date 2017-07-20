--------------------------------------------------------
--  File created - Thursday-January-19-2017   
--------------------------------------------------------
DROP TABLE "SYSTEM"."ADDRESS_NEW" cascade constraints;
DROP TABLE "SYSTEM"."PERSON_NEW" cascade constraints;
DROP TABLE "SYSTEM"."BROKER_NEW" cascade constraints;
DROP TABLE "SYSTEM"."CLIENT_NEW" cascade constraints;
DROP TABLE "SYSTEM"."COMPANY_NEW" cascade constraints;
DROP TABLE "SYSTEM"."STOCKINFORMATION_NEW" cascade constraints;
DROP TABLE "SYSTEM"."STOCKBOOKLIST_NEW" cascade constraints;
DROP TABLE "SYSTEM"."BROKERWORKSFOR_NEW" cascade constraints;
DROP TABLE "SYSTEM"."TRANSACTION_NEW" cascade constraints;

--------------------------------------------------------
--  DDL for Table ADDRESS_NEW
--------------------------------------------------------

  CREATE TABLE "SYSTEM"."ADDRESS_NEW" 
   (	"ID" NUMBER(*,0), 
	"STREET" VARCHAR2(100), 
	"NUMBER" NUMBER(*,0), 
	"CITY" VARCHAR2(100), 
	"STATE" VARCHAR2(100), 
	"COUNTRY" VARCHAR2(100)
   ) ;
--------------------------------------------------------
--  DDL for Table PERSON_NEW
--------------------------------------------------------

  CREATE TABLE "SYSTEM"."PERSON_NEW" 
   (	"ID" NUMBER(*,0), 
	"NAME" VARCHAR2(100), 
	"LASTNAME" VARCHAR2(100), 
	"DATEOFBIRTH" DATE, 
	"SSN" VARCHAR2(100), 
	"ADDRESSID" NUMBER(*,0)
   ) ;
--------------------------------------------------------
--  DDL for Table BROKER_NEW
--------------------------------------------------------

  CREATE TABLE "SYSTEM"."BROKER_NEW" 
   (	"ID" NUMBER(*,0)
   ) ;
--------------------------------------------------------
--  DDL for Table CLIENT_NEW
--------------------------------------------------------

  CREATE TABLE "SYSTEM"."CLIENT_NEW" 
   (	"ID" NUMBER(*,0)
   ) ;
--------------------------------------------------------
--  DDL for Table COMPANY_NEW
--------------------------------------------------------

  CREATE TABLE "SYSTEM"."COMPANY_NEW" 
   (	"ID" NUMBER(*,0), 
	"NAME" VARCHAR2(100), 
	"MARKETSHARES" NUMBER(*,0), 
	"NETWORTH" FLOAT(63), 
	"ADDRESSID" NUMBER(*,0)
   ) ;
--------------------------------------------------------
--  DDL for Table STOCKINFORMATION_NEW
--------------------------------------------------------

  CREATE TABLE "SYSTEM"."STOCKINFORMATION_NEW"
   (	"ID" NUMBER(*,0), 
	"NUMBEROFSHARES" NUMBER(*,0), 
	"SHARETYPE" NUMBER(1,0), 
	"COMPANYID" NUMBER(*,0), 
	"DESCRIPTION" VARCHAR2(1000)
   ) ;
--------------------------------------------------------
--  DDL for Table STOCKBOOKLIST_NEW
--------------------------------------------------------

  CREATE TABLE "SYSTEM"."STOCKBOOKLIST_NEW" 
   (	"DATE" TIMESTAMP (6), 
	"STOCKID" NUMBER(*,0)
   ) ;
--------------------------------------------------------
--  DDL for Table BROKERWORKSFOR_NEW
--------------------------------------------------------

  CREATE TABLE "SYSTEM"."BROKERWORKSFOR_NEW" 
   (	"BROKERID" NUMBER(*,0), 
	"COMPANYID" NUMBER(*,0), 
	"CLIENTID" NUMBER(*,0)
   ) ;
--------------------------------------------------------
--  DDL for Table TRANSACTION_NEW
--------------------------------------------------------

  CREATE TABLE "SYSTEM"."TRANSACTION_NEW" 
   (	"ID" NUMBER(*,0), 
	"DATE" TIMESTAMP (6), 
	"STOCKID" NUMBER(*,0), 
	"TYPE" NUMBER(1,0), 
	"BROKERID" NUMBER(*,0), 
	"FORCLIENTID" NUMBER(*,0), 
	"FORCOMPANYID" NUMBER(*,0), 
	"AMOUNT" NUMBER(10,5)
   ) ;

--------------------------------------------------------
--  DDL for Index ADDRESS_NEW_PKEY
--------------------------------------------------------

  CREATE UNIQUE INDEX "SYSTEM"."ADDRESS_NEW_PKEY" ON "SYSTEM"."ADDRESS_NEW" ("ID") 
  ;
--------------------------------------------------------
--  DDL for Index PERSON_NEW_PKEY
--------------------------------------------------------

  CREATE UNIQUE INDEX "SYSTEM"."PERSON_NEW_PKEY" ON "SYSTEM"."PERSON_NEW" ("ID") 
  ;
--------------------------------------------------------
--  DDL for Index BROKER_NEW_PKEY
--------------------------------------------------------

  CREATE UNIQUE INDEX "SYSTEM"."BROKER_NEW_PKEY" ON "SYSTEM"."BROKER_NEW" ("ID") 
  ;
--------------------------------------------------------
--  DDL for Index CLIENT_NEW_PKEY
--------------------------------------------------------

  CREATE UNIQUE INDEX "SYSTEM"."CLIENT_NEW_PKEY" ON "SYSTEM"."CLIENT_NEW" ("ID") 
  ;
--------------------------------------------------------
--  DDL for Index COMPANY_NEW_PKEY
--------------------------------------------------------

  CREATE UNIQUE INDEX "SYSTEM"."COMPANY_NEW_PKEY" ON "SYSTEM"."COMPANY_NEW" ("ID") 
  ;
--------------------------------------------------------
--  DDL for Index STOCKINFORMATION_NEW_PKEY
--------------------------------------------------------

  CREATE UNIQUE INDEX "SYSTEM"."STOCKINFORMATION_NEW_PKEY" ON "SYSTEM"."STOCKINFORMATION_NEW" ("ID") 
  ;
--------------------------------------------------------
--  DDL for Index STOCKBOOKLIST_NEW_PKEY
--------------------------------------------------------

  CREATE UNIQUE INDEX "SYSTEM"."STOCKBOOKLIST_NEW_PKEY" ON "SYSTEM"."STOCKBOOKLIST_NEW" ("DATE") 
  ;
--------------------------------------------------------
--  DDL for Index TRANSACTION_NEW_PKEY
--------------------------------------------------------

  CREATE UNIQUE INDEX "SYSTEM"."TRANSACTION_NEW_PKEY" ON "SYSTEM"."TRANSACTION_NEW" ("ID") 
  ;
--------------------------------------------------------
--  Ref Constraints for Table PERSON_NEW
--------------------------------------------------------

  ALTER TABLE "SYSTEM"."PERSON_NEW" ADD CONSTRAINT "FK_ADDRESS_PKEY" FOREIGN KEY ("ADDRESSID")
	  REFERENCES "SYSTEM"."ADDRESS_NEW" ("ID") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table CLIENT_NEW
--------------------------------------------------------

  ALTER TABLE "SYSTEM"."CLIENT_NEW" ADD CONSTRAINT "FK_PERSON_CLIENT_PKEY" FOREIGN KEY ("ID")
	  REFERENCES "SYSTEM"."PERSON_NEW" ("ID") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table BROKER_NEW
--------------------------------------------------------

  ALTER TABLE "SYSTEM"."BROKER_NEW" ADD CONSTRAINT "FK_PERSON_BROKER_PKEY" FOREIGN KEY ("ID")
	  REFERENCES "SYSTEM"."PERSON_NEW" ("ID") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table COMPANY_NEW
--------------------------------------------------------

  ALTER TABLE "SYSTEM"."COMPANY_NEW" ADD CONSTRAINT "FK_ADDRESS_COMPANY_PKEY" FOREIGN KEY ("ADDRESSID")
	  REFERENCES "SYSTEM"."ADDRESS_NEW" ("ID") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table STOCKINFORMATION_NEW
--------------------------------------------------------

  ALTER TABLE "SYSTEM"."STOCKINFORMATION_NEW" ADD CONSTRAINT "FK_COMPANY_PKEY" FOREIGN KEY ("COMPANYID")
	  REFERENCES "SYSTEM"."COMPANY_NEW" ("ID") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table STOCKBOOKLIST_NEW
--------------------------------------------------------

  ALTER TABLE "SYSTEM"."STOCKBOOKLIST_NEW" ADD CONSTRAINT "FK_STOCKID_PKEY" FOREIGN KEY ("STOCKID")
	  REFERENCES "SYSTEM"."STOCKINFORMATION_NEW" ("ID") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table BROKERWORKSFOR_NEW
--------------------------------------------------------

  ALTER TABLE "SYSTEM"."BROKERWORKSFOR_NEW" ADD CONSTRAINT "FK_BROKER_PKEY" FOREIGN KEY ("BROKERID")
	  REFERENCES "SYSTEM"."BROKER_NEW" ("ID") ENABLE;
  ALTER TABLE "SYSTEM"."BROKERWORKSFOR_NEW" ADD CONSTRAINT "FK_BWORKSFOR_CLIENTID_PKEY" FOREIGN KEY ("CLIENTID")
	  REFERENCES "SYSTEM"."CLIENT_NEW" ("ID") ENABLE;
  ALTER TABLE "SYSTEM"."BROKERWORKSFOR_NEW" ADD CONSTRAINT "FK_BWORKSFOR_COMPANYID_PKEY" FOREIGN KEY ("COMPANYID")
	  REFERENCES "SYSTEM"."COMPANY_NEW" ("ID") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table TRANSACTION_NEW
--------------------------------------------------------

  ALTER TABLE "SYSTEM"."TRANSACTION_NEW" ADD CONSTRAINT "FK_BROKER_TRANSACTION_PKEY" FOREIGN KEY ("BROKERID")
	  REFERENCES "SYSTEM"."BROKER_NEW" ("ID") ENABLE;
  ALTER TABLE "SYSTEM"."TRANSACTION_NEW" ADD CONSTRAINT "FK_FORCLIENTID_PKEY" FOREIGN KEY ("FORCLIENTID")
	  REFERENCES "SYSTEM"."CLIENT_NEW" ("ID") ENABLE;
  ALTER TABLE "SYSTEM"."TRANSACTION_NEW" ADD CONSTRAINT "FK_FORCOMPANYID_PKEY" FOREIGN KEY ("FORCOMPANYID")
	  REFERENCES "SYSTEM"."COMPANY_NEW" ("ID") ENABLE;
  ALTER TABLE "SYSTEM"."TRANSACTION_NEW" ADD CONSTRAINT "FK_STOCKINFORMATION_PKEY" FOREIGN KEY ("STOCKID")
	  REFERENCES "SYSTEM"."STOCKINFORMATION_NEW" ("ID") ENABLE;

REM INSERTING into SYSTEM.ADDRESS_NEW
SET DEFINE OFF;
Insert into SYSTEM.ADDRESS_NEW (ID,STREET,"NUMBER",CITY,STATE,COUNTRY) values ('991','Road street','24','Chonala','Veracruz','Mexico');
Insert into SYSTEM.ADDRESS_NEW (ID,STREET,"NUMBER",CITY,STATE,COUNTRY) values ('992','Via Marconi','3','Bolzano','Bolzano','Italy');
Insert into SYSTEM.ADDRESS_NEW (ID,STREET,"NUMBER",CITY,STATE,COUNTRY) values ('995','Huberg Strasse','3','Bolzano','Bolzano','Italy');
Insert into SYSTEM.ADDRESS_NEW (ID,STREET,"NUMBER",CITY,STATE,COUNTRY) values ('996','Via Piani di Bolzano','7','Marconi','Trentino','Italy');
Insert into SYSTEM.ADDRESS_NEW (ID,STREET,"NUMBER",CITY,STATE,COUNTRY) values ('993','Romer Street','32','Malaga','Malaga','Spain');
Insert into SYSTEM.ADDRESS_NEW (ID,STREET,"NUMBER",CITY,STATE,COUNTRY) values ('997','Samara road','9976','Puebla','Puebla','Mexico');
Insert into SYSTEM.ADDRESS_NEW (ID,STREET,"NUMBER",CITY,STATE,COUNTRY) values ('998','Jalan Madura 12','245','Jakarta','Jakarta','Indonesia');
REM INSERTING into SYSTEM.PERSON_NEW
SET DEFINE OFF;
Insert into SYSTEM.PERSON_NEW (ID,NAME,LASTNAME,DATEOFBIRTH,SSN,ADDRESSID) values ('111','John','Smith',to_date('21-MAR-50','DD-MON-RR'),'JSRX229500321','991');
Insert into SYSTEM.PERSON_NEW (ID,NAME,LASTNAME,DATEOFBIRTH,SSN,ADDRESSID) values ('112','Joana','Lopatenkko',to_date('14-JUL-70','DD-MON-RR'),'JLPTK54992','992');
Insert into SYSTEM.PERSON_NEW (ID,NAME,LASTNAME,DATEOFBIRTH,SSN,ADDRESSID) values ('113','Walter','Schmidt',to_date('03-SEP-68','DD-MON-RR'),'WSCH9820783903','993');
Insert into SYSTEM.PERSON_NEW (ID,NAME,LASTNAME,DATEOFBIRTH,SSN,ADDRESSID) values ('114','Patricia','Lombrardi',to_date('22-FEB-75','DD-MON-RR'),'PTLM8878767830','997');
REM INSERTING into SYSTEM.BROKER_NEW
SET DEFINE OFF;
Insert into SYSTEM.BROKER_NEW (ID) values ('112');
Insert into SYSTEM.BROKER_NEW (ID) values ('113');
Insert into SYSTEM.BROKER_NEW (ID) values ('114');
REM INSERTING into SYSTEM.CLIENT_NEW
SET DEFINE OFF;
Insert into SYSTEM.CLIENT_NEW (ID) values ('111');
Insert into SYSTEM.CLIENT_NEW (ID) values ('112');
REM INSERTING into SYSTEM.COMPANY_NEW
SET DEFINE OFF;
Insert into SYSTEM.COMPANY_NEW (ID,NAME,MARKETSHARES,NETWORTH,ADDRESSID) values ('211','General Motors','25000000','1234,5678','995');
Insert into SYSTEM.COMPANY_NEW (ID,NAME,MARKETSHARES,NETWORTH,ADDRESSID) values ('212','GnA Investments','100000','1234,5678','996');
REM INSERTING into SYSTEM.STOCKINFORMATION_NEW
SET DEFINE OFF;
Insert into SYSTEM.STOCKINFORMATION_NEW (ID,NUMBEROFSHARES,SHARETYPE,COMPANYID,DESCRIPTION) values ('661','100','0','211','Text description 1');
Insert into SYSTEM.STOCKINFORMATION_NEW (ID,NUMBEROFSHARES,SHARETYPE,COMPANYID,DESCRIPTION) values ('660','100','0','211','Text description 2');
Insert into SYSTEM.STOCKINFORMATION_NEW (ID,NUMBEROFSHARES,SHARETYPE,COMPANYID,DESCRIPTION) values ('662','100','0','211','Text description 3');
Insert into SYSTEM.STOCKINFORMATION_NEW (ID,NUMBEROFSHARES,SHARETYPE,COMPANYID,DESCRIPTION) values ('663','100','0','211','Text description 4');
Insert into SYSTEM.STOCKINFORMATION_NEW (ID,NUMBEROFSHARES,SHARETYPE,COMPANYID,DESCRIPTION) values ('664','100','0','211','Text description 5');
Insert into SYSTEM.STOCKINFORMATION_NEW (ID,NUMBEROFSHARES,SHARETYPE,COMPANYID,DESCRIPTION) values ('665','100','1','211','Testo di descrizione 1');
Insert into SYSTEM.STOCKINFORMATION_NEW (ID,NUMBEROFSHARES,SHARETYPE,COMPANYID,DESCRIPTION) values ('666','100','1','211','Testo di descrizione 2');
Insert into SYSTEM.STOCKINFORMATION_NEW (ID,NUMBEROFSHARES,SHARETYPE,COMPANYID,DESCRIPTION) values ('667','100','1','211','Testo di descrizione 3');
Insert into SYSTEM.STOCKINFORMATION_NEW (ID,NUMBEROFSHARES,SHARETYPE,COMPANYID,DESCRIPTION) values ('669','100','1','211','Testo di descrizione 4');
Insert into SYSTEM.STOCKINFORMATION_NEW (ID,NUMBEROFSHARES,SHARETYPE,COMPANYID,DESCRIPTION) values ('668','100','1','211','Testo di descrizione 5');
REM INSERTING into SYSTEM.STOCKBOOKLIST_NEW
SET DEFINE OFF;
Insert into SYSTEM.STOCKBOOKLIST_NEW ("DATE",STOCKID) values (to_timestamp('01-APR-08 00:00:00,000000000','DD-MON-RR HH24:MI:SSXFF'),'661');
Insert into SYSTEM.STOCKBOOKLIST_NEW ("DATE",STOCKID) values (to_timestamp('02-APR-08 00:00:00,000000000','DD-MON-RR HH24:MI:SSXFF'),'662');
Insert into SYSTEM.STOCKBOOKLIST_NEW ("DATE",STOCKID) values (to_timestamp('03-APR-08 00:00:00,000000000','DD-MON-RR HH24:MI:SSXFF'),'663');
Insert into SYSTEM.STOCKBOOKLIST_NEW ("DATE",STOCKID) values (to_timestamp('04-APR-08 00:00:00,000000000','DD-MON-RR HH24:MI:SSXFF'),'664');
Insert into SYSTEM.STOCKBOOKLIST_NEW ("DATE",STOCKID) values (to_timestamp('05-APR-08 00:00:00,000000000','DD-MON-RR HH24:MI:SSXFF'),'665');
Insert into SYSTEM.STOCKBOOKLIST_NEW ("DATE",STOCKID) values (to_timestamp('06-APR-08 00:00:00,000000000','DD-MON-RR HH24:MI:SSXFF'),'666');
Insert into SYSTEM.STOCKBOOKLIST_NEW ("DATE",STOCKID) values (to_timestamp('07-APR-08 00:00:00,000000000','DD-MON-RR HH24:MI:SSXFF'),'667');
Insert into SYSTEM.STOCKBOOKLIST_NEW ("DATE",STOCKID) values (to_timestamp('08-APR-08 00:00:00,000000000','DD-MON-RR HH24:MI:SSXFF'),'668');
Insert into SYSTEM.STOCKBOOKLIST_NEW ("DATE",STOCKID) values (to_timestamp('09-APR-08 00:00:00,000000000','DD-MON-RR HH24:MI:SSXFF'),'669');
REM INSERTING into SYSTEM.BROKERWORKSFOR_NEW
SET DEFINE OFF;
Insert into SYSTEM.BROKERWORKSFOR_NEW (BROKERID,COMPANYID,CLIENTID) values ('112',null,'111');
Insert into SYSTEM.BROKERWORKSFOR_NEW (BROKERID,COMPANYID,CLIENTID) values ('112',null,'112');
Insert into SYSTEM.BROKERWORKSFOR_NEW (BROKERID,COMPANYID,CLIENTID) values ('113','212',null);
Insert into SYSTEM.BROKERWORKSFOR_NEW (BROKERID,COMPANYID,CLIENTID) values ('113','211',null);
Insert into SYSTEM.BROKERWORKSFOR_NEW (BROKERID,COMPANYID,CLIENTID) values ('114','212',null);
Insert into SYSTEM.BROKERWORKSFOR_NEW (BROKERID,COMPANYID,CLIENTID) values ('114',null,'111');
Insert into SYSTEM.BROKERWORKSFOR_NEW (BROKERID,COMPANYID,CLIENTID) values ('112',null,'111');
Insert into SYSTEM.BROKERWORKSFOR_NEW (BROKERID,COMPANYID,CLIENTID) values ('112',null,'112');
Insert into SYSTEM.BROKERWORKSFOR_NEW (BROKERID,COMPANYID,CLIENTID) values ('113','212',null);
Insert into SYSTEM.BROKERWORKSFOR_NEW (BROKERID,COMPANYID,CLIENTID) values ('113','211',null);
Insert into SYSTEM.BROKERWORKSFOR_NEW (BROKERID,COMPANYID,CLIENTID) values ('114','212',null);
Insert into SYSTEM.BROKERWORKSFOR_NEW (BROKERID,COMPANYID,CLIENTID) values ('114',null,'111');
REM INSERTING into SYSTEM.TRANSACTION_NEW
SET DEFINE OFF;
Insert into SYSTEM.TRANSACTION_NEW (ID,"DATE",STOCKID,TYPE,BROKERID,FORCLIENTID,FORCOMPANYID,AMOUNT) values ('3331',to_timestamp('01-APR-08 00:00:00,000000000','DD-MON-RR HH24:MI:SSXFF'),'661','1','112','111',null,'12,6');
Insert into SYSTEM.TRANSACTION_NEW (ID,"DATE",STOCKID,TYPE,BROKERID,FORCLIENTID,FORCOMPANYID,AMOUNT) values ('3332',to_timestamp('02-APR-08 00:00:00,000000000','DD-MON-RR HH24:MI:SSXFF'),'662','1','112','111',null,'108,34');
Insert into SYSTEM.TRANSACTION_NEW (ID,"DATE",STOCKID,TYPE,BROKERID,FORCLIENTID,FORCOMPANYID,AMOUNT) values ('3333',to_timestamp('03-APR-08 00:00:00,000000000','DD-MON-RR HH24:MI:SSXFF'),'663','1','112',null,'212','-2,349');
Insert into SYSTEM.TRANSACTION_NEW (ID,"DATE",STOCKID,TYPE,BROKERID,FORCLIENTID,FORCOMPANYID,AMOUNT) values ('3334',to_timestamp('14-APR-08 00:00:00,000000000','DD-MON-RR HH24:MI:SSXFF'),'663','1','113',null,null,'1667,0092');
