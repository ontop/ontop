--
-- PostgreSQL database dump
--

-- Dumped from database version 9.1.24
-- Dumped by pg_dump version 9.3.1
-- Started on 2017-03-07 10:05:58 CET

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- TOC entry 2157 (class 1262 OID 1086693)
-- Name: imdbobda; Type: DATABASE; Schema: -; Owner: -
--

CREATE DATABASE imdbobda WITH TEMPLATE = template0 ENCODING = 'UTF8' LC_COLLATE = 'en_US.UTF-8' LC_CTYPE = 'en_US.UTF-8';


\connect imdbobda

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- TOC entry 203 (class 3079 OID 11677)
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- TOC entry 2159 (class 0 OID 0)
-- Dependencies: 203
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

SET default_with_oids = false;

--
-- TOC entry 161 (class 1259 OID 1086944)
-- Name: aka_name; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE aka_name (
    id integer NOT NULL,
    person_id integer NOT NULL,
    name text NOT NULL,
    imdb_index character varying(12),
    name_pcode_cf character varying(5),
    name_pcode_nf character varying(5),
    surname_pcode character varying(5)
);


--
-- TOC entry 162 (class 1259 OID 1086950)
-- Name: aka_name_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE aka_name_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2160 (class 0 OID 0)
-- Dependencies: 162
-- Name: aka_name_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE aka_name_id_seq OWNED BY aka_name.id;


--
-- TOC entry 163 (class 1259 OID 1086952)
-- Name: aka_title; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE aka_title (
    id integer NOT NULL,
    movie_id integer NOT NULL,
    title text NOT NULL,
    imdb_index character varying(12),
    kind_id integer NOT NULL,
    production_year integer,
    phonetic_code character varying(5),
    episode_of_id integer,
    season_nr integer,
    episode_nr integer,
    note text
);


--
-- TOC entry 164 (class 1259 OID 1086958)
-- Name: aka_title_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE aka_title_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2161 (class 0 OID 0)
-- Dependencies: 164
-- Name: aka_title_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE aka_title_id_seq OWNED BY aka_title.id;


--
-- TOC entry 165 (class 1259 OID 1086960)
-- Name: cast_info; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE cast_info (
    id integer NOT NULL,
    person_id integer NOT NULL,
    movie_id integer NOT NULL,
    person_role_id integer,
    note text,
    nr_order integer,
    role_id integer NOT NULL
);


--
-- TOC entry 166 (class 1259 OID 1086966)
-- Name: cast_info_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE cast_info_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2162 (class 0 OID 0)
-- Dependencies: 166
-- Name: cast_info_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE cast_info_id_seq OWNED BY cast_info.id;


--
-- TOC entry 167 (class 1259 OID 1086968)
-- Name: char_name; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE char_name (
    id integer NOT NULL,
    name text NOT NULL,
    imdb_index character varying(12),
    imdb_id integer,
    name_pcode_nf character varying(5),
    surname_pcode character varying(5)
);


--
-- TOC entry 168 (class 1259 OID 1086974)
-- Name: char_name_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE char_name_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2163 (class 0 OID 0)
-- Dependencies: 168
-- Name: char_name_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE char_name_id_seq OWNED BY char_name.id;


--
-- TOC entry 169 (class 1259 OID 1086976)
-- Name: comp_cast_type; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE comp_cast_type (
    id integer NOT NULL,
    kind character varying(32) NOT NULL
);


--
-- TOC entry 170 (class 1259 OID 1086979)
-- Name: comp_cast_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE comp_cast_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2164 (class 0 OID 0)
-- Dependencies: 170
-- Name: comp_cast_type_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE comp_cast_type_id_seq OWNED BY comp_cast_type.id;


--
-- TOC entry 171 (class 1259 OID 1086981)
-- Name: company_name; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE company_name (
    id integer NOT NULL,
    name text NOT NULL,
    country_code character varying(255),
    imdb_id integer,
    name_pcode_nf character varying(5),
    name_pcode_sf character varying(5)
);


--
-- TOC entry 172 (class 1259 OID 1086987)
-- Name: company_name_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE company_name_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2165 (class 0 OID 0)
-- Dependencies: 172
-- Name: company_name_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE company_name_id_seq OWNED BY company_name.id;


