CREATE TABLE address (
    id integer NOT NULL,
    street character varying(100),
    number integer,
    city character varying(100),
    state character varying(100),
    country character varying(100)
);

CREATE TABLE client (
    id integer NOT NULL,
    name character varying(100),
    lastname character varying(100),
    dateofbirth date,
    ssn character varying(100),
    addressid integer,
    sex character varying(100)
);

CREATE TABLE company (
    id integer NOT NULL,
    name character varying(100),
    marketshares integer,
    networth double precision,
    addressid integer
);

INSERT INTO address VALUES (991, 'Road street', 24, 'Chonala', 'Veracruz', 'Mexico');
INSERT INTO address VALUES (992, 'Via Marconi', 3, 'Bolzano', 'Bolzano', 'Italy');
INSERT INTO address VALUES (995, 'Huberg Strasse', 3, 'Bolzano', 'Bolzano', 'Italy');
INSERT INTO address VALUES (996, 'Via Piani di Bolzano', 7, 'Marconi', 'Trentino', 'Italy');
INSERT INTO address VALUES (993, 'Romer Street', 32, 'Malaga', 'Malaga', 'Spain');
INSERT INTO address VALUES (997, 'Samara road', 9976, 'Puebla', 'Puebla', 'Mexico');
INSERT INTO address VALUES (998, 'Jalan Madura 12', 245, 'Jakarta', 'Jakarta', 'Indonesia');

INSERT INTO client VALUES (111, 'John', 'Smith', '1950-03-21', 'JSRX229500321', 991 ,'M');
INSERT INTO client VALUES (112, 'Joana', 'Lopatenkko', '1970-07-14', 'JLPTK54992', 992, 'F');

INSERT INTO company VALUES (211, 'General Motors', 25000000, 1.2345678e+03, 995);
INSERT INTO company VALUES (212, 'GnA Investments', 100000, 1234.5678, 992);


ALTER TABLE address
    ADD CONSTRAINT address_pkey PRIMARY KEY (id);

ALTER TABLE client
    ADD CONSTRAINT client_pkey PRIMARY KEY (id);

ALTER TABLE company
    ADD CONSTRAINT company_pkey PRIMARY KEY (id);

