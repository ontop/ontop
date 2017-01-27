-- This CLP file was created using DB2LOOK Version "9.7" 
-- Timestamp: Fri 20 Jan 2017 04:38:58 PM CET
-- Database Name: STOCKNEW
-- Database Manager Version: DB2/LINUXX8664 Version 9.7.5  
-- Database Codepage: 1208
-- Database Collating Sequence is: IDENTITY
CREATE DATABASE STOCKNEW;

CONNECT TO STOCKNEW;

------------------------------------------------
-- DDL Statements for table "address"
------------------------------------------------
 

CREATE TABLE "address"  (
		  "id" INTEGER NOT NULL , 
		  "street" VARCHAR(100) WITH DEFAULT NULL , 
		  "number" INTEGER WITH DEFAULT NULL , 
		  "city" VARCHAR(100) WITH DEFAULT NULL , 
		  "state" VARCHAR(100) WITH DEFAULT NULL , 
		  "country" VARCHAR(100) WITH DEFAULT NULL )   
		  ;


-- DDL Statements for primary key on Table "address"

ALTER TABLE "address"
	ADD PRIMARY KEY
		("id");


------------------------------------------------
-- DDL Statements for table "broker"
------------------------------------------------


CREATE TABLE "broker"  (
		  "id" INTEGER NOT NULL )
		 ;


-- DDL Statements for primary key on Table "broker"

ALTER TABLE "broker"
	ADD PRIMARY KEY
		("id");


------------------------------------------------
-- DDL Statements for table "brokerworksfor"
------------------------------------------------


CREATE TABLE "brokerworksfor"  (
		  "brokerid" INTEGER NOT NULL ,
		  "companyid" INTEGER WITH DEFAULT NULL ,
		  "clientid" INTEGER WITH DEFAULT NULL )
		 ;


------------------------------------------------
-- DDL Statements for table "client"
------------------------------------------------


CREATE TABLE "client"  (
		  "id" INTEGER NOT NULL )
		 ;


-- DDL Statements for primary key on Table "client"

ALTER TABLE "client"
	ADD PRIMARY KEY
		("id");


------------------------------------------------
-- DDL Statements for table "person"
------------------------------------------------

CREATE TABLE "person"  (
		  "id" INTEGER NOT NULL ,
      "name" VARCHAR(100) WITH DEFAULT NULL ,
      "lastname" VARCHAR(100) WITH DEFAULT NULL ,
      "dateofbirth" DATE WITH DEFAULT NULL ,
      "ssn" VARCHAR(100) WITH DEFAULT NULL ,
      "addressid" INTEGER WITH DEFAULT NULL )
                 ;

ALTER TABLE "person"
	ADD PRIMARY KEY
		("id");


------------------------------------------------
-- DDL Statements for table "company"
------------------------------------------------


CREATE TABLE "company"  (
		  "id" INTEGER NOT NULL ,
		  "name" VARCHAR(100) WITH DEFAULT NULL ,
		  "marketshares" INTEGER WITH DEFAULT NULL ,
		  "networth" DOUBLE WITH DEFAULT NULL ,
		  "addressid" INTEGER WITH DEFAULT NULL )
		 ;


-- DDL Statements for primary key on Table "company"

ALTER TABLE "company"
	ADD PRIMARY KEY
		("id");


------------------------------------------------
-- DDL Statements for table "stockbooklist"
------------------------------------------------


CREATE TABLE "stockbooklist"  (
		  "date" DATE NOT NULL ,
		  "stockid" INTEGER WITH DEFAULT NULL )
		 ;


-- DDL Statements for primary key on Table "stockbooklist"

ALTER TABLE "stockbooklist"
	ADD PRIMARY KEY
		("date");


------------------------------------------------
-- DDL Statements for table "transaction"
------------------------------------------------


