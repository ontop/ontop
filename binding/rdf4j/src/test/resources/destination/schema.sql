
CREATE TABLE "source1_municipalities" (
                                        m_id varchar(100) primary key NOT NULL,
                                        istat text UNIQUE NOT NULL,
                                        name_en text NOT NULL,
                                        name_it text NOT NULL,
                                        name_de text NOT NULL,
                                        population integer NULL,
                                        latitude float NULL,
                                        longitude float NULL,
                                        altitude float NULL
);

insert into "source1_municipalities" (m_id, istat, name_en, name_it, name_de, latitude) values ('212', 'ESTSTE', 'ee', 'eee', 'eeee', 1.5);
INSERT INTO "source1_municipalities" (m_id,istat,name_en,name_it,name_de,population,latitude,longitude,altitude) VALUES
	 ('A7CA017FF0424503827BCD0E552F4648','021069','Proves/Proveis','Proves','Proveis',266,46.4781,11.023,1420.0),
	 ('BB0043517A57498683B2F997B7B68D5F','021065','Ponte Gardena/Waidbruck','Ponte Gardena','Waidbruck',203,46.598,11.5317,470.0),
	 ('516EF5F9F7794997B874828DBE157E6E','021036','Glorenza/Glurns','Glorenza','Glurns',894,46.6711,10.5565,907.0),
	 ('A9DD8986C362423893A5047F9027A10E','021056','Naturno/Naturns','Naturno','Naturns',5732,46.6507,10.9957,528.0),
	 ('DFE9B747E3DE4BE49E048CC2D4A0CCDD','021116','Velturno/Feldthurns','Velturno','Feldthurns',2808,46.67,11.6001,851.0),
	 ('2B6B8F6A17534563ADFAE74D3D6AC8CA','021118','Senale-S. Felix/U.L. Frau i.W.-St. Felix','Senale-S. Felix','U.L. Frau i.W.-St. Felix',773,46.4927,11.1321,1279.0),
	 ('1292EAF0E46B49A4A56A75C0EEA4EDC3','021117','La Val','La Val','La Val',1338,46.6583,11.9215,1348.0),
	 ('9390298788614912B01D7C592D5FAD92','021068','Predoi/Prettau','Predoi','Prettau',576,47.0431,12.1155,1475.0),
	 ('2B138D40992744BDBD38F56B73F45183','021010','Brennero/Brenner','Brennero','Brenner',2132,47.0025,11.5054,1098.0),
	 ('07A38CBBB96D4550A6D55B48F22E224F','021114','Villandro/Villanders','Villandro','Villanders',1861,46.6324,11.5406,880.0);

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
                               r_id varchar(100) primary key NOT NULL,
                               name_en text NOT NULL,
                               name_de text NOT NULL,
                               name_it text NOT NULL,
                               room_units integer NULL,
                               r_type text NOT NULL,
                               capacity integer NULL,
                               description_de text NULL,
                               description_it text NULL,
                               h_id text NULL
);

CREATE TABLE "source2_hotels" (
                                id varchar(100) primary key NOT NULL,
                                english text NOT NULL,
                                italian text NOT NULL,
                                german text NOT NULL,
                                htype integer NULL,
                                lat float NOT NULL,
                                long float NOT NULL,
                                alt float NOT NULL,
                                cat text NOT NULL,
                                mun integer NOT NULL
);

CREATE TABLE "source2_accommodation" (
                                       id varchar(100) primary key NOT NULL,
                                       english_title text NULL,
                                       german_title text NOT NULL,
                                       italian_title text NOT NULL,
                                       acco_type integer NOT NULL,
                                       guest_nb integer NULL,
                                       german_description text NULL,
                                       italian_description text NULL,
                                       hotel text NOT NULL
);

CREATE TABLE "source3_measurement_types" (
                                           name text primary key NOT NULL,
                                           unit text NOT NULL,
                                           description text NULL,
                                           "statisticalType" text NULL
);
INSERT INTO "source3_measurement_types" (name,unit,description,"statisticalType") VALUES
	 ('PM10','g/km/h','polveri sottili','Mean'),
	 ('NOX','g/km/h','Ossidi di azoto',NULL),
	 ('temp_aria','[°C]','Temperatura dell’aria','Mean'),
	 ('umidita_rel','[%]','Umidità relativa dell’aria','Mean'),
	 ('umidita_abs','[g/m^3]','Umidità assoluta dell’aria','Mean'),
	 ('Ozono','µg/m³','Valori a 10 minuti','Mean'),
	 ('wind-direction','° ','Direzione del vento',NULL),
	 ('wind-speed','m/s','Velocità del vento',NULL),
	 ('water-temperature','°C','Temperatura acqua',NULL),
	 ('wind10m_speed','m/s',NULL,NULL);

CREATE TABLE "source3_weather_measurement" (
                                             id integer primary key NOT NULL,
                                             period integer NOT NULL,
                                             timestamp timestamp NOT NULL,
                                             name text NOT NULL,
                                             double_value float NOT NULL,
                                             platform_id integer NOT NULL
);
INSERT INTO "source3_weather_measurement" (id,period,timestamp,name,double_value,platform_id) VALUES
	 (201539,3600,'2019-01-07 23:00:00','water-temperature',11.5,2152),
	 (201984,3600,'2019-01-07 23:00:00','water-temperature',8.8,2147),
	 (201999,3600,'2019-01-07 23:00:00','water-temperature',12.6,2150),
	 (202236,3600,'2019-01-07 23:00:00','water-temperature',13.0,2164),
	 (202265,3600,'2019-01-07 23:00:00','water-temperature',11.0,2159),
	 (202268,3600,'2019-01-07 23:00:00','water-temperature',10.9,2141),
	 (202275,3600,'2019-01-07 23:00:00','water-temperature',12.6,2151),
	 (202326,3600,'2017-01-11 10:00:00','water-temperature',9.222743,2168),
	 (202692,3600,'2019-01-07 23:00:00','water-temperature',9.8,2144),
	 (203021,3600,'2019-01-07 23:00:00','water-temperature',11.3,2161);