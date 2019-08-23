CREATE SCHEMA new;

CREATE TABLE new."old.test" (
    name character varying(100),
);

INSERT INTO new."old.test" (name) VALUES ('John Smith');
INSERT INTO new."old.test" (name) VALUES ('Cote D''ivore');