CREATE TABLE "transaction"  (
		  "id" INTEGER NOT NULL ,
		  "date" TIMESTAMP NOT NULL WITH DEFAULT CURRENT TIMESTAMP ,
		  "stockid" INTEGER WITH DEFAULT NULL ,
		  "type" SMALLINT WITH DEFAULT NULL ,
		  "brokerid" INTEGER WITH DEFAULT NULL ,
		  "forclientid" INTEGER WITH DEFAULT NULL ,
		  "forcompanyid" INTEGER WITH DEFAULT NULL ,
		  "amount" DECIMAL(10,4) WITH DEFAULT NULL )
		 ;


-- DDL Statements for primary key on Table "transaction"

ALTER TABLE "transaction"
	ADD PRIMARY KEY
		("id");


------------------------------------------------
-- DDL Statements for table "stockinformation"
------------------------------------------------


CREATE TABLE "stockinformation"  (
		  "id" INTEGER NOT NULL ,
		  "numberofshares" INTEGER WITH DEFAULT NULL ,
		  "sharetype" SMALLINT WITH DEFAULT NULL ,
		  "companyid" INTEGER WITH DEFAULT NULL ,
		  "description" VARCHAR(255) )
		 ;


-- DDL Statements for primary key on Table "stockinformation"

ALTER TABLE "stockinformation" 
	ADD PRIMARY KEY
		("id");


  ALTER TABLE "person" ADD CONSTRAINT "FK_ADDRESS_PKEY" FOREIGN KEY ("addressid")
	  REFERENCES "address" ("id") ;
--------------------------------------------------------
--  Ref Constraints for Table "client"
--------------------------------------------------------

  ALTER TABLE "client" ADD CONSTRAINT "FK_PERSON_CLIENT_PKEY" FOREIGN KEY ("id")
	  REFERENCES "person" ("id") ;
--------------------------------------------------------
--  Ref Constraints for Table "broker"
--------------------------------------------------------

  ALTER TABLE "broker" ADD CONSTRAINT "FK_PERSON_BROKER_PKEY" FOREIGN KEY ("id")
	  REFERENCES "person" ("id") ;
--------------------------------------------------------
--  Ref Constraints for Table "company"
--------------------------------------------------------

  ALTER TABLE "company" ADD CONSTRAINT "FK_ADDRESS_COMPANY_PKEY" FOREIGN KEY ("addressid")
	  REFERENCES "address" ("id") ;
--------------------------------------------------------
--  Ref Constraints for Table "stockinformation"
--------------------------------------------------------

  ALTER TABLE "stockinformation" ADD CONSTRAINT "FK_COMPANY_PKEY" FOREIGN KEY ("companyid")
	  REFERENCES "company" ("id") ;
--------------------------------------------------------
--  Ref Constraints for Table "stockbooklist"
--------------------------------------------------------

  ALTER TABLE "stockbooklist" ADD CONSTRAINT "FK_STOCKID_PKEY" FOREIGN KEY ("stockid")
	  REFERENCES "stockinformation" ("id") ;
--------------------------------------------------------
--  Ref Constraints for Table "brokerworksfor"
--------------------------------------------------------

  ALTER TABLE "brokerworksfor" ADD CONSTRAINT "FK_BROKER_PKEY" FOREIGN KEY ("brokerid")
	  REFERENCES "broker" ("id") ;
  ALTER TABLE "brokerworksfor" ADD CONSTRAINT "FK_BWORKSFOR_CLIENTID_PKEY" FOREIGN KEY ("clientid")
	  REFERENCES "client" ("id") ;
  ALTER TABLE "brokerworksfor" ADD CONSTRAINT "FK_BWORKSFOR_COMPANYID_PKEY" FOREIGN KEY ("companyid")
	  REFERENCES "company" ("id") ;
