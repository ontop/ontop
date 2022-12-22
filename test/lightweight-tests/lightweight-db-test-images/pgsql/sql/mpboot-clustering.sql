CREATE DATABASE "bootstrapper-clustering";

\connect "bootstrapper-clustering"

CREATE TABLE public.a (
    id integer NOT NULL,
    ca integer
);

COPY public.a (id, ca) FROM stdin;
1	1
2	1
3	1
4	2
5	2
6	2
\.

ALTER TABLE ONLY public.a
    ADD CONSTRAINT a_pkey PRIMARY KEY (id);