--
-- TOC entry 173 (class 1259 OID 1086989)
-- Name: company_type; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE company_type (
    id integer NOT NULL,
    kind character varying(32) NOT NULL
);


--
-- TOC entry 174 (class 1259 OID 1086992)
-- Name: company_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE company_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2166 (class 0 OID 0)
-- Dependencies: 174
-- Name: company_type_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE company_type_id_seq OWNED BY company_type.id;


--
-- TOC entry 175 (class 1259 OID 1086994)
-- Name: complete_cast; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE complete_cast (
    id integer NOT NULL,
    movie_id integer,
    subject_id integer NOT NULL,
    status_id integer NOT NULL
);


--
-- TOC entry 176 (class 1259 OID 1086997)
-- Name: complete_cast_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE complete_cast_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2167 (class 0 OID 0)
-- Dependencies: 176
-- Name: complete_cast_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE complete_cast_id_seq OWNED BY complete_cast.id;


--
-- TOC entry 177 (class 1259 OID 1086999)
-- Name: info_type; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE info_type (
    id integer NOT NULL,
    info character varying(32) NOT NULL
);


--
-- TOC entry 178 (class 1259 OID 1087002)
-- Name: info_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE info_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2168 (class 0 OID 0)
-- Dependencies: 178
-- Name: info_type_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE info_type_id_seq OWNED BY info_type.id;


--
-- TOC entry 179 (class 1259 OID 1087004)
-- Name: keyword; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE keyword (
    id integer NOT NULL,
    keyword character varying(255) NOT NULL,
    phonetic_code character varying(5)
);


--
-- TOC entry 180 (class 1259 OID 1087007)
-- Name: keyword_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE keyword_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2169 (class 0 OID 0)
-- Dependencies: 180
-- Name: keyword_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE keyword_id_seq OWNED BY keyword.id;


--
-- TOC entry 181 (class 1259 OID 1087009)
-- Name: kind_type; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE kind_type (
    id integer NOT NULL,
    kind character varying(15) NOT NULL
);


--
-- TOC entry 182 (class 1259 OID 1087012)
-- Name: kind_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE kind_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2170 (class 0 OID 0)
-- Dependencies: 182
-- Name: kind_type_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE kind_type_id_seq OWNED BY kind_type.id;


--
-- TOC entry 183 (class 1259 OID 1087014)
-- Name: link_type; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE link_type (
    id integer NOT NULL,
    link character varying(32) NOT NULL
);


--
-- TOC entry 184 (class 1259 OID 1087017)
-- Name: link_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE link_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2171 (class 0 OID 0)
-- Dependencies: 184
-- Name: link_type_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE link_type_id_seq OWNED BY link_type.id;


--
-- TOC entry 185 (class 1259 OID 1087019)
-- Name: movie_companies; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE movie_companies (
    id integer NOT NULL,
    movie_id integer NOT NULL,
    company_id integer NOT NULL,
    company_type_id integer NOT NULL,
    note text
);


--
-- TOC entry 186 (class 1259 OID 1087025)
-- Name: movie_companies_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE movie_companies_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2172 (class 0 OID 0)
-- Dependencies: 186
-- Name: movie_companies_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE movie_companies_id_seq OWNED BY movie_companies.id;


--
-- TOC entry 187 (class 1259 OID 1087027)
-- Name: movie_info; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE movie_info (
    id integer NOT NULL,
    movie_id integer NOT NULL,
    info_type_id integer NOT NULL,
    info text NOT NULL,
    note text
);


--
-- TOC entry 188 (class 1259 OID 1087033)
-- Name: movie_info_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE movie_info_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2173 (class 0 OID 0)
-- Dependencies: 188
-- Name: movie_info_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE movie_info_id_seq OWNED BY movie_info.id;


--
-- TOC entry 189 (class 1259 OID 1087035)
-- Name: movie_info_idx; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE movie_info_idx (
    id integer NOT NULL,
    movie_id integer NOT NULL,
    info_type_id integer NOT NULL,
    info text NOT NULL,
    note text
);


--
-- TOC entry 190 (class 1259 OID 1087041)
-- Name: movie_info_idx_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE movie_info_idx_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2174 (class 0 OID 0)
-- Dependencies: 190
-- Name: movie_info_idx_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE movie_info_idx_id_seq OWNED BY movie_info_idx.id;