--------------------------------------------------------
--  Ref Constraints for Table "transaction"
--------------------------------------------------------

  ALTER TABLE "transaction" ADD CONSTRAINT "FK_BROKER_TRANSACTION_PKEY" FOREIGN KEY ("brokerid")
	  REFERENCES "broker" ("id") ;
  ALTER TABLE "transaction" ADD CONSTRAINT "FK_FORCLIENTID_PKEY" FOREIGN KEY ("forclientid")
	  REFERENCES "client" ("id") ;
  ALTER TABLE "transaction" ADD CONSTRAINT "FK_FORCOMPANYID_PKEY" FOREIGN KEY ("forcompanyid")
	  REFERENCES "company" ("id") ;
  ALTER TABLE "transaction" ADD CONSTRAINT "FK_STOCKINFORMATION_PKEY" FOREIGN KEY ("stockid")
	  REFERENCES "stockinformation" ("id") ;



Insert into "address" ("id","street","number","city","state","country") values ('991','Road street','24','Chonala','Veracruz','Mexico');
Insert into "address" ("id","street","number","city","state","country") values ('992','Via Marconi','3','Bolzano','Bolzano','Italy');
Insert into "address" ("id","street","number","city","state","country") values ('995','Huberg Strasse','3','Bolzano','Bolzano','Italy');
Insert into "address" ("id","street","number","city","state","country") values ('996','Via Piani di Bolzano','7','Marconi','Trentino','Italy');
Insert into "address" ("id","street","number","city","state","country") values ('993','Romer Street','32','Malaga','Malaga','Spain');
Insert into "address" ("id","street","number","city","state","country") values ('997','Samara road','9976','Puebla','Puebla','Mexico');
Insert into "address" ("id","street","number","city","state","country") values ('998','Jalan Madura 12','245','Jakarta','Jakarta','Indonesia');

Insert into "person" ("id","name","lastname","dateofbirth","ssn","addressid") values ('111','John','Smith',to_date('21-MAR-50','DD-MON-RR'),'JSRX229500321','991');
Insert into "person" ("id","name","lastname","dateofbirth","ssn","addressid") values ('112','Joana','Lopatenkko',to_date('14-JUL-70','DD-MON-RR'),'JLPTK54992','992');
Insert into "person" ("id","name","lastname","dateofbirth","ssn","addressid") values ('113','Walter','Schmidt',to_date('03-SEP-68','DD-MON-RR'),'WSCH9820783903','993');
Insert into "person" ("id","name","lastname","dateofbirth","ssn","addressid") values ('114','Patricia','Lombrardi',to_date('22-FEB-75','DD-MON-RR'),'PTLM8878767830','997');

Insert into "broker" ("id") values ('112');
Insert into "broker" ("id") values ('113');
Insert into "broker" ("id") values ('114');

Insert into "client" ("id") values ('111');
Insert into "client" ("id") values ('112');

Insert into "company" ("id","name","marketshares","networth","addressid") values ('211','General Motors','25000000','1234.5678','995');
Insert into "company" ("id","name","marketshares","networth","addressid") values ('212','GnA Investments','100000','1234.5678','996');

Insert into "stockinformation" ("id","numberofshares","sharetype","companyid","description") values ('661','100','0','211','Text description 1');
Insert into "stockinformation" ("id","numberofshares","sharetype","companyid","description") values ('660','100','0','211','Text description 2');
Insert into "stockinformation" ("id","numberofshares","sharetype","companyid","description") values ('662','100','0','211','Text description 3');
Insert into "stockinformation" ("id","numberofshares","sharetype","companyid","description") values ('663','100','0','211','Text description 4');
Insert into "stockinformation" ("id","numberofshares","sharetype","companyid","description") values ('664','100','0','211','Text description 5');
Insert into "stockinformation" ("id","numberofshares","sharetype","companyid","description") values ('665','100','1','211','Testo di descrizione 1');
Insert into "stockinformation" ("id","numberofshares","sharetype","companyid","description") values ('666','100','1','211','Testo di descrizione 2');
Insert into "stockinformation" ("id","numberofshares","sharetype","companyid","description") values ('667','100','1','211','Testo di descrizione 3');
Insert into "stockinformation" ("id","numberofshares","sharetype","companyid","description") values ('669','100','1','211','Testo di descrizione 4');
Insert into "stockinformation" ("id","numberofshares","sharetype","companyid","description") values ('668','100','1','211','Testo di descrizione 5');

