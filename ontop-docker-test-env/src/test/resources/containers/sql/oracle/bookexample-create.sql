--------------------------------------------------------
--  File created - Monday-February-20-2017
--------------------------------------------------------
--------------------------------------------------------
--  DDL for Table TB_AFFILIATED_WRITERS
--------------------------------------------------------

  CREATE TABLE "SYSTEM"."TB_AFFILIATED_WRITERS"
   (	"WR_CODE" NUMBER(*,0),
	"WR_NAME" VARCHAR2(100)
   );
--------------------------------------------------------
--  DDL for Table TB_AUTHORS
--------------------------------------------------------

  CREATE TABLE "SYSTEM"."TB_AUTHORS"
   (	"BK_CODE" NUMBER(*,0),
	"WR_ID" NUMBER(*,0)
   );
--------------------------------------------------------
--  DDL for Table TB_BK_GEN
--------------------------------------------------------

  CREATE TABLE "SYSTEM"."TB_BK_GEN"
   (	"ID_BK" NUMBER(*,0),
	"GEN_NAME" VARCHAR2(100)
   );
--------------------------------------------------------
--  DDL for Table TB_BOOKS
--------------------------------------------------------

  CREATE TABLE "SYSTEM"."TB_BOOKS"
   (	"BK_CODE" NUMBER(*,0),
	"BK_TITLE" VARCHAR2(100),
	"BK_TYPE" CHAR(1) DEFAULT 'X'
   );
--------------------------------------------------------
--  DDL for Table TB_EDITION
--------------------------------------------------------

  CREATE TABLE "SYSTEM"."TB_EDITION"
   (	"ED_CODE" NUMBER(*,0),
	"ED_TYPE" CHAR(1),
	"PUB_DATE" DATE,
	"N_EDT" NUMBER(*,0),
	"EDITOR" NUMBER(*,0),
	"BK_ID" NUMBER(*,0)
   );
--------------------------------------------------------
--  DDL for Table TB_EDITOR
--------------------------------------------------------

  CREATE TABLE "SYSTEM"."TB_EDITOR"
   (	"ED_CODE" NUMBER(*,0),
	"ED_NAME" VARCHAR2(100)
   );
--------------------------------------------------------
--  DDL for Table TB_EMERGE_AUTHORS
--------------------------------------------------------

  CREATE TABLE "SYSTEM"."TB_EMERGE_AUTHORS"
   (	"BK_CODE" NUMBER(*,0),
	"WR_ID" NUMBER(*,0)
   );
--------------------------------------------------------
--  DDL for Table TB_ON_PROB_WR
--------------------------------------------------------

  CREATE TABLE "SYSTEM"."TB_ON_PROB_WR"
   (	"WR_CODE" NUMBER(*,0),
	"WR_NAME" VARCHAR2(100)
   );
--------------------------------------------------------
--  DDL for Index AFF_WR_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "SYSTEM"."AFF_WR_PK" ON "SYSTEM"."TB_AFFILIATED_WRITERS" ("WR_CODE");
--------------------------------------------------------
--  DDL for Index PK_AU
--------------------------------------------------------

  CREATE UNIQUE INDEX "SYSTEM"."PK_AU" ON "SYSTEM"."TB_AUTHORS" ("BK_CODE", "WR_ID");
--------------------------------------------------------
--  DDL for Index PK_GEN
--------------------------------------------------------

  CREATE UNIQUE INDEX "SYSTEM"."PK_GEN" ON "SYSTEM"."TB_BK_GEN" ("ID_BK", "GEN_NAME");
--------------------------------------------------------
--  DDL for Index BK_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "SYSTEM"."BK_PK" ON "SYSTEM"."TB_BOOKS" ("BK_CODE");
--------------------------------------------------------
--  DDL for Index ED_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "SYSTEM"."ED_PK" ON "SYSTEM"."TB_EDITOR" ("ED_CODE");
--------------------------------------------------------
--  DDL for Index PK_EMERGE_AUTHORS
--------------------------------------------------------

  CREATE UNIQUE INDEX "SYSTEM"."PK_EMERGE_AUTHORS" ON "SYSTEM"."TB_EMERGE_AUTHORS" ("BK_CODE", "WR_ID");