--
-- TOC entry 191 (class 1259 OID 1087043)
-- Name: movie_keyword; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE movie_keyword (
    id integer NOT NULL,
    movie_id integer NOT NULL,
    keyword_id integer NOT NULL
);


--
-- TOC entry 192 (class 1259 OID 1087046)
-- Name: movie_keyword_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE movie_keyword_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2175 (class 0 OID 0)
-- Dependencies: 192
-- Name: movie_keyword_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE movie_keyword_id_seq OWNED BY movie_keyword.id;


--
-- TOC entry 193 (class 1259 OID 1087048)
-- Name: movie_link; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE movie_link (
    id integer NOT NULL,
    movie_id integer NOT NULL,
    linked_movie_id integer NOT NULL,
    link_type_id integer NOT NULL
);


--
-- TOC entry 194 (class 1259 OID 1087051)
-- Name: movie_link_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE movie_link_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2176 (class 0 OID 0)
-- Dependencies: 194
-- Name: movie_link_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE movie_link_id_seq OWNED BY movie_link.id;


--
-- TOC entry 195 (class 1259 OID 1087053)
-- Name: name; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE name (
    id integer NOT NULL,
    name text NOT NULL,
    imdb_index character varying(12),
    imdb_id integer,
    name_pcode_cf character varying(5),
    name_pcode_nf character varying(5),
    surname_pcode character varying(5)
);


--
-- TOC entry 196 (class 1259 OID 1087059)
-- Name: name_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE name_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2177 (class 0 OID 0)
-- Dependencies: 196
-- Name: name_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE name_id_seq OWNED BY name.id;


--
-- TOC entry 197 (class 1259 OID 1087061)
-- Name: person_info; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE person_info (
    id integer NOT NULL,
    person_id integer NOT NULL,
    info_type_id integer NOT NULL,
    info text NOT NULL,
    note text
);


--
-- TOC entry 198 (class 1259 OID 1087067)
-- Name: person_info_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE person_info_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2178 (class 0 OID 0)
-- Dependencies: 198
-- Name: person_info_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE person_info_id_seq OWNED BY person_info.id;


--
-- TOC entry 199 (class 1259 OID 1087069)
-- Name: role_type; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE role_type (
    id integer NOT NULL,
    role character varying(32) NOT NULL
);


--
-- TOC entry 200 (class 1259 OID 1087072)
-- Name: role_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE role_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2179 (class 0 OID 0)
-- Dependencies: 200
-- Name: role_type_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE role_type_id_seq OWNED BY role_type.id;


--
-- TOC entry 201 (class 1259 OID 1087074)
-- Name: title; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE title (
    id integer NOT NULL,
    title text NOT NULL,
    imdb_index character varying(12),
    kind_id integer NOT NULL,
    production_year integer,
    imdb_id integer,
    phonetic_code character varying(5),
    episode_of_id integer,
    season_nr integer,
    episode_nr integer,
    series_years character varying(49)
);


--
-- TOC entry 202 (class 1259 OID 1087080)
-- Name: title_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE title_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2180 (class 0 OID 0)
-- Dependencies: 202
-- Name: title_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE title_id_seq OWNED BY title.id;


--
-- TOC entry 1914 (class 2604 OID 1087082)
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY aka_name ALTER COLUMN id SET DEFAULT nextval('aka_name_id_seq'::regclass);


--
-- TOC entry 1915 (class 2604 OID 1087083)
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY aka_title ALTER COLUMN id SET DEFAULT nextval('aka_title_id_seq'::regclass);


--
-- TOC entry 1916 (class 2604 OID 1087084)
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY cast_info ALTER COLUMN id SET DEFAULT nextval('cast_info_id_seq'::regclass);


--
-- TOC entry 1917 (class 2604 OID 1087085)
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY char_name ALTER COLUMN id SET DEFAULT nextval('char_name_id_seq'::regclass);


--
-- TOC entry 1918 (class 2604 OID 1087086)
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY comp_cast_type ALTER COLUMN id SET DEFAULT nextval('comp_cast_type_id_seq'::regclass);


--
-- TOC entry 1919 (class 2604 OID 1087087)
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY company_name ALTER COLUMN id SET DEFAULT nextval('company_name_id_seq'::regclass);


