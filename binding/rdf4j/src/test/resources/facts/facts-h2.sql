CREATE TABLE company (
    id integer NOT NULL,
    name character varying(100),
    founding_year integer
);

INSERT INTO company VALUES (1, 'Big Company', 2013);
INSERT INTO company VALUES (2, 'Some Factory', 1970);


ALTER TABLE company
    ADD CONSTRAINT company_pkey PRIMARY KEY (id);