--------------------------------------------------------
--  DDL for Index PR_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "SYSTEM"."PR_PK" ON "SYSTEM"."TB_ON_PROB_WR" ("WR_CODE");

REM INSERTING into SYSTEM.TB_AFFILIATED_WRITERS
SET DEFINE OFF;
Insert into SYSTEM.TB_AFFILIATED_WRITERS (WR_CODE,WR_NAME) values ('23','AJ Scudiere');
Insert into SYSTEM.TB_AFFILIATED_WRITERS (WR_CODE,WR_NAME) values ('25','Anne Rainey');
Insert into SYSTEM.TB_AFFILIATED_WRITERS (WR_CODE,WR_NAME) values ('27','Barbara Delinsky');
Insert into SYSTEM.TB_AFFILIATED_WRITERS (WR_CODE,WR_NAME) values ('34','Chas Wienke');
Insert into SYSTEM.TB_AFFILIATED_WRITERS (WR_CODE,WR_NAME) values ('43','D.C. Ford');
Insert into SYSTEM.TB_AFFILIATED_WRITERS (WR_CODE,WR_NAME) values ('45','D. E. Knobbe');
Insert into SYSTEM.TB_AFFILIATED_WRITERS (WR_CODE,WR_NAME) values ('47','David Cogswell');
Insert into SYSTEM.TB_AFFILIATED_WRITERS (WR_CODE,WR_NAME) values ('76','Douglas Clegg');
Insert into SYSTEM.TB_AFFILIATED_WRITERS (WR_CODE,WR_NAME) values ('78','Iris Johanesen');
Insert into SYSTEM.TB_AFFILIATED_WRITERS (WR_CODE,WR_NAME) values ('98','Jan Groft');
Insert into SYSTEM.TB_AFFILIATED_WRITERS (WR_CODE,WR_NAME) values ('101','Jeff Havens');
Insert into SYSTEM.TB_AFFILIATED_WRITERS (WR_CODE,WR_NAME) values ('102','Kate Pearce');
Insert into SYSTEM.TB_AFFILIATED_WRITERS (WR_CODE,WR_NAME) values ('123','L.C. Higgs');
Insert into SYSTEM.TB_AFFILIATED_WRITERS (WR_CODE,WR_NAME) values ('127','Melissa Mayhue');
Insert into SYSTEM.TB_AFFILIATED_WRITERS (WR_CODE,WR_NAME) values ('134','Mike Green');
Insert into SYSTEM.TB_AFFILIATED_WRITERS (WR_CODE,WR_NAME) values ('145','S. C. Carr');
Insert into SYSTEM.TB_AFFILIATED_WRITERS (WR_CODE,WR_NAME) values ('156','Shirley Tallman');
Insert into SYSTEM.TB_AFFILIATED_WRITERS (WR_CODE,WR_NAME) values ('167','Stacy Choen');
Insert into SYSTEM.TB_AFFILIATED_WRITERS (WR_CODE,WR_NAME) values ('178','Susan Lyons');
Insert into SYSTEM.TB_AFFILIATED_WRITERS (WR_CODE,WR_NAME) values ('189','Tim Davys');
Insert into SYSTEM.TB_AFFILIATED_WRITERS (WR_CODE,WR_NAME) values ('234','Tracy Richardson');
Insert into SYSTEM.TB_AFFILIATED_WRITERS (WR_CODE,WR_NAME) values ('245','William Boyd');
REM INSERTING into SYSTEM.TB_AUTHORS
SET DEFINE OFF;
Insert into SYSTEM.TB_AUTHORS (BK_CODE,WR_ID) values ('1','23');
Insert into SYSTEM.TB_AUTHORS (BK_CODE,WR_ID) values ('1','189');
Insert into SYSTEM.TB_AUTHORS (BK_CODE,WR_ID) values ('2','98');
Insert into SYSTEM.TB_AUTHORS (BK_CODE,WR_ID) values ('2','123');
Insert into SYSTEM.TB_AUTHORS (BK_CODE,WR_ID) values ('3','45');
Insert into SYSTEM.TB_AUTHORS (BK_CODE,WR_ID) values ('4','76');
Insert into SYSTEM.TB_AUTHORS (BK_CODE,WR_ID) values ('5','78');
Insert into SYSTEM.TB_AUTHORS (BK_CODE,WR_ID) values ('6','156');
Insert into SYSTEM.TB_AUTHORS (BK_CODE,WR_ID) values ('7','189');
Insert into SYSTEM.TB_AUTHORS (BK_CODE,WR_ID) values ('8','25');
Insert into SYSTEM.TB_AUTHORS (BK_CODE,WR_ID) values ('8','102');
Insert into SYSTEM.TB_AUTHORS (BK_CODE,WR_ID) values ('8','178');
Insert into SYSTEM.TB_AUTHORS (BK_CODE,WR_ID) values ('9','47');
Insert into SYSTEM.TB_AUTHORS (BK_CODE,WR_ID) values ('10','123');
Insert into SYSTEM.TB_AUTHORS (BK_CODE,WR_ID) values ('11','43');
Insert into SYSTEM.TB_AUTHORS (BK_CODE,WR_ID) values ('11','101');
Insert into SYSTEM.TB_AUTHORS (BK_CODE,WR_ID) values ('11','145');
Insert into SYSTEM.TB_AUTHORS (BK_CODE,WR_ID) values ('16','245');
Insert into SYSTEM.TB_AUTHORS (BK_CODE,WR_ID) values ('17','127');
Insert into SYSTEM.TB_AUTHORS (BK_CODE,WR_ID) values ('18','234');
Insert into SYSTEM.TB_AUTHORS (BK_CODE,WR_ID) values ('19','76');
Insert into SYSTEM.TB_AUTHORS (BK_CODE,WR_ID) values ('20','78');
Insert into SYSTEM.TB_AUTHORS (BK_CODE,WR_ID) values ('21','27');
Insert into SYSTEM.TB_AUTHORS (BK_CODE,WR_ID) values ('22','167');
Insert into SYSTEM.TB_AUTHORS (BK_CODE,WR_ID) values ('23','34');
Insert into SYSTEM.TB_AUTHORS (BK_CODE,WR_ID) values ('24','134');
REM INSERTING into SYSTEM.TB_BK_GEN
SET DEFINE OFF;
Insert into SYSTEM.TB_BK_GEN (ID_BK,GEN_NAME) values ('3','Fiction');
Insert into SYSTEM.TB_BK_GEN (ID_BK,GEN_NAME) values ('4','Horror');
Insert into SYSTEM.TB_BK_GEN (ID_BK,GEN_NAME) values ('5','Mystery');
Insert into SYSTEM.TB_BK_GEN (ID_BK,GEN_NAME) values ('6','Mystery');
Insert into SYSTEM.TB_BK_GEN (ID_BK,GEN_NAME) values ('7','Fantasy');
Insert into SYSTEM.TB_BK_GEN (ID_BK,GEN_NAME) values ('8','Romance');
Insert into SYSTEM.TB_BK_GEN (ID_BK,GEN_NAME) values ('9','Biographies');
Insert into SYSTEM.TB_BK_GEN (ID_BK,GEN_NAME) values ('9','History');
Insert into SYSTEM.TB_BK_GEN (ID_BK,GEN_NAME) values ('9','Politics');
Insert into SYSTEM.TB_BK_GEN (ID_BK,GEN_NAME) values ('10','Historical');
Insert into SYSTEM.TB_BK_GEN (ID_BK,GEN_NAME) values ('10','Novels');
Insert into SYSTEM.TB_BK_GEN (ID_BK,GEN_NAME) values ('11','Self Help');
Insert into SYSTEM.TB_BK_GEN (ID_BK,GEN_NAME) values ('12','Fantasy');
Insert into SYSTEM.TB_BK_GEN (ID_BK,GEN_NAME) values ('12','Fiction');
Insert into SYSTEM.TB_BK_GEN (ID_BK,GEN_NAME) values ('12','Horror');
Insert into SYSTEM.TB_BK_GEN (ID_BK,GEN_NAME) values ('12','Humor');
Insert into SYSTEM.TB_BK_GEN (ID_BK,GEN_NAME) values ('13','Fantasy');
Insert into SYSTEM.TB_BK_GEN (ID_BK,GEN_NAME) values ('13','Horror');
Insert into SYSTEM.TB_BK_GEN (ID_BK,GEN_NAME) values ('14','Cultural');
Insert into SYSTEM.TB_BK_GEN (ID_BK,GEN_NAME) values ('14','Music');
Insert into SYSTEM.TB_BK_GEN (ID_BK,GEN_NAME) values ('15','Science');
Insert into SYSTEM.TB_BK_GEN (ID_BK,GEN_NAME) values ('16','Mystery');
Insert into SYSTEM.TB_BK_GEN (ID_BK,GEN_NAME) values ('17','Romance');
Insert into SYSTEM.TB_BK_GEN (ID_BK,GEN_NAME) values ('18','Children');
Insert into SYSTEM.TB_BK_GEN (ID_BK,GEN_NAME) values ('19','Horror');
Insert into SYSTEM.TB_BK_GEN (ID_BK,GEN_NAME) values ('20','Horror');
Insert into SYSTEM.TB_BK_GEN (ID_BK,GEN_NAME) values ('21','Romance');
Insert into SYSTEM.TB_BK_GEN (ID_BK,GEN_NAME) values ('22','Fiction');
Insert into SYSTEM.TB_BK_GEN (ID_BK,GEN_NAME) values ('23','History');
Insert into SYSTEM.TB_BK_GEN (ID_BK,GEN_NAME) values ('24','Fiction');
REM INSERTING into SYSTEM.TB_BOOKS
SET DEFINE OFF;
Insert into SYSTEM.TB_BOOKS (BK_CODE,BK_TITLE,BK_TYPE) values ('1','Resonance','P');
Insert into SYSTEM.TB_BOOKS (BK_CODE,BK_TITLE,BK_TYPE) values ('2','As we Grieve','P');
Insert into SYSTEM.TB_BOOKS (BK_CODE,BK_TITLE,BK_TYPE) values ('3','Runaway Storm','P');
Insert into SYSTEM.TB_BOOKS (BK_CODE,BK_TITLE,BK_TYPE) values ('4','Neverland','P');
Insert into SYSTEM.TB_BOOKS (BK_CODE,BK_TITLE,BK_TYPE) values ('5','Eight Days to Live','P');
Insert into SYSTEM.TB_BOOKS (BK_CODE,BK_TITLE,BK_TYPE) values ('6','Scandal on Rincon Hill','P');
Insert into SYSTEM.TB_BOOKS (BK_CODE,BK_TITLE,BK_TYPE) values ('7','Amberville','P');
Insert into SYSTEM.TB_BOOKS (BK_CODE,BK_TITLE,BK_TYPE) values ('8','Some Like it Rough','P');
Insert into SYSTEM.TB_BOOKS (BK_CODE,BK_TITLE,BK_TYPE) values ('9','Zinn for Beginners','P');
Insert into SYSTEM.TB_BOOKS (BK_CODE,BK_TITLE,BK_TYPE) values ('10','Here Burns My Candle','P');
Insert into SYSTEM.TB_BOOKS (BK_CODE,BK_TITLE,BK_TYPE) values ('11','How to get fired','P');
Insert into SYSTEM.TB_BOOKS (BK_CODE,BK_TITLE,BK_TYPE) values ('12','The Twelve Little Hitlers','P');
Insert into SYSTEM.TB_BOOKS (BK_CODE,BK_TITLE,BK_TYPE) values ('13','The story of Eight the sparrow','P');
Insert into SYSTEM.TB_BOOKS (BK_CODE,BK_TITLE,BK_TYPE) values ('14','The social impact of Christina Oats songs','P');
Insert into SYSTEM.TB_BOOKS (BK_CODE,BK_TITLE,BK_TYPE) values ('15','Engineering analysis of Mazzinga','P');
Insert into SYSTEM.TB_BOOKS (BK_CODE,BK_TITLE,BK_TYPE) values ('16','Ordinary Thunderstorms','A');
Insert into SYSTEM.TB_BOOKS (BK_CODE,BK_TITLE,BK_TYPE) values ('17','A Highlander''s Homecoming','A');
Insert into SYSTEM.TB_BOOKS (BK_CODE,BK_TITLE,BK_TYPE) values ('18','Indian Summer','A');
Insert into SYSTEM.TB_BOOKS (BK_CODE,BK_TITLE,BK_TYPE) values ('19','A Dark Circus','A');
Insert into SYSTEM.TB_BOOKS (BK_CODE,BK_TITLE,BK_TYPE) values ('20','City of Stars','A');
Insert into SYSTEM.TB_BOOKS (BK_CODE,BK_TITLE,BK_TYPE) values ('21','Not My Daughter','E');
Insert into SYSTEM.TB_BOOKS (BK_CODE,BK_TITLE,BK_TYPE) values ('22','The Last Train From Paris','E');
Insert into SYSTEM.TB_BOOKS (BK_CODE,BK_TITLE,BK_TYPE) values ('23','Our Boomer Years','E');
Insert into SYSTEM.TB_BOOKS (BK_CODE,BK_TITLE,BK_TYPE) values ('24','Path of Thunder','E');
REM INSERTING into SYSTEM.TB_EDITION
SET DEFINE OFF;
Insert into SYSTEM.TB_EDITION (ED_CODE,ED_TYPE,PUB_DATE,N_EDT,EDITOR,BK_ID) values ('12','E',to_date('18-FEB-10','DD-MON-RR'),'1','76','1');
Insert into SYSTEM.TB_EDITION (ED_CODE,ED_TYPE,PUB_DATE,N_EDT,EDITOR,BK_ID) values ('21','E',to_date('12-FEB-00','DD-MON-RR'),'1','76','2');
Insert into SYSTEM.TB_EDITION (ED_CODE,ED_TYPE,PUB_DATE,N_EDT,EDITOR,BK_ID) values ('23','S',to_date('02-JAN-04','DD-MON-RR'),'1','98','3');
Insert into SYSTEM.TB_EDITION (ED_CODE,ED_TYPE,PUB_DATE,N_EDT,EDITOR,BK_ID) values ('32','S',to_date('04-DEC-09','DD-MON-RR'),'1','98','4');
Insert into SYSTEM.TB_EDITION (ED_CODE,ED_TYPE,PUB_DATE,N_EDT,EDITOR,BK_ID) values ('34','E',to_date('06-JUL-00','DD-MON-RR'),'1','23','5');
Insert into SYSTEM.TB_EDITION (ED_CODE,ED_TYPE,PUB_DATE,N_EDT,EDITOR,BK_ID) values ('43','X',to_date('14-MAY-01','DD-MON-RR'),'1','23','6');
Insert into SYSTEM.TB_EDITION (ED_CODE,ED_TYPE,PUB_DATE,N_EDT,EDITOR,BK_ID) values ('45','S',to_date('05-MAY-05','DD-MON-RR'),'1','34','7');
Insert into SYSTEM.TB_EDITION (ED_CODE,ED_TYPE,PUB_DATE,N_EDT,EDITOR,BK_ID) values ('54','X',to_date('11-SEP-08','DD-MON-RR'),'1','54','8');
Insert into SYSTEM.TB_EDITION (ED_CODE,ED_TYPE,PUB_DATE,N_EDT,EDITOR,BK_ID) values ('56','S',to_date('07-FEB-05','DD-MON-RR'),'1','12','9');
Insert into SYSTEM.TB_EDITION (ED_CODE,ED_TYPE,PUB_DATE,N_EDT,EDITOR,BK_ID) values ('65','E',to_date('09-MAY-07','DD-MON-RR'),'1','32','10');
Insert into SYSTEM.TB_EDITION (ED_CODE,ED_TYPE,PUB_DATE,N_EDT,EDITOR,BK_ID) values ('67','X',to_date('03-NOV-04','DD-MON-RR'),'1','87','11');
Insert into SYSTEM.TB_EDITION (ED_CODE,ED_TYPE,PUB_DATE,N_EDT,EDITOR,BK_ID) values ('76','X',to_date('06-DEC-03','DD-MON-RR'),'1','65','12');
Insert into SYSTEM.TB_EDITION (ED_CODE,ED_TYPE,PUB_DATE,N_EDT,EDITOR,BK_ID) values ('78','S',to_date('03-MAY-04','DD-MON-RR'),'1','21','15');
Insert into SYSTEM.TB_EDITION (ED_CODE,ED_TYPE,PUB_DATE,N_EDT,EDITOR,BK_ID) values ('87','S',to_date('09-MAY-07','DD-MON-RR'),'2','34','7');
Insert into SYSTEM.TB_EDITION (ED_CODE,ED_TYPE,PUB_DATE,N_EDT,EDITOR,BK_ID) values ('89','S',to_date('01-MAY-10','DD-MON-RR'),'2','87','2');
Insert into SYSTEM.TB_EDITION (ED_CODE,ED_TYPE,PUB_DATE,N_EDT,EDITOR,BK_ID) values ('98','E',to_date('01-FEB-10','DD-MON-RR'),'2','32','10');
Insert into SYSTEM.TB_EDITION (ED_CODE,ED_TYPE,PUB_DATE,N_EDT,EDITOR,BK_ID) values ('90','E',to_date('09-MAY-06','DD-MON-RR'),'2','23','5');
Insert into SYSTEM.TB_EDITION (ED_CODE,ED_TYPE,PUB_DATE,N_EDT,EDITOR,BK_ID) values ('92','E',to_date('12-JAN-03','DD-MON-RR'),'2','12','6');
Insert into SYSTEM.TB_EDITION (ED_CODE,ED_TYPE,PUB_DATE,N_EDT,EDITOR,BK_ID) values ('91','X',to_date('18-APR-09','DD-MON-RR'),'3','12','6');
Insert into SYSTEM.TB_EDITION (ED_CODE,ED_TYPE,PUB_DATE,N_EDT,EDITOR,BK_ID) values ('82','X',to_date('09-NOV-00','DD-MON-RR'),'1','45','16');
Insert into SYSTEM.TB_EDITION (ED_CODE,ED_TYPE,PUB_DATE,N_EDT,EDITOR,BK_ID) values ('73','X',to_date('01-APR-02','DD-MON-RR'),'1','21','17');
Insert into SYSTEM.TB_EDITION (ED_CODE,ED_TYPE,PUB_DATE,N_EDT,EDITOR,BK_ID) values ('74','X',to_date('03-NOV-03','DD-MON-RR'),'1','87','18');
Insert into SYSTEM.TB_EDITION (ED_CODE,ED_TYPE,PUB_DATE,N_EDT,EDITOR,BK_ID) values ('99','X',to_date('01-APR-06','DD-MON-RR'),'1','23','19');
Insert into SYSTEM.TB_EDITION (ED_CODE,ED_TYPE,PUB_DATE,N_EDT,EDITOR,BK_ID) values ('39','X',to_date('03-FEB-07','DD-MON-RR'),'2','32','20');
Insert into SYSTEM.TB_EDITION (ED_CODE,ED_TYPE,PUB_DATE,N_EDT,EDITOR,BK_ID) values ('40','X',to_date('01-MAR-05','DD-MON-RR'),'1','32','21');
Insert into SYSTEM.TB_EDITION (ED_CODE,ED_TYPE,PUB_DATE,N_EDT,EDITOR,BK_ID) values ('50','X',to_date('03-DEC-01','DD-MON-RR'),'1','87','22');
Insert into SYSTEM.TB_EDITION (ED_CODE,ED_TYPE,PUB_DATE,N_EDT,EDITOR,BK_ID) values ('70','X',to_date('11-MAR-09','DD-MON-RR'),'1','65','23');
Insert into SYSTEM.TB_EDITION (ED_CODE,ED_TYPE,PUB_DATE,N_EDT,EDITOR,BK_ID) values ('10','X',to_date('23-SEP-00','DD-MON-RR'),'1','34','24');
REM INSERTING into SYSTEM.TB_EDITOR
SET DEFINE OFF;
Insert into SYSTEM.TB_EDITOR (ED_CODE,ED_NAME) values ('12','Paul Golden');
Insert into SYSTEM.TB_EDITOR (ED_CODE,ED_NAME) values ('21','Pat Red');
Insert into SYSTEM.TB_EDITOR (ED_CODE,ED_NAME) values ('23','Simon Frost');
Insert into SYSTEM.TB_EDITOR (ED_CODE,ED_NAME) values ('32','Melody Albert');
Insert into SYSTEM.TB_EDITOR (ED_CODE,ED_NAME) values ('34','Valerio Nin');
Insert into SYSTEM.TB_EDITOR (ED_CODE,ED_NAME) values ('45','Victoria Rolls');
Insert into SYSTEM.TB_EDITOR (ED_CODE,ED_NAME) values ('54','Karl Forman');
Insert into SYSTEM.TB_EDITOR (ED_CODE,ED_NAME) values ('65','Fill Luckett');
Insert into SYSTEM.TB_EDITOR (ED_CODE,ED_NAME) values ('76','Eric Jonnes');
Insert into SYSTEM.TB_EDITOR (ED_CODE,ED_NAME) values ('87','Bill Sugar');
Insert into SYSTEM.TB_EDITOR (ED_CODE,ED_NAME) values ('98','Bill Green');
REM INSERTING into SYSTEM.TB_EMERGE_AUTHORS
SET DEFINE OFF;
Insert into SYSTEM.TB_EMERGE_AUTHORS (BK_CODE,WR_ID) values ('14','267');
Insert into SYSTEM.TB_EMERGE_AUTHORS (BK_CODE,WR_ID) values ('14','278');
Insert into SYSTEM.TB_EMERGE_AUTHORS (BK_CODE,WR_ID) values ('15','289');
REM INSERTING into SYSTEM.TB_ON_PROB_WR
SET DEFINE OFF;
Insert into SYSTEM.TB_ON_PROB_WR (WR_CODE,WR_NAME) values ('267','Peter Griffin');
Insert into SYSTEM.TB_ON_PROB_WR (WR_CODE,WR_NAME) values ('278','Homer Simpson');
Insert into SYSTEM.TB_ON_PROB_WR (WR_CODE,WR_NAME) values ('289','Jon Stewart');

