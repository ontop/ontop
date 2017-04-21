--
-- PostgreSQL database dump
--

-- Dumped from database version 9.1.24
-- Dumped by pg_dump version 9.3.1
-- Started on 2017-01-20 11:38:09 CET

DROP DATABASE stockexchange_new;
--
-- TOC entry 1958 (class 1262 OID 1208187)
-- Name: stockexchange_new; Type: DATABASE; Schema: -; Owner: -
--

CREATE DATABASE stockexchange_new WITH TEMPLATE = template0 ENCODING = 'UTF8' LC_COLLATE = 'en_US.UTF-8' LC_CTYPE = 'en_US.UTF-8';


\connect stockexchange_new

--
-- TOC entry 6 (class 2615 OID 2200)
-- Name: public; Type: SCHEMA; Schema: -; Owner: -
--

CREATE SCHEMA public;


--
-- TOC entry 161 (class 1259 OID 1208188)
-- Name: address; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE address (
    id integer NOT NULL,
    street character varying(100),
    number integer,
    city character varying(100),
    state character varying(100),
    country character varying(100)
);


--
-- TOC entry 163 (class 1259 OID 1208197)
-- Name: broker; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE broker (
    id integer NOT NULL
);


--
-- TOC entry 165 (class 1259 OID 1208203)
-- Name: brokerworksfor; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE brokerworksfor (
    brokerid integer NOT NULL,
    companyid integer,
    clientid integer
);


--
-- TOC entry 164 (class 1259 OID 1208200)
-- Name: client; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE client (
    id integer NOT NULL
);


--
-- TOC entry 166 (class 1259 OID 1208206)
-- Name: company; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE company (
    id integer NOT NULL,
    name character varying(100) NOT NULL,
    marketshares integer NOT NULL,
    networth double precision NOT NULL,
    addressid integer NOT NULL
);


--
-- TOC entry 162 (class 1259 OID 1208194)
-- Name: person; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE person (
    id integer NOT NULL,
    name character varying(100) NOT NULL,
    lastname character varying(100) NOT NULL,
    dateofbirth date NOT NULL,
    ssn character varying(100) NOT NULL,
    addressid integer NOT NULL
);


--
-- TOC entry 167 (class 1259 OID 1208209)
-- Name: stockbooklist; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE stockbooklist (
    date date NOT NULL,
    stockid integer NOT NULL
);


--
-- TOC entry 168 (class 1259 OID 1208212)
-- Name: stockinformation; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE stockinformation (
    id integer NOT NULL,
    numberofshares integer NOT NULL,
    sharetype boolean NOT NULL,
    companyid integer NOT NULL,
    description text NOT NULL
);


--
-- TOC entry 169 (class 1259 OID 1208218)
-- Name: transaction; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE transaction (
    id integer NOT NULL,
    date timestamp without time zone NOT NULL,
    stockid integer NOT NULL,
    type boolean NOT NULL,
    brokerid integer NOT NULL,
    forclientid integer,
    forcompanyid integer,
    amount numeric(10,4) NOT NULL
);



--
-- TOC entry 1945 (class 0 OID 1208188)
-- Dependencies: 161
-- Data for Name: address; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO address (id, street, number, city, state, country) VALUES (998, 'Jalan Madura 12', 245, 'Jakarta', 'Jakarta', 'Indonesia');
INSERT INTO address (id, street, number, city, state, country) VALUES (991, 'Road street', 24, 'Chonala', 'Veracruz', 'Mexico');
INSERT INTO address (id, street, number, city, state, country) VALUES (992, 'Via Marconi', 3, 'Bolzano', 'Bolzano', 'Italy');
INSERT INTO address (id, street, number, city, state, country) VALUES (995, 'Huberg Strasse', 3, 'Bolzano', 'Bolzano', 'Italy');
INSERT INTO address (id, street, number, city, state, country) VALUES (996, 'Via Piani di Bolzano', 7, 'Marconi', 'Trentino', 'Italy');
INSERT INTO address (id, street, number, city, state, country) VALUES (993, 'Romer Street', 32, 'Malaga', 'Malaga', 'Spain');
INSERT INTO address (id, street, number, city, state, country) VALUES (997, 'Samara road', 9976, 'Puebla', 'Puebla', 'Mexico');


