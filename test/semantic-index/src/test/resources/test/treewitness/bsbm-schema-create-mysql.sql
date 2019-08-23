CREATE TABLE "producttypeproduct" (
  "product" int(11) NOT NULL,
  "productType" int(11) NOT NULL,
  PRIMARY KEY ("product","productType")
);



CREATE TABLE "product" (
  "nr" int(11) NOT NULL,
  "label" varchar(100) DEFAULT NULL,
  "comment" varchar(2000) DEFAULT NULL,
  "producer" int(11) DEFAULT NULL,
  "propertyNum1" int(11) DEFAULT NULL,
  "propertyNum2" int(11) DEFAULT NULL,
  "propertyNum3" int(11) DEFAULT NULL,
  "propertyNum4" int(11) DEFAULT NULL,
  "propertyNum5" int(11) DEFAULT NULL,
  "propertyNum6" int(11) DEFAULT NULL,
  "propertyTex1" varchar(250) DEFAULT NULL,
  "propertyTex2" varchar(250) DEFAULT NULL,
  "propertyTex3" varchar(250) DEFAULT NULL,
  "propertyTex4" varchar(250) DEFAULT NULL,
  "propertyTex5" varchar(250) DEFAULT NULL,
  "propertyTex6" varchar(250) DEFAULT NULL,
  "publisher" int(11) DEFAULT NULL,
  "publishDate" date DEFAULT NULL,
  PRIMARY KEY ("nr"),
  UNIQUE ("producer","nr")
);


CREATE TABLE "vendor" (
  "nr" int(11) NOT NULL,
  "label" varchar(100) DEFAULT NULL,
  "comment" varchar(2000) DEFAULT NULL,
  "homepage" varchar(100) DEFAULT NULL,
  "country" char(2) DEFAULT NULL,
  "publisher" int(11) DEFAULT NULL,
  "publishDate" date DEFAULT NULL,
  PRIMARY KEY ("nr")
);

CREATE TABLE "producer" (
  "nr" int(11) NOT NULL,
  "label" varchar(100) DEFAULT NULL,
  "comment" varchar(2000) DEFAULT NULL,
  "homepage" varchar(100) DEFAULT NULL,
  "country" char(2) DEFAULT NULL,
  "publisher" int(11) DEFAULT NULL,
  "publishDate" date DEFAULT NULL,
  PRIMARY KEY ("nr")
);



CREATE TABLE "offer" (
  "nr" int(11) NOT NULL,
  "product" int(11) DEFAULT NULL,
  "producer" int(11) DEFAULT NULL,
  "vendor" int(11) DEFAULT NULL,
  "price" double DEFAULT NULL,
  "validFrom" datetime DEFAULT NULL,
  "validTo" datetime DEFAULT NULL,
  "deliveryDays" int(11) DEFAULT NULL,
  "offerWebpage" varchar(100) DEFAULT NULL,
  "publisher" int(11) DEFAULT NULL,
  "publishDate" date DEFAULT NULL,
  PRIMARY KEY ("nr"),
  UNIQUE ("producer","product","nr")
);


CREATE TABLE "review" (
  "nr" int(11) NOT NULL,
  "product" int(11) DEFAULT NULL,
  "producer" int(11) DEFAULT NULL,
  "person" int(11) DEFAULT NULL,
  "reviewDate" datetime DEFAULT NULL,
  "title" varchar(200) DEFAULT NULL,
  "text" text,
  "language" char(2) DEFAULT NULL,
  "rating1" int(11) DEFAULT NULL,
  "rating2" int(11) DEFAULT NULL,
  "rating3" int(11) DEFAULT NULL,
  "rating4" int(11) DEFAULT NULL,
  "publisher" int(11) DEFAULT NULL,
  "publishDate" date DEFAULT NULL,
  PRIMARY KEY ("nr"),
  UNIQUE ("product","producer","nr"),
  UNIQUE ("product","person","producer","nr"),
  UNIQUE ("producer","product","nr")
);



CREATE TABLE "productfeatureproduct" (
  "product" int(11) NOT NULL,
  "productFeature" int(11) NOT NULL,
  PRIMARY KEY ("product","productFeature")
);

CREATE TABLE "person" (
  "nr" int(11) NOT NULL,
  "name" varchar(30) DEFAULT NULL,
  "mbox_sha1sum" char(40) DEFAULT NULL,
  "country" char(2) DEFAULT NULL,
  "publisher" int(11) DEFAULT NULL,
  "publishDate" date DEFAULT NULL,
  PRIMARY KEY ("nr")
);

CREATE TABLE "producttype" (
  "nr" int(11) NOT NULL,
  "label" varchar(100) DEFAULT NULL,
  "comment" varchar(2000) DEFAULT NULL,
  "parent" int(11) DEFAULT NULL,
  "publisher" int(11) DEFAULT NULL,
  "publishDate" date DEFAULT NULL,
  PRIMARY KEY ("nr")
);

CREATE TABLE "productfeature" (
  "nr" int(11) NOT NULL,
  "label" varchar(100) DEFAULT NULL,
  "comment" varchar(2000) DEFAULT NULL,
  "publisher" int(11) DEFAULT NULL,
  "publishDate" date DEFAULT NULL,
  PRIMARY KEY ("nr")
);