--
-- TOC entry 1920 (class 2604 OID 1087088)
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY company_type ALTER COLUMN id SET DEFAULT nextval('company_type_id_seq'::regclass);


--
-- TOC entry 1921 (class 2604 OID 1087089)
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY complete_cast ALTER COLUMN id SET DEFAULT nextval('complete_cast_id_seq'::regclass);


--
-- TOC entry 1922 (class 2604 OID 1087090)
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY info_type ALTER COLUMN id SET DEFAULT nextval('info_type_id_seq'::regclass);


--
-- TOC entry 1923 (class 2604 OID 1087091)
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY keyword ALTER COLUMN id SET DEFAULT nextval('keyword_id_seq'::regclass);


--
-- TOC entry 1924 (class 2604 OID 1087092)
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY kind_type ALTER COLUMN id SET DEFAULT nextval('kind_type_id_seq'::regclass);


--
-- TOC entry 1925 (class 2604 OID 1087093)
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY link_type ALTER COLUMN id SET DEFAULT nextval('link_type_id_seq'::regclass);


--
-- TOC entry 1926 (class 2604 OID 1087094)
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY movie_companies ALTER COLUMN id SET DEFAULT nextval('movie_companies_id_seq'::regclass);


--
-- TOC entry 1927 (class 2604 OID 1087095)
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY movie_info ALTER COLUMN id SET DEFAULT nextval('movie_info_id_seq'::regclass);


--
-- TOC entry 1928 (class 2604 OID 1087096)
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY movie_info_idx ALTER COLUMN id SET DEFAULT nextval('movie_info_idx_id_seq'::regclass);


--
-- TOC entry 1929 (class 2604 OID 1087097)
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY movie_keyword ALTER COLUMN id SET DEFAULT nextval('movie_keyword_id_seq'::regclass);


--
-- TOC entry 1930 (class 2604 OID 1087098)
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY movie_link ALTER COLUMN id SET DEFAULT nextval('movie_link_id_seq'::regclass);


--
-- TOC entry 1931 (class 2604 OID 1087099)
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY name ALTER COLUMN id SET DEFAULT nextval('name_id_seq'::regclass);


--
-- TOC entry 1932 (class 2604 OID 1087100)
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY person_info ALTER COLUMN id SET DEFAULT nextval('person_info_id_seq'::regclass);


--
-- TOC entry 1933 (class 2604 OID 1087101)
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY role_type ALTER COLUMN id SET DEFAULT nextval('role_type_id_seq'::regclass);


--
-- TOC entry 1934 (class 2604 OID 1087102)
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY title ALTER COLUMN id SET DEFAULT nextval('title_id_seq'::regclass);


--
-- TOC entry 1940 (class 2606 OID 1095068)
-- Name: aka_name_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY aka_name
    ADD CONSTRAINT aka_name_pkey PRIMARY KEY (id);


--
-- TOC entry 1945 (class 2606 OID 1095070)
-- Name: aka_title_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY aka_title
    ADD CONSTRAINT aka_title_pkey PRIMARY KEY (id);


--
-- TOC entry 1950 (class 2606 OID 1095072)
-- Name: cast_info_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY cast_info
    ADD CONSTRAINT cast_info_pkey PRIMARY KEY (id);


--
-- TOC entry 1955 (class 2606 OID 1095074)
-- Name: char_name_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY char_name
    ADD CONSTRAINT char_name_pkey PRIMARY KEY (id);


--
-- TOC entry 1957 (class 2606 OID 1095076)
-- Name: comp_cast_type_kind_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY comp_cast_type
    ADD CONSTRAINT comp_cast_type_kind_key UNIQUE (kind);


--
-- TOC entry 1959 (class 2606 OID 1095078)
-- Name: comp_cast_type_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY comp_cast_type
    ADD CONSTRAINT comp_cast_type_pkey PRIMARY KEY (id);


--
-- TOC entry 1964 (class 2606 OID 1095080)
-- Name: company_name_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY company_name
    ADD CONSTRAINT company_name_pkey PRIMARY KEY (id);


