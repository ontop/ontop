CREATE TABLE DS1 (
    wid integer NOT NULL,
    WellboreName character varying(100),
    Active character varying(100),
    OwnerID integer

);


CREATE TABLE DS2 (
    wid integer NOT NULL,
    WellboreName character varying(100),
    Active character varying(100),
    Well character varying(100)

);

CREATE TABLE DS3 (
    wid integer NOT NULL,
    WellboreName character varying(100),
    Depth integer,
    Happy character varying(100)

);

CREATE TABLE DS4 (
    oid integer NOT NULL
);

CREATE TABLE TsameAs12 (
    wid1 integer NOT NULL,
    wid2 integer NOT NULL
);

CREATE TABLE TsameAs23 (
    wid2 integer NOT NULL,
    wid3 integer NOT NULL
);

CREATE TABLE TsameAs13 (
    wid1 integer NOT NULL,
    wid3 integer NOT NULL
);

CREATE TABLE TsameAs14 (
    oid1 integer NOT NULL,
    oid4 integer NOT NULL
);

INSERT INTO DS1 VALUES (1, 'GreenPeace', false, 4);
INSERT INTO DS1 VALUES (2, 'BigBucks', true, 7);

INSERT INTO DS2 VALUES (1, null, false, 'A');
INSERT INTO DS2 VALUES (2, 'BucksBig', true, 'B');
INSERT INTO DS2 VALUES (3, 'FilthyRich', true, 'C');

INSERT INTO DS3 VALUES (1, 'BigBucks', 1, true);
INSERT INTO DS3 VALUES (2, 'GreenPeace', 2, false);
INSERT INTO DS3 VALUES (4, 'ColdBastard', 3, true);
INSERT INTO DS3 VALUES (3, 'FilthyRich', null, true);

INSERT INTO DS4 VALUES (1);
INSERT INTO DS4 VALUES (2);

INSERT INTO TsameAs12 VALUES (1, 1);
INSERT INTO TsameAs12 VALUES (2, 2);

INSERT INTO TsameAs23 VALUES (1,2);
INSERT INTO TsameAs23 VALUES (2,1);
INSERT INTO TsameAs23 VALUES (3,3);

INSERT INTO TsameAs13 VALUES (1, 2);
INSERT INTO TsameAs13 VALUES (2, 1);

INSERT INTO TsameAs14 VALUES (4, 1);
INSERT INTO TsameAs14 VALUES (7, 2);


ALTER TABLE DS1
    ADD CONSTRAINT DS1_pkey PRIMARY KEY (wid);

ALTER TABLE DS2
    ADD CONSTRAINT DS2_pkey PRIMARY KEY (wid);

ALTER TABLE DS3
    ADD CONSTRAINT DS3_pkey PRIMARY KEY (wid);

ALTER TABLE DS4
    ADD CONSTRAINT DS4_pkey PRIMARY KEY (oid);

ALTER TABLE TsameAs12
    ADD CONSTRAINT TsameAs12_pkey PRIMARY KEY (wid1);

ALTER TABLE TsameAs23
    ADD CONSTRAINT TsameAs23_pkey PRIMARY KEY (wid2);

ALTER TABLE TsameAs13
    ADD CONSTRAINT TsameAs13_pkey PRIMARY KEY (wid1);

ALTER TABLE TsameAs14
    ADD CONSTRAINT TsameAs14_pkey PRIMARY KEY (oid1);





