CREATE TABLE "prefix_http_www_ontorais_de_deliverable" (
   "id" IDENTITY PRIMARY KEY,
   "deliverabledate" character varying(100),
   "deliverableplanneddate" character varying(100),
   "deliverablenumber" character varying(100),
   "deliverabletype" character varying(100),
   "deliverablename" character varying(100)
);

CREATE TABLE "prefix_http_www_ontorais_de_report" (
   "id" IDENTITY PRIMARY KEY,
   "abstract" character varying(100),
   "note" character varying(100),
   "financiallysupportedby" character varying(100),
   "title" character varying(100),
   "archivaldate" character varying(100)
);

CREATE TABLE "prefix_http_www_ontorais_de_visual" (
   "id" IDENTITY PRIMARY KEY,
   "financiallysupportedby" character varying(100),
   "title" character varying(100),
   "archivaldate" character varying(100)
);

CREATE TABLE "prefix_http_www_ontorais_de_publication" (
   "id" IDENTITY PRIMARY KEY,
   "abstract" character varying(100),
   "note" character varying(100),
   "financiallysupportedby" character varying(100),
   "title" character varying(100),
   "archivaldate" character varying(100)
);

CREATE TABLE "prefix_http_www_ontorais_de_publisher" (
   "domain" integer,
   "val" character varying(100)
);

CREATE TABLE "prefix_http_www_ontorais_de_stakeholderrelation" (
   "id" IDENTITY PRIMARY KEY
);

CREATE TABLE "prefix_http_www_ontorais_de_stakeholderrightsstatement" (
   "id" IDENTITY PRIMARY KEY,
   "systemrightsstatementname" character varying(100)
);

CREATE TABLE "prefix_http_www_ontorais_de_softwaredescription" (
   "domain" integer,
   "val" character varying(100)
);

CREATE TABLE "prefix_http_www_ontorais_de_file" (
   "id" IDENTITY PRIMARY KEY,
   "filename" character varying(100)
);

CREATE TABLE "prefix_http_www_ontorais_de_sourcecode" (
   "id" IDENTITY PRIMARY KEY,
   "financiallysupportedby" character varying(100),
   "title" character varying(100),
   "archivaldate" character varying(100)
);

CREATE TABLE "prefix_http_www_ontorais_de_isresultof" (
   "domain" integer NOT NULL,
   "range" integer NOT NULL
);

CREATE TABLE "prefix_http_www_ontorais_de_email" (
   "domain" integer,
   "val" character varying(100)
);

CREATE TABLE "prefix_http_www_ontorais_de_book" (
   "id" IDENTITY PRIMARY KEY,
   "isbn" character varying(100),
   "publicationdate" character varying(100),
   "edition" character varying(100),
   "volume" character varying(100),
   "series" character varying(100),
   "abstract" character varying(100),
   "note" character varying(100),
   "financiallysupportedby" character varying(100),
   "title" character varying(100),
   "archivaldate" character varying(100)
);

CREATE TABLE "prefix_http_www_ontorais_de_project" (
   "id" IDENTITY PRIMARY KEY,
   "projecttitle" character varying(100),
   "projectabstract" character varying(100)
);

CREATE TABLE "prefix_http_www_ontorais_de_archiveobject" (
   "id" IDENTITY PRIMARY KEY,
   "financiallysupportedby" character varying(100),
   "title" character varying(100),
   "archivaldate" character varying(100)
);

CREATE TABLE "prefix_http_www_ontorais_de_milestone" (
   "id" IDENTITY PRIMARY KEY,
   "milestonename" character varying(100),
   "milestonearchiveddate" character varying(100),
   "milestoneestimateddate" character varying(100),
   "milestonenumber" character varying(100)
);

CREATE TABLE "prefix_http_www_ontorais_de_hasfile" (
   "domain" integer NOT NULL,
   "range" integer NOT NULL
);
CREATE TABLE "prefix_http_www_ontorais_de_executable" (
   "id" IDENTITY PRIMARY KEY,
   "financiallysupportedby" character varying(100),
   "title" character varying(100),
   "archivaldate" character varying(100)
);

