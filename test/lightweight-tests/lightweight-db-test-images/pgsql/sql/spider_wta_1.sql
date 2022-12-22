CREATE DATABASE spider_wta_1;

\connect spider_wta_1

CREATE TABLE matches (
    best_of integer,
    draw_size integer,
    loser_age double precision,
    loser_entry text,
    loser_hand text,
    loser_ht integer,
    loser_id integer,
    loser_ioc text,
    loser_name text,
    loser_rank integer,
    loser_rank_points integer,
    loser_seed integer,
    match_num integer,
    minutes integer,
    round text,
    score text,
    surface text,
    tourney_date integer,
    tourney_id text,
    tourney_level text,
    tourney_name text,
    winner_age double precision,
    winner_entry text,
    winner_hand text,
    winner_ht integer,
    winner_id integer,
    winner_ioc text,
    winner_name text,
    winner_rank integer,
    winner_rank_points integer,
    winner_seed integer,
    year integer
);


CREATE TABLE players (
    player_id integer NOT NULL,
    first_name text,
    last_name text,
    hand text,
    birth_date integer,
    country_code text
);


CREATE TABLE rankings (
    ranking_date integer,
    ranking integer,
    player_id integer,
    ranking_points integer,
    tours integer
);


ALTER TABLE ONLY players
    ADD CONSTRAINT players_pkey PRIMARY KEY (player_id);


ALTER TABLE ONLY matches
    ADD CONSTRAINT matches_loser_id_fkey FOREIGN KEY (loser_id) REFERENCES players(player_id);


ALTER TABLE ONLY matches
    ADD CONSTRAINT matches_winner_id_fkey FOREIGN KEY (winner_id) REFERENCES players(player_id);


ALTER TABLE ONLY rankings
    ADD CONSTRAINT rankings_player_id_fkey FOREIGN KEY (player_id) REFERENCES players(player_id);
