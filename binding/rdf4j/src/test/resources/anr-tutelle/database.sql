create table tutelle (
                         partner_id text unique not null,
                         tutelle_name text,
                         tutelle_category text);

create table category (tutelle_category text unique not null);

insert into tutelle (partner_id, tutelle_name, tutelle_category) values ('x1', 'univ-1', 'univ');
insert into tutelle (partner_id, tutelle_name, tutelle_category) values ('x2', 'univ-1', 'univ');

insert into category (tutelle_category) values ('univ');

ALTER TABLE tutelle ADD CONSTRAINT "FK_CAT" FOREIGN KEY (tutelle_category)
    REFERENCES category (tutelle_category) ;