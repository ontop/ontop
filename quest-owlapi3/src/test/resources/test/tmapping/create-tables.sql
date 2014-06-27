CREATE TABLE client (
    id integer NOT NULL,
    namePerson character varying(100) NOT NULL,
);


INSERT INTO client VALUES (111, 'Mark');
INSERT INTO client VALUES (112, 'Andrew');

ALTER TABLE client
    ADD CONSTRAINT table1_pkey PRIMARY KEY (id);