--------------------------------------------------------
--  Constraints for Table TB_AFFILIATED_WRITERS
--------------------------------------------------------

  ALTER TABLE "SYSTEM"."TB_AFFILIATED_WRITERS" ADD CONSTRAINT "AFF_WR_PK" PRIMARY KEY ("WR_CODE") ENABLE;
  ALTER TABLE "SYSTEM"."TB_AFFILIATED_WRITERS" MODIFY ("WR_NAME" NOT NULL ENABLE);
  ALTER TABLE "SYSTEM"."TB_AFFILIATED_WRITERS" MODIFY ("WR_CODE" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table TB_AUTHORS
--------------------------------------------------------

  ALTER TABLE "SYSTEM"."TB_AUTHORS" ADD CONSTRAINT "PK_AU" PRIMARY KEY ("BK_CODE", "WR_ID") ENABLE;
  ALTER TABLE "SYSTEM"."TB_AUTHORS" MODIFY ("WR_ID" NOT NULL ENABLE);
  ALTER TABLE "SYSTEM"."TB_AUTHORS" MODIFY ("BK_CODE" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table TB_BK_GEN
--------------------------------------------------------

  ALTER TABLE "SYSTEM"."TB_BK_GEN" ADD CONSTRAINT "PK_GEN" PRIMARY KEY ("ID_BK", "GEN_NAME") ENABLE;
  ALTER TABLE "SYSTEM"."TB_BK_GEN" MODIFY ("GEN_NAME" NOT NULL ENABLE);
  ALTER TABLE "SYSTEM"."TB_BK_GEN" MODIFY ("ID_BK" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table TB_BOOKS
--------------------------------------------------------

  ALTER TABLE "SYSTEM"."TB_BOOKS" ADD CONSTRAINT "BK_PK" PRIMARY KEY ("BK_CODE") ENABLE;
  ALTER TABLE "SYSTEM"."TB_BOOKS" MODIFY ("BK_TYPE" NOT NULL ENABLE);
  ALTER TABLE "SYSTEM"."TB_BOOKS" MODIFY ("BK_TITLE" NOT NULL ENABLE);
  ALTER TABLE "SYSTEM"."TB_BOOKS" MODIFY ("BK_CODE" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table TB_EDITION
--------------------------------------------------------

  ALTER TABLE "SYSTEM"."TB_EDITION" MODIFY ("BK_ID" NOT NULL ENABLE);
  ALTER TABLE "SYSTEM"."TB_EDITION" MODIFY ("EDITOR" NOT NULL ENABLE);
  ALTER TABLE "SYSTEM"."TB_EDITION" MODIFY ("N_EDT" NOT NULL ENABLE);
  ALTER TABLE "SYSTEM"."TB_EDITION" MODIFY ("PUB_DATE" NOT NULL ENABLE);
  ALTER TABLE "SYSTEM"."TB_EDITION" MODIFY ("ED_TYPE" NOT NULL ENABLE);
  ALTER TABLE "SYSTEM"."TB_EDITION" MODIFY ("ED_CODE" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table TB_EDITOR
--------------------------------------------------------

  ALTER TABLE "SYSTEM"."TB_EDITOR" ADD CONSTRAINT "ED_PK" PRIMARY KEY ("ED_CODE") ENABLE;
  ALTER TABLE "SYSTEM"."TB_EDITOR" MODIFY ("ED_NAME" NOT NULL ENABLE);
  ALTER TABLE "SYSTEM"."TB_EDITOR" MODIFY ("ED_CODE" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table TB_EMERGE_AUTHORS
--------------------------------------------------------

  ALTER TABLE "SYSTEM"."TB_EMERGE_AUTHORS" ADD CONSTRAINT "PK_EMERGE_AUTHORS" PRIMARY KEY ("BK_CODE", "WR_ID") ENABLE;
  ALTER TABLE "SYSTEM"."TB_EMERGE_AUTHORS" MODIFY ("WR_ID" NOT NULL ENABLE);
  ALTER TABLE "SYSTEM"."TB_EMERGE_AUTHORS" MODIFY ("BK_CODE" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table TB_ON_PROB_WR
--------------------------------------------------------

  ALTER TABLE "SYSTEM"."TB_ON_PROB_WR" ADD CONSTRAINT "PR_PK" PRIMARY KEY ("WR_CODE") ENABLE;
  ALTER TABLE "SYSTEM"."TB_ON_PROB_WR" MODIFY ("WR_NAME" NOT NULL ENABLE);
  ALTER TABLE "SYSTEM"."TB_ON_PROB_WR" MODIFY ("WR_CODE" NOT NULL ENABLE);
--------------------------------------------------------
--  Ref Constraints for Table TB_AUTHORS
--------------------------------------------------------

  ALTER TABLE "SYSTEM"."TB_AUTHORS" ADD CONSTRAINT "FK_AFFILIATED_WRITES_BOOK" FOREIGN KEY ("WR_ID")
	  REFERENCES "SYSTEM"."TB_AFFILIATED_WRITERS" ("WR_CODE") ENABLE;
  ALTER TABLE "SYSTEM"."TB_AUTHORS" ADD CONSTRAINT "FK_WRITTEN_BOOKS" FOREIGN KEY ("BK_CODE")
	  REFERENCES "SYSTEM"."TB_BOOKS" ("BK_CODE") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table TB_BK_GEN
--------------------------------------------------------

  ALTER TABLE "SYSTEM"."TB_BK_GEN" ADD CONSTRAINT "FK_BK_GEN" FOREIGN KEY ("ID_BK")
	  REFERENCES "SYSTEM"."TB_BOOKS" ("BK_CODE") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table TB_EDITION
--------------------------------------------------------

  ALTER TABLE "SYSTEM"."TB_EDITION" ADD CONSTRAINT "FK_BOOK_HAS_EDITION" FOREIGN KEY ("BK_ID")
	  REFERENCES "SYSTEM"."TB_BOOKS" ("BK_CODE") ENABLE;
  ALTER TABLE "SYSTEM"."TB_EDITION" ADD CONSTRAINT "FK_EDITION_HAS_EDITOR" FOREIGN KEY ("EDITOR")
	  REFERENCES "SYSTEM"."TB_EDITOR" ("ED_CODE") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table TB_EMERGE_AUTHORS
--------------------------------------------------------

  ALTER TABLE "SYSTEM"."TB_EMERGE_AUTHORS" ADD CONSTRAINT "FK_EMERGE_WRITES_BOOK" FOREIGN KEY ("WR_ID")
	  REFERENCES "SYSTEM"."TB_ON_PROB_WR" ("WR_CODE") ENABLE;
  ALTER TABLE "SYSTEM"."TB_EMERGE_AUTHORS" ADD CONSTRAINT "FK_WRITTEN_BOOK2" FOREIGN KEY ("BK_CODE")
	  REFERENCES "SYSTEM"."TB_BOOKS" ("BK_CODE") ENABLE;