--
-- TOC entry 1966 (class 2606 OID 1095082)
-- Name: company_type_kind_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY company_type
    ADD CONSTRAINT company_type_kind_key UNIQUE (kind);


--
-- TOC entry 1968 (class 2606 OID 1095084)
-- Name: company_type_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY company_type
    ADD CONSTRAINT company_type_pkey PRIMARY KEY (id);


--
-- TOC entry 1971 (class 2606 OID 1095086)
-- Name: complete_cast_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY complete_cast
    ADD CONSTRAINT complete_cast_pkey PRIMARY KEY (id);


--
-- TOC entry 1973 (class 2606 OID 1095088)
-- Name: info_type_info_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY info_type
    ADD CONSTRAINT info_type_info_key UNIQUE (info);


--
-- TOC entry 1975 (class 2606 OID 1095090)
-- Name: info_type_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY info_type
    ADD CONSTRAINT info_type_pkey PRIMARY KEY (id);


--
-- TOC entry 1979 (class 2606 OID 1095092)
-- Name: keyword_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY keyword
    ADD CONSTRAINT keyword_pkey PRIMARY KEY (id);


--
-- TOC entry 1981 (class 2606 OID 1095094)
-- Name: kind_type_kind_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY kind_type
    ADD CONSTRAINT kind_type_kind_key UNIQUE (kind);


--
-- TOC entry 1983 (class 2606 OID 1095096)
-- Name: kind_type_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY kind_type
    ADD CONSTRAINT kind_type_pkey PRIMARY KEY (id);


--
-- TOC entry 1985 (class 2606 OID 1095098)
-- Name: link_type_link_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY link_type
    ADD CONSTRAINT link_type_link_key UNIQUE (link);


--
-- TOC entry 1987 (class 2606 OID 1095100)
-- Name: link_type_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY link_type
    ADD CONSTRAINT link_type_pkey PRIMARY KEY (id);


--
-- TOC entry 1991 (class 2606 OID 1095102)
-- Name: movie_companies_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY movie_companies
    ADD CONSTRAINT movie_companies_pkey PRIMARY KEY (id);


--
-- TOC entry 1999 (class 2606 OID 1095104)
-- Name: movie_info_idx_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY movie_info_idx
    ADD CONSTRAINT movie_info_idx_pkey PRIMARY KEY (id);


--
-- TOC entry 1994 (class 2606 OID 1095106)
-- Name: movie_info_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY movie_info
    ADD CONSTRAINT movie_info_pkey PRIMARY KEY (id);


--
-- TOC entry 2003 (class 2606 OID 1095108)
-- Name: movie_keyword_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY movie_keyword
    ADD CONSTRAINT movie_keyword_pkey PRIMARY KEY (id);


--
-- TOC entry 2006 (class 2606 OID 1095111)
-- Name: movie_link_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY movie_link
    ADD CONSTRAINT movie_link_pkey PRIMARY KEY (id);


--
-- TOC entry 2012 (class 2606 OID 1095113)
-- Name: name_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY name
    ADD CONSTRAINT name_pkey PRIMARY KEY (id);


--
-- TOC entry 2015 (class 2606 OID 1095117)
-- Name: person_info_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY person_info
    ADD CONSTRAINT person_info_pkey PRIMARY KEY (id);


--
-- TOC entry 2017 (class 2606 OID 1095120)
-- Name: role_type_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY role_type
    ADD CONSTRAINT role_type_pkey PRIMARY KEY (id);


--
-- TOC entry 2019 (class 2606 OID 1095122)
-- Name: role_type_role_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY role_type
    ADD CONSTRAINT role_type_role_key UNIQUE (role);


--
-- TOC entry 2024 (class 2606 OID 1095124)
-- Name: title_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY title
    ADD CONSTRAINT title_pkey PRIMARY KEY (id);


--
-- TOC entry 1935 (class 1259 OID 1095127)
-- Name: aka_name_idx_pcode; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX aka_name_idx_pcode ON aka_name USING btree (surname_pcode);


--
-- TOC entry 1936 (class 1259 OID 1095128)
-- Name: aka_name_idx_pcodecf; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX aka_name_idx_pcodecf ON aka_name USING btree (name_pcode_cf);


