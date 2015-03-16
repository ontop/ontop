CREATE TABLE wellboreSpain (
    ids integer NOT NULL,
    name character varying(100)

);


CREATE TABLE wellboreFinland (
    idf integer NOT NULL,
    name character varying(100),
    depth integer

);

CREATE TABLE namesMap (
    id integer NOT NULL,
    idspain integer NOT NULL,
    idfinland integer NOT NULL

);

INSERT INTO wellboreSpain VALUES (991, 'Amerigo');
INSERT INTO wellboreSpain VALUES (992, 'Luis');
INSERT INTO wellboreSpain VALUES (993, 'Sagrada Familia');
INSERT INTO wellboreFinland VALUES (1, 'Aleksi', 13);
INSERT INTO wellboreFinland VALUES (2, 'Eljas', 100);

INSERT INTO namesMap VALUES (0, 991, 1);


ALTER TABLE wellboreSpain
    ADD CONSTRAINT wellboreSpain_pkey PRIMARY KEY (ids);

ALTER TABLE wellboreFinland
    ADD CONSTRAINT wellboreFinland_pkey PRIMARY KEY (idf);

ALTER TABLE namesMap
    ADD CONSTRAINT namesMap_pkey PRIMARY KEY (id);


