CREATE TABLE company_data_arrays (
    id integer NOT NULL PRIMARY KEY,
    days super,
    income super,
    workers super,
    managers super
);

INSERT INTO company_data_arrays VALUES (1,  ARRAY('2023-01-01 18:00:00', '2023-01-15 18:00:00', '2023-01-29 12:00:00'), ARRAY(10000, 18000, 13000), ARRAY(ARRAY('Sam', 'Cynthia'), ARRAY('Bob'), ARRAY('Jim')), ARRAY('{"firstName": "Mary", "lastName": "Jane", "age": 28}', '{"firstName": "Carlos", "lastName": "Carlson", "age": 45}', '{"firstName": "John", "lastName": "Moriarty", "age": 60}'));
INSERT INTO company_data_arrays VALUES (2,  ARRAY('2023-02-12 18:00:00', '2023-02-26 18:00:00'), ARRAY(14000, 0), ARRAY(ARRAY('Jim', 'Cynthia'), ARRAY()), ARRAY('{"firstName": "Helena", "lastName": "of Troy"}', '{"firstName": "Robert", "lastName": "Smith", "age": 48}'));
INSERT INTO company_data_arrays VALUES (3,  ARRAY('2023-03-12 18:00:00', '2023-03-26 18:00:00'), ARRAY(15000, 20000), ARRAY(ARRAY('Carl', 'Bob', 'Cynthia'), ARRAY('Jim', 'Bob')), ARRAY('{"firstName": "Joseph", "lastName": "Grey"}', '{"firstName": "Godfrey", "lastName": "Hamilton", "age": 59}'));
INSERT INTO company_data_arrays VALUES (4,  ARRAY(), ARRAY(), NULL, ARRAY());

GRANT SELECT ON company_data_arrays TO "IAM:ontop-athena";