--
-- TOC entry 1947 (class 0 OID 1208197)
-- Dependencies: 163
-- Data for Name: broker; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO broker (id) VALUES (112);
INSERT INTO broker (id) VALUES (113);
INSERT INTO broker (id) VALUES (114);


--
-- TOC entry 1949 (class 0 OID 1208203)
-- Dependencies: 165
-- Data for Name: brokerworksfor; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO brokerworksfor (brokerid, companyid, clientid) VALUES (112, NULL, 111);
INSERT INTO brokerworksfor (brokerid, companyid, clientid) VALUES (112, NULL, 112);
INSERT INTO brokerworksfor (brokerid, companyid, clientid) VALUES (113, 212, NULL);
INSERT INTO brokerworksfor (brokerid, companyid, clientid) VALUES (113, 211, NULL);
INSERT INTO brokerworksfor (brokerid, companyid, clientid) VALUES (114, 212, NULL);
INSERT INTO brokerworksfor (brokerid, companyid, clientid) VALUES (114, NULL, 111);
INSERT INTO brokerworksfor (brokerid, companyid, clientid) VALUES (112, NULL, 111);
INSERT INTO brokerworksfor (brokerid, companyid, clientid) VALUES (112, NULL, 112);
INSERT INTO brokerworksfor (brokerid, companyid, clientid) VALUES (113, 212, NULL);
INSERT INTO brokerworksfor (brokerid, companyid, clientid) VALUES (113, 211, NULL);
INSERT INTO brokerworksfor (brokerid, companyid, clientid) VALUES (114, 212, NULL);
INSERT INTO brokerworksfor (brokerid, companyid, clientid) VALUES (114, NULL, 111);


--
-- TOC entry 1948 (class 0 OID 1208200)
-- Dependencies: 164
-- Data for Name: client; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO client (id) VALUES (111);
INSERT INTO client (id) VALUES (112);


--
-- TOC entry 1950 (class 0 OID 1208206)
-- Dependencies: 166
-- Data for Name: company; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO company (id, name, marketshares, networth, addressid) VALUES (211, 'General Motors', 25000000, 1234.56780000000003, 995);
INSERT INTO company (id, name, marketshares, networth, addressid) VALUES (212, 'GnA Investments', 100000, 1234.56780000000003, 996);


--
-- TOC entry 1946 (class 0 OID 1208194)
-- Dependencies: 162
-- Data for Name: person; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO person (id, name, lastname, dateofbirth, ssn, addressid) VALUES (111, 'John', 'Smith', '1950-03-21', 'JSRX229500321', 991);
INSERT INTO person (id, name, lastname, dateofbirth, ssn, addressid) VALUES (112, 'Joana', 'Lopatenkko', '1970-07-14', 'JLPTK54992', 992);
INSERT INTO person (id, name, lastname, dateofbirth, ssn, addressid) VALUES (113, 'Walter', 'Schmidt', '1968-09-03', 'WSCH9820783903', 993);
INSERT INTO person (id, name, lastname, dateofbirth, ssn, addressid) VALUES (114, 'Patricia', 'Lombrardi', '1975-02-22', 'PTLM8878767830', 997);


--
-- TOC entry 1951 (class 0 OID 1208209)
-- Dependencies: 167
-- Data for Name: stockbooklist; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO stockbooklist (date, stockid) VALUES ('2008-04-01', 661);
INSERT INTO stockbooklist (date, stockid) VALUES ('2008-04-02', 662);
INSERT INTO stockbooklist (date, stockid) VALUES ('2008-04-03', 663);
INSERT INTO stockbooklist (date, stockid) VALUES ('2008-04-04', 664);
INSERT INTO stockbooklist (date, stockid) VALUES ('2008-04-05', 665);
INSERT INTO stockbooklist (date, stockid) VALUES ('2008-04-06', 666);
INSERT INTO stockbooklist (date, stockid) VALUES ('2008-04-07', 667);
INSERT INTO stockbooklist (date, stockid) VALUES ('2008-04-08', 668);
INSERT INTO stockbooklist (date, stockid) VALUES ('2008-04-09', 669);


