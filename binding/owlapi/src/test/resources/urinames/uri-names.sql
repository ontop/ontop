CREATE TABLE zoos (
    name character varying(100),
);

CREATE TABLE entertainments (
    name character varying(100),
	city character varying(100)
);

CREATE TABLE everything (
    name character varying(100),
	city character varying(100)
);


INSERT INTO zoos (name) VALUES ('Berlin');
INSERT INTO everything (name, city) VALUES ('zoo', 'Berlin');
INSERT INTO entertainments (name, city) VALUES ('http://www.ontop.org/zoo-zoo-Berlin', 'Berlin');
INSERT INTO entertainments (name, city) VALUES ('http://www.ontop.org/other-activity-Berlin', 'Berlin');
