
CREATE TABLE "source1_municipalities" (
                                        m_id varchar(100) primary key NOT NULL,
                                        istat text NOT NULL,
                                        name_en text NOT NULL,
                                        name_it text NOT NULL,
                                        name_de text NOT NULL,
                                        population integer NULL,
                                        latitude float NULL,
                                        longitude float NULL,
                                        altitude float NULL
);

insert into "source1_municipalities" (m_id, istat, name_en, name_it, name_de, latitude) values ('212', 'ESTSTE', 'ee', 'eee', 'eeee', 1.5);

CREATE TABLE "source1_hospitality" (
                                     h_id varchar(100) primary key NOT NULL,
                                     name_en text NOT NULL,
                                     name_it text NULL,
                                     name_de text NOT NULL,
                                     telephone text NULL,
                                     email text NULL,
                                     h_type text NOT NULL,
                                     latitude float NULL,
                                     longitude float NULL,
                                     altitude float NULL,
                                     category text NULL,
                                     m_id text NOT NULL
);

insert into "source1_hospitality" (h_id, name_en, name_it, name_de, h_type, m_id) values ('aaa', 'Hotel 1', 'ee', 'eee', 'Camping', '212');

CREATE TABLE "source1_rooms" (
                               r_id varchar(100) NOT NULL,
                               name_en text NOT NULL,
                               name_de text NOT NULL,
                               name_it text NOT NULL,
                               room_units integer NULL,
                               r_type text NOT NULL,
                               capacity integer NULL,
                               description_de text NULL,
                               description_it text NULL,
                               h_id text NOT NULL
);

CREATE TABLE "source2_hotels" (
                                id varchar(100) primary key NOT NULL,
                                english text NOT NULL,
                                italian text NULL,
                                german text NOT NULL,
                                htype integer NULL,
                                lat float NOT NULL,
                                long float NOT NULL,
                                alt float NOT NULL,
                                cat text NOT NULL,
                                mun integer NOT NULL
);

CREATE TABLE "source2_accommodation" (
                                       id varchar(100) NOT NULL,
                                       english_title text NOT NULL,
                                       german_title text NOT NULL,
                                       italian_title text NOT NULL,
                                       acco_type integer NOT NULL,
                                       guest_nb integer NULL,
                                       german_description text NULL,
                                       italian_description text NULL,
                                       hotel text NOT NULL
);