--
-- TOC entry 1952 (class 0 OID 1208212)
-- Dependencies: 168
-- Data for Name: stockinformation; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO stockinformation (id, numberofshares, sharetype, companyid, description) VALUES (661, 100, false, 211, 'Text description 1');
INSERT INTO stockinformation (id, numberofshares, sharetype, companyid, description) VALUES (660, 100, false, 211, 'Text description 2');
INSERT INTO stockinformation (id, numberofshares, sharetype, companyid, description) VALUES (662, 100, false, 211, 'Text description 3');
INSERT INTO stockinformation (id, numberofshares, sharetype, companyid, description) VALUES (663, 100, false, 211, 'Text description 4');
INSERT INTO stockinformation (id, numberofshares, sharetype, companyid, description) VALUES (664, 100, false, 211, 'Text description 5');
INSERT INTO stockinformation (id, numberofshares, sharetype, companyid, description) VALUES (665, 100, true, 211, 'Testo di descrizione 1');
INSERT INTO stockinformation (id, numberofshares, sharetype, companyid, description) VALUES (666, 100, true, 211, 'Testo di descrizione 2');
INSERT INTO stockinformation (id, numberofshares, sharetype, companyid, description) VALUES (667, 100, true, 211, 'Testo di descrizione 3');
INSERT INTO stockinformation (id, numberofshares, sharetype, companyid, description) VALUES (669, 100, true, 211, 'Testo di descrizione 4');
INSERT INTO stockinformation (id, numberofshares, sharetype, companyid, description) VALUES (668, 100, true, 211, 'Testo di descrizione 5');


--
-- TOC entry 1953 (class 0 OID 1208218)
-- Dependencies: 169
-- Data for Name: transaction; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO transaction (id, date, stockid, type, brokerid, forclientid, forcompanyid, amount) VALUES (3331, '2008-04-01 00:00:00', 661, true, 112, 111, NULL, 12.6000);
INSERT INTO transaction (id, date, stockid, type, brokerid, forclientid, forcompanyid, amount) VALUES (3332, '2008-04-02 00:00:00', 662, true, 112, 111, NULL, 108.3400);
INSERT INTO transaction (id, date, stockid, type, brokerid, forclientid, forcompanyid, amount) VALUES (3333, '2008-04-03 00:00:00', 663, true, 112, NULL, 212, -2.3490);
INSERT INTO transaction (id, date, stockid, type, brokerid, forclientid, forcompanyid, amount) VALUES (3334, '2008-04-14 00:00:00', 663, true, 113, NULL, NULL, 1667.0092);



--
-- TOC entry 1815 (class 2606 OID 1208256)
-- Name: address_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY address
    ADD CONSTRAINT address_pkey PRIMARY KEY (id);


--
-- TOC entry 1819 (class 2606 OID 1208244)
-- Name: broker_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY broker
    ADD CONSTRAINT broker_pkey PRIMARY KEY (id);


--
-- TOC entry 1821 (class 2606 OID 1208242)
-- Name: client_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY client
    ADD CONSTRAINT client_pkey PRIMARY KEY (id);


--
-- TOC entry 1823 (class 2606 OID 1208224)
-- Name: company_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY company
    ADD CONSTRAINT company_pkey PRIMARY KEY (id);


--
-- TOC entry 1817 (class 2606 OID 1208222)
-- Name: person_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY person
    ADD CONSTRAINT person_pkey PRIMARY KEY (id);


--
-- TOC entry 1825 (class 2606 OID 1208226)
-- Name: stockbooklist_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY stockbooklist
    ADD CONSTRAINT stockbooklist_pkey PRIMARY KEY (date);