--
-- TOC entry 1937 (class 1259 OID 1095129)
-- Name: aka_name_idx_pcodenf; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX aka_name_idx_pcodenf ON aka_name USING btree (name_pcode_nf);


--
-- TOC entry 1938 (class 1259 OID 1095130)
-- Name: aka_name_idx_person; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX aka_name_idx_person ON aka_name USING btree (person_id);


--
-- TOC entry 1941 (class 1259 OID 1095131)
-- Name: aka_title_idx_epof; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX aka_title_idx_epof ON aka_title USING btree (episode_of_id);


--
-- TOC entry 1942 (class 1259 OID 1095132)
-- Name: aka_title_idx_movieid; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX aka_title_idx_movieid ON aka_title USING btree (movie_id);


--
-- TOC entry 1943 (class 1259 OID 1095133)
-- Name: aka_title_idx_pcode; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX aka_title_idx_pcode ON aka_title USING btree (phonetic_code);


--
-- TOC entry 1946 (class 1259 OID 1095134)
-- Name: cast_info_idx_cid; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX cast_info_idx_cid ON cast_info USING btree (person_role_id);


--
-- TOC entry 1947 (class 1259 OID 1095139)
-- Name: cast_info_idx_mid; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX cast_info_idx_mid ON cast_info USING btree (movie_id);


--
-- TOC entry 1948 (class 1259 OID 1095140)
-- Name: cast_info_idx_pid; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX cast_info_idx_pid ON cast_info USING btree (person_id);


--
-- TOC entry 1951 (class 1259 OID 1095141)
-- Name: char_name_idx_name; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX char_name_idx_name ON char_name USING btree (name);


--
-- TOC entry 1952 (class 1259 OID 1095142)
-- Name: char_name_idx_pcode; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX char_name_idx_pcode ON char_name USING btree (surname_pcode);


--
-- TOC entry 1953 (class 1259 OID 1095143)
-- Name: char_name_idx_pcodenf; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX char_name_idx_pcodenf ON char_name USING btree (name_pcode_nf);


--
-- TOC entry 1960 (class 1259 OID 1095144)
-- Name: company_name_idx_name; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX company_name_idx_name ON company_name USING btree (name);


--
-- TOC entry 1961 (class 1259 OID 1095145)
-- Name: company_name_idx_pcodenf; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX company_name_idx_pcodenf ON company_name USING btree (name_pcode_nf);


--
-- TOC entry 1962 (class 1259 OID 1095146)
-- Name: company_name_idx_pcodesf; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX company_name_idx_pcodesf ON company_name USING btree (name_pcode_sf);


--
-- TOC entry 1969 (class 1259 OID 1095147)
-- Name: complete_cast_idx_mid; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX complete_cast_idx_mid ON complete_cast USING btree (movie_id);


--
-- TOC entry 1976 (class 1259 OID 1095148)
-- Name: keyword_idx_keyword; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX keyword_idx_keyword ON keyword USING btree (keyword);


--
-- TOC entry 1977 (class 1259 OID 1095149)
-- Name: keyword_idx_pcode; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX keyword_idx_pcode ON keyword USING btree (phonetic_code);


--
-- TOC entry 1988 (class 1259 OID 1095150)
-- Name: movie_companies_idx_cid; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX movie_companies_idx_cid ON movie_companies USING btree (company_id);


--
-- TOC entry 1989 (class 1259 OID 1095151)
-- Name: movie_companies_idx_mid; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX movie_companies_idx_mid ON movie_companies USING btree (movie_id);


--
-- TOC entry 1995 (class 1259 OID 1095152)
-- Name: movie_info_idx_idx_info; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX movie_info_idx_idx_info ON movie_info_idx USING btree (info);


--
-- TOC entry 1996 (class 1259 OID 1095153)
-- Name: movie_info_idx_idx_infotypeid; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX movie_info_idx_idx_infotypeid ON movie_info_idx USING btree (info_type_id);


--
-- TOC entry 1997 (class 1259 OID 1095154)
-- Name: movie_info_idx_idx_mid; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX movie_info_idx_idx_mid ON movie_info_idx USING btree (movie_id);


--
-- TOC entry 1992 (class 1259 OID 1095155)
-- Name: movie_info_idx_mid; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX movie_info_idx_mid ON movie_info USING btree (movie_id);


