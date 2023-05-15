CREATE DATABASE STOCKNEW;

CONNECT TO STOCKNEW;

CREATE TABLE "address"  (
                                       "id" INTEGER PRIMARY KEY NOT NULL ,
                                       "street" VARCHAR(100) WITH DEFAULT NULL ,
                                       "number" INTEGER WITH DEFAULT NULL ,
                                       "city" VARCHAR(100) WITH DEFAULT NULL ,
                                       "state" VARCHAR(100) WITH DEFAULT NULL ,
                                       "country" VARCHAR(100) WITH DEFAULT NULL )
;


CREATE TABLE "broker"  (
    "id" INTEGER PRIMARY KEY NOT NULL )
;


CREATE TABLE "brokerworksfor"  (
                                              "brokerid" INTEGER NOT NULL ,
                                              "companyid" INTEGER WITH DEFAULT NULL ,
                                              "clientid" INTEGER WITH DEFAULT NULL )
;


CREATE TABLE "client"  (
    "id" INTEGER PRIMARY KEY NOT NULL )
;


CREATE TABLE "person"  (
                                      "id" INTEGER PRIMARY KEY NOT NULL ,
                                      "name" VARCHAR(100) WITH DEFAULT NULL ,
                                      "lastname" VARCHAR(100) WITH DEFAULT NULL ,
                                      "dateofbirth" DATE WITH DEFAULT NULL ,
                                      "ssn" VARCHAR(100) WITH DEFAULT NULL ,
                                      "addressid" INTEGER WITH DEFAULT NULL )
;


CREATE TABLE "company"  (
                                       "id" INTEGER PRIMARY KEY NOT NULL ,
                                       "name" VARCHAR(100) WITH DEFAULT NULL ,
                                       "marketshares" INTEGER WITH DEFAULT NULL ,
                                       "networth" DOUBLE WITH DEFAULT NULL ,
                                       "addressid" INTEGER WITH DEFAULT NULL )
;


CREATE TABLE "stockbooklist"  (
                                             "date" DATE PRIMARY KEY NOT NULL ,
                                             "stockid" INTEGER WITH DEFAULT NULL )
;



CREATE TABLE "transaction"  (
                                           "id" INTEGER PRIMARY KEY NOT NULL ,
                                           "date" TIMESTAMP NOT NULL ,
                                           "stockid" INTEGER WITH DEFAULT NULL ,
                                           "type" BOOLEAN WITH DEFAULT NULL ,
                                           "brokerid" INTEGER WITH DEFAULT NULL ,
                                           "forclientid" INTEGER WITH DEFAULT NULL ,
                                           "forcompanyid" INTEGER WITH DEFAULT NULL ,
                                           "amount" DECIMAL(10,4) WITH DEFAULT NULL )
;


CREATE TABLE "stockinformation"  (
                                                "id" INTEGER PRIMARY KEY NOT NULL ,
                                                "numberofshares" INTEGER WITH DEFAULT NULL ,
                                                "sharetype" BOOLEAN WITH DEFAULT NULL ,
                                                "companyid" INTEGER WITH DEFAULT NULL ,
                                                "description" VARCHAR(255) )
;



INSERT INTO "address" ("id","street","number","city","state","country") VALUES
    ('991','Road street','24','Chonala','Veracruz','Mexico'),
    ('992','Via Marconi','3','Bolzano','Bolzano','Italy'),
    ('995','Huberg Strasse','3','Bolzano','Bolzano','Italy'),
    ('996','Via Piani di Bolzano','7','Marconi','Trentino','Italy'),
    ('993','Romer Street','32','Malaga','Malaga','Spain'),
    ('997','Samara road','9976','Puebla','Puebla','Mexico'),
    ('998','Jalan Madura 12','245','Jakarta','Jakarta','Indonesia');

INSERT INTO "person" ("id","name","lastname","dateofbirth","ssn","addressid") VALUES
    ('111','John','Smith',to_date('21-MAR-50','DD-MON-RR'),'JSRX229500321','991'),
    ('112','Joana','Lopatenkko',to_date('14-JUL-70','DD-MON-RR'),'JLPTK54992','992'),
    ('113','Walter','Schmidt',to_date('03-SEP-68','DD-MON-RR'),'WSCH9820783903','993'),
    ('114','Patricia','Lombrardi',to_date('22-FEB-75','DD-MON-RR'),'PTLM8878767830','997');

INSERT INTO "broker" ("id") VALUES ('112'), ('113'), ('114');

INSERT INTO "client" ("id") VALUES ('111'), ('112');

INSERT INTO "company" ("id","name","marketshares","networth","addressid") VALUES
    ('211','General Motors','25000000','1234.5678','995'),
    ('212','GnA Investments','100000','1234.5678','996');

INSERT INTO "stockinformation" ("id","numberofshares","sharetype","companyid","description") VALUES
    ('661','100','0','211','Text description 1'),
    ('660','100','0','211','Text description 2'),
    ('662','100','0','211','Text description 3'),
    ('663','100','0','211','Text description 4'),
    ('664','100','0','211','Text description 5'),
    ('665','100','1','211','Testo di descrizione 1'),
    ('666','100','1','211','Testo di descrizione 2'),
    ('667','100','1','211','Testo di descrizione 3'),
    ('669','100','1','211','Testo di descrizione 4'),
    ('668','100','1','211','Testo di descrizione 5');

