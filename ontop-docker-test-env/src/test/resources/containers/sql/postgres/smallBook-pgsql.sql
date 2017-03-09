--
-- PostgreSQL database dump
--

-- Dumped from database version 9.1.24
-- Dumped by pg_dump version 9.3.1
-- Started on 2017-03-06 18:25:16 CET

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- TOC entry 1890 (class 1262 OID 1162823)
-- Name: smallBook; Type: DATABASE; Schema: -; Owner: -
--

CREATE DATABASE "smallBook" WITH TEMPLATE = template0 ENCODING = 'UTF8' LC_COLLATE = 'en_US.UTF-8' LC_CTYPE = 'en_US.UTF-8';


\connect "smallBook"

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- TOC entry 162 (class 3079 OID 11677)
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- TOC entry 1892 (class 0 OID 0)
-- Dependencies: 162
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

SET default_with_oids = false;

--
-- TOC entry 161 (class 1259 OID 1162824)
-- Name: books; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE books (
    id integer NOT NULL,
    title character varying(100),
    price integer,
    discount numeric,
    description character varying(100),
    lang character varying(100),
    publication_date timestamp with time zone
);


--
-- TOC entry 1885 (class 0 OID 1162824)
-- Dependencies: 161
-- Data for Name: books; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO books (id, title, price, discount, description, lang, publication_date) VALUES (1, 'SPARQL Tutorial', 43, 0.2, 'good', 'en', '2014-07-14 12:47:52+02');
INSERT INTO books (id, title, price, discount, description, lang, publication_date) VALUES (2, 'The Semantic Web', 23, 0.25, 'bad', 'en', '2011-12-08 12:30:00+01');
INSERT INTO books (id, title, price, discount, description, lang, publication_date) VALUES (3, 'Crime and Punishment', 34, 0.2, 'good', 'en', '2015-09-21 11:23:06+02');
INSERT INTO books (id, title, price, discount, description, lang, publication_date) VALUES (4, 'The Logic Book: Introduction, Second Edition', 10, 0.15, 'good', 'en', '1967-11-05 07:50:00+01');


--
-- TOC entry 1783 (class 2606 OID 1162831)
-- Name: books_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY books
    ADD CONSTRAINT books_pkey PRIMARY KEY (id);

ALTER TABLE books CLUSTER ON books_pkey;


-- Completed on 2017-03-06 18:25:16 CET

--
-- PostgreSQL database dump complete
--

