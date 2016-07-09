
CREATE TABLE regex_test (
    id integer,
    valuestr character varying(100)
);

INSERT INTO regex_test (id,valuestr) VALUES (1,'aaaabbbb');
INSERT INTO regex_test (id,valuestr) VALUES (2,'aaaabbb');
INSERT INTO regex_test (id,valuestr) VALUES (3,'aaaabb');
INSERT INTO regex_test (id,valuestr) VALUES (4,'aaaab');
INSERT INTO regex_test (id,valuestr) VALUES (5,'ababbaba');
INSERT INTO regex_test (id,valuestr) VALUES (6,'ababbabab');
