CREATE TABLE archiveobject (
   id IDENTITY PRIMARY KEY,
   financiallysupportedby character varying(100),
   title character varying(100),
   archivaldate character varying(100)
);

CREATE TABLE physicalobject (
   id IDENTITY PRIMARY KEY,
   financiallysupportedby character varying(100),
   title character varying(100),
   archivaldate character varying(100)
);

INSERT INTO archiveobject VALUES (1,'agency1','archive1','2016');

INSERT INTO archiveobject VALUES (2,'agency1','archive2', null);

INSERT INTO physicalobject  VALUES (1,'agency2','physical1','2016');

INSERT INTO physicalobject  VALUES (2,'agency2','physical2','2017');