--
-- TOC entry 2000 (class 1259 OID 1095156)
-- Name: movie_keyword_idx_keywordid; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX movie_keyword_idx_keywordid ON movie_keyword USING btree (keyword_id);


--
-- TOC entry 2001 (class 1259 OID 1095157)
-- Name: movie_keyword_idx_mid; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX movie_keyword_idx_mid ON movie_keyword USING btree (movie_id);


--
-- TOC entry 2004 (class 1259 OID 1095158)
-- Name: movie_link_idx_mid; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX movie_link_idx_mid ON movie_link USING btree (movie_id);


--
-- TOC entry 2007 (class 1259 OID 1095159)
-- Name: name_idx_name; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX name_idx_name ON name USING btree (name);


--
-- TOC entry 2008 (class 1259 OID 1095160)
-- Name: name_idx_pcode; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX name_idx_pcode ON name USING btree (surname_pcode);


--
-- TOC entry 2009 (class 1259 OID 1095161)
-- Name: name_idx_pcodecf; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX name_idx_pcodecf ON name USING btree (name_pcode_cf);


--
-- TOC entry 2010 (class 1259 OID 1095162)
-- Name: name_idx_pcodenf; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX name_idx_pcodenf ON name USING btree (name_pcode_nf);


--
-- TOC entry 2013 (class 1259 OID 1095163)
-- Name: person_info_idx_pid; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX person_info_idx_pid ON person_info USING btree (person_id);


--
-- TOC entry 2020 (class 1259 OID 1095164)
-- Name: title_idx_epof; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX title_idx_epof ON title USING btree (episode_of_id);


--
-- TOC entry 2021 (class 1259 OID 1095165)
-- Name: title_idx_pcode; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX title_idx_pcode ON title USING btree (phonetic_code);


--
-- TOC entry 2022 (class 1259 OID 1095166)
-- Name: title_idx_title; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX title_idx_title ON title USING btree (title);


--
-- TOC entry 2036 (class 2606 OID 1095167)
-- Name: company_id_exists; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY movie_companies
    ADD CONSTRAINT company_id_exists FOREIGN KEY (company_id) REFERENCES company_name(id);


--
-- TOC entry 2037 (class 2606 OID 1095172)
-- Name: company_type_id_exists; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY movie_companies
    ADD CONSTRAINT company_type_id_exists FOREIGN KEY (company_type_id) REFERENCES company_type(id);


--
-- TOC entry 2050 (class 2606 OID 1095177)
-- Name: episode_of_id_exists; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY title
    ADD CONSTRAINT episode_of_id_exists FOREIGN KEY (episode_of_id) REFERENCES title(id);


--
-- TOC entry 2026 (class 2606 OID 1095182)
-- Name: episode_of_id_exists; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY aka_title
    ADD CONSTRAINT episode_of_id_exists FOREIGN KEY (episode_of_id) REFERENCES aka_title(id);


--
-- TOC entry 2039 (class 2606 OID 1095187)
-- Name: info_type_id_exists; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY movie_info
    ADD CONSTRAINT info_type_id_exists FOREIGN KEY (info_type_id) REFERENCES info_type(id);


--
-- TOC entry 2041 (class 2606 OID 1095192)
-- Name: info_type_id_exists; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY movie_info_idx
    ADD CONSTRAINT info_type_id_exists FOREIGN KEY (info_type_id) REFERENCES info_type(id);


--
-- TOC entry 2048 (class 2606 OID 1095197)
-- Name: info_type_id_exists; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY person_info
    ADD CONSTRAINT info_type_id_exists FOREIGN KEY (info_type_id) REFERENCES info_type(id);


--
-- TOC entry 2043 (class 2606 OID 1095202)
-- Name: keyword_id_exists; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY movie_keyword
    ADD CONSTRAINT keyword_id_exists FOREIGN KEY (keyword_id) REFERENCES keyword(id);


--
-- TOC entry 2051 (class 2606 OID 1095207)
-- Name: kind_id_exists; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY title
    ADD CONSTRAINT kind_id_exists FOREIGN KEY (kind_id) REFERENCES kind_type(id);


--
-- TOC entry 2027 (class 2606 OID 1095212)
-- Name: kind_id_exists; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY aka_title
    ADD CONSTRAINT kind_id_exists FOREIGN KEY (kind_id) REFERENCES kind_type(id);


