CREATE TABLE measure (
    unite_code integer NOT NULL,
    unite_url character varying(100)
);

INSERT INTO measure VALUES (1, 'http://urlconstants.org/32');
INSERT INTO measure VALUES (2, 'http://urlconstants.org/25');

ALTER TABLE measure
    ADD CONSTRAINT "measure_pkey" PRIMARY KEY (unite_code);
