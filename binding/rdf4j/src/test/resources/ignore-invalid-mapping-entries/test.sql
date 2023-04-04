CREATE TABLE dogs(
    name varchar,
    age integer,
    breed varchar
);

CREATE TABLE cats(
    name varchar,
    age integer,
    breed varchar
);

CREATE TABLE turtles(
    name varchar,
    age integer
);

CREATE TABLE rabbits(
    name varchar,
    age integer
);

INSERT INTO dogs VALUES ('Layka', 3, 'Mongrel');
INSERT INTO dogs VALUES ('Iggy', 5, 'Boston Terrier');
INSERT INTO dogs VALUES ('Tessa', 2, 'Yorkshire Terrier');

INSERT INTO cats VALUES ('Mittens', 10, 'Turkish Angora');
INSERT INTO cats VALUES ('Shan', 4, 'Siamese');
INSERT INTO cats VALUES ('Grumpy', 7, 'Mixed');

INSERT INTO turtles VALUES ('Cassiopeia', 50);
INSERT INTO turtles VALUES ('Lemmy K', 8);

INSERT INTO rabbits VALUES ('Bugs', 14);
INSERT INTO rabbits VALUES ('White Rabbit', 100);