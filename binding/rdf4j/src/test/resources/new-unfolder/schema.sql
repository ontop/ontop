
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

INSERT INTO "source1_hospitality" (h_id, name_en, name_it, name_de, telephone, email, h_type, latitude, longitude, altitude, category, m_id) VALUES
    ('745EB990148B974EBB057DF103E5D7D3', 'Appartamento Conciapelli', 'Appartamento Conciapelli', 'Appartamento Conciapelli', '+39 348 2891561', 'p.risa@tiscali.it', 'Notdefined', 46.499543, 11.358798, 0.0, 'Not categorized', '50FCFD4334A04DB087C1FD10ED864018'),
    ('B5347F54471411D2A0C1444553540000', 'Haspingerhof - Specker', 'Haspingerhof - Specker', 'Haspingerhof - Specker', '+39 0474 948110', 'info@haspingerhof.com', 'Farm', 46.8369, 12.245, 1400.0, '3flowers', '0EB9C7C089F54852830D7ECE17A90C37'),
    ('5F74DCC404AAA52B318A12507A1F27F7', 'Camping Gisser Vitha Hotels', 'Camping Gisser Vitha Hotels', 'Camping Gisser Vitha Hotels', '+39 0474 569605', 'reception@hotelgisser.it', 'Camping', 46.807976, 11.812105, 778.0, '3stars', '1E84922B82234EE682A341531E1D1925'),
    ('7E84CCE874E35F150F0999BA43F3DDD1', 'Schlosshäuslhof', 'Schlosshäuslhof', 'Schlosshäuslhof', '+39 0474 476110', 'edith.mutschlechner@gmail.com', 'Farm', 46.772921, 11.895599, 930.0, '1flower', 'BCAFBACB231F410D832FAD3AB25C9A83'),
    ('EFF0FACBA54C11D1AD760020AFF92740', 'Residence Tuberis', 'Residence Tuberis', 'Residence Tuberis', '+39 0474 678488', 'info@tubris.com', 'HotelPension', 46.9191, 11.9547, 865.0, '3stars', '6A5FF36917FA48D2B1996B76C7AA8BC6'),
    ('2542F92A544811D2968200A0244EAF51', 'Birbamerhof', 'Maso Birbamerhof', 'Birbamerhof', '+39 0472 850152', 'anton.pliger@dnet.it', 'Farm', 46.6969, 11.6775, 1000.0, '1flower', '2B2B22E275734BB990DE4A3FC98C6A18'),
    ('5D57C90EFB0D25E9017671733FCCD542', 'Holiday house merano Appartamento Portici - Centro storico', 'Holiday house merano Appartamento Portici - Centro storico', 'Holiday house merano Appartamento Portici - Centro storico', '+39 3711 985442', 'info@holidayhousemerano.com', 'BedBreakfast', 46.671773, 11.163071, 324.0, '1sun', '418E5CC913764648802DD2BE30AD91AC'),
    ('A92A692C413911D483B90050BAC0A490', 'Residence Haus Waldner', 'Residence Waldner', 'Residence Haus Waldner', '+39 0473 447231', 'info@residencewaldner.it', 'HotelPension', 46.6431, 11.1472, 330.0, '2stars', '30F534A4D2FC4E78998EED517F788D7C'),
    ('0068C0F83421A072FF794944E4391DED', 'Aurturist Appartement 142', 'Aurturist Appartement 142', 'Aurturist Appartement 142', '+39 0474 946118', 'info@aurturist.com', 'BedBreakfast', 0.0, 0.0, 0.0, 'Not categorized', 'CCD5827195AD4CA98B17D837EAAA4723'),
    ('F1FF6082A38211D1925F00805A150B0B', 'Schneiderhof', 'Schneiderhof', 'Schneiderhof', '', 'florian.fauster@rolmail.net', 'Farm', 46.7373, 12.172, 1154.0, '2flowers', '01D4DD1D1DE545C38EDF9B74387DEFCE');



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
INSERT INTO "source2_hotels" (id,english,italian,german,htype,lat,long,alt,cat,mun) VALUES
    ('00048F4DEDEFA5F5235E241A895A9F38','Sulden Mountain Chalets','Sulden Mountain Chalets','Sulden Mountain Chalets',1,46.51768,10.597204,1900.0,'3suns',21095),
    ('001AE4C0FA0781A2CDD3750811DBDAEB','Apartment Haideblick','Appartamento Haideblick','App. Ferienwohnung Haideblick',2,46.766831,10.533657,1470.0,'2suns',21027),
    ('0316AB454F854E4989AD3A27D3571560','Haus Steiner','Haus Steiner','Haus Steiner',3,46.6225,10.5818,920.0,'1flower',21067),
    ('045B2B5A157B881DCDBB3B20F3137D94','Ansitz Vogelsang','Ansitz Vogelsang','Ansitz Vogelsang',4,46.62907,10.782926,730.0,'3suns',21093),
    ('0471FC2FAE5744EC9D66B4220D0E6F39','Mühlander-Hof','Mühlander-Hof','UAB Mühlander-Hof',2,46.8308,10.5108,1500.0,'2flowers',21027);



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

CREATE TABLE "source1_xyz" (
                             id integer primary key NOT NULL,
                             x text NOT NULL,
                             y text NOT NULL,
                             z text NOT NULL
);
INSERT INTO "source1_xyz"(id, x, y, z) VALUES
    (1, 'x', 'y', 'z');

CREATE TABLE "source2_xyz" (
                             id integer primary key NOT NULL,
                             x text NOT NULL,
                             y text NOT NULL,
                             z text NOT NULL
);
INSERT INTO "source2_xyz"(id, x, y, z) VALUES
    (1, 'x', 'y', 'z');

CREATE TABLE "rating" (
                         id varchar(250) primary key NOT NULL,
                         rating integer NOT NULL
);
INSERT INTO "rating"(id, rating) VALUES
    ('http://destination.example.org/data/source2/hotels/0471FC2FAE5744EC9D66B4220D0E6F39', 2),
    ('http://destination.example.org/data/municipality/021036', 1),
    ('http://destination.example.org/data/municipality/021010', 10),
    ('http://destination.example.org/data/municipality/021056', 9),
    ('http://destination.example.org/data/source2/hotels/001AE4C0FA0781A2CDD3750811DBDAEB', 5),
    ('http://destination.example.org/data/municipality/021068', 3),
    ('http://destination.example.org/data/municipality/021065', 8),
    ('http://destination.example.org/data/source1/hospitality/0471FC2FAE5744EC9D66B4220D0E6F39', 7),
    ('http://destination.example.org/data/municipality/021069', 10),
    ('http://destination.example.org/data/source1/hospitality/A92A692C413911D483B90050BAC0A490', 1);

CREATE TABLE "concat_test"(
                            id varchar(250) primary key NOT NULL,
                            associated_value varchar(250) NOT NULL
);
INSERT INTO "concat_test"(id, associated_value) VALUES
    (CONCAT('http://destination.example.org/data/test1/', '1022'), 'ciao'),
    (CONCAT('http://destination.example.org/data/test1/', '2002'), 'come'),
    (CONCAT('http://destination.example.org/data/test2/', '2121'), 'va'),
    (CONCAT('http://destination.example.org/data/test2/', '2000'), '?');
