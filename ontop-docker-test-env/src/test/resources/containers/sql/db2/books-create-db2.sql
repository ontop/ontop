--------------------------------------------------------
--  File created - Thursday-February-23-2017
--------------------------------------------------------
--------------------------------------------------------
--  DDL for Table BOOKS
--------------------------------------------------------
CREATE DATABASE SBOOKS;

CONNECT TO SBOOKS;

  CREATE TABLE "BOOKS"
   (	"ID" INTEGER NOT NULL,
	"TITLE" VARCHAR(100) WITH DEFAULT NULL ,
	"PRICE" DECIMAL(8,2) WITH DEFAULT NULL ,
	"DISCOUNT" DECIMAL(8,2) WITH DEFAULT NULL ,
	"DESCRIPTION" VARCHAR(100) WITH DEFAULT NULL ,
	"LANG" VARCHAR(100) WITH DEFAULT NULL ,
	"PUBLICATION_DATE" TIMESTAMP
   ) ;


Insert into BOOKS (ID,TITLE,PRICE,DISCOUNT,DESCRIPTION,LANG,PUBLICATION_DATE) values ('1','SPARQL Tutorial','42.5','0.2','good','en',to_timestamp('05-JUN-14 18:47:52','DD-MON-RR HH24:MI:SS'));
Insert into BOOKS (ID,TITLE,PRICE,DISCOUNT,DESCRIPTION,LANG,PUBLICATION_DATE) values ('2','The Semantic Web','23','0.25','bad','en',to_timestamp('08-DEC-11 12:30:00','DD-MON-RR HH24:MI:SS'));
Insert into BOOKS (ID,TITLE,PRICE,DISCOUNT,DESCRIPTION,LANG,PUBLICATION_DATE) values ('3','Crime and Punishment','33.5','0.2','good','en',to_timestamp('21-SEP-15 09:23:06','DD-MON-RR HH24:MI:SS'));
Insert into BOOKS (ID,TITLE,PRICE,DISCOUNT,DESCRIPTION,LANG,PUBLICATION_DATE) values ('4','The Logic Book: Introduction, Second Edition','10','0.15','good','en',to_timestamp('05-NOV-70 07:50:00','DD-MON-RR HH24:MI:SS'));

--------------------------------------------------------
--  Constraints for Table BOOKS
--------------------------------------------------------
  ALTER TABLE "BOOKS"
	ADD PRIMARY KEY
		("ID");


COMMIT WORK;

CONNECT RESET;

TERMINATE;