Insert into "stockbooklist" ("date","stockid") values (to_timestamp('01-APR-08 00:00:00','DD-MON-RR HH24:MI:SS'),'661');
Insert into "stockbooklist" ("date","stockid") values (to_timestamp('02-APR-08 00:00:00','DD-MON-RR HH24:MI:SS'),'662');
Insert into "stockbooklist" ("date","stockid") values (to_timestamp('03-APR-08 00:00:00','DD-MON-RR HH24:MI:SS'),'663');
Insert into "stockbooklist" ("date","stockid") values (to_timestamp('04-APR-08 00:00:00','DD-MON-RR HH24:MI:SS'),'664');
Insert into "stockbooklist" ("date","stockid") values (to_timestamp('05-APR-08 00:00:00','DD-MON-RR HH24:MI:SS'),'665');
Insert into "stockbooklist" ("date","stockid") values (to_timestamp('06-APR-08 00:00:00','DD-MON-RR HH24:MI:SS'),'666');
Insert into "stockbooklist" ("date","stockid") values (to_timestamp('07-APR-08 00:00:00','DD-MON-RR HH24:MI:SS'),'667');
Insert into "stockbooklist" ("date","stockid") values (to_timestamp('08-APR-08 00:00:00','DD-MON-RR HH24:MI:SS'),'668');
Insert into "stockbooklist" ("date","stockid") values (to_timestamp('09-APR-08 00:00:00','DD-MON-RR HH24:MI:SS'),'669');

Insert into "brokerworksfor" ("brokerid","companyid","clientid") values ('112',null,'111');
Insert into "brokerworksfor" ("brokerid","companyid","clientid") values ('112',null,'112');
Insert into "brokerworksfor" ("brokerid","companyid","clientid") values ('113','212',null);
Insert into "brokerworksfor" ("brokerid","companyid","clientid") values ('113','211',null);
Insert into "brokerworksfor" ("brokerid","companyid","clientid") values ('114','212',null);
Insert into "brokerworksfor" ("brokerid","companyid","clientid") values ('114',null,'111');
Insert into "brokerworksfor" ("brokerid","companyid","clientid") values ('112',null,'111');
Insert into "brokerworksfor" ("brokerid","companyid","clientid") values ('112',null,'112');
Insert into "brokerworksfor" ("brokerid","companyid","clientid") values ('113','212',null);
Insert into "brokerworksfor" ("brokerid","companyid","clientid") values ('113','211',null);
Insert into "brokerworksfor" ("brokerid","companyid","clientid") values ('114','212',null);
Insert into "brokerworksfor" ("brokerid","companyid","clientid") values ('114',null,'111');

Insert into "transaction" ("id","date","stockid","type","brokerid","forclientid","forcompanyid","amount") values ('3331',to_timestamp('01-APR-08 00:00:00','DD-MON-RR HH24:MI:SS'),'661','1','112','111',null,'12.6');
Insert into "transaction" ("id","date","stockid","type","brokerid","forclientid","forcompanyid","amount") values ('3332',to_timestamp('02-APR-08 00:00:00','DD-MON-RR HH24:MI:SS'),'662','1','112','111',null,'108.34');
Insert into "transaction" ("id","date","stockid","type","brokerid","forclientid","forcompanyid","amount") values ('3333',to_timestamp('03-APR-08 00:00:00','DD-MON-RR HH24:MI:SS'),'663','1','112',null,'212','-2.349');
Insert into "transaction" ("id","date","stockid","type","brokerid","forclientid","forcompanyid","amount") values ('3334',to_timestamp('14-APR-08 00:00:00','DD-MON-RR HH24:MI:SS'),'663','1','113',null,null,'1667.0092');


COMMIT WORK;

CONNECT RESET;

TERMINATE;

