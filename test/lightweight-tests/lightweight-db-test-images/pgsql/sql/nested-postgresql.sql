CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE DATABASE nested;

\connect nested

CREATE TABLE company_data (
    id integer NOT NULL,
    days jsonb,
    income jsonb,
    workers jsonb,
    managers jsonb
);

ALTER TABLE ONLY company_data
    ADD CONSTRAINT company_data_pk PRIMARY KEY (id);

INSERT INTO company_data VALUES (1,  jsonb_build_array('2023-01-01 18:00:00', '2023-01-15 18:00:00', '2023-01-29 12:00:00'), jsonb_build_array(10000, 18000, 13000), jsonb_build_array(jsonb_build_array('Sam', 'Cynthia'), jsonb_build_array('Bob'), jsonb_build_array('Jim')), '[{"firstName": "Mary", "lastName": "Jane", "age": 28}, {"firstName": "Carlos", "lastName": "Carlson", "age": 45}, {"firstName": "John", "lastName": "Moriarty", "age": 60}]'::jsonb);
INSERT INTO company_data VALUES (2,  jsonb_build_array('2023-02-12 18:00:00', '2023-02-26 18:00:00'), jsonb_build_array(14000, 0), jsonb_build_array(jsonb_build_array('Jim', 'Cynthia'), jsonb_build_array()), '[{"firstName": "Helena", "lastName": "of Troy"}, {"firstName": "Robert", "lastName": "Smith", "age": 48}]'::jsonb);
INSERT INTO company_data VALUES (3,  jsonb_build_array('2023-03-12 18:00:00', '2023-03-26 18:00:00'), jsonb_build_array(15000, 20000), jsonb_build_array(jsonb_build_array('Carl', 'Bob', 'Cynthia'), jsonb_build_array('Jim', 'Bob')), '[{"firstName": "Joseph", "lastName": "Grey"}, {"firstName": "Godfrey", "lastName": "Hamilton", "age": 59}]'::jsonb);
INSERT INTO company_data VALUES (4,  '[]', '[]', NULL, '[]');

CREATE TABLE company_data_arrays (
    id integer NOT NULL,
    days timestamp[],
    income integer[],
    workers text[][],
    managers jsonb[]
);

ALTER TABLE ONLY company_data_arrays
    ADD CONSTRAINT company_data_arrays_pk PRIMARY KEY (id);

INSERT INTO company_data_arrays VALUES (1,  ARRAY['2023-01-01 18:00:00'::TIMESTAMP, '2023-01-15 18:00:00'::TIMESTAMP, '2023-01-29 12:00:00'::TIMESTAMP], ARRAY[10000, 18000, 13000], ARRAY[['Sam', 'Cynthia'], ['Bob', NULL], ['Jim', NULL]], ARRAY['{"firstName": "Mary", "lastName": "Jane", "age": 28}'::jsonb, '{"firstName": "Carlos", "lastName": "Carlson", "age": 45}'::jsonb, '{"firstName": "John", "lastName": "Moriarty", "age": 60}'::jsonb]);
INSERT INTO company_data_arrays VALUES (2,  ARRAY['2023-02-12 18:00:00'::TIMESTAMP, '2023-02-26 18:00:00'::TIMESTAMP], ARRAY[14000, 0], ARRAY[['Jim', 'Cynthia'], [NULL, NULL]], ARRAY['{"firstName": "Helena", "lastName": "of Troy"}'::jsonb, '{"firstName": "Robert", "lastName": "Smith", "age": 48}'::jsonb]);
INSERT INTO company_data_arrays VALUES (3,  ARRAY['2023-03-12 18:00:00'::TIMESTAMP, '2023-03-26 18:00:00'::TIMESTAMP], ARRAY[15000, 20000], ARRAY[['Carl', 'Bob', 'Cynthia'], ['Jim', 'Bob', NULL]], ARRAY['{"firstName": "Joseph", "lastName": "Grey"}'::jsonb, '{"firstName": "Godfrey", "lastName": "Hamilton", "age": 59}'::jsonb]);
INSERT INTO company_data_arrays VALUES (4,  ARRAY[]::TIMESTAMP[], ARRAY[]::integer[], NULL, ARRAY[]::jsonb[]);