--
-- TOC entry 2045 (class 2606 OID 1095217)
-- Name: link_type_id_exists; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY movie_link
    ADD CONSTRAINT link_type_id_exists FOREIGN KEY (link_type_id) REFERENCES link_type(id);


--
-- TOC entry 2046 (class 2606 OID 1095222)
-- Name: linked_movie_id_exists; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY movie_link
    ADD CONSTRAINT linked_movie_id_exists FOREIGN KEY (linked_movie_id) REFERENCES title(id);


--
-- TOC entry 2028 (class 2606 OID 1095227)
-- Name: movie_id_exists; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY aka_title
    ADD CONSTRAINT movie_id_exists FOREIGN KEY (movie_id) REFERENCES title(id);


--
-- TOC entry 2029 (class 2606 OID 1095232)
-- Name: movie_id_exists; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY cast_info
    ADD CONSTRAINT movie_id_exists FOREIGN KEY (movie_id) REFERENCES title(id);


--
-- TOC entry 2033 (class 2606 OID 1095237)
-- Name: movie_id_exists; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY complete_cast
    ADD CONSTRAINT movie_id_exists FOREIGN KEY (movie_id) REFERENCES title(id);


--
-- TOC entry 2044 (class 2606 OID 1095242)
-- Name: movie_id_exists; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY movie_keyword
    ADD CONSTRAINT movie_id_exists FOREIGN KEY (movie_id) REFERENCES title(id);


--
-- TOC entry 2047 (class 2606 OID 1095247)
-- Name: movie_id_exists; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY movie_link
    ADD CONSTRAINT movie_id_exists FOREIGN KEY (movie_id) REFERENCES title(id);


--
-- TOC entry 2040 (class 2606 OID 1095252)
-- Name: movie_id_exists; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY movie_info
    ADD CONSTRAINT movie_id_exists FOREIGN KEY (movie_id) REFERENCES title(id);


--
-- TOC entry 2042 (class 2606 OID 1095257)
-- Name: movie_id_exists; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY movie_info_idx
    ADD CONSTRAINT movie_id_exists FOREIGN KEY (movie_id) REFERENCES title(id);


--
-- TOC entry 2038 (class 2606 OID 1095262)
-- Name: movie_id_exists; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY movie_companies
    ADD CONSTRAINT movie_id_exists FOREIGN KEY (movie_id) REFERENCES title(id);


--
-- TOC entry 2025 (class 2606 OID 1095267)
-- Name: person_id_exists; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY aka_name
    ADD CONSTRAINT person_id_exists FOREIGN KEY (person_id) REFERENCES name(id);


--
-- TOC entry 2030 (class 2606 OID 1095272)
-- Name: person_id_exists; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY cast_info
    ADD CONSTRAINT person_id_exists FOREIGN KEY (person_id) REFERENCES name(id);


--
-- TOC entry 2049 (class 2606 OID 1095277)
-- Name: person_id_exists; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY person_info
    ADD CONSTRAINT person_id_exists FOREIGN KEY (person_id) REFERENCES name(id);


--
-- TOC entry 2031 (class 2606 OID 1095282)
-- Name: person_role_id_exists; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY cast_info
    ADD CONSTRAINT person_role_id_exists FOREIGN KEY (person_role_id) REFERENCES char_name(id);


--
-- TOC entry 2032 (class 2606 OID 1095287)
-- Name: role_id_exists; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY cast_info
    ADD CONSTRAINT role_id_exists FOREIGN KEY (role_id) REFERENCES role_type(id);


--
-- TOC entry 2034 (class 2606 OID 1095292)
-- Name: status_id_exists; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY complete_cast
    ADD CONSTRAINT status_id_exists FOREIGN KEY (status_id) REFERENCES comp_cast_type(id);


--
-- TOC entry 2035 (class 2606 OID 1095297)
-- Name: subject_id_exists; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY complete_cast
    ADD CONSTRAINT subject_id_exists FOREIGN KEY (subject_id) REFERENCES comp_cast_type(id);


-- Completed on 2017-03-07 10:05:59 CET

--
-- PostgreSQL database dump complete
--

