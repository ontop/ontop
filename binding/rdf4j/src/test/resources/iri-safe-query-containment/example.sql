CREATE TABLE t (t_id VARCHAR(100) primary key);
CREATE TABLE r (r_id VARCHAR(100) primary key,
    t_id VARCHAR(100) not null references t(t_id));
