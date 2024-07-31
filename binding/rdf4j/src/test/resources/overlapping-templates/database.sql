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