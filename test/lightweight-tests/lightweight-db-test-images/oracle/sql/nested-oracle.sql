ALTER SESSION SET container=XEPDB1;

CREATE USER nested IDENTIFIED BY nested_xyz container=current;
GRANT CREATE SESSION TO nested;
GRANT CREATE TABLE TO nested;
ALTER USER nested QUOTA UNLIMITED ON users;

CREATE TABLE nested.company_data (
    id integer NOT NULL,
    days varchar2(1000),
    income varchar2(1000),
    workers varchar2(1000),
    managers varchar2(1000)
);

ALTER TABLE nested.company_data ADD CONSTRAINT "nested_pkey" PRIMARY KEY (id) ENABLE;

INSERT INTO nested.company_data VALUES (1,  '["2023-01-01 18:00:00", "2023-01-15 18:00:00", "2023-01-29 12:00:00"]', '[10000, 18000, 13000]', '[["Sam", "Cynthia"], ["Bob"], ["Jim"]]', '[{"firstName": "Mary", "lastName": "Jane", "age": 28}, {"firstName": "Carlos", "lastName": "Carlson", "age": 45}, {"firstName": "John", "lastName": "Moriarty", "age": 60}]');
INSERT INTO nested.company_data VALUES (2,  '["2023-02-12 18:00:00", "2023-02-26 18:00:00"]', '[14000, 0]', '[["Jim", "Cynthia"], []]', '[{"firstName": "Helena", "lastName": "of Troy"}, {"firstName": "Robert", "lastName": "Smith", "age": 48}]');
INSERT INTO nested.company_data VALUES (3,  '["2023-03-12 18:00:00", "2023-03-26 18:00:00"]', '[15000, 20000]', '[["Carl", "Bob", "Cynthia"], ["Jim", "Bob"]]', '[{"firstName": "Joseph", "lastName": "Grey"}, {"firstName": "Godfrey", "lastName": "Hamilton", "age": 59}]');
INSERT INTO nested.company_data VALUES (4,  '[]', '[]', NULL, '[]');
