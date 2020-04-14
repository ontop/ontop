--
--

CREATE TABLE broker (
    id integer NOT NULL
);

ALTER TABLE  broker
    ADD CONSTRAINT broker_pkey PRIMARY KEY (id);

INSERT INTO broker (id) VALUES (112);
INSERT INTO broker (id) VALUES (113);
INSERT INTO broker (id) VALUES (114);