INSERT INTO "stockbooklist" ("date","stockid") VALUES
    (to_timestamp('01-APR-08 00:00:00','DD-MON-RR HH24:MI:SS'),'661'),
    (to_timestamp('02-APR-08 00:00:00','DD-MON-RR HH24:MI:SS'),'662'),
    (to_timestamp('03-APR-08 00:00:00','DD-MON-RR HH24:MI:SS'),'663'),
    (to_timestamp('04-APR-08 00:00:00','DD-MON-RR HH24:MI:SS'),'664'),
    (to_timestamp('05-APR-08 00:00:00','DD-MON-RR HH24:MI:SS'),'665'),
    (to_timestamp('06-APR-08 00:00:00','DD-MON-RR HH24:MI:SS'),'666'),
    (to_timestamp('07-APR-08 00:00:00','DD-MON-RR HH24:MI:SS'),'667'),
    (to_timestamp('08-APR-08 00:00:00','DD-MON-RR HH24:MI:SS'),'668'),
    (to_timestamp('09-APR-08 00:00:00','DD-MON-RR HH24:MI:SS'),'669');

INSERT INTO "brokerworksfor" ("brokerid","companyid","clientid") VALUES
    ('112',null,'111'),
    ('112',null,'112'),
    ('113','212',null),
    ('113','211',null),
    ('114','212',null),
    ('114',null,'111'),
    ('112',null,'111'),
    ('112',null,'112'),
    ('113','212',null),
    ('113','211',null),
    ('114','212',null),
    ('114',null,'111');

INSERT INTO "transaction" ("id","date","stockid","type","brokerid","forclientid","forcompanyid","amount") VALUES
    ('3331',to_timestamp('01-APR-08 00:00:00','DD-MON-RR HH24:MI:SS'),'661','1','112','111',null,'12.6'),
    ('3332',to_timestamp('02-APR-08 00:00:00','DD-MON-RR HH24:MI:SS'),'662','1','112','111',null,'108.34'),
    ('3333',to_timestamp('03-APR-08 00:00:00','DD-MON-RR HH24:MI:SS'),'663','1','112',null,'212','-2.349'),
    ('3334',to_timestamp('14-APR-08 00:00:00','DD-MON-RR HH24:MI:SS'),'663','1','113',null,null,'1667.0092');

ALTER TABLE "person" ADD CONSTRAINT "FK_ADDRESS_PKEY" FOREIGN KEY ("addressid")
    REFERENCES "address" ("id") ;


ALTER TABLE "client" ADD CONSTRAINT "FK_PERSON_CLIENT_PKEY" FOREIGN KEY ("id")
    REFERENCES "person" ("id") ;


ALTER TABLE "broker" ADD CONSTRAINT "FK_PERSON_BROKER_PKEY" FOREIGN KEY ("id")
    REFERENCES "person" ("id") ;


ALTER TABLE "company" ADD CONSTRAINT "FK_ADDRESS_COMPANY_PKEY" FOREIGN KEY ("addressid")
    REFERENCES "address" ("id") ;


ALTER TABLE "stockinformation" ADD CONSTRAINT "FK_COMPANY_PKEY" FOREIGN KEY ("companyid")
    REFERENCES "company" ("id") ;


ALTER TABLE "stockbooklist" ADD CONSTRAINT "FK_STOCKID_PKEY" FOREIGN KEY ("stockid")
    REFERENCES "stockinformation" ("id") ;


ALTER TABLE "brokerworksfor" ADD CONSTRAINT "FK_BROKER_PKEY" FOREIGN KEY ("brokerid")
    REFERENCES "broker" ("id") ;
ALTER TABLE "brokerworksfor" ADD CONSTRAINT "FK_BWORKSFOR_CLIENTID_PKEY" FOREIGN KEY ("clientid")
    REFERENCES "client" ("id") ;
ALTER TABLE "brokerworksfor" ADD CONSTRAINT "FK_BWORKSFOR_COMPANYID_PKEY" FOREIGN KEY ("companyid")
    REFERENCES "company" ("id") ;


ALTER TABLE "transaction" ADD CONSTRAINT "FK_BROKER_TRANSACTION_PKEY" FOREIGN KEY ("brokerid")
    REFERENCES "broker" ("id") ;
ALTER TABLE "transaction" ADD CONSTRAINT "FK_FORCLIENTID_PKEY" FOREIGN KEY ("forclientid")
    REFERENCES "client" ("id") ;
ALTER TABLE "transaction" ADD CONSTRAINT "FK_FORCOMPANYID_PKEY" FOREIGN KEY ("forcompanyid")
    REFERENCES "company" ("id") ;
ALTER TABLE "transaction" ADD CONSTRAINT "FK_STOCKINFORMATION_PKEY" FOREIGN KEY ("stockid")
    REFERENCES "stockinformation" ("id") ;

COMMIT WORK;

CONNECT RESET;

TERMINATE;