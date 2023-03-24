CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE DATABASE nested;

\connect nested

CREATE TABLE "person-xt" (
    id integer NOT NULL,
    ssn integer,
    fullname character varying,
    tags jsonb,
    friends jsonb
);

ALTER TABLE ONLY "person-xt"
    ADD CONSTRAINT "person-xt_pk" PRIMARY KEY (id);

INSERT INTO "person-xt" VALUES (1,
123,
'Mary Poppins',
'[111, 222, 333]',
'[{ "fname": "Alice","nickname": "Al","address": {"city": "Bolzano","street": "via Roma","number": "33"}},{ "fname": "Robert","nickname": "Bob","address":{"city": "Merano","street": "via Dante","number": "23"}}]');


INSERT INTO "person-xt" VALUES
(2,
1234,
'Roger Rabbit',
'[111, 222]',
'{ "fname": "Mickey", "lname": "Mouse"}'
);


INSERT INTO "person-xt" VALUES
(3,
23,
'Bob Loblaw',
NULL,
'[]'
);


INSERT INTO "person-xt" VALUES
(4,
24,
'Kenny McCormick',
'[]',
NULL
);


CREATE TABLE "person" (
    id integer NOT NULL,
    name character varying,
    publication jsonb,
    contribs jsonb
);

ALTER TABLE ONLY "person"
    ADD CONSTRAINT "person_pk" PRIMARY KEY (id);

INSERT INTO "person" VALUES (
	1,
  'Sanjay Ghemawat',
  '[ { "title": "The Google file system", "id": 1, "year": 2003, "venue":"SOSP", "editor": [ {"name": "M. Scott"}, {"name": "L. Peterson"} ] }, { "title": "Bigtable: A Distributed Storage System for Structured Data", "id": 2, "year": 2008, "venue":"ACM TOCS" , "editor": [ {"name": "M. Swift"} ] }, { "title": "MapReduce: Simplified Data Processing on Large Clusters", "id": 3, "year": 2004, "venue":"OSDI", "editor": [ {"name": "E. Brewer"}, {"name": "P. Chen"} ] }]',
  '[{"value": "Google File System"},{"value": "MapReduce "},{"value": "Bigtable "},{"value": "Spanner "}]'
);


INSERT INTO "person" VALUES (
	2,
  'Jeffrey Dean',
  '[ { "title": "Bigtable: A Distributed Storage System for Structured Data", "id": 2, "year": 2008, "venue":"ACM TOCS", "editor": [ {"name": "M. Swift"} ] }, { "title": "MapReduce: Simplified Data Processing on Large Clusters", "id": 3, "year": 2004, "venue":"OSDI", "editor": [ {"name": "E. Brewer"}, {"name": "P. Chen"} ] }, { "title": "Large Scale Distributed Deep Networks", "id": 4, "year": 2012, "venue":"NeurIPS", "editor": [ {"name": "P. Bartlett"}, {"name": "F. Pereira"}, {"name": "C. Burges"}, {"name": "L. Bottou"}, {"name": "K. Weinberger "} ] } ]',
  '[ {"value": "MapReduce "}, {"value": "Bigtable "}, {"value": "Spanner "}, {"value": "TensorFlow "} ]');


CREATE TABLE "person-xt-array" (
    id integer NOT NULL,
    ssn integer,
    fullname character varying,
    tags integer[],
    friends jsonb[]
);

ALTER TABLE ONLY "person-xt-array"
    ADD CONSTRAINT "person-xt-array_pk" PRIMARY KEY (id);

INSERT INTO "person-xt-array" VALUES (1,
123,
'Mary Poppins',
ARRAY[111, 222, 333],
ARRAY['{ "fname": "Alice","nickname": "Al","address": {"city": "Bolzano","street": "via Roma","number": "33"}}'::jsonb,'{ "fname": "Robert","nickname": "Bob","address":{"city": "Merano","street": "via Dante","number": "23"}}'::jsonb]);


INSERT INTO "person-xt-array" VALUES
(2,
1234,
'Roger Rabbit',
ARRAY[111, 222],
ARRAY[]::jsonb[]
);


INSERT INTO "person-xt-array" VALUES
(3,
23,
'Bob Loblaw',
NULL,
ARRAY[]::jsonb[]
);


INSERT INTO "person-xt-array" VALUES
(4,
24,
'Kenny McCormick',
ARRAY[]::integer[],
NULL
);


CREATE TABLE "person-array" (
    id integer NOT NULL,
    name character varying,
    publication jsonb[],
    contribs jsonb[]
);

ALTER TABLE ONLY "person-array"
    ADD CONSTRAINT "person-array_pk" PRIMARY KEY (id);

INSERT INTO "person-array" VALUES (
	1,
  'Sanjay Ghemawat',
  ARRAY['{ "title": "The Google file system", "id": 1, "year": 2003, "venue":"SOSP", "editor": [ {"name": "M. Scott"}, {"name": "L. Peterson"} ] }'::jsonb, '{ "title": "Bigtable: A Distributed Storage System for Structured Data", "id": 2, "year": 2008, "venue":"ACM TOCS" , "editor": [ {"name": "M. Swift"} ] }', '{ "title": "MapReduce: Simplified Data Processing on Large Clusters", "id": 3, "year": 2004, "venue":"OSDI", "editor": [ {"name": "E. Brewer"}, {"name": "P. Chen"} ] }'::jsonb],
  ARRAY['{"value": "Google File System"}'::jsonb,'{"value": "MapReduce "}'::jsonb,'{"value": "Bigtable "}'::jsonb,'{"value": "Spanner "}'::jsonb]
);


INSERT INTO "person-array" VALUES (
	2,
  'Jeffrey Dean',
  ARRAY['{ "title": "Bigtable: A Distributed Storage System for Structured Data", "id": 2, "year": 2008, "venue":"ACM TOCS", "editor": [ {"name": "M. Swift"} ] }'::jsonb, '{ "title": "MapReduce: Simplified Data Processing on Large Clusters", "id": 3, "year": 2004, "venue":"OSDI", "editor": [ {"name": "E. Brewer"}, {"name": "P. Chen"} ] }'::jsonb, '{ "title": "Large Scale Distributed Deep Networks", "id": 4, "year": 2012, "venue":"NeurIPS", "editor": [ {"name": "P. Bartlett"}, {"name": "F. Pereira"}, {"name": "C. Burges"}, {"name": "L. Bottou"}, {"name": "K. Weinberger "} ] }'::jsonb ],
  ARRAY['{"value": "MapReduce "}'::jsonb, '{"value": "Bigtable "}'::jsonb, '{"value": "Spanner "}'::jsonb, '{"value": "TensorFlow "}'::jsonb]);
