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

CREATE TABLE W1 (
    id integer NOT NULL,
    name character varying(100),
    wellbore integer
);

CREATE TABLE T_can_link (
    can_id integer NOT NULL,
    can_prov INTEGER NOT NULL,
    id1 INTEGER,
    id2 INTEGER,
    id3 INTEGER
);

CREATE TABLE T_can_well_link (
    can_id integer NOT NULL,
    can_prov character varying(100) NOT NULL,
    id1 character varying(100),
    id2 character varying(100)
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

INSERT INTO W1 VALUES (1, 'GreenPeace', 1);
INSERT INTO W1 VALUES (2, 'BigBucks', 3);
INSERT INTO W1 VALUES (3, 'HealthyWell', 6);
INSERT INTO W1 VALUES (4, 'NoMore', 7);


INSERT INTO T_can_link VALUES (1,1,1,3,5) ;
INSERT INTO T_can_link VALUES (4,2,null,4,6) ;
INSERT INTO T_can_link VALUES (1,3,3,null,null) ;
INSERT INTO T_can_link VALUES (6,1,6,null, 10) ;
INSERT INTO T_can_link VALUES (7,1,7,7,7) ;

INSERT INTO T_can_well_link VALUES (1,1,'GreenPeace',null) ;
INSERT INTO T_can_well_link VALUES (2,1,'BigBucks',null) ;
INSERT INTO T_can_well_link VALUES (3,1,'HealthyWell',null) ;
INSERT INTO T_can_well_link VALUES (4,1,'NoMore',null) ;
INSERT INTO T_can_well_link VALUES (5,2, null, 'BucksBig' ) ;
INSERT INTO T_can_well_link VALUES (6,2, null, 'FilthyRich') ;
INSERT INTO T_can_well_link VALUES (7,2, null, 'OtherWell') ;
INSERT INTO T_can_well_link VALUES (8,2, null, 'TiredOne') ;
INSERT INTO T_can_well_link VALUES (9,2, null, 'ColdBridge') ;
INSERT INTO T_can_well_link VALUES (10,2, null, 'Heather') ;


ALTER TABLE T1
    ADD CONSTRAINT T1_pkey PRIMARY KEY (id);

ALTER TABLE T2
    ADD CONSTRAINT T2_pkey PRIMARY KEY (id);

ALTER TABLE T3
    ADD CONSTRAINT T3_pkey PRIMARY KEY (id);

ALTER TABLE T_can_link
    ADD CONSTRAINT T_can_link_pkey PRIMARY KEY (can_id,can_prov);

ALTER TABLE T_can_link
ADD CONSTRAINT T_can_pkey_12 UNIQUE (id1, id2);

ALTER TABLE T_can_link
ADD CONSTRAINT T_can_pkey_23 UNIQUE (id2, id3);

ALTER TABLE T_can_link
ADD CONSTRAINT T_can_pkey_13 UNIQUE (id1, id3);

ALTER TABLE T1 ADD FOREIGN KEY ( id ) REFERENCES T_can_link(id1) ;
ALTER TABLE T2 ADD FOREIGN KEY ( id ) REFERENCES T_can_link(id2) ;
ALTER TABLE T3 ADD FOREIGN KEY ( id ) REFERENCES T_can_link(id3) ;
