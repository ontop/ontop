create table tutelle (
                         partner_id varchar unique not null,
                         tutelle_name text,
                         tutelle_category text);

insert into tutelle (partner_id, tutelle_name, tutelle_category) values ('x1', 'univ-1', 'univ');
insert into tutelle (partner_id, tutelle_name, tutelle_category) values ('x2', 'univ-1', 'univ');