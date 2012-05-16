CREATE TABLE address (
    "id" integer NOT NULL,
    street varchar(100),
    "number" integer,
    city varchar(100),
    state varchar(100),
    country varchar(100),
	CONSTRAINT address_pkey PRIMARY KEY ("id")
);

CREATE TABLE broker (
    "id" integer NOT NULL,
    "name" varchar(100),
    lastname varchar(100),
    dateofbirth date,
    ssn varchar(100),
    addressid integer,
	CONSTRAINT broker_pkey PRIMARY KEY ("id")
);

CREATE TABLE brokerworksfor (
    brokerid integer NOT NULL,
    companyid integer,
    clientid integer
);


CREATE TABLE client (
    "id" integer NOT NULL,
    "name" varchar(100),
    lastname varchar(100),
    dateofbirth date,
    ssn varchar(100),
    addressid integer,
	CONSTRAINT client_pkey PRIMARY KEY ("id")
);

CREATE TABLE company (
    "id" integer NOT NULL,
    "name" varchar(100),
    marketshares integer,
    networth double precision,
    addressid integer,
	CONSTRAINT company_pkey PRIMARY KEY ("id")
);

CREATE TABLE stockbooklist (
    "date" timestamp NOT NULL,
    stockid integer,
	CONSTRAINT stockbooklist_pkey PRIMARY KEY ("date")
);

CREATE TABLE stockinformation (
    "id" integer NOT NULL,
    numberofshares integer,
    sharetype number(1),
    companyid integer,
    description clob,
	CONSTRAINT stockinformation_pkey PRIMARY KEY ("id")
);

CREATE TABLE "TRANSACTION" (
    "id" integer NOT NULL,
    "date" timestamp,
    stockid integer,
    "type" number(1),
    brokerid integer,
    forclientid integer,
    forcompanyid integer,
    amount decimal,
	CONSTRAINT transaction_pkey PRIMARY KEY ("id")
);

ALTER SESSION SET NLS_TIMESTAMP_FORMAT = 'YYYY-MM-DD HH24:MI:SS.FF';

INSERT INTO address VALUES (991, 'Road street', 24, 'Chonala', 'Veracruz', 'Mexico');
INSERT INTO address VALUES (992, 'Via Marconi', 3, 'Bolzano', 'Bolzano', 'Italy');
INSERT INTO address VALUES (995, 'Huberg Strasse', 3, 'Bolzano', 'Bolzano', 'Italy');
INSERT INTO address VALUES (996, 'Via Piani di Bolzano', 7, 'Marconi', 'Trentino', 'Italy');
INSERT INTO address VALUES (993, 'Romer Street', 32, 'Malaga', 'Malaga', 'Spain');
INSERT INTO address VALUES (997, 'Samara road', 9976, 'Puebla', 'Puebla', 'Mexico');
INSERT INTO address VALUES (998, 'Jalan Madura 12', 245, 'Jakarta', 'Jakarta', 'Indonesia');

INSERT INTO broker VALUES (112, 'Joana', 'Lopatenkko', '14-JUL-1970', 'JLPTK54992', 992);
INSERT INTO broker VALUES (113, 'Walter', 'Schmidt', '03-SEP-1968', 'WSCH9820783903', 993);
INSERT INTO broker VALUES (114, 'Patricia', 'Lombrardi', '22-FEB-1975', 'PTLM8878767830', 997);

INSERT INTO brokerworksfor VALUES (112, NULL, 111);
INSERT INTO brokerworksfor VALUES (112, NULL, 112);
INSERT INTO brokerworksfor VALUES (113, 212, NULL);
INSERT INTO brokerworksfor VALUES (113, 211, NULL);
INSERT INTO brokerworksfor VALUES (114, 212, NULL);
INSERT INTO brokerworksfor VALUES (114, NULL, 111);
INSERT INTO brokerworksfor VALUES (112, NULL, 111);
INSERT INTO brokerworksfor VALUES (112, NULL, 112);
INSERT INTO brokerworksfor VALUES (113, 212, NULL);
INSERT INTO brokerworksfor VALUES (113, 211, NULL);
INSERT INTO brokerworksfor VALUES (114, 212, NULL);
INSERT INTO brokerworksfor VALUES (114, NULL, 111);

INSERT INTO client VALUES (111, 'John', 'Smith', '21-MAR-1950', 'JSRX229500321', 991);
INSERT INTO client VALUES (112, 'Joana', 'Lopatenkko', '14-JUL-1970', 'JLPTK54992', 992);

INSERT INTO company VALUES (211, 'General Motors', 25000000, 1.2345678e+03, 995);
INSERT INTO company VALUES (212, 'GnA Investments', 100000, 1234.5678, 996);

INSERT INTO stockbooklist VALUES ('2008-04-01 00:00:00.000', 661);
INSERT INTO stockbooklist VALUES ('2008-04-02 00:00:00.000', 662);
INSERT INTO stockbooklist VALUES ('2008-04-03 00:00:00.000', 663);
INSERT INTO stockbooklist VALUES ('2008-04-04 00:00:00.000', 664);
INSERT INTO stockbooklist VALUES ('2008-04-05 00:00:00.000', 665);
INSERT INTO stockbooklist VALUES ('2008-04-06 00:00:00.000', 666);
INSERT INTO stockbooklist VALUES ('2008-04-07 00:00:00.000', 667);
INSERT INTO stockbooklist VALUES ('2008-04-08 00:00:00.000', 668);
INSERT INTO stockbooklist VALUES ('2008-04-09 00:00:00.000', 669);

INSERT INTO stockinformation VALUES (661, 100, 0, 211, 'Text description 1');
INSERT INTO stockinformation VALUES (660, 100, 0, 211, 'Text description 2');
INSERT INTO stockinformation VALUES (662, 100, 0, 211, 'Text description 3');
INSERT INTO stockinformation VALUES (663, 100, 0, 211, 'Text description 4');
INSERT INTO stockinformation VALUES (664, 100, 0, 211, 'Text description 5');
INSERT INTO stockinformation VALUES (665, 100, 1, 211, 'Testo di descrizione 1');
INSERT INTO stockinformation VALUES (666, 100, 1, 211, 'Testo di descrizione 2');
INSERT INTO stockinformation VALUES (667, 100, 1, 211, 'Testo di descrizione 3');
INSERT INTO stockinformation VALUES (669, 100, 1, 211, 'Testo di descrizione 4');
INSERT INTO stockinformation VALUES (668, 100, 1, 211, 'Testo di descrizione 5');

INSERT INTO "TRANSACTION" VALUES (3331, '2008-04-01 00:00:00.000', 661, 1, 112, 111, NULL, 12.6);
INSERT INTO "TRANSACTION" VALUES (3332, '2008-04-02 00:00:00.000', 662, 1, 112, 111, NULL, 108.34);
INSERT INTO "TRANSACTION" VALUES (3333, '2008-04-03 00:00:00.000', 663, 1, 112, NULL, 212, -2.349);
INSERT INTO "TRANSACTION" VALUES (3334, '2008-04-14 00:00:00.000', 663, 1, 113, NULL, NULL, 1667.0092);