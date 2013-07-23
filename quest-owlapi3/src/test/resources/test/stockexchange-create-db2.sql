CREATE TABLE address (
    id integer NOT NULL,
    street character varying(100),
    number integer,
    city character varying(100),
    state character varying(100),
    country character varying(100)
);


CREATE TABLE broker (
    id integer NOT NULL,
    name character varying(100),
    lastname character varying(100),
    dateofbirth date,
    ssn character varying(100),
    addressid integer
);

CREATE TABLE brokerworksfor (
    brokerid integer NOT NULL,
    companyid integer,
    clientid integer
);


CREATE TABLE client (
    id integer NOT NULL,
    name character varying(100),
    lastname character varying(100),
    dateofbirth date,
    ssn character varying(100),
    addressid integer
);

CREATE TABLE company (
    id integer NOT NULL,
    name character varying(100),
    marketshares integer,
    networth double precision,
    addressid integer
);

CREATE TABLE stockbooklist (
    date date NOT NULL,
    stockid integer
);

CREATE TABLE stockinformation (
    id integer NOT NULL,
    numberofshares integer,
    sharetype smallint,
    companyid integer,
    description character varying(1000)
);

CREATE TABLE transaction (
    id integer NOT NULL,
    date timestamp,
    stockid integer,
    type smallint,
    brokerid integer,
    forclientid integer,
    forcompanyid integer,
    amount decimal(10,4)
);

INSERT INTO address VALUES (991, 'Road street', 24, 'Chonala', 'Veracruz', 'Mexico');
INSERT INTO address VALUES (992, 'Via Marconi', 3, 'Bolzano', 'Bolzano', 'Italy');
INSERT INTO address VALUES (995, 'Huberg Strasse', 3, 'Bolzano', 'Bolzano', 'Italy');
INSERT INTO address VALUES (996, 'Via Piani di Bolzano', 7, 'Marconi', 'Trentino', 'Italy');
INSERT INTO address VALUES (993, 'Romer Street', 32, 'Malaga', 'Malaga', 'Spain');
INSERT INTO address VALUES (997, 'Samara road', 9976, 'Puebla', 'Puebla', 'Mexico');
INSERT INTO address VALUES (998, 'Jalan Madura 12', 245, 'Jakarta', 'Jakarta', 'Indonesia');

INSERT INTO broker VALUES (112, 'Joana', 'Lopatenkko', '1970-07-14', 'JLPTK54992', 992);
INSERT INTO broker VALUES (113, 'Walter', 'Schmidt', '1968-09-03', 'WSCH9820783903', 993);
INSERT INTO broker VALUES (114, 'Patricia', 'Lombrardi', '1975-02-22', 'PTLM8878767830', 997);

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

INSERT INTO client VALUES (111, 'John', 'Smith', '1950-03-21', 'JSRX229500321', 991);
INSERT INTO client VALUES (112, 'Joana', 'Lopatenkko', '1970-07-14', 'JLPTK54992', 992);

INSERT INTO company VALUES (211, 'General Motors', 25000000, 1.2345678e+03, 995);
INSERT INTO company VALUES (212, 'GnA Investments', 100000, 1234.5678, 996);

INSERT INTO stockbooklist VALUES ('2008-04-01', 661);
INSERT INTO stockbooklist VALUES ('2008-04-02', 662);
INSERT INTO stockbooklist VALUES ('2008-04-03', 663);
INSERT INTO stockbooklist VALUES ('2008-04-04', 664);
INSERT INTO stockbooklist VALUES ('2008-04-05', 665);
INSERT INTO stockbooklist VALUES ('2008-04-06', 666);
INSERT INTO stockbooklist VALUES ('2008-04-07', 667);
INSERT INTO stockbooklist VALUES ('2008-04-08', 668);
INSERT INTO stockbooklist VALUES ('2008-04-09', 669);

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

INSERT INTO transaction VALUES (3331, '2008-04-01', 661, 1, 112, 111, NULL, 12.6);
INSERT INTO transaction VALUES (3332, '2008-04-02', 662, 1, 112, 111, NULL, 108.34);
INSERT INTO transaction VALUES (3333, '2008-04-03', 663, 1, 112, NULL, 212, -2.349);
INSERT INTO transaction VALUES (3334, '2008-04-14', 663, 1, 113, NULL, NULL, 1667.0092);


ALTER TABLE address
    ADD CONSTRAINT address_pkey PRIMARY KEY (id);

ALTER TABLE broker
    ADD CONSTRAINT broker_pkey PRIMARY KEY (id);

ALTER TABLE client
    ADD CONSTRAINT client_pkey PRIMARY KEY (id);

ALTER TABLE company
    ADD CONSTRAINT company_pkey PRIMARY KEY (id);

ALTER TABLE stockbooklist
    ADD CONSTRAINT stockbooklist_pkey PRIMARY KEY (date);

ALTER TABLE stockinformation
    ADD CONSTRAINT stockinformation_pkey PRIMARY KEY (id);

ALTER TABLE transaction
    ADD CONSTRAINT transaction_pkey PRIMARY KEY (id);

