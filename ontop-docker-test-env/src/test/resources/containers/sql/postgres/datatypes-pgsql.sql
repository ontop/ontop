--
-- PostgreSQL database dump
--

-- Dumped from database version 9.1.24
-- Dumped by pg_dump version 9.3.1
-- Started on 2017-03-06 18:26:00 CET

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- TOC entry 1935 (class 1262 OID 1086654)
-- Name: datatypes; Type: DATABASE; Schema: -; Owner: -
--

CREATE DATABASE datatypes WITH TEMPLATE = template0 ENCODING = 'UTF8' LC_COLLATE = 'en_US.UTF-8' LC_CTYPE = 'en_US.UTF-8';


\connect datatypes

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- TOC entry 169 (class 3079 OID 11677)
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- TOC entry 1937 (class 0 OID 0)
-- Dependencies: 169
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

SET default_with_oids = false;

--
-- TOC entry 168 (class 1259 OID 1096429)
-- Name: Binaries; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE "Binaries" (
    id integer NOT NULL,
    type_bit bit(1),
    type_bitvarying bit varying
);


--
-- TOC entry 164 (class 1259 OID 1086673)
-- Name: Booleans; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE "Booleans" (
    id integer NOT NULL,
    type_boolean boolean
);


--
-- TOC entry 162 (class 1259 OID 1086660)
-- Name: Characters; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE "Characters" (
    id integer NOT NULL,
    type_varchar character varying(100),
    type_character character(100),
    type_text text,
    type_char "char",
    type_name name
);


--
-- TOC entry 163 (class 1259 OID 1086668)
-- Name: DateTimes; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE "DateTimes" (
    id integer NOT NULL,
    type_timestamp timestamp without time zone,
    type_timestamp_tz timestamp with time zone,
    type_date date,
    type_time time without time zone,
    type_time_tz time with time zone,
    type_interval interval
);


--
-- TOC entry 161 (class 1259 OID 1086655)
-- Name: Numerics; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE "Numerics" (
    id integer NOT NULL,
    type_smallint smallint,
    type_integer integer,
    type_bigint bigint,
    type_numeric numeric(16,5),
    type_real real,
    type_double double precision,
    type_serial integer NOT NULL,
    type_bigserial bigint NOT NULL
);


--
-- TOC entry 167 (class 1259 OID 1095397)
-- Name: Numeric_bigserial_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE "Numeric_bigserial_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 1938 (class 0 OID 0)
-- Dependencies: 167
-- Name: Numeric_bigserial_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE "Numeric_bigserial_seq" OWNED BY "Numerics".type_bigserial;


--
-- TOC entry 166 (class 1259 OID 1095390)
-- Name: Numeric_serial_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE "Numeric_serial_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 1939 (class 0 OID 0)
-- Dependencies: 166
-- Name: Numeric_serial_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE "Numeric_serial_seq" OWNED BY "Numerics".type_serial;


--
-- TOC entry 165 (class 1259 OID 1095382)
-- Name: Others; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE "Others" (
    id integer NOT NULL,
    type_bit bit(1),
    type_bitvarying bit varying,
    type_box box,
    type_bytea bytea,
    type_cidr cidr,
    type_circle circle,
    type_inet inet,
    type_lseg lseg,
    type_macaddr macaddr,
    type_money money,
    type_path path,
    type_point point,
    type_polygon polygon,
    type_uuid uuid,
    type_line line
);


--
-- TOC entry 1808 (class 2604 OID 1095392)
-- Name: type_serial; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY "Numerics" ALTER COLUMN type_serial SET DEFAULT nextval('"Numeric_serial_seq"'::regclass);


--
-- TOC entry 1809 (class 2604 OID 1095399)
-- Name: type_bigserial; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY "Numerics" ALTER COLUMN type_bigserial SET DEFAULT nextval('"Numeric_bigserial_seq"'::regclass);


--
-- TOC entry 1930 (class 0 OID 1096429)
-- Dependencies: 168
-- Data for Name: Binaries; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 1926 (class 0 OID 1086673)
-- Dependencies: 164
-- Data for Name: Booleans; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO "Booleans" (id, type_boolean) VALUES (1, true);
INSERT INTO "Booleans" (id, type_boolean) VALUES (2, false);


--
-- TOC entry 1924 (class 0 OID 1086660)
-- Dependencies: 162
-- Data for Name: Characters; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO "Characters" (id, type_varchar, type_character, type_text, type_char, type_name) VALUES (1, 'abc', 'a                                                                                                   ', 'abc', 'a', 'abc');


--
-- TOC entry 1925 (class 0 OID 1086668)
-- Dependencies: 163
-- Data for Name: DateTimes; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO "DateTimes" (id, type_timestamp, type_timestamp_tz, type_date, type_time, type_time_tz, type_interval) VALUES (1, '2013-03-18 10:12:10', '2013-03-19 03:12:10+01', '2013-03-18', '10:12:10', '18:12:10-08', '00:01:40');


--
-- TOC entry 1940 (class 0 OID 0)
-- Dependencies: 167
-- Name: Numeric_bigserial_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('"Numeric_bigserial_seq"', 2, true);


--
-- TOC entry 1941 (class 0 OID 0)
-- Dependencies: 166
-- Name: Numeric_serial_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('"Numeric_serial_seq"', 2, true);


--
-- TOC entry 1923 (class 0 OID 1086655)
-- Dependencies: 161
-- Data for Name: Numerics; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO "Numerics" (id, type_smallint, type_integer, type_bigint, type_numeric, type_real, type_double, type_serial, type_bigserial) VALUES (1, 1, 1, 1, 1.00000, 1, 1, 1, 1);


--
-- TOC entry 1927 (class 0 OID 1095382)
-- Dependencies: 165
-- Data for Name: Others; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 1821 (class 2606 OID 1096436)
-- Name: binary_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY "Binaries"
    ADD CONSTRAINT binary_pk PRIMARY KEY (id);


--
-- TOC entry 1817 (class 2606 OID 1086677)
-- Name: pk_boolean; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY "Booleans"
    ADD CONSTRAINT pk_boolean PRIMARY KEY (id);


--
-- TOC entry 1813 (class 2606 OID 1086667)
-- Name: pk_character; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY "Characters"
    ADD CONSTRAINT pk_character PRIMARY KEY (id);


--
-- TOC entry 1815 (class 2606 OID 1086672)
-- Name: pk_datetime; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY "DateTimes"
    ADD CONSTRAINT pk_datetime PRIMARY KEY (id);


--
-- TOC entry 1811 (class 2606 OID 1086659)
-- Name: pk_numeric; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY "Numerics"
    ADD CONSTRAINT pk_numeric PRIMARY KEY (id);


--
-- TOC entry 1819 (class 2606 OID 1095389)
-- Name: pk_others; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY "Others"
    ADD CONSTRAINT pk_others PRIMARY KEY (id);


-- Completed on 2017-03-06 18:26:00 CET

--
-- PostgreSQL database dump complete
--