CREATE TABLE "prefix_http_www_ontorais_de_masterthesis" (
   "id" IDENTITY PRIMARY KEY,
   "school" character varying(100),
   "abstract" character varying(100),
   "note" character varying(100),
   "financiallysupportedby" character varying(100),
   "title" character varying(100),
   "archivaldate" character varying(100)
);

CREATE TABLE "prefix_http_www_ontorais_de_organisation" (
   "id" IDENTITY PRIMARY KEY,
   "organisationname" character varying(100),
   "organisationdescription" character varying(100)
);

CREATE TABLE "prefix_http_www_ontorais_de_physicalobject" (
   "id" IDENTITY PRIMARY KEY,
   "financiallysupportedby" character varying(100),
   "title" character varying(100),
   "archivaldate" character varying(100)
);

CREATE TABLE "prefix_http_www_ontorais_de_agent" (
   "id" IDENTITY PRIMARY KEY
);

CREATE TABLE "prefix_http_www_ontorais_de_audiovisual" (
   "id" IDENTITY PRIMARY KEY,
   "description" character varying(100),
   "financiallysupportedby" character varying(100),
   "title" character varying(100),
   "archivaldate" character varying(100)
);

CREATE TABLE "prefix_http_www_ontorais_de_contributesto" (
   "domain" integer NOT NULL,
   "range" integer NOT NULL
);

CREATE TABLE "prefix_http_www_ontorais_de_digitalobject" (
   "id" IDENTITY PRIMARY KEY,
   "financiallysupportedby" character varying(100),
   "title" character varying(100),
   "archivaldate" character varying(100)
);

CREATE TABLE "prefix_http_www_ontorais_de_conference" (
   "domain" integer,
   "val" character varying(100)
);

CREATE TABLE "prefix_http_www_ontorais_de_user" (
   "id" IDENTITY PRIMARY KEY,
   "username" character varying(100),
   "password" character varying(100),
   "lastname" character varying(100),
   "firstname" character varying(100)
);

CREATE TABLE "prefix_http_www_ontorais_de_programminglanguage" (
   "domain" integer,
   "val" character varying(100)
);

CREATE TABLE "prefix_http_www_ontorais_de_character varying(100)" (
   "id" IDENTITY PRIMARY KEY,
   "financiallysupportedby" character varying(100),
   "title" character varying(100),
   "archivaldate" character varying(100)
);

CREATE TABLE "prefix_http_www_ontorais_de_isorganisationmember" (
   "domain" integer NOT NULL,
   "range" integer NOT NULL
);

CREATE TABLE "prefix_http_www_ontorais_de_person" (
   "id" IDENTITY PRIMARY KEY,
   "lastname" character varying(100),
   "firstname" character varying(100)
);

CREATE TABLE "prefix_http_www_ontorais_de_isstakeholder" (
   "domain" integer NOT NULL,
   "range" integer NOT NULL
);

CREATE TABLE "prefix_http_www_ontorais_de_projectfinaciallysupportedby" (
   "domain" integer,
   "val" character varying(100)
);

CREATE TABLE "prefix_http_www_ontorais_de_compilerdescription" (
   "domain" integer,
   "val" character varying(100)
);

CREATE TABLE "prefix_http_www_ontorais_de_isauthorof" (
   "domain" integer NOT NULL,
   "range" integer NOT NULL
);

CREATE TABLE "prefix_http_www_ontorais_de_proceedings" (
   "id" IDENTITY PRIMARY KEY,
   "abstract" character varying(100),
   "note" character varying(100),
   "financiallysupportedby" character varying(100),
   "title" character varying(100),
   "archivaldate" character varying(100)
);

CREATE TABLE "prefix_http_www_ontorais_de_hasprojectelement" (
   "domain" integer NOT NULL,
   "range" integer NOT NULL
);

CREATE TABLE "prefix_http_www_ontorais_de_audio" (
   "id" IDENTITY PRIMARY KEY,
   "description" character varying(100),
   "financiallysupportedby" character varying(100),
   "title" character varying(100),
   "archivaldate" character varying(100)
);

