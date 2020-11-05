DROP TABLE IF EXISTS test;

CREATE TABLE test (
    name1 character varying(100)
);

INSERT INTO test (name1) VALUES ('ABABAB');
INSERT INTO test (name1) VALUES ('ABABABA');
INSERT INTO test (name1) VALUES ('AAAABBBB');
