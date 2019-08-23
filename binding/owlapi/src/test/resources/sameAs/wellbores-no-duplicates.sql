CREATE TABLE T1 (
    id integer NOT NULL,
    well character varying(100),
    name character varying(100),
);


CREATE TABLE T2 (
    id integer NOT NULL,
    well character varying(100),
    name character varying(100),
);

CREATE TABLE T3 (
    id integer NOT NULL,
    well character varying(100),
    name character varying(100),
);


CREATE TABLE T_can (
    can_id integer,
    can_prov INTEGER,
    id INTEGER NOT NULL,
    prov INTEGER NOT NULL
);



INSERT INTO T1 VALUES (1, 'GreenPeace', 'w11');
INSERT INTO T1 VALUES (3, 'BigBucks', 'w13');
INSERT INTO T1 VALUES (6, 'HealthyWell', 'w16');
INSERT INTO T1 VALUES (7, 'NoMore', 'w17');

INSERT INTO T2 VALUES (3, null, 'w23');
INSERT INTO T2 VALUES (4, 'BucksBig', 'w24');
INSERT INTO T2 VALUES (7, 'FilthyRich', 'w27');

INSERT INTO T3 VALUES (5, 'OtherWell', 'w35');
INSERT INTO T3 VALUES (6, 'TiredOne', 'w36');
INSERT INTO T3 VALUES (10, 'ColdBridge', 'w310');
INSERT INTO T3 VALUES (7, 'Heather', 'w37');

INSERT INTO T_can VALUES (1,1,1,1) ;
INSERT INTO T_can VALUES (1,1,3,2) ;
INSERT INTO T_can VALUES (1,1,5,4) ;
INSERT INTO T_can VALUES (4,2,4,2) ;
INSERT INTO T_can VALUES (4,2,6,3) ;
INSERT INTO T_can VALUES (6,1,6,1) ;
INSERT INTO T_can VALUES (6,1,10,3) ;
INSERT INTO T_can VALUES (7,1,7,1) ;
INSERT INTO T_can VALUES (7,1,7,2) ;
INSERT INTO T_can VALUES (7,1,7,3) ;



ALTER TABLE T1
    ADD CONSTRAINT T1_pkey PRIMARY KEY (id);

ALTER TABLE T2
    ADD CONSTRAINT T2_pkey PRIMARY KEY (id);

ALTER TABLE T3
    ADD CONSTRAINT T3_pkey PRIMARY KEY (id);

ALTER TABLE T_can
ADD CONSTRAINT T_can_pkey PRIMARY KEY (id, prov);

ALTER TABLE T_can
ADD CONSTRAINT T_can_pkey_2 UNIQUE (can_id, can_prov, prov);


