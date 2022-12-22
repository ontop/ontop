CREATE DATABASE spider_orchestra;

\connect spider_orchestra

CREATE TABLE conductor (
    conductor_id integer NOT NULL,
    name text,
    age integer,
    nationality text,
    year_of_work integer
);


CREATE TABLE orchestra (
    orchestra_id integer NOT NULL,
    orchestra text,
    conductor_id integer,
    record_company text,
    year_of_founded real,
    major_record_format text
);

CREATE TABLE performance (
    performance_id integer NOT NULL,
    orchestra_id integer,
    type text,
    date text,
    official_ratings_millions real,
    weekly_rank text,
    share text
);

CREATE TABLE show (
    show_id integer,
    performance_id integer,
    result text,
    if_first_show boolean,
    attendance real
);

COPY conductor (conductor_id, name, age, nationality, year_of_work) FROM stdin;
1	Antal Doráti	40	USA	10
2	Igor Stravinsky	41	UK	11
3	Colin Davis	42	USA	6
4	Paul Jorgensen	43	UK	11
5	Antal Brown	43	USA	20
6	Charles Dutoit	43	France	12
7	Gerard Schwarz	50	USA	20
8	Pierre Boulez	49	UK	11
9	Valeri Gergiev	47	USA	16
10	Robert Craft	63	UK	21
11	Seiji Ozawa	43	USA	10
12	Michael Tilson Thomas	42	France	12
\.

COPY orchestra (orchestra_id, orchestra, conductor_id, record_company, year_of_founded, major_record_format) FROM stdin;
1	London Symphony Orchestra	1	Mercury Records	2003	CD
2	Columbia Symphony Orchestra	2	Columbia Masterworks	2009	CD / LP
3	Royal Concertgebouw Orchestra	3	Philips	2008	CD
4	Royal Danish Orchestra	4	Kultur	2002	DVD
5	Detroit Symphony Orchestra	5	Decca Records	2002	CD
6	Montreal Symphony Orchestra	6	Decca Records	2004	CD
7	Seattle Symphony Orchestra	7	Delos Records	2002	CD
8	Chicago Symphony Orchestra	8	Deutsche Grammophon	2003	CD
9	Kirov Orchestra	9	Philips Classics Records	2008	CD
10	Philharmonia Orchestra	10	Koch Records / Naxos Records	2006	CD
11	Orchestre de Paris	11	EMI	2007	CD
12	San Francisco Symphony Orchestra	12	RCA	2008	CD
\.

COPY performance (performance_id, orchestra_id, type, date, official_ratings_millions, weekly_rank, share) FROM stdin;
1	1	Auditions 1	9 June	5.19999981	12	22.7%
2	2	Auditions 2	10 June	6.73000002	8	28.0%
3	3	Auditions 3	11 June	7.28000021	15	29.4%
4	4	Auditions 4	12 June	7.38999987	13	29.3%
5	5	Auditions 5	13 June	7.51000023	11	29.2%
11	11	Semi-final 1	14 June	8.35999966	9	34.0%
6	6	Semi-final 2	15 June	9.27999973	8	38.1%
7	7	Semi-final 3	16 June	9.28999996	7	40.9%
8	8	Live final	17 June	11.5799999	1	43.7%
9	9	Live final results	17 June	11.4499998	2	44.7%
10	10	Series average	2007	8.38000011	TBC	34%
\.

COPY show (show_id, performance_id, result, if_first_show, attendance) FROM stdin;
1	1	Glebe Park	t	1026
2	2	Fir Park	t	695
3	3	St. Mirren Park	f	555
4	4	St. Mirren Park	f	1925
5	5	Hampden Park	t	2431
\.

ALTER TABLE ONLY conductor
    ADD CONSTRAINT conductor_pkey PRIMARY KEY (conductor_id);

ALTER TABLE ONLY orchestra
    ADD CONSTRAINT orchestra_pkey PRIMARY KEY (orchestra_id);


ALTER TABLE ONLY performance
    ADD CONSTRAINT performance_pkey PRIMARY KEY (performance_id);

ALTER TABLE ONLY orchestra
    ADD CONSTRAINT orchestra_conductor_id_fkey FOREIGN KEY (conductor_id) REFERENCES conductor(conductor_id);

ALTER TABLE ONLY performance
    ADD CONSTRAINT performance_orchestra_id_fkey FOREIGN KEY (orchestra_id) REFERENCES orchestra(orchestra_id);

ALTER TABLE ONLY show
    ADD CONSTRAINT show_performance_id_fkey FOREIGN KEY (performance_id) REFERENCES performance(performance_id);