--
-- TOC entry 1827 (class 2606 OID 1208228)
-- Name: stockinformation_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY stockinformation
    ADD CONSTRAINT stockinformation_pkey PRIMARY KEY (id);


--
-- TOC entry 1829 (class 2606 OID 1208230)
-- Name: transaction_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY transaction
    ADD CONSTRAINT transaction_pkey PRIMARY KEY (id);


--
-- TOC entry 1837 (class 2606 OID 1208262)
-- Name: fk_address_company_pkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY company
    ADD CONSTRAINT fk_address_company_pkey FOREIGN KEY (addressid) REFERENCES address(id);


--
-- TOC entry 1830 (class 2606 OID 1208257)
-- Name: fk_address_pkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY person
    ADD CONSTRAINT fk_address_pkey FOREIGN KEY (addressid) REFERENCES address(id);


--
-- TOC entry 1833 (class 2606 OID 1208245)
-- Name: fk_broker_pkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY brokerworksfor
    ADD CONSTRAINT fk_broker_pkey FOREIGN KEY (brokerid) REFERENCES broker(id);


--
-- TOC entry 1841 (class 2606 OID 1208272)
-- Name: fk_broker_transaction_pkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY transaction
    ADD CONSTRAINT fk_broker_transaction_pkey FOREIGN KEY (brokerid) REFERENCES broker(id);


--
-- TOC entry 1834 (class 2606 OID 1208292)
-- Name: fk_brokerworksfor_brokerid_pkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY brokerworksfor
    ADD CONSTRAINT fk_brokerworksfor_brokerid_pkey FOREIGN KEY (brokerid) REFERENCES broker(id);


--
-- TOC entry 1836 (class 2606 OID 1208302)
-- Name: fk_brokerworksfor_clientid_pkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY brokerworksfor
    ADD CONSTRAINT fk_brokerworksfor_clientid_pkey FOREIGN KEY (clientid) REFERENCES client(id);


--
-- TOC entry 1835 (class 2606 OID 1208297)
-- Name: fk_brokerworksfor_companyid_pkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY brokerworksfor
    ADD CONSTRAINT fk_brokerworksfor_companyid_pkey FOREIGN KEY (companyid) REFERENCES company(id);


--
-- TOC entry 1839 (class 2606 OID 1208250)
-- Name: fk_company_pkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY stockinformation
    ADD CONSTRAINT fk_company_pkey FOREIGN KEY (companyid) REFERENCES company(id);


--
-- TOC entry 1842 (class 2606 OID 1208277)
-- Name: fk_forclientid_pkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY transaction
    ADD CONSTRAINT fk_forclientid_pkey FOREIGN KEY (forclientid) REFERENCES client(id);


--
-- TOC entry 1843 (class 2606 OID 1208282)
-- Name: fk_forcompanyid_pkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY transaction
    ADD CONSTRAINT fk_forcompanyid_pkey FOREIGN KEY (forcompanyid) REFERENCES company(id);


--
-- TOC entry 1831 (class 2606 OID 1208236)
-- Name: fk_person_broker_pkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY broker
    ADD CONSTRAINT fk_person_broker_pkey FOREIGN KEY (id) REFERENCES person(id);


--
-- TOC entry 1832 (class 2606 OID 1208231)
-- Name: fk_person_client_pkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY client
    ADD CONSTRAINT fk_person_client_pkey FOREIGN KEY (id) REFERENCES person(id);


--
-- TOC entry 1838 (class 2606 OID 1208287)
-- Name: fk_stockid_pkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY stockbooklist
    ADD CONSTRAINT fk_stockid_pkey FOREIGN KEY (stockid) REFERENCES stockinformation(id);


--
-- TOC entry 1840 (class 2606 OID 1208267)
-- Name: fk_stockinformation_pkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY transaction
    ADD CONSTRAINT fk_stockinformation_pkey FOREIGN KEY (stockid) REFERENCES stockinformation(id);




-- Completed on 2017-01-20 11:38:09 CET

--
-- PostgreSQL database dump complete
--

