--
-- PostgreSQL database dump
--

-- Dumped from database version 9.1.24
-- Dumped by pg_dump version 9.3.1
-- Started on 2017-03-06 18:26:21 CET

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- TOC entry 1911 (class 1262 OID 1096885)
-- Name: dbconstraints; Type: DATABASE; Schema: -; Owner: -
--

CREATE DATABASE dbconstraints WITH TEMPLATE = template0 ENCODING = 'UTF8' LC_COLLATE = 'en_US.UTF-8' LC_CTYPE = 'en_US.UTF-8';


\connect dbconstraints

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- TOC entry 165 (class 3079 OID 11677)
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- TOC entry 1913 (class 0 OID 0)
-- Dependencies: 165
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

SET default_with_oids = false;

--
-- TOC entry 161 (class 1259 OID 1096886)
-- Name: Book; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE "Book" (
    bk_code integer NOT NULL,
    bk_title character varying(100)
);


--
-- TOC entry 164 (class 1259 OID 1096906)
-- Name: BookWriter; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE "BookWriter" (
    bk_code integer,
    wr_code integer
);


--
-- TOC entry 163 (class 1259 OID 1096896)
-- Name: Edition; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE "Edition" (
    ed_code integer NOT NULL,
    ed_year integer,
    bk_code integer
);


--
-- TOC entry 162 (class 1259 OID 1096891)
-- Name: Writer; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE "Writer" (
    wr_code integer NOT NULL,
    wr_name character varying(100)
);


--
-- TOC entry 1903 (class 0 OID 1096886)
-- Dependencies: 161
-- Data for Name: Book; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 1906 (class 0 OID 1096906)
-- Dependencies: 164
-- Data for Name: BookWriter; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 1905 (class 0 OID 1096896)
-- Dependencies: 163
-- Data for Name: Edition; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 1904 (class 0 OID 1096891)
-- Dependencies: 162
-- Data for Name: Writer; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 1794 (class 2606 OID 1096890)
-- Name: pk_book; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY "Book"
    ADD CONSTRAINT pk_book PRIMARY KEY (bk_code);


--
-- TOC entry 1798 (class 2606 OID 1096900)
-- Name: pk_edition; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY "Edition"
    ADD CONSTRAINT pk_edition PRIMARY KEY (ed_code);


--
-- TOC entry 1796 (class 2606 OID 1096895)
-- Name: pk_writer; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY "Writer"
    ADD CONSTRAINT pk_writer PRIMARY KEY (wr_code);


--
-- TOC entry 1799 (class 2606 OID 1096901)
-- Name: fk_book_edition; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY "Edition"
    ADD CONSTRAINT fk_book_edition FOREIGN KEY (bk_code) REFERENCES "Book"(bk_code);


--
-- TOC entry 1801 (class 2606 OID 1096914)
-- Name: fk_book_writer; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY "BookWriter"
    ADD CONSTRAINT fk_book_writer FOREIGN KEY (bk_code) REFERENCES "Book"(bk_code);


--
-- TOC entry 1800 (class 2606 OID 1096909)
-- Name: fk_writer_book; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY "BookWriter"
    ADD CONSTRAINT fk_writer_book FOREIGN KEY (wr_code) REFERENCES "Writer"(wr_code);


-- Completed on 2017-03-06 18:26:21 CET

--
-- PostgreSQL database dump complete
--

