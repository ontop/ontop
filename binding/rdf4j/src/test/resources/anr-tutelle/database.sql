create table tutelle (
                         partner_id text unique not null,
                         tutelle_name text,
                         tutelle_category text);

create table category (tutelle_category text unique not null, label_id text not null);

create table category_label (label_id text unique not null, label text not null);

insert into tutelle (partner_id, tutelle_name, tutelle_category) values ('x1', 'univ-1', 'univ');
insert into tutelle (partner_id, tutelle_name, tutelle_category) values ('x2', 'univ-1', 'univ');

insert into category (tutelle_category, label_id) values ('univ', 'l1');

insert into category_label (label_id, label) values ('l1', 'univ');

ALTER TABLE tutelle ADD CONSTRAINT fk_cat FOREIGN KEY (tutelle_category)
    REFERENCES category (tutelle_category) ;

ALTER TABLE category ADD CONSTRAINT fk_lab FOREIGN KEY (label_id)
    REFERENCES category_label (label_id)  ;