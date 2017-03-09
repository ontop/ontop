--------------------------------------------------------
--  File created - Tuesday-February-21-2017   
--------------------------------------------------------

--------------------------------------------------------
--  DDL for Table T_ASSAY
--------------------------------------------------------

  CREATE TABLE "SYSTEM"."T_ASSAY" 
   (	"ID" NUMBER(*,0), 
	"SHORT_NAME" VARCHAR2(200), 
	"DESCRIPTION" VARCHAR2(4000), 
	"DEPARTMENT_CODE" VARCHAR2(20), 
	"PTT_REF" VARCHAR2(15)
   ) ;

REM INSERTING into SYSTEM.T_ASSAY
SET DEFINE OFF;
Insert into SYSTEM.T_ASSAY (ID,SHORT_NAME,DESCRIPTION,DEPARTMENT_CODE,PTT_REF) values ('11111111111','sadassdasdasda',' 081036 c-SRC assay (Bone): %change of primary screen @ 10uM or 0','sadasdasdasd','asdsadasd');
Insert into SYSTEM.T_ASSAY (ID,SHORT_NAME,DESCRIPTION,DEPARTMENT_CODE,PTT_REF) values ('111111112221','sadassdasdasda','asdasdasdasdasd','sadasdasdasd','asdsadasd');
Insert into SYSTEM.T_ASSAY (ID,SHORT_NAME,DESCRIPTION,DEPARTMENT_CODE,PTT_REF) values ('222222','dsdsdsd','martinrezk@gmail.com','ffff','ffff');

--------------------------------------------------------
--  Constraints for Table T_ASSAY
--------------------------------------------------------

  ALTER TABLE "SYSTEM"."T_ASSAY" MODIFY ("SHORT_NAME" NOT NULL ENABLE);
  ALTER TABLE "SYSTEM"."T_ASSAY" MODIFY ("ID" NOT NULL ENABLE);