CREATE TABLE "prefix_http_www_ontorais_de_userrightsstatement" (
   "id" IDENTITY PRIMARY KEY,
   "systemrightsstatementname" character varying(100)
);

CREATE TABLE "prefix_http_www_ontorais_de_hasaccessright" (
   "domain" integer NOT NULL,
   "range" integer NOT NULL
);

CREATE TABLE "prefix_http_www_ontorais_de_hasstakeholderrights" (
   "domain" integer NOT NULL,
   "range" integer NOT NULL
);

CREATE TABLE "prefix_http_www_ontorais_de_software" (
   "id" IDENTITY PRIMARY KEY,
   "financiallysupportedby" character varying(100),
   "title" character varying(100),
   "archivaldate" character varying(100)
);

CREATE TABLE "prefix_http_www_ontorais_de_manual" (
   "id" IDENTITY PRIMARY KEY,
   "abstract" character varying(100),
   "note" character varying(100),
   "financiallysupportedby" character varying(100),
   "title" character varying(100),
   "archivaldate" character varying(100)
);

CREATE TABLE "prefix_http_www_ontorais_de_isprojectstakeholder" (
   "domain" integer NOT NULL,
   "range" integer NOT NULL
);

CREATE TABLE "prefix_http_www_ontorais_de_virtualmachine" (
   "id" IDENTITY PRIMARY KEY,
   "financiallysupportedby" character varying(100),
   "title" character varying(100),
   "archivaldate" character varying(100)
);

CREATE TABLE "prefix_http_www_ontorais_de_inproceedings" (
   "id" IDENTITY PRIMARY KEY,
   "abstract" character varying(100),
   "note" character varying(100),
   "financiallysupportedby" character varying(100),
   "title" character varying(100),
   "archivaldate" character varying(100)
);

CREATE TABLE "prefix_http_www_ontorais_de_inbook" (
   "id" IDENTITY PRIMARY KEY,
   "publicationdate" character varying(100),
   "series" character varying(100),
   "chapter" character varying(100),
   "booktitle" character varying(100),
   "isbn" character varying(100),
   "volume" character varying(100),
   "edition" character varying(100),
   "abstract" character varying(100),
   "note" character varying(100),
   "financiallysupportedby" character varying(100),
   "title" character varying(100),
   "archivaldate" character varying(100)
);

CREATE TABLE "prefix_http_www_ontorais_de_stakeholdertype" (
   "id" IDENTITY PRIMARY KEY,
   "stakeholdername" character varying(100)
);

CREATE TABLE "prefix_http_www_ontorais_de_isstakeholdertype" (
   "domain" integer NOT NULL,
   "range" integer NOT NULL
);

CREATE TABLE "prefix_http_www_ontorais_de_keyword" (
   "domain" integer,
   "val" character varying(100)
);

CREATE TABLE "prefix_http_www_ontorais_de_phdthesis" (
   "id" IDENTITY PRIMARY KEY,
   "school" character varying(100),
   "abstract" character varying(100),
   "note" character varying(100),
   "financiallysupportedby" character varying(100),
   "title" character varying(100),
   "archivaldate" character varying(100)
);

CREATE TABLE "prefix_http_www_ontorais_de_iscreatorof" (
   "domain" integer NOT NULL,
   "range" integer NOT NULL
);

CREATE TABLE "prefix_http_www_ontorais_de_article" (
   "id" IDENTITY PRIMARY KEY,
   "volume" character varying(100),
   "abstract" character varying(100),
   "note" character varying(100),
   "financiallysupportedby" character varying(100),
   "title" character varying(100),
   "archivaldate" character varying(100)
);

CREATE TABLE "prefix_http_www_ontorais_de_rightsstatement" (
   "id" IDENTITY PRIMARY KEY,
   "systemrightsstatementname" character varying(100)
);
CREATE TABLE "prefix_http_www_ontorais_de_projectelement" (
   "id" IDENTITY PRIMARY KEY
);

CREATE TABLE "prefix_http_www_ontorais_de_bibliographicidentifier" (
   "domain" integer,
   "val" character varying(100)
);


